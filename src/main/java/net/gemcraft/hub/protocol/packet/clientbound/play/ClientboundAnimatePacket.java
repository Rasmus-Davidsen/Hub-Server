package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record ClientboundAnimatePacket(@NotNull Entity entity, @NotNull Animation animation) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeVarInt(entity.getEntityID()); // Entity ID
        msg.writeByte(animation.getID());      // Animation
    }

    public enum Animation {
        SWING_ARM(0),
        LEAVE_BED(2),
        SWING_OFFHAND(3),
        CRITICAL_EFFECT(4),
        MAGIC_CRITICAL_EFFECT(5);

        private final int id;

        Animation(final int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }
    }
}
