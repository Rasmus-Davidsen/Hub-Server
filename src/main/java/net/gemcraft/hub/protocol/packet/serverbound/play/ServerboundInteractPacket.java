package net.gemcraft.hub.protocol.packet.serverbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.handler.InteractHandler;
import net.gemcraft.hub.protocol.packet.serverbound.ServerboundPacket;
import net.gemcraft.hub.world.Vec3;
import net.gemcraft.hub.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ServerboundInteractPacket(int entityId, Player.@Nullable Hand hand, @NotNull Vec3 targetOffset, boolean isSneaking) implements ServerboundPacket {
    public ServerboundInteractPacket(final @NotNull ByteMessage msg) {
        this(
                msg.readVarInt(),
                msg.readEnum(Player.Hand.class, null),
                msg.readLpVec3(),
                msg.readBoolean()
        );
    }

    @Override
    public void handle(final @NotNull ClientConnection connection) {
        InteractHandler.handle(
                connection, this
        );
    }
}
