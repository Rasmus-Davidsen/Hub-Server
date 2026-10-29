package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.world.Environment;
import net.gemcraft.hub.world.NamespacedKey;
import net.gemcraft.hub.world.registry.Registries;
import net.gemcraft.hub.world.registry.Registry;
import org.jetbrains.annotations.NotNull;

public record ClientboundSetTimePacket(long time, float tickRate) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeLong(time);    // Game time
        encodeClockStates(msg); // Clock states
    }

    private void encodeClockStates(final @NotNull ByteMessage msg) {
        // Entry count
        msg.writeVarInt(1);

        // Index of Overworld in the World Clock registry packet
        final Registry registry = Registries.get(
                Registry.Type.WORLD_CLOCK
        );

        if(registry == null) {
            throw new IllegalStateException(
                    "World clock registry has not been created"
            );
        }

        final NamespacedKey key = Environment.OVERWORLD.getKey();
        final Integer i = registry.getIndex(key);

        if(i == null) {
            throw new IllegalStateException(
                    "No such value in world clock registry: " + key.toString()
            );
        }

        msg.writeVarInt(i);

        // Clock network state
        msg.writeVarLong(time);    // Total ticks
        msg.writeFloat(0.0f);      // Partial ticks (0-1)
        msg.writeFloat(tickRate);  // Tick rate
    }
}
