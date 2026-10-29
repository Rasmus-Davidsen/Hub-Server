package net.gemcraft.hub.protocol.packet.handler;

import net.gemcraft.hub.protocol.State;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.clientbound.play.ClientboundSetEntityDataPacket;
import net.gemcraft.hub.protocol.packet.serverbound.common.ServerboundClientInformationPacket;
import net.gemcraft.hub.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ClientInformationHandler {
    public static void handle(final @NotNull ClientConnection connection, final @NotNull ServerboundClientInformationPacket packet) {
        final Player player = connection.getPlayer();

        player.setSkinParts(Player.SkinPart.fromMask(
                packet.skinMask()
        ));

        player.setMainHand(
                packet.mainHand()
        );

        final State state = connection.getEncoder().getState();

        if(state == State.PLAY) {
            connection.sendPacket(
                    new ClientboundSetEntityDataPacket(player)
            );
        }
    }
}
