package net.gemcraft.hub.protocol.packet.serverbound.login;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.handler.HelloHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ServerboundHelloPacket(@NotNull String username, @NotNull UUID uuid) implements ServerboundPacket {
    public ServerboundHelloPacket(final @NotNull ByteMessage msg) {
        this(
                msg.readString(),
                msg.readUUID() // Untrustworthy with Velocity
        );
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        HelloHandler.handle(
                connection, this
        );
    }
}
