package net.gemcraft.hub.protocol.packet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gemcraft.hub.Hub;
import net.gemcraft.hub.protocol.State;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResourceLoader {
    private static final String PATH = "/generated/packets.json";

    public static @NotNull Map<@NotNull String, @NotNull Integer> load(final @NotNull State state, final @NotNull PacketFlow flow) {
        final JsonObject root;

        try {
            root = readRegistry();
        } catch(final IOException ex) {
            return Collections.emptyMap();
        }

        final JsonElement stateJson = root.get(
                state.name().toLowerCase()
        );

        if(stateJson == null || !stateJson.isJsonObject()) {
            return Collections.emptyMap();
        }

        final JsonElement flowJson = stateJson.getAsJsonObject().get(
                flow.name().toLowerCase()
        );

        if(flowJson == null || !flowJson.isJsonObject()) {
            return Collections.emptyMap();
        }

        final JsonObject jsonObj = flowJson.getAsJsonObject();
        return loadPackets(jsonObj);
    }

    private static @NotNull JsonObject readRegistry() throws IOException {
        try(final InputStream in = Hub.class.getResourceAsStream(PATH)) {
            if(in == null) {
                throw new FileNotFoundException(
                        "Packet registry missing in resources at " + PATH
                );
            }

            final InputStreamReader reader = new InputStreamReader(
                    in, StandardCharsets.UTF_8
            );

            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static @NotNull Map<@NotNull String, @NotNull Integer> loadPackets(final @NotNull JsonObject flowJson) {
        final Map<String, Integer> packets = new HashMap<>();

        for(final Map.Entry<String, JsonElement> entry : flowJson.entrySet()) {
            final JsonElement content = entry.getValue();

            if(!content.isJsonObject()) {
                continue;
            }

            final JsonElement idElement = content.getAsJsonObject().get("protocol_id");

            if(idElement == null) {
                continue;
            }

            final int id;

            try {
                id = idElement.getAsInt();
            } catch(final NumberFormatException ex) {
                continue;
            }

            packets.put(entry.getKey(), id);
        }

        return packets;
    }
}
