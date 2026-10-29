package net.gemcraft.hub.protocol.client;

import net.gemcraft.hub.Hub;
import net.gemcraft.hub.world.entity.player.Skin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.stream.Collectors;

public class SkinUtils {
    @Nullable
    public static Skin requestSkin(final @NotNull UUID uuid) {
        try {
            final HttpsURLConnection connection = getRequestConnection(
                    uuid
            );

            if(connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                Hub.getLogger().warn(String.format(
                        "Failed to open skin request. Code: %s, Message: %s",
                        connection.getResponseCode(), connection.getResponseMessage()
                ));

                return null;
            }

            final InputStreamReader reader = new InputStreamReader(
                    connection.getInputStream()
            );

            final BufferedReader bufferedReader = new BufferedReader(
                    reader
            );

            final String reply = bufferedReader
                    .lines()
                    .collect(Collectors.joining(""))
                    .replace(" ", "");

            final String skin = reply
                    .split("\"value\":\"")[1]
                    .split("\"")[0];

            final String signature = reply
                    .split("\"signature\":\"")[1]
                    .split("\"")[0];

            return new Skin(
                    skin, signature
            );
        } catch(final IOException | URISyntaxException ex) {
            Hub.getLogger().warn(
                    "Couldn't complete the skin request: " + ex
            );

            return null;
        }
    }

    private static @NotNull HttpsURLConnection getRequestConnection(final @NotNull UUID uuid) throws URISyntaxException, IOException {
        final URLConnection urlConnection = getURL(uuid).openConnection();
        final HttpsURLConnection connection = (HttpsURLConnection) urlConnection;

        connection.setUseCaches(
                false
        );

        connection.setDefaultUseCaches(
                false
        );

        connection.addRequestProperty(
                "User-Agent", "Mozilla/5.0"
        );

        connection.addRequestProperty(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );

        connection.addRequestProperty(
                "Pragma", "no-cache"
        );

        return connection;
    }

    public static @NotNull URL getURL(final @NotNull UUID uuid) throws URISyntaxException, IOException {
        final URI uri = new URI(String.format(
                "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", uuid
        ));

        return uri.toURL();
    }
}
