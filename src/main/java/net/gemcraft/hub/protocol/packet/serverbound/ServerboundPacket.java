package net.gemcraft.hub.protocol.packet.serverbound;

import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.Packet;
import org.jetbrains.annotations.NotNull;

public interface ServerboundPacket extends Packet {
    void handle(final @NotNull ClientConnection connection);
}
