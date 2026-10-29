package net.gemcraft.hub.protocol.packet.clientbound;

import io.netty.buffer.Unpooled;
import net.gemcraft.hub.protocol.ByteMessage;
import org.jetbrains.annotations.NotNull;

public record EncodedPacket(@NotNull Class<? extends ClientboundPacket> packetClass, byte[] data) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeBytes(data);
    }

    public static @NotNull EncodedPacket of(final @NotNull ClientboundPacket packet) {
        final ByteMessage msg = new ByteMessage(
                Unpooled.buffer()
        );

        packet.encode(msg);

        final byte[] data = new byte[msg.readableBytes()];
        msg.readBytes(data);

        return new EncodedPacket(
                packet.getClass(), data
        );
    }
}
