package net.gemcraft.hub.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ConversionUtils {
    /**
     * @param value String to be converted
     * @return Converted value or null if not an integer
     */
    public static @Nullable Integer getInteger(final @NotNull String value) {
        try {
            return Integer.parseInt(value);
        } catch(final NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * @param uuid String to be converted
     * @return Converted UUID or null if not a UUID
     */
    public static @Nullable UUID getUUID(final @NotNull String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch(final IllegalArgumentException ignored) {
            return null;
        }
    }
}
