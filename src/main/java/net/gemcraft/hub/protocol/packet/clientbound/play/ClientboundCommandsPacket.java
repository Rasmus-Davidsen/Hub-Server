package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record ClientboundCommandsPacket(@NotNull Set<@NotNull String> commands) implements ClientboundPacket {
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeVarInt(
                commands.size() * 2 + 1
        );

        // Root node
        msg.writeByte(0);

        msg.writeVarInt(
                commands.size()
        );

        for(int i = 1; i <= commands.size() * 2; i++) {
            msg.writeVarInt(i);

            i++;
        }

        int i = 1;

        for(final String cmd : commands) {
            msg.writeByte(1 | 0x04);
            msg.writeVarInt(1);
            msg.writeVarInt(i + 1);
            msg.writeString(cmd);

            i++;

            msg.writeByte(2 | 0x04 | 0x10);
            msg.writeVarInt(0);
            msg.writeString("arg");

            // Brigadier string
            msg.writeVarInt(5);
            msg.writeVarInt(2);

            msg.writeString(
                    "minecraft:ask_server"
            );

            i++;
        }

        // Root index
        msg.writeVarInt(0);
    }
}
