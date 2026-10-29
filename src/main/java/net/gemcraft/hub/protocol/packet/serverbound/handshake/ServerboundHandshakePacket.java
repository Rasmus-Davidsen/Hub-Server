package net.gemcraft.hub.protocol.packet.serverbound.handshake;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.State;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.handler.HandshakeHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import org.jetbrains.annotations.NotNull;

public record ServerboundHandshakePacket(int protocol, @NotNull String host, int port, @NotNull State nextState) implements ServerboundPacket {
    public ServerboundHandshakePacket(final @NotNull ByteMessage msg) {
        this(
                msg.readVarInt(),
                msg.readString(),
                msg.readUnsignedShort(),
                State.getState(msg.readVarInt())
        );
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        HandshakeHandler.handle(
                connection, this
        );
    }
}
