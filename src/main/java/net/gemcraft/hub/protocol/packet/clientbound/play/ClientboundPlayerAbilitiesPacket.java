package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import org.jetbrains.annotations.NotNull;

public record ClientboundPlayerAbilitiesPacket(float flyingSpeed, float fovModifier, @NotNull Flag @NotNull[] flags) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeFlags(flags);         // Flags
        msg.writeFloat(flyingSpeed);  // Flying speed
        msg.writeFloat(fovModifier);  // FOV modifier
    }

    public enum Flag {
        INVULNERABLE, FLYING, ALLOW_FLYING, CREATIVE_MODE
    }
}
