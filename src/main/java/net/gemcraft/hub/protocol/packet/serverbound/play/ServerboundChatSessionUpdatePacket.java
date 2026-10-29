package net.gemcraft.hub.protocol.packet.serverbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.handler.ChatSessionUpdateHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ServerboundChatSessionUpdatePacket(@NotNull UUID sessionId, long expiresAt, byte[] publicKey, byte[] keySignature) implements ServerboundPacket {
    public ServerboundChatSessionUpdatePacket(final @NotNull ByteMessage msg) {
        this(
                msg.readUUID(),
                msg.readLong(),
                msg.readPrefixedBytes(),
                msg.readPrefixedBytes()
        );
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        ChatSessionUpdateHandler.handle(connection, this);
    }
}
