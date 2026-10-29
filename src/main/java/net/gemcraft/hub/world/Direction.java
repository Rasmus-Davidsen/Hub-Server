package net.gemcraft.hub.world;

import org.jetbrains.annotations.NotNull;

public enum Direction {
    DOWN(new BlockVec3(0, -1, 0)),
    UP(new BlockVec3(0, 1, 0)),
    NORTH(new BlockVec3(0, 0, -1)),
    SOUTH(new BlockVec3(0, 0, 1)),
    WEST(new BlockVec3(-1, 0, 0)),
    EAST(new BlockVec3(1, 0, 0));

    private final @NotNull BlockVec3 normal;

    Direction(final @NotNull BlockVec3 normal) {
        this.normal = normal;
    }

    public @NotNull BlockVec3 getNormal() {
        return normal;
    }
}
