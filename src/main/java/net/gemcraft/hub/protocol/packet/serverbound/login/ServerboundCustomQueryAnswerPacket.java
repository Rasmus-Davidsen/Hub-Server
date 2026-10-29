package net.gemcraft.hub.protocol.packet.serverbound.login;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.handler.CustomQueryAnswerHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ServerboundCustomQueryAnswerPacket(int id, boolean isSuccessful, @Nullable ByteMessage data) implements ServerboundPacket {
    public ServerboundCustomQueryAnswerPacket(final @NotNull ByteMessage msg) {
        this(
                msg.readVarInt(),
                msg.readBoolean(),
                decodeData(msg)
        );
    }

    private static @Nullable ByteMessage decodeData(final @NotNull ByteMessage msg) {
        if(msg.readableBytes() < 1) {
            return null;
        }

        final int bytes = msg.readableBytes();

        return new ByteMessage(
                msg.readBytes(bytes)
        );
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        CustomQueryAnswerHandler.handle(
                connection, this
        );
    }

}
