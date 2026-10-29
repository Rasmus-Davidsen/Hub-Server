package net.gemcraft.hub.protocol.packet.clientbound.common;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.Channel;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import org.jetbrains.annotations.NotNull;

public record ClientboundCustomPayloadPacket(@NotNull Channel channel, @NotNull ByteMessage payload) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeString(channel.getKey()); // Channel
        msg.writeBytes(payload);           // Payload
    }
}
