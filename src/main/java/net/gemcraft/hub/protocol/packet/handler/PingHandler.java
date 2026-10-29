package net.gemcraft.hub.protocol.packet.handler;

import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.clientbound.status.ClientboundPingPacket;
import net.gemcraft.hub.protocol.packet.serverbound.status.ServerboundPingPacket;
import org.jetbrains.annotations.NotNull;

public class PingHandler {
    public static void handle(final @NotNull ClientConnection connection, final @NotNull ServerboundPingPacket packet) {
        connection.sendPacketAndClose(
                new ClientboundPingPacket(packet.id())
        );
    }
}
