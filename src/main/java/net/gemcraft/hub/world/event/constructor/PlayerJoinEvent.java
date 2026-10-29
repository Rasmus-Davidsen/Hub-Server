package net.gemcraft.hub.world.event.constructor;

import net.gemcraft.hub.world.entity.player.Player;
import net.gemcraft.hub.world.event.Event;
import net.gemcraft.hub.world.event.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public record PlayerJoinEvent(@NotNull Player player) implements Event<PlayerJoinEvent> {
    private static final @NotNull Set<@NotNull EventListener<PlayerJoinEvent>> HANDLERS = new HashSet<>();

    @Override
    public @NotNull Set<@NotNull EventListener<PlayerJoinEvent>> getHandlers() {
        return PlayerJoinEvent.HANDLERS;
    }
}
