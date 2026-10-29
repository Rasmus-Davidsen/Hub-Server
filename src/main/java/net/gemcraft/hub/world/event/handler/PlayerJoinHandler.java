package net.gemcraft.hub.world.event.handler;

import net.gemcraft.hub.Hub;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.clientbound.play.ClientboundCommandsPacket;
import net.gemcraft.hub.protocol.packet.clientbound.play.ClientboundSetPlayerTeamPacket;
import net.gemcraft.hub.utils.TextUtils;
import net.gemcraft.hub.world.entity.player.Player;
import net.gemcraft.hub.world.entity.player.Rank;
import net.gemcraft.hub.world.event.EventListener;
import net.gemcraft.hub.world.event.constructor.PlayerJoinEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;

public class PlayerJoinHandler implements EventListener<PlayerJoinEvent> {
    @Override
    public void onEvent(final @NotNull PlayerJoinEvent e) {
        final Player player = e.player();

        sendLogMessage(player); // Log message
        sendCommands(player); // Commands

        addToTeam(player);

        player.sendMessage(
                createJoinMsg(player)
        );
    }

    private void sendLogMessage(final @NotNull Player player) {
        final String version = player
                .getVersion()
                .getName();

        final String client = Objects.requireNonNullElse(
                player.getClientBrand(), TextUtils.UNKNOWN
        );

        Hub.getLogger().info(String.format(
                "%s connected with UUID %s in version %s using client %s",
                player.getName(), player.getUUID(), version, client
        ));
    }

    private void sendCommands(final @NotNull Player player) {
        final ClientConnection connection = player.getConnection();

        connection.sendPacket(new ClientboundCommandsPacket(
                new HashSet<>()
        ));
    }

    private @NotNull Component createJoinMsg(final @NotNull Player player) {
        return Component.empty()
                .append(TextUtils.simpleColor(
                        "+", NamedTextColor.DARK_GREEN
                ))
                .appendSpace()
                .append(TextUtils.simpleColor(
                        player.getName(), NamedTextColor.AQUA
                ))
                .appendSpace()
                .append(TextUtils.simpleColor(
                        "joined", NamedTextColor.DARK_AQUA
                ))
                .appendSpace()
                .append(TextUtils.asServerText("Hub"));
    }

    private void addToTeam(final @NotNull Player player) {
        // Add team
        final ClientboundSetPlayerTeamPacket.Properties properties = new ClientboundSetPlayerTeamPacket.Properties();

        final Rank rank = player.getRank();

        final Component prefix = Component.empty()
                .append(rank.getPrefix())
                .appendSpace();

        properties.setPrefix(prefix);

        properties.setColor(
                ClientboundSetPlayerTeamPacket.Color.AQUA
        );

        final ClientConnection connection = player.getConnection();

        connection.sendPacket(ClientboundSetPlayerTeamPacket.createTeam(
                "player_team", properties, player.getName()
        ));
    }
}
