package net.gemcraft.hub.world;

import org.jetbrains.annotations.NotNull;

public enum Environment {
    OVERWORLD("overworld"),
    NETHER("the_nether"),
    END("the_end"),
    UNIVERSE(NamespacedKey.GEMCRAFT, "universe");

    private final @NotNull NamespacedKey resourceKey;

    Environment(final @NotNull String key) {
        this(NamespacedKey.MINECRAFT, key);
    }

    Environment(final @NotNull String namespace, final @NotNull String key) {
        resourceKey = new NamespacedKey(
                namespace, key
        );
    }

    public @NotNull NamespacedKey getKey() {
        return resourceKey;
    }
}
