package net.gemcraft.hub.server;

import org.jetbrains.annotations.NotNull;

public enum ServerType {
    HUB("Hub"),
    SURVIVAL("Survival");

    private final @NotNull String name;

    ServerType(final @NotNull String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }
}