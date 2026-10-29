package net.gemcraft.hub.server.config;

import net.gemcraft.hub.Hub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

public class ConfigLoader {
    private static final @NotNull String FILE_NAME = "config.yml";
    private final @NotNull Properties config = new Properties();

    public @NotNull ConfigLoader load() {
        try {
            final File file = getFile();

            try(final FileInputStream in = new FileInputStream(file)) {
                config.load(in);
            }
        } catch(final IOException ex) {
            Hub.getLogger().warn("Couldn't load some of the config: " + ex);
        }

        return this;
    }

    private @NotNull File getFile() throws IOException {
        final File file = new File(FILE_NAME);

        if(file.createNewFile()) {
            populateConfig(file);

            Hub.getLogger().info(
                    "Created default " + FILE_NAME
            );
        }

        return file;
    }

    private void populateConfig(final @NotNull File file) throws IOException {
        // Set all config properties with their default value
        try(final PrintWriter writer = new PrintWriter(file)) {
            for(final Config.Property property : Config.Property.values()) {
                final String key = property.getKey();

                final String value = String.valueOf(
                        property.getDefaultValue()
                );

                // Set in loaded config
                config.setProperty(
                        key, value
                );

                // Write to config file
                writer.println(String.format(
                        "%s=%s", key, value
                ));
            }
        }
    }

    public <T> @NotNull T getValue(final @NotNull Config.Property property, final @NotNull Class<T> valueClass) {
        final String valueStr = config.getProperty(
                property.getKey()
        );

        final Object value = processValue(
                property, valueStr, valueClass
        );

        return valueClass.cast(value);
    }

    private @NotNull Object processValue(final @NotNull Config.Property property, final @Nullable String valueStr, final @NotNull Class<?> valueClass) {
        final Object value = castValue(
                property, valueStr, valueClass
        );

        return convertValue(
                value, valueClass
        );
    }

    private @NotNull Object castValue(final @NotNull Config.Property property, final @Nullable String valueStr, final @NotNull Class<?> valueClass) {
        if(valueStr == null) {
            return property.getDefaultValue();
        }

        try {
            return parseValue(
                    valueStr, valueClass
            );
        } catch(final IllegalArgumentException ex) {
            Hub.getLogger().warn(String.format(
                    "Couldn't load %s from the config: " + ex,
                    property.getKey()
            ));

            return property.getDefaultValue();
        }
    }

    private @NotNull Object parseValue(final @NotNull String value, final @NotNull Class<?> valueClass) throws IllegalArgumentException {
        if(valueClass == String.class) {
            return value;
        }

        if(valueClass == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        if(valueClass == Integer.class) {
            return Integer.parseInt(value);
        }

        if(valueClass == Double.class) {
            return Double.parseDouble(value);
        }

        if(valueClass == Float.class) {
            return Float.parseFloat(value);
        }

        if(valueClass == Long.class) {
            return Long.parseLong(value);
        }

        if(valueClass == Short.class) {
            return Short.parseShort(value);
        }

        throw new IllegalArgumentException(
                "Unsupported Object Type: " + valueClass.getName()
        );
    }

    private <T> @NotNull T convertValue(final @NotNull Object value, final @NotNull Class<T> valueClass) throws IllegalArgumentException {
        if(valueClass.isInstance(value)) {
            return valueClass.cast(value);
        }

        throw new IllegalArgumentException(String.format(
                "Supposed class of value does not match the actual value class. Supposed: %s, Actual: %s ",
                valueClass.getName(), value.getClass().getName()
        ));
    }
}
