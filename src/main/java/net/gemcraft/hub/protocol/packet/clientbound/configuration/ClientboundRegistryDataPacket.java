package net.gemcraft.hub.protocol.packet.clientbound.configuration;

import com.google.gson.JsonElement;
import net.gemcraft.hub.protocol.BinaryTagUtils;
import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.world.NamespacedKey;
import net.gemcraft.hub.world.registry.Registry;
import net.kyori.adventure.nbt.BinaryTag;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record ClientboundRegistryDataPacket(@NotNull NamespacedKey key, @NotNull Registry registry) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        encodeType(msg);    // Type
        encodeEntries(msg); // Entries
    }

    private void encodeType(final @NotNull ByteMessage msg) {
        msg.writeString(key.toString());
    }

    private void encodeEntries(final @NotNull ByteMessage msg) {
        final Map<NamespacedKey, JsonElement> entries = registry.entries();

        // Length prefix
        msg.writeVarInt(entries.size());

        for(final Map.Entry<NamespacedKey, JsonElement> entry : entries.entrySet()) {
            // Resource
            final NamespacedKey resource = entry.getKey();
            msg.writeString(resource.toString());

            // Properties
            encodeProperties(msg, entry);
        }
    }

    private void encodeProperties(final @NotNull ByteMessage msg, final Map.@NotNull Entry<@NotNull NamespacedKey, @NotNull JsonElement> entry) {
        msg.writeBoolean(true);

        final BinaryTag propertiesTag = BinaryTagUtils.create(
                entry.getValue()
        );

        msg.writeNamelessCompoundTag(
                propertiesTag
        );
    }
}
