package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record ClientboundSystemChatPacket(@NotNull Component content, boolean isOverlay) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeComponent(content); // Content
        msg.writeBoolean(isOverlay); // Overlay
    }
}
