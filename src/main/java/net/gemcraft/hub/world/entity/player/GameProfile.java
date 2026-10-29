package net.gemcraft.hub.world.entity.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GameProfile {
    private @NotNull UUID uuid;
    private @NotNull String name;
    private @Nullable Skin skin;

    public GameProfile(final @NotNull UUID uuid, final @NotNull String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public void setUUID(final @NotNull UUID uuid) {
        this.uuid = uuid;
    }

    public @NotNull UUID getUUID() {
        return uuid;
    }

    public void setName(final @NotNull String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }

    public void setSkin(final @NotNull Skin skin) {
        this.skin = skin;
    }

    public @Nullable Skin getSkin() {
        return skin;
    }
}
