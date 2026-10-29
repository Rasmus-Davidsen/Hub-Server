package net.gemcraft.hub.protocol;

import org.jetbrains.annotations.NotNull;

/**
 * @see <a href="https://docs.papermc.io/paper/dev/plugin-messaging/">Channels from Paper</a>
 * @see <a href="https://minecraft.wiki/w/Java_Edition_protocol/Plugin_channels">Channels on the wiki</a>
 */
public enum Channel {
    BUNGEECORD("bungeecord:main"),
    VELOCITY_INFO_CHANNEL("velocity:player_info"),
    BRAND("minecraft:brand");

    private final @NotNull String key;

    Channel(final @NotNull String key) {
        this.key = key;
    }

    public @NotNull String getKey() {
        return key;
    }
}
