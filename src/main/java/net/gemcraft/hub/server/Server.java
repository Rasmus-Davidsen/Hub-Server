package net.gemcraft.hub.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.gemcraft.hub.Hub;
import net.gemcraft.hub.protocol.Address;
import net.gemcraft.hub.protocol.client.ClientChannelInitializer;
import net.gemcraft.hub.protocol.packet.clientbound.EncodedPacket;
import net.gemcraft.hub.protocol.packet.clientbound.configuration.ClientboundRegistryDataPacket;
import net.gemcraft.hub.protocol.packet.clientbound.configuration.ClientboundUpdateTagsPacket;
import net.gemcraft.hub.protocol.packet.clientbound.play.ClientboundLevelChunkWithLightPacket;
import net.gemcraft.hub.server.config.Config;
import net.gemcraft.hub.world.*;
import net.gemcraft.hub.world.entity.Avatar;
import net.gemcraft.hub.world.entity.Mannequin;
import net.gemcraft.hub.world.entity.player.Player;
import net.gemcraft.hub.world.entity.player.Skin;
import net.gemcraft.hub.world.entity.player.Version;
import net.gemcraft.hub.world.registry.BlockRegistry;
import net.gemcraft.hub.world.registry.Registries;
import net.gemcraft.hub.world.registry.Registry;
import net.gemcraft.hub.world.registry.RegistryTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.BindException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static final @NotNull Version VERSION = Version.getLatest();
    public static final boolean ENFORCE_SECURE_CHAT = !Config.VELOCITY;
    private static final @NotNull Biome BIOME = Biome.SPACE;
    private static final int WORLD_TIME = 18_000; // Midnight
    private static final boolean DAYLIGHT_CYCLE = false;
    public static final String SURVIVAL_TAG = "join_survival";

    private final @NotNull Set<@NotNull ClientboundRegistryDataPacket> registryPackets;
    private final @NotNull ClientboundUpdateTagsPacket updateTagsPacket;
    private final @NotNull Set<@NotNull EncodedPacket> chunkDataPackets;
    private final @NotNull Map<@NotNull UUID, @NotNull Player> players;
    private final @NotNull World world;

    public Server() throws Exception {
        players = new ConcurrentHashMap<>();

        final Location worldSpawn = new Location(
                0.5, 100, 0.5, 90, 0
        );

        final Schematic schematic = loadSchematic(
                worldSpawn.getBlockVector()
        );

        world = new World(schematic, Environment.UNIVERSE.getKey());
        world.setTime(WORLD_TIME);
        world.setAdvanceTime(DAYLIGHT_CYCLE);
        world.setReduceDebugInfo(Config.REDUCE_DEBUG_INFO);
        world.setWorldSpawn(worldSpawn);

        // Registries
        registryPackets = Registries.createPackets();
        updateTagsPacket = RegistryTags.createPacket();

        // Chunk data
        final int biomeId = getBiomeId();

        chunkDataPackets = createChunkDataPackets(
                schematic, biomeId
        );

        // Entities
        spawnEntities();

        Hub.getLogger().info(String.format(
                "Running version %s (%d)",
                VERSION.getHighestName(), VERSION.getProtocol()
        ));

        createBootstrap();
        System.gc();
    }

    public @NotNull Set<@NotNull ClientboundRegistryDataPacket> getRegistryPackets() {
        return registryPackets;
    }

    public @NotNull ClientboundUpdateTagsPacket getUpdateTagsPacket() {
        return updateTagsPacket;
    }

    public @NotNull Set<@NotNull EncodedPacket> getChunkDataPackets() {
        return chunkDataPackets;
    }

    public @NotNull World getWorld() {
        return world;
    }

    private void createBootstrap() throws BindException {
        final Hub instance = Hub.getInstance();
        final Address address = Config.ADDRESS;

        final ServerBootstrap bootstrap = new ServerBootstrap()
                .group(instance.getBossGroup(), instance.getWorkerGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ClientChannelInitializer(this))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(address.host(), address.port());

        try {
            bootstrap.bind().sync();
        } catch(final Exception ex) {
            // Already bound (Most likely another instance)
            final Throwable cause = ex.getCause() != null
                    ? ex.getCause() : ex;

            if(cause instanceof BindException) {
                throw new BindException(
                        "Another process is already bound to " + address
                );
            } else {
                throw new BindException(String.format(
                        "Couldn't bind to %s: %s", address, ex
                ));
            }
        }
    }

    public void disconnectAll(final @NotNull String reason) {
        players.values().forEach(
                p -> p.disconnect(reason)
        );
    }

    private @NotNull Schematic loadSchematic(final @NotNull BlockVec3 origin) throws IOException {
        final BlockRegistry blockRegistry = BlockRegistry.load();
        final Path path = Path.of(Config.SCHEMATIC);

        return Schematic.load(
                path, origin, blockRegistry
        );
    }

    private int getBiomeId() {
        final Registry biomeRegistry = Registries.get(
                Registry.Type.BIOME
        );

        if(biomeRegistry == null) {
            throw new IllegalStateException(
                    "Unable to locate biome registry"
            );
        }

        final Integer biomeId = biomeRegistry.getIndex(
                Server.BIOME.getKey()
        );

        if(biomeId == null) {
            throw new NoSuchElementException(
                    "Biome doesn't exist: " + Server.BIOME.getKey()
            );
        }

        return biomeId;
    }

    private void spawnEntities() {
        final Location loc = new Location(
                -11.5, 100, 0.5, 270, 0
        );

        final Mannequin mannequin = world.spawnEntity(
                loc, Mannequin.class
        );

        mannequin.getTags().add(SURVIVAL_TAG);
        mannequin.setImmovable(true);
        mannequin.setCustomNameVisible(true);

        mannequin.setCustomName(Component.text(
                "Survival", NamedTextColor.AQUA, TextDecoration.BOLD
        ));

        mannequin.setDescription(Component.text(
                "Click to join", NamedTextColor.GRAY
        ));

        final Skin skin = new Skin(
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NzUxODA5MDU5NywKICAicHJvZmlsZUlkIiA6ICIyZDUxZmI0ZmJjM2Y0MjJjOTMyYzIzMDg5NGU2YmM1MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJGR0ZTX3N0dWRpbyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85MGY1ODY0ZDBiYmY4NmIzY2YwNzdmZDQ5NTRlZWU1NTdjYTlkZjE2MjczMDdkYjczMmFhOTgzODBjYjQwNThlIgogICAgfQogIH0KfQ==",
                "UcVJrFjwF/SWslAAoLhuz0qcxlA5FL+cfFqPk2WjQBEE6aza/sPFXMfnlIcaVCZcMvi60tYe95Qof00o7is8E1tVYNEFWvWING8mh1DTOnuyhBICRZaU6XXOOkf3pZ+nQyg4keTzi1hQGOcDlgq+gDTIv7bmxxOuqom2Kv3Biji6wIxNUdHpFgzO+1MOZBLX58QKBGdCqyzX12W8mBfMjOq4oKzpyUGo5q/L2zU1RWv4pvHg7qeDgLyfN2tgIGpom3l2ImTV4qenyAbkSr3g2EIMkaKAK3ene6QU61jYsYeTaDVBIDVAyFbPfM96dMmx6kLU0TFZhW8MZRBpQT9AVtTAEolZYtbad6SREGB1w/5UcgKzImSg7ltfPR1Nko8BaInxRP5zWuH69kjwBudeGaJk02XeVaR97Uugy1r8sVKh0m8ZQsblneEtvvUC373237LZlraJHHDh4vOo6uec/Ge07svPcdDTifYJabpUrD3mO2vVtiP8Pwa2Q9udAr6tUWFnzIFMaV3wsh1cfUYQa3KYNHRKMJxB1yfb6WoDZu9ZH0MMUSJuH0fu3++Dmkykq3TDblzc9KC7PRze9sU4vl4rQAJRI+Quf2hwbMVKaETFSft9+YsCdMUdhMHxevS3sgLOsgvWwSLU9/MjDxZFtDwImRJJAda0316/wUsJ7Gs="
        );

        mannequin.setProfile(
                new Mannequin.ResolvableProfile(skin)
        );

        mannequin.setSkinParts(Set.of(
                Avatar.SkinPart.JACKET, Avatar.SkinPart.LEFT_SLEEVE, Avatar.SkinPart.RIGHT_SLEEVE,
                Avatar.SkinPart.LEFT_PANTS_LEG, Avatar.SkinPart.RIGHT_PANTS_LEG, Avatar.SkinPart.HAT
        ));
    }

    private static final int PADDING_CHUNKS = 1;

    private @NotNull Set<@NotNull EncodedPacket> createChunkDataPackets(final @NotNull Schematic schematic, final int biomeId) {
        final Set<EncodedPacket> packets = new HashSet<>();

        final BlockVec3 origin = schematic.getPlacement();
        final BlockVec3 offset = schematic.getOffset();

        final int minX = origin.x() + offset.x();
        final int minZ = origin.z() + offset.z();
        final int maxX = minX + schematic.getWidth();
        final int maxZ = minZ + schematic.getLength();

        final int minChunkX = (minX >> 4) - PADDING_CHUNKS;
        final int minChunkZ = (minZ >> 4) - PADDING_CHUNKS;
        final int maxChunkX = (maxX >> 4) + PADDING_CHUNKS;
        final int maxChunkZ = (maxZ >> 4) + PADDING_CHUNKS;

        for(int x = minChunkX; x <= maxChunkX; x++) {
            for(int z = minChunkZ; z <= maxChunkZ; z++) {
                final ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(
                        x, z, world, biomeId
                );

                packets.add(EncodedPacket.of(
                        packet
                ));
            }
        }

        return packets;
    }

    public static @NotNull Server getServer() {
        final Hub instance = Hub.getInstance();

        return instance.getServer();
    }

    public static void addPlayer(final @NotNull Player player) {
        final Server server = getServer();

        server.players.put(
                player.getUUID(), player
        );
    }

    public static void removePlayer(final @NotNull Player player) {
        final Server server = getServer();
        final UUID uuid = player.getUUID();

        if(server.players.remove(uuid) == null) {
            return;
        }

        Hub.getLogger().info(String.format(
                "%s disconnected with UUID %s",
                player.getName(), uuid
        ));
    }

    public static @NotNull Collection<@NotNull Player> getPlayers() {
        return Server.getServer()
                .players
                .values();
    }
}
