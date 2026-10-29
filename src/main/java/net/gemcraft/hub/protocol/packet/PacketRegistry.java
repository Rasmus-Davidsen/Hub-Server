package net.gemcraft.hub.protocol.packet;

import net.gemcraft.hub.Hub;
import net.gemcraft.hub.protocol.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PacketRegistry<T extends Packet> {
    private final Map<@NotNull Integer, @NotNull Class<? extends T>> packets;
    private final Map<@NotNull Class<? extends T>, @NotNull Integer> packetIdByClass;

    public PacketRegistry(final Map<@NotNull Integer, @NotNull Class<? extends T>> packets) {
        this.packets = Collections.unmodifiableMap(packets);

        this.packetIdByClass = packets.entrySet().stream().collect(
                Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey)
        );
    }

    public @Nullable Class<? extends T> getPacket(final int id) {
        return packets.get(id);
    }

    public @Nullable Integer getPacketId(final @NotNull Class<? extends T> packetClass) {
        return packetIdByClass.get(
                packetClass
        );
    }

    public static <T extends Packet> @NotNull PacketRegistry<T> of(final @NotNull State state, final @NotNull PacketFlow flow,
                                                                   final @NotNull Map<@NotNull String, @NotNull Class<? extends T>> resourceMappings)
    {
        final Map<String, Integer> resources = ResourceLoader.load(
                state, flow
        );

        final Map<Integer, Class<? extends T>> packets = new HashMap<>();

        for(final Map.Entry<String, Class<? extends T>> entry : resourceMappings.entrySet()) {
            String resource = entry.getKey();

            if(!resource.contains(":")) {
                resource = "minecraft:" + resource;
            }

            final Integer id = resources.get(resource);

            if(id == null) {
                Hub.getLogger().warn("No packet with resource: " + resource);
                continue;
            }

            packets.put(id, entry.getValue());
        }

        return new PacketRegistry<>(
                packets
        );
    }
}
