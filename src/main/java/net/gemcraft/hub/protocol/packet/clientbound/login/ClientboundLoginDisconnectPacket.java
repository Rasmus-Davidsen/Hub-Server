package net.gemcraft.hub.protocol.packet.clientbound.login;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;

public record ClientboundLoginDisconnectPacket(@NotNull Component reason) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        // Reason
        final GsonComponentSerializer gson = GsonComponentSerializer.gson();
        final String json = gson.serialize(reason);

        msg.writeString(json);
    }
}
