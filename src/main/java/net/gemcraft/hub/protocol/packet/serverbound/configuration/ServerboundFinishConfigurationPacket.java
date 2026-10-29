package net.gemcraft.hub.protocol.packet.serverbound.configuration;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.handler.FinishConfigurationHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import org.jetbrains.annotations.NotNull;

public record ServerboundFinishConfigurationPacket() implements ServerboundPacket {
    public ServerboundFinishConfigurationPacket(final @NotNull ByteMessage msg) {
        this();
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        FinishConfigurationHandler.handle(
                connection
        );
    }
}
