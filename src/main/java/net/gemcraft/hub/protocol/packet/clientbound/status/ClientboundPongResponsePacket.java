package net.gemcraft.hub.protocol.packet.clientbound.status;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public record ClientboundPongResponsePacket(int playerCount, int maxPlayerCount, @Nullable String motd, int protocol, @Nullable String versionMsg,
                                            @NotNull SamplePlayer @Nullable[] playerSamples) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        final JsonObject json = new JsonObject();

        json.add("version", getVersion()); // Version
        json.add("players", getPlayers()); // Players

        // Motd
        final String description = Objects.requireNonNullElse(motd, "");

        json.addProperty(
                "description", description
        );

        msg.writeString(
                json.toString()
        );
    }

    private @NotNull JsonObject getVersion() {
        final JsonObject json = new JsonObject();

        // Version message
        final String name = versionMsg == null
                ? "" : versionMsg;

        json.addProperty(
                "name", name
        );

        // Protocol
        json.addProperty(
                "protocol", protocol
        );

        return json;
    }

    private @NotNull JsonObject getPlayers() {
        final JsonObject json = new JsonObject();

        // Max players
        json.addProperty("max", maxPlayerCount);

        // Player count
        json.addProperty("online", playerCount);

        // Hover text
        json.add("sample", getPlayerSamples());

        return json;
    }

    private @NotNull JsonArray getPlayerSamples() {
        final JsonArray array = new JsonArray();

        if(playerSamples == null) {
            return array;
        }

        for(final SamplePlayer sample : playerSamples) {
            array.add(
                    getPlayerSample(sample)
            );
        }

        return array;
    }

    private @NotNull JsonObject getPlayerSample(final @NotNull SamplePlayer sample) {
        final JsonObject json = new JsonObject();

        json.addProperty(
                "name", sample.username()
        );

        json.addProperty(
                "id", sample.uuid().toString()
        );

        return json;
    }

    public record SamplePlayer(@NotNull UUID uuid, @NotNull String username) {}
}
