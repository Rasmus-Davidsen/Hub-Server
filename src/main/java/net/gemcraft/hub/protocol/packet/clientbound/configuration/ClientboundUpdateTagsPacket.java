package net.gemcraft.hub.protocol.packet.clientbound.configuration;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.world.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record ClientboundUpdateTagsPacket(@NotNull RegistryTags @NotNull... registries) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        // Array length
        msg.writeVarInt(
                registries.length
        );

        for(final RegistryTags registry : registries) {
            // Registry identifier
            final NamespacedKey id = registry.registryID();

            msg.writeString(
                    id.toString()
            );

            // Registry tags
            encodeTags(
                    registry, msg
            );
        }
    }

    private void encodeTags(final @NotNull RegistryTags registry, final @NotNull ByteMessage msg) {
        final Map<String, Integer[]> tags = registry.tags();

        // Prefixed size
        msg.writeVarInt(
                tags.size()
        );

        for(final Map.Entry<String, Integer[]> tag : tags.entrySet()) {
            // Tag name
            msg.writeString(
                    tag.getKey()
            );

            // Prefixed entry size
            final Integer[] ids = tag.getValue();

            msg.writeVarInt(
                    ids.length
            );

            // These IDs refer to the index of an entry sent with the ClientboundRegistryDataPacket
            // That's to reduce the amount of data sent, by making the server and client agree on a
            // numeric ID for each entry in a registry instead of sending the namespaced ID
            for(final int id : ids) {
                msg.writeVarInt(id);
            }
        }
    }

    public record RegistryTags(@NotNull NamespacedKey registryID, @NotNull Map<@NotNull String, @NotNull Integer @NotNull[]> tags) {}
}
