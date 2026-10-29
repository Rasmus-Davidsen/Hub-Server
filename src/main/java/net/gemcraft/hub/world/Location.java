package net.gemcraft.hub.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Location(@Nullable World world, double x, double y, double z, float yaw, float pitch) {
    public static final @NotNull Location EMPTY = new Location(0, 0, 0);

    public Location(final @Nullable World world, final int x, final int y, final int z) {
        this(world, x, y, z, 0, 0);
    }

    public Location(final double x, final double y, final double z) {
        this(x, y, z, 0, 0);
    }

    public Location(final double x, final double y, final double z, float yaw, float pitch) {
        this(null, x, y, z, yaw, pitch);
    }

    public @Nullable World getWorld() {
        return world;
    }

    public int getBlockX() {
        return (int) Math.floor(x);
    }

    public int getBlockY() {
        return (int) Math.floor(y);
    }

    public int getBlockZ() {
        return (int) Math.floor(z);
    }

    public @NotNull BlockVec3 getBlockVector() {
        return new BlockVec3(
                getBlockX(), getBlockY(), getBlockZ()
        );
    }
}
