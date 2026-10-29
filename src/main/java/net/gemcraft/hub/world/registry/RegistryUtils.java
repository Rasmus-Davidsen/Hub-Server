package net.gemcraft.hub.world.registry;

import net.gemcraft.hub.server.Server;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

class RegistryUtils {
    protected static final @NotNull String DATA_PATH = "/data";

    protected static @Nullable URI getDataURI() throws URISyntaxException {
        final URL dataURL = Server.class.getResource(DATA_PATH);

        if(dataURL == null) {
            return null;
        }

        return dataURL.toURI();
    }
}
