package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.world.Location;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public record ClientboundPlayerPositionPacket(int id, @NotNull Location loc, double velX, double velY, double velZ, int flags) implements ClientboundPacket {
    public ClientboundPlayerPositionPacket(final @NotNull Location loc) {
        this(loc, 0, 0, 0);
    }

    public ClientboundPlayerPositionPacket(final @NotNull Location loc, final double velX, final double velY, final double velZ) {
        this(loc, velX, velY, velZ, 0);
    }

    public ClientboundPlayerPositionPacket(final @NotNull Location loc, final double velX, final double velY, final double velZ, final int flags) {
        final int id = ThreadLocalRandom.current().nextInt();

        this(id, loc, velX, velY, velZ, flags);
    }

    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeVarInt(id);         // Teleport ID
        msg.writeDouble(loc.x());    // X
        msg.writeDouble(loc.y());    // Y
        msg.writeDouble(loc.z());    // Z
        msg.writeDouble(velX);       // Vel X
        msg.writeDouble(velY);       // Vel Y
        msg.writeDouble(velZ);       // Vel Z
        msg.writeFloat(loc.yaw());   // Yaw
        msg.writeFloat(loc.pitch()); // Pitch
        msg.writeInt(flags);         // Flags
    }
}
