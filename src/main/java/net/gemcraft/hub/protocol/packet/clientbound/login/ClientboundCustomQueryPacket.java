package net.gemcraft.hub.protocol.packet.clientbound.login;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.Channel;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public record ClientboundCustomQueryPacket(@NotNull Channel channel, byte[] data, int id) implements ClientboundPacket {
    public ClientboundCustomQueryPacket(final @NotNull Channel channel, final byte[] data) {
        final int id = ThreadLocalRandom.current().nextInt();

        this(channel, data, id);
    }

    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeVarInt(id);               // ID
        msg.writeString(channel.getKey()); // Channel
        msg.writeBytes(data);              // Data
    }
}
