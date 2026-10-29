package net.gemcraft.hub.protocol.packet.serverbound.status;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.handler.PingHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import org.jetbrains.annotations.NotNull;

public record ServerboundPingPacket(long id) implements ServerboundPacket {
    public ServerboundPingPacket(final @NotNull ByteMessage msg) {
        this(msg.readLong());
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        PingHandler.handle(
                connection, this
        );
    }
}
