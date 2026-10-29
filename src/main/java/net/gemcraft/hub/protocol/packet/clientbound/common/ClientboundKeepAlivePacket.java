package net.gemcraft.hub.protocol.packet.clientbound.common;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import org.jetbrains.annotations.NotNull;

public record ClientboundKeepAlivePacket(long id) implements ClientboundPacket {
    public ClientboundKeepAlivePacket() {
        this(System.currentTimeMillis());
    }

    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeLong(id); // ID
    }
}
