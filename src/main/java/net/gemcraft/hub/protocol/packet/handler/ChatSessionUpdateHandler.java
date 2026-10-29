package net.gemcraft.hub.protocol.packet.handler;

import net.gemcraft.hub.protocol.client.ChatSession;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.clientbound.play.ClientboundPlayerInfoUpdatePacket;
import net.gemcraft.hub.protocol.packet.serverbound.play.ServerboundChatSessionUpdatePacket;
import net.gemcraft.hub.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

public class ChatSessionUpdateHandler {
    public static void handle(final @NotNull ClientConnection connection, final @NotNull ServerboundChatSessionUpdatePacket packet) {
        final Player player = connection.getPlayer();

        player.setChatSession(new ChatSession(
                packet.sessionId(), packet.expiresAt(), packet.publicKey(), packet.keySignature()
        ));

        connection.sendPacket(new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT), Set.of(player)
        ));
    }
}
