package net.gemcraft.hub.protocol.packet.handler;

import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.clientbound.play.ClientboundPlayerChatPacket;
import net.gemcraft.hub.protocol.packet.serverbound.play.ServerboundChatPacket;
import net.gemcraft.hub.utils.TextUtils;
import net.gemcraft.hub.world.entity.player.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatHandler {
    public static void handle(final @NotNull ClientConnection connection, final @NotNull ServerboundChatPacket packet) {
        final Player sender = connection.getPlayer();

        final List<byte[]> previous = sender.getSignatureTracker().acknowledge(
                packet.count(), packet.acknowledged()
        );

        final Component displayName = createDisplayName(sender);

        final ClientboundPlayerChatPacket chatPacket = new ClientboundPlayerChatPacket(
                packet, sender, displayName, sender.nextChatIndex(), sender.nextSenderIndex(), previous
        );

        connection.sendPacket(chatPacket);

        if(packet.signature() != null) {
            sender.getSignatureTracker().add(
                    packet.signature()
            );
        }
    }

    private static @NotNull Component createDisplayName(final @NotNull Player sender) {
        return TextUtils.join(
                sender.getRank().getPrefix(),
                TextUtils.profile(sender),
                Component.text("->", NamedTextColor.DARK_AQUA)
        );
    }
}