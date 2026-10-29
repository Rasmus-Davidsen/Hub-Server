package net.gemcraft.hub.protocol.packet.clientbound.status;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import org.jetbrains.annotations.NotNull;

public record ClientboundPingPacket(long id) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeLong(id); // ID
    }
}
