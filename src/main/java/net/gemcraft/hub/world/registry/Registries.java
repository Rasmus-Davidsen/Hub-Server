package net.gemcraft.hub.world.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.gemcraft.hub.Hub;
import net.gemcraft.hub.protocol.packet.clientbound.configuration.ClientboundRegistryDataPacket;
import net.gemcraft.hub.server.Server;
import net.gemcraft.hub.world.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Registries {
    private static final @NotNull Map<@NotNull NamespacedKey, @NotNull Registry> REGISTRIES = new HashMap<>();

    static {
        try {
            REGISTRIES.putAll(
                    createRegistries()
            );
        } catch(final Exception ex) {
            Hub.getLogger().warn(
                    ex.toString()
            );
        }
    }

    public static @Nullable Registry get(final Registry.@NotNull Type type) {
        return REGISTRIES.get(
                type.getKey()
        );
    }

    public static @NotNull Set<@NotNull ClientboundRegistryDataPacket> createPackets() {
        final Set<ClientboundRegistryDataPacket> packets = new HashSet<>();

        for(final Map.Entry<NamespacedKey, Registry> entry : REGISTRIES.entrySet()) {
            packets.add(new ClientboundRegistryDataPacket(
                    entry.getKey(), entry.getValue()
            ));
        }

        return packets;
    }

    public static @NotNull Map<@NotNull NamespacedKey, @NotNull Registry> createRegistries() throws Exception {
        final URI dataURI = RegistryUtils.getDataURI();

        if(dataURI == null) {
            return Collections.emptyMap();
        }

        try(final FileSystem fs = FileSystems.newFileSystem(dataURI, Collections.emptyMap())) {
            final Map<NamespacedKey, Map<NamespacedKey, JsonElement>> registries = new HashMap<>();

            final Path dataDir = fs.getPath(
                    RegistryUtils.DATA_PATH
            );

            try(final Stream<Path> paths = Files.walk(dataDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json")) // Ensure file extension is .json
                        .forEach(path -> processFile(path, dataDir, registries));
            }

            final Map<NamespacedKey, Registry> registries1 = new HashMap<>();

            for(final Map.Entry<NamespacedKey, Map<NamespacedKey, JsonElement>> entry : registries.entrySet()) {
                final Registry registry = new Registry(
                        entry.getKey(), entry.getValue()
                );

                registries1.put(
                        entry.getKey(), registry
                );
            }

            return registries1;
        }
    }

    private static void processFile(final @NotNull Path path, final @NotNull Path dataDir, final @NotNull Map<@NotNull NamespacedKey, @NotNull Map<@NotNull NamespacedKey, @NotNull JsonElement>> registries) {
        final Path relPath = dataDir.relativize(path);
        final int nameCount = relPath.getNameCount();

        if(nameCount < 3) {
            return;
        }

        // Namespace (Index = 0)
        final String namespace = relPath
                .getName(0)
                .toString();

        // Registry (Index = 1+ - last)
        final String[] paths = relPath
                .toString()
                .split("/");

        final String registryKey = Arrays
                .stream(paths, 1, paths.length - 1)
                .collect(Collectors.joining("/"));

        if(registryKey.startsWith("tags")) {
            return;
        }

        final NamespacedKey registry = new NamespacedKey(
                NamespacedKey.MINECRAFT, registryKey
        );

        final Map<NamespacedKey, JsonElement> entries = registries.computeIfAbsent(
                registry, _ -> new HashMap<>()
        );

        // Resource (Index = last)
        final int i = paths.length - 1;

        final String fileName = paths[i].replace(
                ".json", ""
        );

        final NamespacedKey resource = new NamespacedKey(
                namespace, fileName
        );

        final JsonElement properties = readProperties(path);

        if(properties != null) {
            entries.put(resource, properties);
        }
    }

    private static @Nullable JsonElement readProperties(final @NotNull Path path) {
        try(final InputStream in = Server.class.getResourceAsStream(path.toString())) {
            if(in == null) {
                return null;
            }

            try(final InputStreamReader reader = new InputStreamReader(in)) {
                return JsonParser.parseReader(reader);
            }
        } catch(final Exception ex) {
            return null;
        }
    }
}
