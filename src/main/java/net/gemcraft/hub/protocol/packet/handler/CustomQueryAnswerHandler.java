package net.gemcraft.hub.protocol.packet.handler;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.serverbound.login.ServerboundCustomQueryAnswerPacket;
import net.gemcraft.hub.server.Server;
import net.gemcraft.hub.server.config.Config;
import net.gemcraft.hub.world.entity.player.GameProfile;
import net.gemcraft.hub.world.entity.player.Skin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CustomQueryAnswerHandler {
    public static void handle(final @NotNull ClientConnection connection, final @NotNull ServerboundCustomQueryAnswerPacket packet) {
        if(!Config.VELOCITY) {
            return;
        }

        if(packet.id() != connection.getVelocityLoginID()) {
            return;
        }

        final ByteMessage data = packet.data();

        if(!packet.isSuccessful() || data == null) {
            connection.disconnectDirect("You need to connect with Velocity");
            return;
        }

        if(!connection.hasVelocityKey(data)) {
            connection.disconnect("Can't verify forwarded information");
            return;
        }

        // Address
        connection.setAddress(
                data.readString()
        );

        final UUID uuid = data.readUUID();
        final String name = data.readString();

        final GameProfile gameProfile = new GameProfile(
                uuid, name
        );

        // Properties
        final int count = data.readVarInt();

        for(int i = 0; i < count; i++) {
            final String property = data.readString();
            final String value = data.readString();

            String signature = null;

            if(data.readBoolean()) { // Has signature
                signature = data.readString();
            }

            if(property.equals("textures")) {
                gameProfile.setSkin(new Skin(
                        value, signature
                ));
            }
        }

        data.release();

        connection.fireLoginSuccess(
                Server.VERSION, gameProfile
        );
    }
}
