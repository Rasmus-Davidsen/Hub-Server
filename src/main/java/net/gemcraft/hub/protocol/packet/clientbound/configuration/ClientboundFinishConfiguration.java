package net.gemcraft.hub.protocol.packet.clientbound.configuration;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import org.jetbrains.annotations.NotNull;

public class ClientboundFinishConfiguration implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {}
}
