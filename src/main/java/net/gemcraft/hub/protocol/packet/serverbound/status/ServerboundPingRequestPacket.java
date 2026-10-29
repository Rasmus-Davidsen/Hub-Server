package net.gemcraft.hub.protocol.packet.serverbound.status;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.handler.PingRequestHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import org.jetbrains.annotations.NotNull;

public record ServerboundPingRequestPacket() implements ServerboundPacket {
    public ServerboundPingRequestPacket(final @NotNull ByteMessage msg) {
        this();
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        PingRequestHandler.handle(
                connection
        );
    }
}
