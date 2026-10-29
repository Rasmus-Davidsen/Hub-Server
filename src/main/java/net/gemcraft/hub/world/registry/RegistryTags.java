package net.gemcraft.hub.world.registry;

import net.gemcraft.hub.protocol.packet.clientbound.configuration.ClientboundUpdateTagsPacket;
import net.gemcraft.hub.server.Server;
import net.gemcraft.hub.world.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegistryTags {
    private static final @NotNull String DATA_PATH = "/data";

    public static @NotNull ClientboundUpdateTagsPacket createPacket() throws Exception {
        return new ClientboundUpdateTagsPacket(
                RegistryTags.createTags()
        );
    }

    public static ClientboundUpdateTagsPacket.@NotNull RegistryTags @NotNull[] createTags() throws Exception {
        final URI dataURI = getDataURI();

        if(dataURI == null) {
            return new ClientboundUpdateTagsPacket.RegistryTags[]{};
        }

        try(final FileSystem fs = FileSystems.newFileSystem(dataURI, Collections.emptyMap())) {
            final Map<NamespacedKey, Map<String, Integer[]>> tags = new HashMap<>();
            final Path dataDir = fs.getPath(DATA_PATH);

            try(final Stream<Path> paths = Files.walk(dataDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json")) // Ensure file extension is .json
                        .forEach(path -> processFile(path, dataDir, tags));
            }

            return tags.entrySet().stream()
                    .map(RegistryTags::createTag)
                    .toArray(ClientboundUpdateTagsPacket.RegistryTags[]::new);
        }
    }

    private static @Nullable URI getDataURI() throws URISyntaxException {
        final URL dataURL = Server.class.getResource(DATA_PATH);

        if(dataURL == null) {
            return null;
        }

        return dataURL.toURI();
    }

    private static void processFile(final @NotNull Path path, final @NotNull Path dataDir, final @NotNull Map<@NotNull NamespacedKey, @NotNull Map<@NotNull String, @NotNull Integer @NotNull[]>> tags) {
        final Path relPath = dataDir.relativize(path);
        final int nameCount = relPath.getNameCount();

        if(nameCount < 3) {
            return;
        }

        // Namespace (Index = 0)
        final String namespace = relPath
                .getName(0)
                .toString();

        // Tags folder (Index = 1)
        final String registry = relPath
                .getName(1)
                .toString();

        if(!registry.equals("tags")) {
            return;
        }

        // Registry (Index = 2)
        final String key = relPath
                .getName(2)
                .toString();

        final NamespacedKey registryID = new NamespacedKey(
                namespace, key
        );

        final Map<String, Integer[]> registryTags = tags.computeIfAbsent(
                registryID, _ -> new HashMap<>()
        );

        // Resource (Index = 3+)
        final String[] paths = relPath
                .toString()
                .split("/");

        final String tag = Arrays
                .stream(paths, 3, paths.length)
                .collect(Collectors.joining("/"))
                .replace(".json", "");

        registryTags.put(
                tag, new Integer[]{} // IDs
        );
    }

    private static ClientboundUpdateTagsPacket.@NotNull RegistryTags createTag(final Map.@NotNull Entry<@NotNull NamespacedKey, @NotNull Map<@NotNull String, @NotNull Integer @NotNull[]>> entry) {
        return new ClientboundUpdateTagsPacket.RegistryTags(
                entry.getKey(), entry.getValue()
        );
    }
}
