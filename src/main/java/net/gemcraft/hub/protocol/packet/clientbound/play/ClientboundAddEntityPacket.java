package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.world.Location;
import net.gemcraft.hub.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record ClientboundAddEntityPacket(@NotNull Entity entity) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        final Location loc = entity.getLocation();

        msg.writeVarInt(entity.getEntityID());     // Entity ID
        msg.writeUUID(entity.getUUID());           // UUID
        msg.writeVarInt(entity.getType().getID()); // Type
        msg.writeDouble(loc.x());                  // X
        msg.writeDouble(loc.y());                  // Y
        msg.writeDouble(loc.z());                  // Z
        msg.writeVelocity(entity.getVelocity());   // Velocity
        msg.writeRotation(loc.pitch());            // Pitch
        msg.writeRotation(loc.yaw());              // Yaw
        msg.writeRotation(loc.yaw());              // Head yaw
        msg.writeVarInt(entity.getData());         // Data
    }
}
