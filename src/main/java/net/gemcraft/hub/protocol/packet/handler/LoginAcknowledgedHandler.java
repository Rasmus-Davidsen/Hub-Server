package net.gemcraft.hub.protocol.packet.handler;

import io.netty.buffer.Unpooled;
import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.Channel;
import net.gemcraft.hub.protocol.State;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.protocol.packet.clientbound.common.ClientboundCustomPayloadPacket;
import net.gemcraft.hub.protocol.packet.clientbound.configuration.ClientboundFinishConfiguration;
import net.gemcraft.hub.server.Server;
import net.gemcraft.hub.server.config.Config;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LoginAcknowledgedHandler {
    public static void handle(final @NotNull ClientConnection connection) {
        connection.updateState(State.CONFIGURATION);

        // Registry data
        final Server server = connection.getServer();

        final List<ClientboundPacket> packets = new ArrayList<>(
                server.getRegistryPackets()
        );

        // Server brand
        final ByteMessage payload = new ByteMessage(
                Unpooled.buffer()
        );

        payload.writeString(Config.SERVER_BRAND);

        packets.add(new ClientboundCustomPayloadPacket(
                Channel.BRAND, payload
        ));

        // Update tags
        packets.add(server.getUpdateTagsPacket());

        // Finish configuration
        packets.add(new ClientboundFinishConfiguration());

        connection.sendPackets(packets);
    }
}
