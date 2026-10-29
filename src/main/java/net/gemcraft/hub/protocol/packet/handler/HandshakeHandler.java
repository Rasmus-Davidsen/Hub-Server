package net.gemcraft.hub.protocol.packet.handler;

import net.gemcraft.hub.protocol.Address;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.serverbound.handshake.ServerboundHandshakePacket;
import net.gemcraft.hub.server.Server;
import net.gemcraft.hub.server.config.Config;
import net.gemcraft.hub.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class HandshakeHandler {
    public static void handle(final @NotNull ClientConnection connection, final @NotNull ServerboundHandshakePacket packet) {
        // Update state
        connection.updateState(
                packet.nextState()
        );

        // Connection details
        if(Config.VELOCITY) {
            return;
        }

        // Version protocol
        if(packet.protocol() != Server.VERSION.getProtocol()) {
            final String version = Server.VERSION.getName();

            connection.disconnectDirect(String.format(
                    "You need to connect with version %s",
                    version
            ));

            return;
        }

        // IP address
        final Address address = Config.ADDRESS;

        final boolean isSimilarHost = Address.isSimilar(
                packet.host(), address.host()
        );

        if(!isSimilarHost || packet.port() != address.port()) {
            final Component msg = Component.empty()
                    .append(TextUtils.asServerText(
                            "GemCraft"
                    ))
                    .appendNewline()
                    .append(TextUtils.simpleColor(
                            "You need to connect directly",
                            NamedTextColor.AQUA
                    ));

            connection.disconnectRaw(msg);
        }
    }
}
