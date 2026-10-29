package net.gemcraft.hub.protocol.packet.handler;

import net.gemcraft.hub.protocol.Channel;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.clientbound.login.ClientboundCustomQueryPacket;
import net.gemcraft.hub.protocol.packet.serverbound.login.ServerboundHelloPacket;
import net.gemcraft.hub.server.Server;
import net.gemcraft.hub.server.config.Config;
import net.gemcraft.hub.world.entity.player.GameProfile;
import org.jetbrains.annotations.NotNull;

public class HelloHandler {
    public static void handle(final @NotNull ClientConnection connection, final @NotNull ServerboundHelloPacket packet) {
        final int playerCount = Server
                .getPlayers()
                .size();

        if(playerCount >= Config.MAX_PLAYERS) {
            connection.disconnect(
                    "Hub is full"
            );

            return;
        }

        if(Config.VELOCITY) {
            final ClientboundCustomQueryPacket request = new ClientboundCustomQueryPacket(
                    Channel.VELOCITY_INFO_CHANNEL, new byte[]{}
            );

            connection.setVelocityLoginID(
                    request.id()
            );

            connection.sendPacket(
                    request
            );
        } else {
            final GameProfile gameProfile = new GameProfile(
                    packet.uuid(), packet.username()
            );

            connection.fireLoginSuccess(
                    Server.VERSION, gameProfile
            );
        }
    }
}
