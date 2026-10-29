package net.gemcraft.hub.protocol.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.gemcraft.hub.protocol.pipeline.*;
import net.gemcraft.hub.server.Server;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class ClientChannelInitializer extends ChannelInitializer<Channel> {
    private final @NotNull Server server;

    public ClientChannelInitializer(final @NotNull Server server) {
        this.server = server;
    }

    @Override
    protected void initChannel(final @NotNull Channel channel) {
        final ChannelPipeline pipeline = channel.pipeline();

        final PacketEncoder encoder = new PacketEncoder();
        final PacketDecoder decoder = new PacketDecoder();

        final ClientConnection connection = new ClientConnection(
                server, channel, encoder, decoder
        );

        // Read timeout
        final ReadTimeoutHandler readTimeout = new ReadTimeoutHandler(
                30, TimeUnit.SECONDS
        );

        pipeline.addLast(
                "timeout", readTimeout
        );

        // Traffic limiter
        final ChannelTrafficLimiter trafficLimiter = new ChannelTrafficLimiter(
                connection
        );

        pipeline.addLast(
                "traffic_limiter", trafficLimiter
        );

        // Decoders and encoders
        pipeline.addLast(
                "frame_decoder", new VarIntFrameDecoder()
        );

        pipeline.addLast(
                "frame_encoder", new VarIntLengthEncoder()
        );

        pipeline.addLast(
                "decoder", decoder
        );

        pipeline.addLast(
                "encoder", encoder
        );

        pipeline.addLast(
                "handler", connection
        );

        pipeline.addLast(
                "exception_handler", new ExceptionHandler()
        );
    }
}
