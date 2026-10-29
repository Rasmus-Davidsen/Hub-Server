package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record ClientboundSetEntityDataPacket(@NotNull Entity entity) implements ClientboundPacket {
    private static final int END_OF_DATA = 0xFF;

    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeVarInt(entity.getEntityID()); // Entity ID
        entity.encodeData(msg);                // Entity data
        msg.writeByte(END_OF_DATA);            // End of data
    }
}
