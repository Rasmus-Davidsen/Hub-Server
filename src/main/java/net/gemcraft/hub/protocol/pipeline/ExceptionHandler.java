package net.gemcraft.hub.protocol.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.gemcraft.hub.Hub;
import org.jetbrains.annotations.NotNull;

public class ExceptionHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        Hub.getLogger().warn(String.format(
                "Unhandled exception in pipeline: %s", cause
        ));

        ctx.close();
    }

    @Override
    protected void channelRead0(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
    }
}
