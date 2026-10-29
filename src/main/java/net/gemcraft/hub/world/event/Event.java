package net.gemcraft.hub.world.event;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Event<E extends Event<E>> {
    @NotNull Set<@NotNull EventListener<E>> getHandlers();
}
