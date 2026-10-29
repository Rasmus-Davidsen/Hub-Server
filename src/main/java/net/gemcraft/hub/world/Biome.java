package net.gemcraft.hub.world;

import org.jetbrains.annotations.NotNull;

public enum Biome {
    OCEAN("ocean"),
    PLAINS("plains"),
    DESERT("desert"),
    SPACE(NamespacedKey.GEMCRAFT, "space");

    private final @NotNull NamespacedKey key;

    Biome(final @NotNull String key) {
        this(NamespacedKey.MINECRAFT, key);
    }

    Biome(final @NotNull String namespace, final @NotNull String key) {
        this.key = new NamespacedKey(
                namespace, key
        );
    }

    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
