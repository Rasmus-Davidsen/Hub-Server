package net.gemcraft.hub.protocol.packet.serverbound.login;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.handler.LoginAcknowledgedHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import org.jetbrains.annotations.NotNull;

public record ServerboundLoginAcknowledgedPacket() implements ServerboundPacket {
    public ServerboundLoginAcknowledgedPacket(final @NotNull ByteMessage msg) {
        this();
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        LoginAcknowledgedHandler.handle(
                connection
        );
    }
}
