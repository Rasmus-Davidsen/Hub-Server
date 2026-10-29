package net.gemcraft.hub.protocol.packet.handler;

import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.clientbound.status.ClientboundPongResponsePacket;
import net.gemcraft.hub.server.Server;
import net.gemcraft.hub.server.config.Config;
import org.jetbrains.annotations.NotNull;

public class PingRequestHandler {
    public static void handle(final @NotNull ClientConnection connection) {
        final int playerCount = Server.getPlayers().size();

        final ClientboundPongResponsePacket responsePacket = new ClientboundPongResponsePacket(
                playerCount, Config.MAX_PLAYERS, null, Server.VERSION.getProtocol(), null, null
        );

        connection.sendPacket(
                responsePacket
        );
    }
}
