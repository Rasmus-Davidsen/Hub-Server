package net.gemcraft.hub.world;

public record Vec3(double x, double y, double z) {
    public static final Vec3 EMPTY;

    static {
        EMPTY = new Vec3(0.0d, 0.0d, 0.0d);
    }
}
