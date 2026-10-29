package net.gemcraft.hub.server.config;

import net.gemcraft.hub.Hub;
import net.gemcraft.hub.protocol.Address;
import net.gemcraft.hub.server.ServerType;
import org.jetbrains.annotations.NotNull;

public class Config {
    private static final @NotNull ConfigLoader LOADER = new ConfigLoader().load();

    public static final @NotNull Address ADDRESS;
    public static final boolean VELOCITY;
    public static final @NotNull String VELOCITY_KEY;
    public static final @NotNull String SCHEMATIC;
    public static final int MAX_PLAYERS;
    public static final @NotNull String SERVER_BRAND;
    public static final boolean REDUCE_DEBUG_INFO;
    public static final boolean HARDCORE;

    static {
        // Address
        ADDRESS = loadAddress();

        // Velocity
        VELOCITY = LOADER.getValue(
                Property.VELOCITY, Boolean.class
        );

        // Velocity key
        VELOCITY_KEY = LOADER.getValue(
                Property.VELOCITY_KEY, String.class
        );

        // Schematic
        SCHEMATIC = LOADER.getValue(
                Property.SCHEMATIC, String.class
        );

        // Max players
        MAX_PLAYERS = LOADER.getValue(
                Property.MAX_PLAYERS, Integer.class
        );

        // Server brand
        SERVER_BRAND = LOADER.getValue(
                Property.SERVER_BRAND, String.class
        );

        // Reduce debug info
        REDUCE_DEBUG_INFO = LOADER.getValue(
                Property.REDUCE_DEBUG_INFO, Boolean.class
        );

        // Hardcore
        HARDCORE = LOADER.getValue(
                Property.HARDCORE, Boolean.class
        );
    }

    private static @NotNull Address loadAddress() {
        final String address = LOADER.getValue(
                Property.ADDRESS, String.class
        );

        try {
            return Address.of(address);
        } catch(final IllegalArgumentException ex) {
            Hub.getLogger().warn(
                    "Address should be in the format of '<Host>:<Port>': " + address
            );

            return (Address) Property.ADDRESS.getDefaultValue();
        }
    }

    public enum Property {
        ADDRESS(Address.getRandomLocal().toString()),
        VELOCITY(true),
        VELOCITY_KEY(""),
        SCHEMATIC("map.schem"),
        MAX_PLAYERS(50),
        SERVER_BRAND(ServerType.HUB.getName()),
        REDUCE_DEBUG_INFO(false),
        HARDCORE(false);

        private final @NotNull Object defaultValue;

        Property(final @NotNull Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        public @NotNull Object getDefaultValue() {
            return defaultValue;
        }

        public @NotNull String getKey() {
            return toString()
                    .toLowerCase()
                    .replace('_', '-');
        }
    }
}
