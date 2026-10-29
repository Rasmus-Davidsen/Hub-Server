package net.gemcraft.hub.protocol.packet.serverbound.common;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.handler.ClientInformationHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import net.gemcraft.hub.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record ServerboundClientInformationPacket(@NotNull String locale, byte viewDistance, @NotNull ChatMode chatMode, boolean chatColors,
                                                 byte skinMask, @NotNull Player.Hand mainHand, boolean filterText, boolean allowServerListing,
                                                 @NotNull ParticleStatus particleStatus) implements ServerboundPacket
{
    public ServerboundClientInformationPacket(final @NotNull ByteMessage msg) {
        this(
                msg.readString(),
                msg.readByte(),
                msg.readEnum(ChatMode.class, ChatMode.ENABLED),
                msg.readBoolean(),
                msg.readByte(),
                msg.readEnum(Player.Hand.class, Player.Hand.LEFT),
                msg.readBoolean(),
                msg.readBoolean(),
                msg.readEnum(ParticleStatus.class, ParticleStatus.ALL)
        );
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        ClientInformationHandler.handle(
                connection, this
        );
    }

    public enum ChatMode {
        ENABLED, COMMANDS_ONLY, HIDDEN
    }

    public enum ParticleStatus {
        ALL, DECREASED, MINIMAL
    }
}
