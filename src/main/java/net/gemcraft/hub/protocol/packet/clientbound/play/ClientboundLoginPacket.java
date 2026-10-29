package net.gemcraft.hub.protocol.packet.clientbound.play;

import net.gemcraft.hub.protocol.ByteMessage;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.server.config.Config;
import net.gemcraft.hub.world.Location;
import net.gemcraft.hub.world.NamespacedKey;
import net.gemcraft.hub.world.World;
import net.gemcraft.hub.world.entity.player.Player;
import net.gemcraft.hub.world.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record ClientboundLoginPacket(@NotNull Player player, @NotNull Registry environmentRegistry, @NotNull World world, boolean isSecureProfile,
                                     int maxPlayers, @NotNull NamespacedKey @NotNull[] environments) implements ClientboundPacket
{
    @Override
    public void encode(final @NotNull ByteMessage msg) {
        msg.writeInt(player.getEntityID());                  // Entity ID
        msg.writeBoolean(world.isHardcore());                // Hardcore mode
        encodeDimensions(msg);                               // Dimensions
        msg.writeVarInt(maxPlayers);                         // Max players
        msg.writeVarInt(player.getViewDistance());           // Render distance
        msg.writeVarInt(player.getSimulationDistance());     // Simulation distance
        msg.writeBoolean(world.isReduceDebugInfo());         // Reduced debug info game rule
        msg.writeBoolean(!world.isImmediateRespawn());       // Non-immediate respawn game rule
        msg.writeBoolean(world.isLimitedCrafting());         // Limited crafting game rule
        encodeDimensionType(msg);                            // Dimension type
        msg.writeString(world.getEnvironment().toString());  // Dimension name
        msg.writeLong(world.getHashedSeed());                // Hashed world seed
        msg.writeByte(player.getGameMode().ordinal());       // Gamemode
        encodePreviousGamemode(msg);                         // Previous gamemode
        msg.writeBoolean(world.isDebug());                   // Debug mode
        msg.writeBoolean(world.isFlat());                    // Flat world
        encodeDeathLocation(msg);                            // Death location
        msg.writeVarInt(player.getPortalCooldown());         // Portal cooldown
        msg.writeVarInt(world.getSeaLevel());                // Sea level
        msg.writeBoolean(!Config.VELOCITY);                  // Online mode
        msg.writeBoolean(isSecureProfile);                   // Enforce secure chat
    }

    private void encodeDeathLocation(final @NotNull ByteMessage msg) {
        final Location deathLoc = player.getDeathLocation();

        if(deathLoc == null) {
            msg.writeBoolean(false);
            return;
        }

        msg.writeBoolean(true);

        // Dimension
        final World world = deathLoc.getWorld();

        if(world != null) {
            final String dimension = world
                    .getEnvironment()
                    .toString();

            msg.writeString(dimension);
        }

        // Position
        msg.writeBlockPos(deathLoc);
    }

    private void encodeDimensions(final @NotNull ByteMessage msg) {
        final String[] dimensions = Arrays.stream(environments)
                .map(NamespacedKey::toString)
                .toArray(String[]::new);

        msg.writeStringsArray(
                dimensions
        );
    }

    private void encodeDimensionType(final @NotNull ByteMessage msg) {
        final Integer dimensionID = environmentRegistry.getIndex(
                world.getEnvironment()
        );

        if(dimensionID != null) {
            msg.writeVarInt(
                    dimensionID
            );
        }
    }

    private void encodePreviousGamemode(final @NotNull ByteMessage msg) {
        final Player.GameMode gameMode = player.getPreviousGameMode();

        final int previousGamemode = gameMode == null
                ? 0 : gameMode.ordinal() + 1;

        msg.writeByte(
                previousGamemode
        );
    }
}
