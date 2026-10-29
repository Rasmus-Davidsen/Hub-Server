package net.gemcraft.hub.server;

import io.netty.buffer.Unpooled;
import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.Channel;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.clientbound.common.ClientboundCustomPayloadPacket;
import net.gemcraft.hub.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class Velocity {
    public static void connect(final @NotNull Player player, final @NotNull ServerType server) {
        final ByteMessage payload = new ByteMessage(
                Unpooled.buffer()
        );

        payload.writeUTF("Connect");
        payload.writeUTF(server.getName());

        final ClientConnection connection = player.getConnection();

        connection.sendPacket(new ClientboundCustomPayloadPacket(
                Channel.BUNGEECORD, payload
        ));
    }
}
