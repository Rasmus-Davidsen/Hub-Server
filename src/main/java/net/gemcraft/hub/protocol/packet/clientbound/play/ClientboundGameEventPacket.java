package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import org.jetbrains.annotations.NotNull;

public record ClientboundGameEventPacket(byte type, float value) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeByte(type);   // Type
        msg.writeFloat(value); // Value
    }
}
