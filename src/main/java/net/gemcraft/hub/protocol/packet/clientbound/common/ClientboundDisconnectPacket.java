package net.gemcraft.hub.protocol.packet.clientbound.common;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record ClientboundDisconnectPacket(@NotNull Component reason) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeComponent(reason); // Reason
    }
}
