package net.gemcraft.hub.world.event;

import org.jetbrains.annotations.NotNull;

public interface EventListener<E extends Event<E>> {
    void onEvent(final @NotNull E e);
}
