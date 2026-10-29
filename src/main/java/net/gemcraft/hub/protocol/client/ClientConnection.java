package net.gemcraft.hub.protocol.client;

import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutException;
import net.gemcraft.hub.Hub;
import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.State;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.protocol.packet.clientbound.common.ClientboundDisconnectPacket;
import net.gemcraft.hub.protocol.packet.clientbound.common.ClientboundKeepAlivePacket;
import net.gemcraft.hub.protocol.packet.clientbound.login.ClientboundLoginDisconnectPacket;
import net.gemcraft.hub.protocol.packet.clientbound.login.ClientboundLoginFinishedPacket;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import net.gemcraft.hub.protocol.pipeline.PacketDecoder;
import net.gemcraft.hub.protocol.pipeline.PacketEncoder;
import net.gemcraft.hub.server.Server;
import net.gemcraft.hub.server.config.Config;
import net.gemcraft.hub.utils.TextUtils;
import net.gemcraft.hub.world.entity.player.GameProfile;
import net.gemcraft.hub.world.entity.player.Player;
import net.gemcraft.hub.world.entity.player.Skin;
import net.gemcraft.hub.world.entity.player.Version;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ClientConnection extends ChannelInboundHandlerAdapter {
    private static final @NotNull String VELOCITY_SECRET_ENCRYPTION = "HmacSHA256";
    private final static int DISCONNECT_TRIES = 3; // Waiting for a suitable state
    private final static int DISCONNECT_DELAY_MS = 500; // Milliseconds to wait before retrying

    private final @NotNull Server server;
    private final @NotNull Channel channel;
    private final @NotNull PacketEncoder encoder;
    private final @NotNull PacketDecoder decoder;
    private int velocityLoginID = -1;
    private @NotNull SocketAddress address;
    private @Nullable ScheduledFuture<?> keepAliveTask;
    private @Nullable Long keepAlivePayload;
    private @Nullable Player player;

    public ClientConnection(final @NotNull Server server, final @NotNull Channel channel, final @NotNull PacketEncoder encoder, final @NotNull PacketDecoder decoder) {
        this.server = server;
        this.channel = channel;
        this.encoder = encoder;
        this.decoder = decoder;

        address = channel.remoteAddress();

        channel.closeFuture().addListener(
                _ -> remove()
        );
    }

    public @NotNull Server getServer() {
        return server;
    }

    public @NotNull PacketEncoder getEncoder() {
        return encoder;
    }

    public void setVelocityLoginID(final int id) {
        velocityLoginID = id;
    }

    public int getVelocityLoginID() {
        return velocityLoginID;
    }

    public @NotNull Channel getChannel() {
        return channel;
    }

    public @Nullable Long getKeepAlivePayload() {
        return keepAlivePayload;
    }

    public void resetKeepAlivePayload() {
        keepAlivePayload = null;
    }

    public @NotNull Player getPlayer() {
        if(player == null) {
            disconnect("Invalid player state");

            throw new IllegalStateException(
                    "Player object not initialized"
            );
        }

        return player;
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if(msg instanceof final ServerboundPacket packet) {
            packet.handle(this);
        }

        ctx.fireChannelRead(msg);
    }

    /**
     * @param packets Packets to be sent
     * @apiNote Write and flush the channel
     */
    public void sendPackets(final @NotNull List<? extends ClientboundPacket> packets) {
        if(!channel.isActive()) {
            return;
        }

        for(final ClientboundPacket packet : packets) {
            channel.write(packet, channel.voidPromise());
        }

        channel.flush();
    }

    /**
     * @param packet Packet to be sent
     * @apiNote Write and flush the channel
     */
    public void sendPacket(final @NotNull ClientboundPacket packet) {
        if(!channel.isActive()) {
            return;
        }

        channel.writeAndFlush(
                packet, channel.voidPromise()
        );
    }

    public void sendPacketAndClose(final @NotNull ClientboundPacket packet) {
        if(!channel.isActive()) {
            return;
        }

        final ChannelFuture future = channel.writeAndFlush(packet);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    public void updateState(final @NotNull State state) {
        encoder.setState(state);
        decoder.setState(state);
    }

    private void remove() {
        if(player != null) {
            Server.removePlayer(player);
        }
    }

    public void setAddress(final @NotNull String host) {
        final InetSocketAddress address = (InetSocketAddress) this.address;

        this.address = new InetSocketAddress(
                host, address.getPort()
        );
    }

    public void fireLoginSuccess(final @NotNull Version version, final @NotNull GameProfile gameProfile) {
        if(Config.VELOCITY) {
            if(velocityLoginID == -1) {
                disconnectDirect("You need to connect with Velocity");
                return;
            }
        } else {
            final Skin skin = SkinUtils.requestSkin(
                    gameProfile.getUUID()
            );

            if(skin != null) {
                gameProfile.setSkin(skin);
            }
        }

        if(!Hub.isRunning()) {
            return;
        }

        sendPacket(new ClientboundLoginFinishedPacket(
                gameProfile, UUID.randomUUID()
        ));

        player = new Player(
                this, server.getWorld(), gameProfile, version
        );

        Server.addPlayer(player);

        encoder.setState(
                State.CONFIGURATION
        );
    }

    public void disconnect(final @NotNull String reason) {
        // Cancel keep alive task
        if(keepAliveTask != null) {
            keepAliveTask.cancel(false);
        }

        if(!Config.VELOCITY) {
            disconnectDirect(reason);
            return;
        }

        disconnectRaw(Component.text(
                reason
        ));
    }

    public void disconnectDirect(final @NotNull String reason) {
        disconnectRaw(TextUtils.simpleColor(
                reason, NamedTextColor.AQUA
        ));
    }

    public void disconnectRaw(final @NotNull Component msg) {
        disconnectRaw(msg, 0);
    }

    private void disconnectRaw(final @NotNull Component reason, final int fails) {
        final State state = decoder.getState();

        if(state == State.STATUS) {
            return;
        }

        if(state == State.LOGIN) {
            sendPacketAndClose(new ClientboundLoginDisconnectPacket(reason));
            return;
        }

        if(state == State.CONFIGURATION || state == State.PLAY) {
            sendPacketAndClose(new ClientboundDisconnectPacket(reason));
            return;
        }

        if(!Hub.isRunning()) {
            return;
        }

        final int failsNow = fails + 1;

        if(failsNow < DISCONNECT_TRIES) {
            final Runnable runnable = () -> disconnectRaw(
                    reason, failsNow
            );

            Hub.getInstance().createDelayedTask(
                    runnable, DISCONNECT_DELAY_MS,
                    TimeUnit.MILLISECONDS
            );

            return;
        }

        channel.close();

        if(player == null) {
            Hub.getLogger().warn(String.format(
                    "Couldn't softly kick an unknown player after %d tries",
                    DISCONNECT_TRIES
            ));
        } else {
            Hub.getLogger().warn(String.format(
                    "Couldn't softly kick %s with the UUID %s after %d tries",
                    player.getName(), player.getUUID(), DISCONNECT_TRIES
            ));
        }
    }

    public void createKeepAliveTask() {
        final Hub instance = Hub.getInstance();

        this.keepAliveTask = instance.createRepeatingTask(
                this::sendKeepAlivePacket, 10, TimeUnit.SECONDS
        );
    }

    private void sendKeepAlivePacket() {
        // The previous response wasn't received with the correct ID
        if(keepAlivePayload != null) {
            disconnect("You've been timed out");
            return;
        }

        final ClientboundKeepAlivePacket packet = new ClientboundKeepAlivePacket();

        sendPacket(packet);

        this.keepAlivePayload = packet.id();
    }

    public boolean hasVelocityKey(final @NotNull ByteMessage buffer) {
        // Signature
        buffer.readBytes(
                new byte[32]
        );

        final int len = buffer.readableBytes();
        final byte[] data = new byte[len];

        buffer.getBytes(
                buffer.readerIndex(), data
        );

        try {
            final Mac mac = Mac.getInstance(
                    VELOCITY_SECRET_ENCRYPTION
            );

            final byte[] keyBytes = Config.VELOCITY_KEY.getBytes(
                    StandardCharsets.UTF_8
            );

            mac.init(new SecretKeySpec(
                    keyBytes, VELOCITY_SECRET_ENCRYPTION
            ));
        } catch(final InvalidKeyException ex) {
            Hub.getLogger().warn("Invalid velocity key: " + ex);

            return false;
        } catch(final NoSuchAlgorithmException ex) {
            Hub.getLogger().warn("Invalid key algorithm: " + ex);
            return false;
        }

        final int version = buffer.readVarInt();

        if(version != 1) {
            Hub.getLogger().warn("Unsupported velocity forwarding version");
        }

        return true;
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        if(!ctx.channel().isActive()) {
            return;
        }

        if(cause instanceof ReadTimeoutException) {
            return;
        }

        if(cause instanceof SocketException) {
            ctx.close();
        }

        Hub.getLogger().warn(
                "Unhandled exception in connection: " + cause
        );
    }
}
