package net.gemcraft.hub.protocol.packet.handler;

import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.serverbound.common.ServerboundKeepAlivePacket;
import org.jetbrains.annotations.NotNull;

public class KeepAliveHandler {
    public static void handle(final @NotNull ClientConnection connection, final @NotNull ServerboundKeepAlivePacket packet) {
        if(connection.getKeepAlivePayload() == null) {
            return;
        }

        if(connection.getKeepAlivePayload() == packet.id()) {
            connection.resetKeepAlivePayload();
        }
    }
}
