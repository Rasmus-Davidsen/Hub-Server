package net.gemcraft.hub.world;

import net.gemcraft.hub.server.config.Config;
import net.gemcraft.hub.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class World {
    private static final int DEFAULT_SEA_LEVEL = 63;
    private static final boolean DEFAULT_IS_DEBUG = false;
    private static final boolean DEFAULT_IS_FLAT = false;

    private final @NotNull NamespacedKey environment;
    private final int seaLevel;
    private final long hashedSeed;
    private final boolean isHardcore;
    private final boolean isDebug;
    private final boolean isFlat;
    private long time = 0;
    private @NotNull Location worldSpawn = Location.EMPTY;
    private boolean isLimitedCrafting = false;
    private boolean reduceDebugInfo = false;
    private boolean immediateRespawn = false;
    private boolean advanceTime = false;
    private float tickRate = 20;
    private final @NotNull Schematic schematic;
    private final @NotNull LightEngine lightEngine;
    private final Map<@NotNull Integer, @NotNull Entity> entities = new HashMap<>();

    public World(final @NotNull Schematic schematic, final @NotNull NamespacedKey environment) {
        final int hashedSeed = ThreadLocalRandom.current().nextInt();

        this(schematic, environment, DEFAULT_SEA_LEVEL, hashedSeed, Config.HARDCORE, DEFAULT_IS_DEBUG, DEFAULT_IS_FLAT);
    }

    public World(final @NotNull Schematic schematic, final @NotNull NamespacedKey environment, final int seaLevel,
                 final int hashedSeed, final boolean isHardcore, final boolean isDebug, final boolean isFlat)
    {
        this.schematic = schematic;
        this.lightEngine = new LightEngine(schematic);
        this.environment = environment;
        this.seaLevel = seaLevel;
        this.hashedSeed = hashedSeed;
        this.isHardcore = isHardcore;
        this.isDebug = isDebug;
        this.isFlat = isFlat;
    }

    public <T extends Entity> @NotNull T spawnEntity(final @NotNull Location location, final @NotNull Class<T> entityClass) throws IllegalStateException {
        final T entity;

        try {
            final Constructor<T> constructor = entityClass.getConstructor(Location.class);
            entity = constructor.newInstance(location);
        } catch(final Exception ex) {
            throw new IllegalStateException(
                    "Unable to spawn entity: " + ex
            );
        }

        entities.put(entity.getEntityID(), entity);
        return entity;
    }

    public @NotNull NamespacedKey getEnvironment() {
        return environment;
    }

    public int getSeaLevel() {
        return seaLevel;
    }

    public long getHashedSeed() {
        return hashedSeed;
    }

    public boolean isHardcore() {
        return isHardcore;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public boolean isFlat() {
        return isFlat;
    }

    public void setTime(final int ticks) {
        this.time = ticks;
    }

    public long getTime() {
        return time;
    }

    public void setWorldSpawn(final @NotNull Location location) {
        this.worldSpawn = location;
    }

    public @NotNull Location getWorldSpawn() {
        return worldSpawn;
    }

    public void setLimitedCrafting(final boolean isLimitedCrafting) {
        this.isLimitedCrafting = isLimitedCrafting;
    }

    public boolean isLimitedCrafting() {
        return isLimitedCrafting;
    }

    public void setReduceDebugInfo(final boolean reduceDebugInfo) {
        this.reduceDebugInfo = reduceDebugInfo;
    }

    public boolean isReduceDebugInfo() {
        return reduceDebugInfo;
    }

    public void setImmediateRespawn(final boolean immediateRespawn) {
        this.immediateRespawn = immediateRespawn;
    }

    public boolean isImmediateRespawn() {
        return immediateRespawn;
    }

    public void setAdvanceTime(final boolean advanceTime) {
        this.advanceTime = advanceTime;
    }

    public boolean advanceTime() {
        return advanceTime;
    }

    public void setTickRate(final float tickRate) {
        this.tickRate = tickRate;
    }

    public float getTickRate() {
        return tickRate;
    }

    public @NotNull Schematic getSchematic() {
        return schematic;
    }

    public @NotNull LightEngine getLightEngine() {
        return lightEngine;
    }

    public Map<@NotNull Integer, @NotNull Entity> getEntities() {
        return entities;
    }
}
