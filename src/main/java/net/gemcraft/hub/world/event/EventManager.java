package net.gemcraft.hub.world.event;

import net.gemcraft.hub.Hub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class EventManager {
    private static final @NotNull String HANDLERS_FIELD = "HANDLERS";

    public static <E extends Event<E>> void register(final @NotNull EventListener<E> handler) {
        // Event class
        final Class<E> event = getEventClass(
                handler
        );

        if(event == null) {
            return;
        }

        // Handlers field
        final Field handlersField = getHandlersField(
                handler, event
        );

        if(handlersField == null) {
            return;
        }

        // Handlers object
        final Object handlersObj = getHandlersObject(
                handler, event, handlersField
        );

        if(handlersObj == null) {
            return;
        }

        // Handlers set
        final Set<?> handlersSet = getHandlersSet(
                handler, event, handlersObj
        );

        if(handlersSet == null) {
            return;
        }

        // Handlers
        final Set<EventListener<E>> handlers = getHandlers(
                handler, event, handlersSet
        );

        if(handlers == null) {
            return;
        }

        handlers.add(
                handler
        );
    }

    @SuppressWarnings("unchecked")
    private static <E extends Event<E>> @Nullable Class<E> getEventClass(final @NotNull EventListener<E> handler) {
        final Type[] interfaces = handler
                .getClass()
                .getGenericInterfaces();

        for(final Type iface: interfaces) {
            if(!(iface instanceof final ParameterizedType parameterized)) {
                continue;
            }

            if(!(parameterized.getRawType() instanceof Class<?> raw) || !EventListener.class.isAssignableFrom(raw)) {
                continue;
            }

            final Type typeArg = parameterized.getActualTypeArguments()[0];

            if(!(typeArg instanceof Class<?> clazz) || !Event.class.isAssignableFrom(clazz)) {
                continue;
            }

            return (Class<E>) clazz;
        }

        Hub.getLogger().warn(String.format(
                "Couldn't register %s as a handler because the event parameter couldn't be inferred",
                handler.getClass()
        ));

        return null;
    }

    private static <E extends Event<E>> @Nullable Field getHandlersField(final @NotNull EventListener<E> handler, final @NotNull Class<E> event) {
        try {
            return event.getDeclaredField(
                    HANDLERS_FIELD
            );
        } catch(final NoSuchFieldException | SecurityException ex) {
            Hub.getLogger().warn(String.format(
                    "Couldn't register %s as an event handler for %s because it doesn't have the required '%s' field",
                    handler.getClass(), event, HANDLERS_FIELD
            ));

            return null;
        }
    }

    private static <E extends Event<E>> @Nullable Object getHandlersObject(final @NotNull EventListener<E> handler,
                                                      final @NotNull Class<E> event, final @NotNull Field handlersField) {
        handlersField.setAccessible(true);

        try {
            return handlersField.get(null);
        } catch(final IllegalAccessException ex) {
            Hub.getLogger().warn(String.format(
                    "Couldn't register %s as an event handler for %s because the 'HANDLERS' field isn't static",
                    handler.getClass(), event
            ));

            return null;
        }
    }

    private static <E extends Event<E>> @Nullable Set<?> getHandlersSet(final @NotNull EventListener<E> handler,
                                                   final @NotNull Class<E> event, final @NotNull Object handlersObj) {
        if(handlersObj instanceof final Set<?> set) {
            return set;
        }

        Hub.getLogger().warn(String.format(
                "Couldn't register %s as an event handler for %s because it isn't a java.utils.Set",
                handler.getClass(), event
        ));

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Event<E>> @Nullable Set<@NotNull EventListener<E>> getHandlers(final @NotNull EventListener<E> handler,
                                                                                             final @NotNull Class<E> event, final @NotNull Set<?> handlersSet) {
        for(final Object obj : handlersSet) {
            if(obj instanceof EventListener<? extends Event>) {
                continue;
            }

            Hub.getLogger().warn(String.format(
                    "Couldn't register %s as an event handler for %s because the handler doesn't have 'EventHandler<? extends Event>' as it's parameter",
                    handler.getClass(), event
            ));

            return null;
        }

        return (Set<EventListener<E>>) handlersSet;
    }

    public static <E extends Event<E>> @NotNull CompletableFuture<@NotNull Void> call(final @NotNull E e) {
        final Runnable callEvent = () -> {
            for(final EventListener<E> handler : e.getHandlers()) {
                handler.onEvent(e);
            }
        };

        return CompletableFuture.runAsync(
                callEvent
        );
    }
}
