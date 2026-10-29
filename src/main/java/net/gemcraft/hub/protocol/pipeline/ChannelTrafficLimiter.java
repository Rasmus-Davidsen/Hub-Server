package net.gemcraft.hub.protocol.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.gemcraft.hub.protocol.client.ClientConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public class ChannelTrafficLimiter extends ChannelInboundHandlerAdapter {
    public final Deque<@NotNull Long> timestamps = new LinkedBlockingDeque<>();

    private static final int MAX_PACKET_SIZE = 4_096; // Max size in bytes
    private static final long MS_INTERVAL = 1_000;    // Interval in milliseconds before the packet is forgotten
    private static final double MAX_PACKET_RATE = 60; // Max packet rate within the interval

    private final @NotNull ClientConnection connection;

    public ChannelTrafficLimiter(final @NotNull ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if(getPacketSize(msg) > MAX_PACKET_SIZE) {
            ctx.close();
            return;
        }

        final long msNow = System.currentTimeMillis();
        final long threshold = msNow - MS_INTERVAL;

        while(!timestamps.isEmpty() && timestamps.peekFirst() < threshold) {
            timestamps.pollFirst();
        }

        if(timestamps.size() >= MAX_PACKET_RATE) {
            connection.disconnect(
                    "You're sending too many packets"
            );

            return;
        }

        timestamps.addLast(msNow);
        ctx.fireChannelRead(msg);
    }

    private int getPacketSize(final @NotNull Object msg) {
        if(msg instanceof final ByteBuf buf) {
            return buf.readableBytes();
        }

        return 0;
    }
}
