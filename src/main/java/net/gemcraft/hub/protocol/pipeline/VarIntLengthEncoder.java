package net.gemcraft.hub.protocol.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.gemcraft.hub.protocol.ByteMessage;
import org.jetbrains.annotations.NotNull;

public class VarIntLengthEncoder extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(final @NotNull ChannelHandlerContext ctx, final @NotNull ByteBuf buf, final @NotNull ByteBuf out) {
        final ByteMessage msg = new ByteMessage(out);
        msg.writeVarInt(buf.readableBytes());
        msg.writeBytes(buf);
    }

    @Override
    protected @NotNull ByteBuf allocateBuffer(final @NotNull ChannelHandlerContext ctx, final @NotNull ByteBuf msg, final boolean preferDirect) {
        return ctx.alloc().heapBuffer(
                5 + msg.readableBytes()
        );
    }
}
