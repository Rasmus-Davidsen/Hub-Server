package net.gemcraft.hub.protocol.packet.clientbound.login;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.world.entity.player.GameProfile;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ClientboundLoginFinishedPacket(@NotNull GameProfile gameProfile, @NotNull UUID sessionId) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        // Game profile
        msg.writeUUID(gameProfile.getUUID());   // UUID
        msg.writeString(gameProfile.getName()); // Name
        msg.writeVarInt(0);                     // Properties

        // Session Id
        msg.writeUUID(sessionId);
    }
}
