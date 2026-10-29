package net.gemcraft.hub.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import net.gemcraft.hub.world.Location;
import net.gemcraft.hub.world.Vec3;
import net.gemcraft.hub.world.entity.Mannequin;
import net.gemcraft.hub.world.entity.player.GameProfile;
import net.gemcraft.hub.world.entity.player.Skin;
import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

public class ByteMessage extends ByteBuf {
    private final @NotNull ByteBuf buf;

    public ByteMessage(final @NotNull ByteBuf buf) {
        this.buf = buf;
    }

    public @NotNull BitSet readBitSet(final int length) {
        final byte[] bytes = new byte[(length + 7) / 8];
        readBytes(bytes);

        return BitSet.valueOf(bytes);
    }

    public void writeVelocity(final @NotNull Vec3 velocity) {
        final double x = clampValue(
                velocity.x()
        );

        final double y = clampValue(
                velocity.y()
        );

        final double z = clampValue(
                velocity.z()
        );

        final double maxVel = Math.max(
                Math.abs(x), Math.max(Math.abs(y), Math.abs(z))
        );

        // Precision is limited to 1/32.768
        if(maxVel < 1.0d / (1 << 15)) {
            buf.writeByte(0);
            return;
        }

        final long size = (long) Math.ceil(maxVel);
        final boolean hasLargerSize = (size & 3L) != size;

        final long actualSize = hasLargerSize
                ? size & 3L | 4L : size;

        final long packedX = quantizeAxis(x / size) << 3;
        final long packedY = quantizeAxis(y / size) << 18;
        final long packedZ = quantizeAxis(z / size) << 33;

        final long packed = actualSize | packedX | packedY | packedZ;

        // Bytes 0-7
        buf.writeByte(
                (byte) packed
        );

        // Bytes 8-15
        buf.writeByte(
                (byte) (packed >> 8)
        );

        // Bytes 16-23
        buf.writeInt(
                (int) (packed >> 16)
        );

        if(hasLargerSize) {
            writeVarInt(
                    (int) (size >> 2)
            );
        }
    }

    private long quantizeAxis(final double value) {
        return Math.round(
                (value * 0.5 + 0.5) * (1 << 15)
        );
    }

    private double clampValue(final double value) {
        if(Double.isNaN(value)) {
            return 0;
        }

        final double packingLimit = (double) (1L << 34);

        return Math.clamp(
                value, -packingLimit, packingLimit
        );
    }

    public void writeRotation(final float degrees) {
        writeByte((byte) Math.floor(
                degrees * (1 << 8) / 360f
        ));
    }

    public void writeProfile(final @NotNull GameProfile gameProfile) {
        // Name
        writeString(gameProfile.getName());

        // Properties
        final Skin skin = gameProfile.getSkin();

        if(skin == null) {
            writeVarInt(0);
            return;
        }

        writeVarInt(1); // Property count
        writeSkinProperty(skin);  // Skin property
    }

    public void writeProfile(final @NotNull Mannequin.ResolvableProfile resolvableProfile) {
        writeVarInt(0);

        writeBoolean(false); // Optional name
        writeBoolean(false); // Optional UUID

        // Property count
        writeVarInt(1);

        // Skin property
        final Skin skin = resolvableProfile.skin();
        writeSkinProperty(skin);

        writeBoolean(false); // Body texture override (Built-in)
        writeBoolean(false); // Cape texture override (Built-in)
        writeBoolean(false); // Elytra texture override (Built-in)
        writeBoolean(false); // Model override
    }

    private void writeSkinProperty(final @NotNull Skin skin) {
        writePrefixedBytes("textures");      // Property name
        writePrefixedBytes(skin.value()); // Skin value

        // Signature
        final String signature = skin.signature();

        if(signature != null) {
            writeBoolean(true);
            writePrefixedBytes(signature);
        } else {
            writeBoolean(false);
        }
    }

    public void writeBlockPos(final @NotNull Location loc) {
        final long x = (loc.getBlockX() & 0x3FFFFFFL) << 38; // X
        final long y = loc.getBlockY() & 0xFFFL;             // Y
        final long z = (loc.getBlockZ() & 0x3FFFFFFL) << 12; // Z

        writeLong(x | z | y);
    }

    public void writeComponent(final @NotNull Component component) {
        final BinaryTag tag = BinaryTagUtils.create(component);
        writeNamelessCompoundTag(tag);
    }

    public void writeEnum(final @NotNull Enum<?> value) {
        writeVarInt(value.ordinal());
    }

    public @UnknownNullability <T extends Enum<T>> T readEnum(final @NotNull Class<T> enumClass, final @Nullable T defaultTo) {
        final T[] constants = enumClass.getEnumConstants();
        final int i = readVarInt();

        if(i < 0 || i >= constants.length) {
            return defaultTo;
        }

        return constants[i];
    }

    public void writeVarInt(int value) {
        while((value & ~0x7F) != 0) {
            writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }

        writeByte(value);
    }

    public int readVarInt() {
        int value = 0;
        int position = 0;

        while(true) {
            final byte currentByte = buf.readByte();

            value |= (currentByte & 0x7F) << position;

            if((currentByte & 0x80) == 0) {
                break;
            }

            position += 7;

            if(position >= 32) {
                throw new RuntimeException(
                        "VarInt is too big"
                );
            }
        }

        return value;
    }

    public void writeVarLong(long value) {
        while((value & ~0x7FL) != 0L) {
            writeByte((int) ((value & 0x7F) | 0x80));

            value >>>= 7;
        }

        writeByte((int) value);
    }

    public void writePrefixedBytes(final @NotNull String str) {
        final byte[] bytes = str.getBytes(
                StandardCharsets.UTF_8
        );

        writePrefixedBytes(bytes);
    }

    public void writePrefixedBytes(final byte[] bytes) {
        writeVarInt(bytes.length);
        writeBytes(bytes);
    }

    public void writeString(final @NotNull String str) {
        final byte[] bytes = str.getBytes(
                StandardCharsets.UTF_8
        );

        writeVarInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public @NotNull String readString(final int length) {
        final String str = buf.toString(
                buf.readerIndex(), length, StandardCharsets.UTF_8
        );

        buf.skipBytes(length);
        return str;
    }

    public @NotNull String readString() {
        final int len = readVarInt();
        return readString(len);
    }

    public void writeUTF(final @NotNull String value) {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        writeShort(bytes.length);
        writeBytes(bytes);
    }

    public void writeStringsArray(final @NotNull String @NotNull[] values) {
        writeVarInt(values.length);

        for(final String str : values) {
            writeString(str);
        }
    }

    public void writeLongArray(final long @NotNull[] values) {
        writeVarInt(values.length);

        for(final long n : values) {
            writeLong(n);
        }
    }

    public void writeUUID(final @NotNull UUID uuid) {
        buf.writeLong(
                uuid.getMostSignificantBits()
        );

        buf.writeLong(
                uuid.getLeastSignificantBits()
        );
    }

    public @NotNull UUID readUUID() {
        return new UUID(
                buf.readLong(), buf.readLong()
        );
    }

    public void writeNamelessCompoundTag(final @NotNull BinaryTag binaryTag) {
        try(final ByteBufOutputStream stream = new ByteBufOutputStream(buf)) {
            stream.writeByte(
                    binaryTag.type().id()
            );

            switch(binaryTag) {
                case final CompoundBinaryTag tag -> tag.type().write(tag, stream);
                case final ByteBinaryTag tag -> tag.type().write(tag, stream);
                case final ShortBinaryTag tag -> tag.type().write(tag, stream);
                case final IntBinaryTag tag -> tag.type().write(tag, stream);
                case final LongBinaryTag tag -> tag.type().write(tag, stream);
                case final DoubleBinaryTag tag -> tag.type().write(tag, stream);
                case final StringBinaryTag tag -> tag.type().write(tag, stream);
                case final ListBinaryTag tag -> tag.type().write(tag, stream);
                case final EndBinaryTag tag -> tag.type().write(tag, stream);
                default -> {}
            }
        } catch(final IOException ex) {
            throw new EncoderException(
                    "Can't write NBT CompoundTag"
            );
        }
    }

    public <E extends Enum<E>> void writeEnumSet(final @NotNull EnumSet<E> enumset, final @NotNull Class<E> clazz) {
        final E[] enums = clazz.getEnumConstants();

        final BitSet bits = new BitSet(
                enums.length
        );

        for(int i = 0; i < enums.length; ++i) {
            final boolean value = enumset.contains(
                    enums[i]
            );

            bits.set(i, value);
        }

        writeFixedBitSet(
                bits, enums.length, buf
        );
    }

    private void writeFixedBitSet(final @NotNull BitSet bits, final int size, final @NotNull ByteBuf buf) {
        if(bits.length() > size) {
            throw new StackOverflowError(String.format(
                    "BitSet too large expected %d and got %d", size, bits.size()
            ));
        }

        buf.writeBytes(Arrays.copyOf(
                bits.toByteArray(), (size + 8) >> 3
        ));
    }

    private static final double MAX_QUANTIZED_VALUE = 32766;
    private static final long CONTINUATION_FLAG = 0x04;
    private static final long SCALE_BITS = 0x03;

    public @NotNull Vec3 readLpVec3() {
        final int byte1 = readUnsignedByte();

        if(byte1 == 0) {
            return Vec3.EMPTY;
        }

        final int byte2 = readUnsignedByte();

        final long bytes3To6 = readUnsignedInt();
        final long packed = (bytes3To6 << 16) | (long) (byte2 << 8) | (long) byte1;
        long scaleFactor = (long) byte1 & SCALE_BITS;

        if((byte1 & CONTINUATION_FLAG) != 0) {
            scaleFactor |= Integer.toUnsignedLong(readVarInt()) << 2L;
        }

        double scaleFactorD = (double) scaleFactor;

        return new Vec3(
                unpack(packed >> 3L)  * scaleFactorD,
                unpack(packed >> 18L) * scaleFactorD,
                unpack(packed >> 33L) * scaleFactorD
        );
    }

    private double unpack(final long value) {
        return Math.min((double) (value & 32767L), MAX_QUANTIZED_VALUE) * 2 / MAX_QUANTIZED_VALUE - 1;
    }

    public byte[] readPrefixedBytes() {
        final int len = readVarInt();

        final byte[] bytes = new byte[len];
        readBytes(bytes);

        return bytes;
    }

    public void writeFlags(final @NotNull Enum<?> @NotNull... flags) {
        int total = 0;

        for(final Enum<?> flag : flags) {
            total |= (1 << flag.ordinal());
        }

        writeByte(total);
    }

    @Override
    public int capacity() {
        return buf.capacity();
    }

    @Override
    public @NotNull ByteBuf capacity(final int newCapacity) {
        return buf.capacity(
                newCapacity
        );
    }

    @Override
    public int maxCapacity() {
        return buf.maxCapacity();
    }

    @Override
    public @NotNull ByteBufAllocator alloc() {
        return buf.alloc();
    }

    @Override
    @Deprecated
    public @NotNull ByteOrder order() {
        return buf.order();
    }

    @Override
    @Deprecated
    public @NotNull ByteBuf order(final @NotNull ByteOrder endianness) {
        return buf.order(
                endianness
        );
    }

    @Override
    public @NotNull ByteBuf unwrap() {
        return buf.unwrap();
    }

    @Override
    public boolean isDirect() {
        return buf.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return buf.isReadOnly();
    }

    @Override
    public @NotNull ByteBuf asReadOnly() {
        return buf.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return buf.readerIndex();
    }

    @Override
    public @NotNull ByteBuf readerIndex(final int readerIndex) {
        return buf.readerIndex(
                readerIndex
        );
    }

    @Override
    public int writerIndex() {
        return buf.writerIndex();
    }

    @Override
    public @NotNull ByteBuf writerIndex(final int writerIndex) {
        return buf.writerIndex(
                writerIndex
        );
    }

    @Override
    public @NotNull ByteBuf setIndex(final int readerIndex, final int writerIndex) {
        return buf.setIndex(
                readerIndex, writerIndex
        );
    }

    @Override
    public int readableBytes() {
        return buf.readableBytes();
    }

    @Override
    public int writableBytes() {
        return buf.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return buf.maxWritableBytes();
    }

    @Override
    public int maxFastWritableBytes() {
        return buf.maxFastWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return buf.isReadable();
    }

    @Override
    public boolean isReadable(final int size) {
        return buf.isReadable(size);
    }

    @Override
    public boolean isWritable() {
        return buf.isWritable();
    }

    @Override
    public boolean isWritable(final int size) {
        return buf.isWritable(size);
    }

    @Override
    public @NotNull ByteBuf clear() {
        return buf.clear();
    }

    @Override
    public @NotNull ByteBuf markReaderIndex() {
        return buf.markReaderIndex();
    }

    @Override
    public @NotNull ByteBuf resetReaderIndex() {
        return buf.resetReaderIndex();
    }

    @Override
    public @NotNull ByteBuf markWriterIndex() {
        return buf.markWriterIndex();
    }

    @Override
    public @NotNull ByteBuf resetWriterIndex() {
        return buf.resetWriterIndex();
    }

    @Override
    public @NotNull ByteBuf discardReadBytes() {
        return buf.discardReadBytes();
    }

    @Override
    public @NotNull ByteBuf discardSomeReadBytes() {
        return buf.discardSomeReadBytes();
    }

    @Override
    public @NotNull ByteBuf ensureWritable(final int minWritableBytes) {
        return buf.ensureWritable(
                minWritableBytes
        );
    }

    @Override
    public int ensureWritable(final int minWritableBytes, final boolean force) {
        return buf.ensureWritable(
                minWritableBytes, force
        );
    }

    @Override
    public boolean getBoolean(final int i) {
        return buf.getBoolean(i);
    }

    @Override
    public byte getByte(final int i) {
        return buf.getByte(i);
    }

    @Override
    public short getUnsignedByte(final int i) {
        return buf.getUnsignedByte(i);
    }

    @Override
    public short getShort(final int i) {
        return buf.getShort(i);
    }

    @Override
    public short getShortLE(final int i) {
        return buf.getShortLE(i);
    }

    @Override
    public int getUnsignedShort(final int i) {
        return buf.getUnsignedShort(i);
    }

    @Override
    public int getUnsignedShortLE(final int i) {
        return buf.getUnsignedShortLE(i);
    }

    @Override
    public int getMedium(final int i) {
        return buf.getMedium(i);
    }

    @Override
    public int getMediumLE(final int i) {
        return buf.getMediumLE(i);
    }

    @Override
    public int getUnsignedMedium(final int i) {
        return buf.getUnsignedMedium(i);
    }

    @Override
    public int getUnsignedMediumLE(final int i) {
        return buf.getUnsignedMediumLE(i);
    }

    @Override
    public int getInt(final int i) {
        return buf.getInt(i);
    }

    @Override
    public int getIntLE(final int i) {
        return buf.getIntLE(i);
    }

    @Override
    public long getUnsignedInt(final int i) {
        return buf.getUnsignedInt(i);
    }

    @Override
    public long getUnsignedIntLE(final int i) {
        return buf.getUnsignedIntLE(i);
    }

    @Override
    public long getLong(final int i) {
        return buf.getLong(i);
    }

    @Override
    public long getLongLE(final int i) {
        return buf.getLongLE(i);
    }

    @Override
    public char getChar(final int i) {
        return buf.getChar(i);
    }

    @Override
    public float getFloat(final int i) {
        return buf.getFloat(i);
    }

    @Override
    public float getFloatLE(final int i) {
        return buf.getFloatLE(i);
    }

    @Override
    public double getDouble(final int i) {
        return buf.getDouble(i);
    }

    @Override
    public double getDoubleLE(final int i) {
        return buf.getDoubleLE(i);
    }

    @Override
    public @NotNull ByteBuf getBytes(final int i, final @NotNull ByteBuf dst) {
        return buf.getBytes(
                i, dst
        );
    }

    @Override
    public @NotNull ByteBuf getBytes(final int i, final @NotNull ByteBuf dst, final int len) {
        return buf.getBytes(
                i, dst, len
        );
    }

    @Override
    public @NotNull ByteBuf getBytes(final int i, final @NotNull ByteBuf dst, final int dstIndex, final int len) {
        return buf.getBytes(
                i, dst, dstIndex, len
        );
    }

    @Override
    public @NotNull ByteBuf getBytes(final int i, final byte @NotNull[] dst) {
        return buf.getBytes(
                i, dst
        );
    }

    @Override
    public @NotNull ByteBuf getBytes(final int i, final byte @NotNull[] dst, final int dstIndex, final int len) {
        return buf.getBytes(
                i, dst, dstIndex, len
        );
    }

    @Override
    public @NotNull ByteBuf getBytes(final int i, final @NotNull ByteBuffer dst) {
        return buf.getBytes(
                i, dst
        );
    }

    @Override
    public @NotNull ByteBuf getBytes(final int i, final @NotNull OutputStream out, final int len) throws IOException {
        return buf.getBytes(
                i, out, len
        );
    }

    @Override
    public int getBytes(final int i, final @NotNull GatheringByteChannel out, final int len) throws IOException {
        return buf.getBytes(
                i, out, len
        );
    }

    @Override
    public int getBytes(final int i, final @NotNull FileChannel out, final long pos, final int len) throws IOException {
        return buf.getBytes(
                i, out, pos, len
        );
    }

    @Override
    public @NotNull CharSequence getCharSequence(final int i, final int len, final @NotNull Charset charset) {
        return buf.getCharSequence(
                i, len, charset
        );
    }

    @Override
    public @NotNull ByteBuf setBoolean(final int i, final boolean value) {
        return buf.setBoolean(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setByte(final int i, final int value) {
        return buf.setByte(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setShort(final int i, final int value) {
        return buf.setShort(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setShortLE(final int i, final int value) {
        return buf.setShortLE(
                i, value
        );
    }

    @Override
    public ByteBuf setMedium(final int i, final int value) {
        return buf.setMedium(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setMediumLE(final int i, final int value) {
        return buf.setMediumLE(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setInt(final int i, final int value) {
        return buf.setInt(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setIntLE(final int i, final int value) {
        return buf.setIntLE(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setLong(final int i, final long value) {
        return buf.setLong(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setLongLE(final int i, final long value) {
        return buf.setLongLE(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setChar(final int i, final int value) {
        return buf.setChar(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setFloat(final int i, final float value) {
        return buf.setFloat(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setFloatLE(final int i, final float value) {
        return buf.setFloatLE(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setDouble(final int i, final double value) {
        return buf.setDouble(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setDoubleLE(final int i, final double value) {
        return buf.setDoubleLE(
                i, value
        );
    }

    @Override
    public @NotNull ByteBuf setBytes(final int i, final @NotNull ByteBuf src) {
        return buf.setBytes(
                i, src
        );
    }

    @Override
    public @NotNull ByteBuf setBytes(final int i, @NotNull ByteBuf src, final int len) {
        return buf.setBytes(
                i, src, len
        );
    }

    @Override
    public @NotNull ByteBuf setBytes(final int i, @NotNull ByteBuf src, final int srcIndex, final int len) {
        return buf.setBytes(
                i, src, srcIndex, len
        );
    }

    @Override
    public @NotNull ByteBuf setBytes(final int i, final byte @NotNull[] src) {
        return buf.setBytes(
                i, src
        );
    }

    @Override
    public @NotNull ByteBuf setBytes(final int i, final byte @NotNull[] src, final int srcIndex, final int len) {
        return buf.setBytes(
                i, src, srcIndex, len
        );
    }

    @Override
    public @NotNull ByteBuf setBytes(final int i, final ByteBuffer src) {
        return buf.setBytes(
                i, src
        );
    }

    @Override
    public int setBytes(final int i, final @NotNull InputStream in, final int len) throws IOException {
        return buf.setBytes(
                i, in, len
        );
    }

    @Override
    public int setBytes(final int i, @NotNull ScatteringByteChannel in, final int len) throws IOException {
        return buf.setBytes(
                i, in, len
        );
    }

    @Override
    public int setBytes(final int i, final @NotNull FileChannel in, final long pos, final int len) throws IOException {
        return buf.setBytes(
                i, in, pos, len
        );
    }

    @Override
    public @NotNull ByteBuf setZero(final int i, final int len) {
        return buf.setZero(
                i, len
        );
    }

    @Override
    public int setCharSequence(final int i, final @NotNull CharSequence sequence, final @NotNull Charset charset) {
        return buf.setCharSequence(
                i, sequence, charset
        );
    }

    @Override
    public boolean readBoolean() {
        return buf.readBoolean();
    }

    @Override
    public byte readByte() {
        return buf.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return buf.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return buf.readShort();
    }

    @Override
    public short readShortLE() {
        return buf.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return buf.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return buf.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return buf.readMedium();
    }

    @Override
    public int readMediumLE() {
        return buf.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return buf.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return buf.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return buf.readInt();
    }

    @Override
    public int readIntLE() {
        return buf.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return buf.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return buf.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return buf.readLong();
    }

    @Override
    public long readLongLE() {
        return buf.readLongLE();
    }

    @Override
    public char readChar() {
        return buf.readChar();
    }

    @Override
    public float readFloat() {
        return buf.readFloat();
    }

    @Override
    public float readFloatLE() {
        return buf.readFloatLE();
    }

    @Override
    public double readDouble() {
        return buf.readDouble();
    }

    @Override
    public double readDoubleLE() {
        return buf.readDoubleLE();
    }

    @Override
    public @NotNull ByteBuf readBytes(final int len) {
        return buf.readBytes(len);
    }

    @Override
    public @NotNull ByteBuf readSlice(final int len) {
        return buf.readSlice(len);
    }

    @Override
    public @NotNull ByteBuf readRetainedSlice(final int len) {
        return buf.readRetainedSlice(len);
    }

    @Override
    public @NotNull ByteBuf readBytes(final @NotNull ByteBuf dst) {
        return buf.readBytes(dst);
    }

    @Override
    public @NotNull ByteBuf readBytes(final @NotNull ByteBuf dst, final int len) {
        return buf.readBytes(
                dst, len
        );
    }

    @Override
    public @NotNull ByteBuf readBytes(final @NotNull ByteBuf dst, final int dstIndex, final int len) {
        return buf.readBytes(
                dst, dstIndex, len
        );
    }

    @Override
    public @NotNull ByteBuf readBytes(final byte @NotNull[] dst) {
        return buf.readBytes(dst);
    }

    @Override
    public @NotNull ByteBuf readBytes(final byte @NotNull[] dst, final int dstIndex, final int len) {
        return buf.readBytes(
                dst, dstIndex, len
        );
    }

    @Override
    public @NotNull ByteBuf readBytes(final @NotNull ByteBuffer dst) {
        return buf.readBytes(dst);
    }

    @Override
    public @NotNull ByteBuf readBytes(final @NotNull OutputStream out, final int len) throws IOException {
        return buf.readBytes(
                out, len
        );
    }

    @Override
    public int readBytes(final @NotNull GatheringByteChannel out, final int len) throws IOException {
        return buf.readBytes(
                out, len
        );
    }

    @Override
    public @NotNull CharSequence readCharSequence(final int len, final @NotNull Charset charset) {
        return buf.readCharSequence(
                len, charset
        );
    }

    @Override
    public int readBytes(final @NotNull FileChannel out, final long pos, final int len) throws IOException {
        return buf.readBytes(
                out, pos, len
        );
    }

    @Override
    public @NotNull ByteBuf skipBytes(final int len) {
        return buf.skipBytes(len);
    }

    @Override
    public @NotNull ByteBuf writeBoolean(final boolean value) {
        return buf.writeBoolean(value);
    }

    @Override
    public @NotNull ByteBuf writeByte(final int value) {
        return buf.writeByte(value);
    }

    @Override
    public @NotNull ByteBuf writeShort(final int value) {
        return buf.writeShort(value);
    }

    @Override
    public @NotNull ByteBuf writeShortLE(final int value) {
        return buf.writeShortLE(value);
    }

    @Override
    public @NotNull ByteBuf writeMedium(final int value) {
        return buf.writeMedium(value);
    }

    @Override
    public @NotNull ByteBuf writeMediumLE(final int value) {
        return buf.writeMediumLE(value);
    }

    @Override
    public @NotNull ByteBuf writeInt(final int value) {
        return buf.writeInt(value);
    }

    @Override
    public @NotNull ByteBuf writeIntLE(final int value) {
        return buf.writeIntLE(value);
    }

    @Override
    public @NotNull ByteBuf writeLong(final long value) {
        return buf.writeLong(value);
    }

    @Override
    public @NotNull ByteBuf writeLongLE(final long value) {
        return buf.writeLongLE(value);
    }

    @Override
    public @NotNull ByteBuf writeChar(final int value) {
        return buf.writeChar(value);
    }

    @Override
    public @NotNull ByteBuf writeFloat(final float value) {
        return buf.writeFloat(value);
    }

    @Override
    public @NotNull ByteBuf writeFloatLE(final float value) {
        return buf.writeFloatLE(value);
    }

    @Override
    public @NotNull ByteBuf writeDouble(final double value) {
        return buf.writeDouble(value);
    }

    @Override
    public @NotNull ByteBuf writeDoubleLE(final double value) {
        return buf.writeDoubleLE(value);
    }

    @Override
    public @NotNull ByteBuf writeBytes(final @NotNull ByteBuf src) {
        return buf.writeBytes(src);
    }

    @Override
    public @NotNull ByteBuf writeBytes(final @NotNull ByteBuf src, final int len) {
        return buf.writeBytes(
                src, len
        );
    }

    @Override
    public @NotNull ByteBuf writeBytes(final @NotNull ByteBuf src, final int srcIndex, final int len) {
        return buf.writeBytes(
                src, srcIndex, len
        );
    }

    @Override
    public @NotNull ByteBuf writeBytes(final byte[] src) {
        return buf.writeBytes(src);
    }

    @Override
    public @NotNull ByteBuf writeBytes(final byte @NotNull[] src, final int srcIndex, final int len) {
        return buf.writeBytes(
                src, srcIndex, len
        );
    }

    @Override
    public @NotNull ByteBuf writeBytes(final @NotNull ByteBuffer src) {
        return buf.writeBytes(src);
    }

    @Override
    public int writeBytes(final @NotNull InputStream in, final int len) throws IOException {
        return buf.writeBytes(
                in, len
        );
    }

    @Override
    public int writeBytes(final @NotNull ScatteringByteChannel in, final int len) throws IOException {
        return buf.writeBytes(
                in, len
        );
    }

    @Override
    public int writeBytes(final @NotNull FileChannel in, final long pos, final int len) throws IOException {
        return buf.writeBytes(
                in, pos, len
        );
    }

    @Override
    public @NotNull ByteBuf writeZero(final int len) {
        return buf.writeZero(len);
    }

    @Override
    public int writeCharSequence(final @NotNull CharSequence sequence, final @NotNull Charset charset) {
        return buf.writeCharSequence(
                sequence, charset
        );
    }

    @Override
    public int indexOf(final int fromIndex, final int toIndex, final byte value) {
        return buf.indexOf(
                fromIndex, toIndex, value
        );
    }

    @Override
    public int bytesBefore(final byte value) {
        return buf.bytesBefore(value);
    }

    @Override
    public int bytesBefore(final int len, final byte value) {
        return buf.bytesBefore(
                len, value
        );
    }

    @Override
    public int bytesBefore(final int i, final int len, final byte value) {
        return buf.bytesBefore(
                i, len, value
        );
    }

    @Override
    public int forEachByte(final @NotNull ByteProcessor processor) {
        return buf.forEachByte(
                processor
        );
    }

    @Override
    public int forEachByte(final int i, final int len, final @NotNull ByteProcessor processor) {
        return buf.forEachByte(
                i, len, processor
        );
    }

    @Override
    public int forEachByteDesc(final @NotNull ByteProcessor processor) {
        return buf.forEachByteDesc(
                processor
        );
    }

    @Override
    public int forEachByteDesc(final int i, final int len, final @NotNull ByteProcessor processor) {
        return buf.forEachByteDesc(
                i, len, processor
        );
    }

    @Override
    public @NotNull ByteBuf copy() {
        return buf.copy();
    }

    @Override
    public @NotNull ByteBuf copy(final int i, final int len) {
        return buf.copy(
                i, len
        );
    }

    @Override
    public @NotNull ByteBuf slice() {
        return buf.slice();
    }

    @Override
    public @NotNull ByteBuf retainedSlice() {
        return buf.retainedSlice();
    }

    @Override
    public @NotNull ByteBuf slice(final int i, final int len) {
        return buf.slice(
                i, len
        );
    }

    @Override
    public @NotNull ByteBuf retainedSlice(final int i, final int len) {
        return buf.retainedSlice(
                i, len
        );
    }

    @Override
    public @NotNull ByteBuf duplicate() {
        return buf.duplicate();
    }

    @Override
    public @NotNull ByteBuf retainedDuplicate() {
        return buf.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return buf.nioBufferCount();
    }

    @Override
    public @NotNull ByteBuffer nioBuffer() {
        return buf.nioBuffer();
    }

    @Override
    public @NotNull ByteBuffer nioBuffer(final int i, final int len) {
        return buf.nioBuffer(
                i, len
        );
    }

    @Override
    public @NotNull ByteBuffer internalNioBuffer(final int i, final int len) {
        return buf.internalNioBuffer(
                i, len
        );
    }

    @Override
    public @NotNull ByteBuffer @NotNull[] nioBuffers() {
        return buf.nioBuffers();
    }

    @Override
    public @NotNull ByteBuffer @NotNull[] nioBuffers(final int i, final int len) {
        return buf.nioBuffers(
                i, len
        );
    }

    @Override
    public boolean hasArray() {
        return buf.hasArray();
    }

    @Override
    public byte[] array() {
        return buf.array();
    }

    @Override
    public int arrayOffset() {
        return buf.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return buf.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return buf.memoryAddress();
    }

    @Override
    public boolean isContiguous() {
        return buf.isContiguous();
    }

    @Override
    public @NotNull String toString(final @NotNull Charset charset) {
        return buf.toString(charset);
    }

    @Override
    public @NotNull String toString(final int i, final int len, final @NotNull Charset charset) {
        return buf.toString(
                i, len, charset
        );
    }

    @Override
    public int hashCode() {
        return buf.hashCode();
    }

    @Override
    public boolean equals(final @NotNull Object obj) {
        if(!obj.getClass().equals(buf.getClass())) {
            return false;
        }

        return buf.equals(obj);
    }

    @Override
    public int compareTo(final @NotNull ByteBuf buffer) {
        return buf.compareTo(buffer);
    }

    @Override
    public @NotNull String toString() {
        return buf.toString();
    }

    @Override
    public @NotNull ByteBuf retain(final int increment) {
        return buf.retain(
                increment
        );
    }

    @Override
    public @NotNull ByteBuf retain() {
        return buf.retain();
    }

    @Override
    public @NotNull ByteBuf touch() {
        return buf.touch();
    }

    @Override
    public @NotNull ByteBuf touch(final @NotNull Object hint) {
        return buf.touch(hint);
    }

    @Override
    public int refCnt() {
        return buf.refCnt();
    }

    @Override
    public boolean release() {
        return buf.release();
    }

    @Override
    public boolean release(final int decrement) {
        return buf.release(
                decrement
        );
    }
}