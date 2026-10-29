package net.gemcraft.hub.protocol.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.State;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.List;

public class PacketDecoder extends MessageToMessageDecoder<ByteBuf> {
    private @NotNull State state = State.HANDSHAKE;

    public void setState(final @NotNull State state) {
        this.state = state;
    }

    public @NotNull State getState() {
        return state;
    }

    @Override
    protected void decode(final @NotNull ChannelHandlerContext ctx, final @NotNull ByteBuf buf, final @NotNull List<@NotNull Object> out) {
        if(!ctx.channel().isActive()) {
            return;
        }

        final ByteMessage msg = new ByteMessage(buf);

        final int id = msg.readVarInt();
        final Class<? extends ServerboundPacket> packetClass = state.getServerboundRegistry().getPacket(id);

        if(packetClass == null) {
            return;
        }

        final ServerboundPacket packet;

        try {
            final Constructor<? extends ServerboundPacket> constructor = packetClass.getDeclaredConstructor(
                    ByteMessage.class
            );

            packet = constructor.newInstance(msg);
        } catch(final Exception ex) {
            return;
        }

        ctx.fireChannelRead(packet);
    }
}
