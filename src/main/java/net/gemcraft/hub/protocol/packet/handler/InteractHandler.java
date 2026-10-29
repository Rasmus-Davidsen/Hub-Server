package net.gemcraft.hub.protocol.packet.handler;

import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.serverbound.play.ServerboundInteractPacket;
import net.gemcraft.hub.server.Server;
import net.gemcraft.hub.server.ServerType;
import net.gemcraft.hub.server.Velocity;
import net.gemcraft.hub.world.World;
import net.gemcraft.hub.world.entity.Entity;
import net.gemcraft.hub.world.entity.Mannequin;
import net.gemcraft.hub.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class InteractHandler {
    public static void handle(final @NotNull ClientConnection connection, final @NotNull ServerboundInteractPacket packet) {
        final Player player = connection.getPlayer();

        if(packet.hand() != player.getMainHand()) {
            return;
        }

        final World world = connection.getServer().getWorld();

        final Entity entity = world.getEntities().get(
                packet.entityId()
        );

        if(entity == null) {
            return;
        }

        if(entity instanceof final Mannequin mannequin) {
            handleMannequin(player, mannequin);
        }
    }

    private static void handleMannequin(final @NotNull Player player, final @NotNull Mannequin mannequin) {
        if(mannequin.getTags().contains(Server.SURVIVAL_TAG)) {
            Velocity.connect(player, ServerType.SURVIVAL);
        }
    }
}
