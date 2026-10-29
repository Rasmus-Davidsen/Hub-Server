package net.gemcraft.hub.protocol;

import net.gemcraft.hub.utils.ConversionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public record Address(@NotNull String host, int port) {
    public Address {
        if(isIllegalPort(port)) {
            throw new IllegalArgumentException(String.format(
                    "Port is outside the valid range of 0-65535. Host: %s, Port: %s",
                    host, port
            ));
        }
    }

    public static @NotNull Address of(final @NotNull String address) throws IllegalArgumentException {
        final String[] parts = address.split(":");

        if(parts.length < 2) {
            throw new IllegalArgumentException(String.format(
                    "Address is missing a port separated by ':'. Address: %s",
                    address
            ));
        }

        final Integer port = ConversionUtils.getInteger(parts[1]);

        if(port == null) {
            throw new IllegalArgumentException(String.format(
                    "Port is not a valid integer. Host: %s, Port: %s",
                    parts[0], parts[1]
            ));
        }

        return new Address(
                parts[0], port
        );
    }

    public static @NotNull Address getRandomLocal() {
        final int port = getRandomPort();

        return new Address(
                "localhost", port
        );
    }

    public static int getRandomPort() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();

        return 1 + random.nextInt(
                49152, 65535
        );
    }

    public static boolean isIllegalPort(final int port) {
        return port < 0 || port > 65535;
    }

    public static boolean isLocal(final @NotNull String host) {
        return host.equals("localhost")    // Localhost
                || host.startsWith("127.") // Loopback
                || host.equals("0.0.0.0"); // Placeholder
    }

    public static boolean isSimilar(final @NotNull String host, final @NotNull String host1) {
        return isLocal(host)
                ? isLocal(host1)
                : host1.equals(host);
    }

    @Override
    public @NotNull String toString() {
        return host + ':' + port;
    }
}
