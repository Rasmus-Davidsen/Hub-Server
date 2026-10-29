package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ChatSession;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.world.entity.player.GameProfile;
import net.gemcraft.hub.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record ClientboundPlayerInfoUpdatePacket(@NotNull EnumSet<@NotNull Action> actions, @NotNull List<@NotNull PlayerEntry> entries) implements ClientboundPacket {
    public ClientboundPlayerInfoUpdatePacket(final @NotNull EnumSet<@NotNull Action> actions, final @NotNull Set<@NotNull Player> players) {
        final List<PlayerEntry> entries = players.stream()
                .map(PlayerEntry::new)
                .toList();

        this(actions, entries);
    }

    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeEnumSet(actions, Action.class); // Actions
        writePlayerEntries(msg);
    }

    private void writePlayerEntries(final @NotNull ByteMessage msg) {
        // Entry count
        msg.writeVarInt(entries.size());

        for(final PlayerEntry entry : entries) {
            final GameProfile gameProfile = entry.gameProfile();
            msg.writeUUID(gameProfile.getUUID());

            writeActionData(
                    msg, entry
            );
        }
    }

    private void writeActionData(final @NotNull ByteMessage msg, final @NotNull PlayerEntry entry) {
        // Add player
        if(actions.contains(Action.ADD_PLAYER)) {
            msg.writeProfile(entry.gameProfile());
        }

        // Initialize chat
        if(actions.contains(Action.INITIALIZE_CHAT)) {
            writeInitializeChat(msg, entry);
        }

        // Updated listed
        if(actions.contains(Action.UPDATE_LISTED)) {
            msg.writeBoolean(entry.isListed());
        }
    }

    private void writeInitializeChat(final @NotNull ByteMessage msg, final @NotNull PlayerEntry entry) {
        final ChatSession session = entry.chatSession();
        msg.writeBoolean(session != null);

        if(session != null) {
            msg.writeUUID(session.sessionId());             // Session Id
            msg.writeLong(session.expiresAt());             // Expiration date
            msg.writePrefixedBytes(session.publicKey());    // Public key
            msg.writePrefixedBytes(session.keySignature()); // Key signature
        }
    }

    public static @NotNull ClientboundPlayerInfoUpdatePacket createInitializing(final @NotNull Set<@NotNull Player> players) {
        final EnumSet<Action> actions = EnumSet.of(
                Action.ADD_PLAYER, Action.INITIALIZE_CHAT, Action.UPDATE_LISTED
        );

        return new ClientboundPlayerInfoUpdatePacket(
                actions, players
        );
    }

    private record PlayerEntry(@NotNull GameProfile gameProfile, boolean isListed, @Nullable ChatSession chatSession) {
        public PlayerEntry(final @NotNull Player player) {
            this(
                    player.getGameProfile(),
                    player.isListed(),
                    player.getChatSession()
            );
        }
    }

    public enum Action {
        ADD_PLAYER, INITIALIZE_CHAT, UPDATE_GAMEMODE,
        UPDATE_LISTED, UPDATE_LATENCY, UPDATE_DISPLAY_NAME
    }
}
