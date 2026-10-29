package net.gemcraft.hub.protocol.packet.serverbound.common;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.handler.KeepAliveHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import org.jetbrains.annotations.NotNull;

public record ServerboundKeepAlivePacket(long id) implements ServerboundPacket {
    public ServerboundKeepAlivePacket(final @NotNull ByteMessage msg) {
        this(msg.readLong());
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        KeepAliveHandler.handle(
                connection, this
        );
    }
}
