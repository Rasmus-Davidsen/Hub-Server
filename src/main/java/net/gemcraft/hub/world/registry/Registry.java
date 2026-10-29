package net.gemcraft.hub.world.registry;

import com.google.gson.JsonElement;
import net.gemcraft.hub.world.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record Registry(@NotNull NamespacedKey type, @NotNull Map<@NotNull NamespacedKey, @NotNull JsonElement> entries) {
    public @Nullable Integer getIndex(final @NotNull NamespacedKey resource) {
        int i = 0;

        for(final Map.Entry<NamespacedKey, JsonElement> entry : entries.entrySet()) {
            if(entry.getKey().equals(resource)) {
                return i;
            }

            i++;
        }

        return null;
    }

    public enum Type {
        DIMENSION_TYPE("dimension_type"),
        BIOME("worldgen/biome"),
        WORLD_CLOCK("world_clock"),
        CHAT_TYPE("chat_type");

        private final @NotNull NamespacedKey key;

        Type(final @NotNull String key) {
            this.key = new NamespacedKey(
                    NamespacedKey.MINECRAFT, key
            );
        }

        public @NotNull NamespacedKey getKey() {
            return key;
        }
    }
}
