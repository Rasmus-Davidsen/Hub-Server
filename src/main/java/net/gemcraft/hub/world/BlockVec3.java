package net.gemcraft.hub.world;

public record BlockVec3(int x, int y, int z) {
    public static final BlockVec3 EMPTY;

    static {
        EMPTY = new BlockVec3(0, 0, 0);
    }
}
