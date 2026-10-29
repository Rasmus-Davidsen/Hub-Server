package net.gemcraft.hub.protocol.packet.handler;

import net.gemcraft.hub.Hub;
import net.gemcraft.hub.protocol.State;
import net.gemcraft.hub.protocol.client.ClientConnection;
import net.gemcraft.hub.protocol.packet.clientbound.ClientboundPacket;
import net.gemcraft.hub.protocol.packet.clientbound.play.*;
import net.gemcraft.hub.server.Server;
import net.gemcraft.hub.server.config.Config;
import net.gemcraft.hub.world.Environment;
import net.gemcraft.hub.world.NamespacedKey;
import net.gemcraft.hub.world.World;
import net.gemcraft.hub.world.entity.Entity;
import net.gemcraft.hub.world.entity.player.Player;
import net.gemcraft.hub.world.event.EventManager;
import net.gemcraft.hub.world.event.constructor.PlayerJoinEvent;
import net.gemcraft.hub.world.registry.Registries;
import net.gemcraft.hub.world.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FinishConfigurationHandler {
    public static void handle(final @NotNull ClientConnection connection) {
        connection.updateState(State.PLAY);

        // Login
        final Registry dimensionRegistry = Registries.get(
                Registry.Type.DIMENSION_TYPE
        );

        if(dimensionRegistry == null) {
            Hub.getLogger().warn(
                    "Couldn't find dimension type registry"
            );

            return;
        }

        final Player player = connection.getPlayer();
        final Server server = connection.getServer();
        final World world = server.getWorld();

        final NamespacedKey[] environments = Arrays.stream(Environment.values())
                .map(Environment::getKey)
                .toArray(NamespacedKey[]::new);

        final List<ClientboundPacket> packets = new ArrayList<>();

        packets.add(new ClientboundLoginPacket(
                player, dimensionRegistry, world, Server.ENFORCE_SECURE_CHAT,
                Config.MAX_PLAYERS, environments
        ));

        // Player abilities
        final ClientboundPlayerAbilitiesPacket.Flag[] flags = getPlayerAbilityFlags(player);

        packets.add(new ClientboundPlayerAbilitiesPacket(
                player.getFlyingSpeed(), player.getFovModifier(), flags
        ));

        // Player position
        packets.add(new ClientboundPlayerPositionPacket(
                player.getLocation()
        ));

        // Player info update
        packets.add(ClientboundPlayerInfoUpdatePacket.createInitializing(
                Set.of(player)
        ));

        // Entity data
        packets.add(new ClientboundSetEntityDataPacket(player));

        // Start waiting for chunks
        packets.add(new ClientboundGameEventPacket(
                (byte) 13, 0
        ));

        // Load chunk data
        packets.addAll(server.getChunkDataPackets());

        // Load entities
        for(final Entity entity : world.getEntities().values()) {
            packets.add(new ClientboundAddEntityPacket(entity));
            packets.add(new ClientboundSetEntityDataPacket(entity));
        }

        // Time of day (Versions below 1.21.11 ignore visual attributes)
        final float tickRate = world.advanceTime()
                ? world.getTickRate() : 0;

        packets.add(new ClientboundSetTimePacket(
                world.getTime(), tickRate
        ));

        connection.sendPackets(packets);

        // Call join event
        final CompletableFuture<Void> completableFuture = EventManager.call(
                new PlayerJoinEvent(player)
        );

        // Keep alive
        completableFuture.thenRun(
                connection::createKeepAliveTask
        );
    }

    private static ClientboundPlayerAbilitiesPacket.@NotNull Flag @NotNull[] getPlayerAbilityFlags(final @NotNull Player player) {
        final Set<ClientboundPlayerAbilitiesPacket.Flag> flags = new HashSet<>();
        flags.add(ClientboundPlayerAbilitiesPacket.Flag.INVULNERABLE);

        if(player.getGameMode().isInvulnerable()) {
            flags.add(ClientboundPlayerAbilitiesPacket.Flag.FLYING);
            flags.add(ClientboundPlayerAbilitiesPacket.Flag.ALLOW_FLYING);
        }

        return flags.toArray(ClientboundPlayerAbilitiesPacket.Flag[]::new);
    }
}
