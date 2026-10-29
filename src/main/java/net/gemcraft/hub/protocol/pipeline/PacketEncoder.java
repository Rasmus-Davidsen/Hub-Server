package net.gemcraft.hub.protocol.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.gemcraft.hub.Hub;
import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.State;
import net.gemcraft.hub.protocol.packet.PacketRegistry;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.protocol.packet.clientbound.EncodedPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacketEncoder extends MessageToByteEncoder<ClientboundPacket> {
    private @NotNull State state = State.HANDSHAKE;

    public void setState(final @NotNull State state) {
        this.state = state;
    }

    public @NotNull State getState() {
        return state;
    }

    @Override
    protected void encode(final @NotNull ChannelHandlerContext ctx, final @NotNull ClientboundPacket packet, final @NotNull ByteBuf buf) {
        final Integer packetId = getPacketId(packet);

        if(packetId == null) {
            Hub.getLogger().warn("Unable to encode packet with missing id: " + packet.getClass().getSimpleName());
            return;
        }

        final ByteMessage msg = new ByteMessage(buf);
        msg.writeVarInt(packetId);

        packet.encode(msg);
    }

    private @Nullable Integer getPacketId(final @NotNull ClientboundPacket packet) {
        final PacketRegistry<ClientboundPacket> registry = state.getClientboundRegistry();
        final Class<? extends ClientboundPacket> packetClass;

        if(packet instanceof final EncodedPacket encodedPacket) {
            packetClass = encodedPacket.packetClass();
        } else {
            packetClass = packet.getClass();
        }

        return registry.getPacketId(
                packetClass
        );
    }
}
