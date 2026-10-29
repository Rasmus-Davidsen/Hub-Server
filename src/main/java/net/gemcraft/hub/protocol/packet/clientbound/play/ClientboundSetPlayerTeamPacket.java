package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ClientboundSetPlayerTeamPacket(@NotNull String teamName, @NotNull Action action, @Nullable Properties properties,
                                             @NotNull String @Nullable[] entities) implements ClientboundPacket {

    public static @NotNull ClientboundSetPlayerTeamPacket createTeam(final @NotNull String teamName, final @NotNull Properties properties,
                                                                     final @NotNull String @NotNull... entities)
    {
        return new ClientboundSetPlayerTeamPacket(
                teamName, Action.CREATE, properties, entities
        );
    }

    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeString(teamName);       // Team name
        msg.writeByte(action.ordinal()); // Action
        writeProperties(msg);            // Properties
        writeEntities(msg);              // Entities
    }

    private void writeProperties(final @NotNull ByteMessage msg) {
        if(properties == null) {
            return;
        }

        // Display name
        msg.writeComponent(Objects.requireNonNullElseGet(
                properties.getDisplayName(), Component::empty
        ));

        // Prefix
        msg.writeComponent(Objects.requireNonNullElseGet(
                properties.getPrefix(), Component::empty
        ));

        // Suffix
        msg.writeComponent(Objects.requireNonNullElseGet(
                properties.getSuffix(), Component::empty
        ));

        msg.writeEnum(properties.getNameTagVisibility()); // Nametag visibility
        msg.writeEnum(properties.getCollisionRule()); // Collision rule

        msg.writeBoolean(true); // Has color
        msg.writeEnum(properties.getColor()); // Color

        // Friendly flags
        msg.writeFlags(properties.getFlags());
    }

    private void writeEntities(final @NotNull ByteMessage msg) {
        if(entities == null) {
            return;
        }

        // Length prefix
        msg.writeVarInt(entities.length);

        // Identifier (Player name or UUID for other entities)
        for(final String entity : entities) {
            msg.writeString(entity);
        }
    }

    public static class Properties {
        private @NotNull TeamFlag @NotNull[] flags = new TeamFlag[]{};
        private @NotNull CollisionRule collisionRule = CollisionRule.ALWAYS;
        private @NotNull NameTagVisibility nameTagVisibility = NameTagVisibility.ALWAYS;
        private @NotNull Color color = Color.WHITE;
        private @Nullable Component displayName = null;
        private @Nullable Component prefix = null;
        private @Nullable Component suffix = null;

        public void setFlags(final @NotNull TeamFlag @NotNull... flags) {
            this.flags = flags;
        }

        public @NotNull TeamFlag @NotNull[] getFlags() {
            return this.flags;
        }

        public @NotNull CollisionRule getCollisionRule() {
            return this.collisionRule;
        }

        public void setCollisionRule(final @NotNull CollisionRule rule) {
            this.collisionRule = rule;
        }

        public @NotNull NameTagVisibility getNameTagVisibility() {
            return this.nameTagVisibility;
        }

        public void setNameTagVisibility(final @NotNull NameTagVisibility visibility) {
            this.nameTagVisibility = visibility;
        }

        public @NotNull Color getColor() {
            return this.color;
        }

        public void setColor(final @NotNull Color color) {
            this.color = color;
        }

        public @Nullable Component getDisplayName() {
            return this.displayName;
        }

        public void setDisplayName(final @NotNull Component displayName) {
            this.displayName = displayName;
        }

        public @Nullable Component getPrefix() {
            return this.prefix;
        }

        public void setPrefix(final @NotNull Component prefix) {
            this.prefix = prefix;
        }

        public @Nullable Component getSuffix() {
            return this.suffix;
        }

        public void setSuffix(final @NotNull Component suffix) {
            this.suffix = suffix;
        }
    }

    public enum Action {
        CREATE, REMOVE, UPDATE,
        ADD_ENTITIES, REMOVE_ENTITIES
    }

    public enum NameTagVisibility {
        ALWAYS, NEVER, HIDE_FOR_OTHER_TEAMS, HIDE_FOR_OWN_TEAM
    }

    public enum CollisionRule {
        ALWAYS, NEVER, OTHER_TEAMS, OWN_TEAM
    }

    public enum Color {
        // Colors
        BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD,
        GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE,

        // Formatting
        OBFUSCATED, BOLD, STRIKETHROUGH, UNDERLINED, ITALIC, RESET
    }

    public enum TeamFlag {
        ALLOW_FRIENDLY_FIRE, SHOW_INVISIBLE_TEAMMATES
    }
}
