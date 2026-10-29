package net.gemcraft.hub.world;

import net.kyori.adventure.key.Namespaced;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record NamespacedKey(@NotNull String namespace, @NotNull String key) implements Namespaced {
    public static final @NotNull String MINECRAFT = "minecraft";
    public static final @NotNull String GEMCRAFT = "gemcraft";

    public NamespacedKey(final @NotNull String key) {
        this(MINECRAFT, key);
    }

    public NamespacedKey(final @NotNull String namespace, final @NotNull String key) {
        this.namespace = namespace;
        this.key = key;

        // Validation
        if(isInvalid(namespace) || isInvalid(key)) {
            throw new IllegalArgumentException(String.format(
                    "Namespace and key must be follow the pattern [a-z0-9/._-]. Namespace: %s, Key: %s",
                    namespace, key
            ));
        }
    }

    @Override
    public @NotNull String toString() {
        return namespace + ':' + key;
    }

    private static boolean isInvalid(final @NotNull String str) {
        if(str.isEmpty()) {
            return true;
        }

        for(final char c : str.toCharArray()) {
            if(isInvalid(c)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isInvalid(char c) {
        return (c < 'a' || c > 'z')
                && (c < '0' || c > '9')
                && c != '/'
                && c != '.'
                && c != '_'
                && c != '-';
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if(obj == null) {
            return false;
        }

        if(!(obj instanceof NamespacedKey(String namespace1, String key1))) {
            return false;
        }

        return namespace.equals(namespace1)
                && key.equals(key1);
    }
}
