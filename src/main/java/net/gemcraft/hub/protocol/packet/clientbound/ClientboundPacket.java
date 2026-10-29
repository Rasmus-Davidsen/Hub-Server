package net.gemcraft.hub.protocol.packet.clientbound;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.Packet;
import org.jetbrains.annotations.NotNull;

public interface ClientboundPacket extends Packet {
    void encode(final @NotNull ByteMessage msg);
}
