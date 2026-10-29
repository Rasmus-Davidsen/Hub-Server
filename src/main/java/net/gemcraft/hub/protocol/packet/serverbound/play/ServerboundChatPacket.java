package net.gemcraft.hub.protocol.packet.serverbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.client.SignatureTracker;
import net.gemcraft.hub.protocol.packet.handler.ChatHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public record ServerboundChatPacket(@NotNull String content, long timestamp, long salt, byte @Nullable[] signature,
                                    int count, @NotNull BitSet acknowledged, byte checksum) implements ServerboundPacket
{
    public ServerboundChatPacket(final @NotNull ByteMessage msg) {
        this(
                msg.readString(),   // Content
                msg.readLong(),     // Timestamp
                msg.readLong(),     // Salt
                readSignature(msg), // Signature
                msg.readVarInt(),   // Message count
                msg.readBitSet(SignatureTracker.TRACKED), // Acknowledged
                msg.readByte()      // Checksum
        );
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        ChatHandler.handle(connection, this);
    }

    private static byte @Nullable[] readSignature(final @NotNull ByteMessage msg) {
        if(!msg.readBoolean()) {
            return null;
        }

        final byte[] bytes = new byte[256];
        msg.readBytes(bytes);

        return bytes;
    }
}
