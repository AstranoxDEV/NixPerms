package de.syntaxjason.nixperms.event;

import de.syntaxjason.nixperms.api.event.EventResult;
import de.syntaxjason.nixperms.api.event.ICancellable;
import de.syntaxjason.nixperms.api.event.INixEvent;
import de.syntaxjason.nixperms.api.event.INixEventBus;
import de.syntaxjason.nixperms.api.event.INixPermsListener;
import de.syntaxjason.nixperms.api.event.NixPriority;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public final class SimpleEventBus implements INixEventBus {

    private static final class ListenerRegistration<T extends INixEvent> implements AutoCloseable {
        private static final AtomicInteger GLOBAL_SEQUENCE = new AtomicInteger(0);

        private final int insertionOrder;
        private final Class<T> eventClass;
        private final NixPriority priority;
        private final INixPermsListener<T> listener;
        private final Predicate<T> filter;
        private final CopyOnWriteArrayList<ListenerRegistration<?>> ownerList;

        ListenerRegistration(Class<T> eventClass, NixPriority priority, INixPermsListener<T> listener, Predicate<T> filter, CopyOnWriteArrayList<ListenerRegistration<?>> ownerList) {
            this.insertionOrder = GLOBAL_SEQUENCE.getAndIncrement();
            this.eventClass = eventClass;
            this.priority = priority;
            this.listener = listener;
            this.filter = filter != null ? filter : e -> true;
            this.ownerList = ownerList;
        }

        @Override
        public void close() {
            ownerList.remove(this);
        }
    }

    private final ConcurrentMap<Class<?>, CopyOnWriteArrayList<ListenerRegistration<?>>> listeners = new ConcurrentHashMap<>();

    @Override
    public <T extends INixEvent> AutoCloseable register(Class<T> eventClass, NixPriority priority, INixPermsListener<T> listener, Predicate<T> filter) {
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(priority, "priority");
        Objects.requireNonNull(listener, "listener");

        CopyOnWriteArrayList<ListenerRegistration<?>> registrations =
                listeners.computeIfAbsent(eventClass, key -> new CopyOnWriteArrayList<>());

        ListenerRegistration<T> registration =
                new ListenerRegistration<>(eventClass, priority, listener, filter, registrations);

        registrations.add(registration);

        registrations.sort(Comparator
                .comparing((ListenerRegistration<?> r) -> r.priority)
                .thenComparingInt(r -> r.insertionOrder));

        return registration;
    }

    @Override
    public <T extends INixEvent> AutoCloseable register(Class<T> eventClass, INixPermsListener<T> listener) {
        return register(eventClass, NixPriority.NORMAL, listener, null);
    }

    @Override
    public EventResult post(INixEvent event) {
        if (event == null) {
            return EventResult.failure(new NullPointerException("event == null"), 0);
        }

        List<ListenerRegistration<?>> registrations = listeners.get(event.getClass());
        if (registrations == null || registrations.isEmpty()) {
            return EventResult.empty();
        }

        int invokedCount = 0;
        Throwable firstError = null;
        boolean cancelled = false;
        String cancelReason = null;

        for (ListenerRegistration<?> rawRegistration : registrations) {
            @SuppressWarnings("unchecked")
            ListenerRegistration<INixEvent> registration = (ListenerRegistration<INixEvent>) rawRegistration;

            boolean accepted = registration.filter.test(event);
            if (!accepted) {
                continue;
            }

            try {
                registration.listener.handle(event);
                invokedCount++;

                if (event instanceof ICancellable cancellable && cancellable.isCancelled()) {
                    cancelled = true;
                }
            } catch (Throwable error) {
                if (firstError == null) {
                    firstError = error;
                }
                if (firstError != null && firstError != error) {
                    firstError.addSuppressed(error);
                }
            }
        }

        if (firstError != null) {
            return EventResult.failure(firstError, invokedCount);
        }

        if (cancelled) {
            return EventResult.cancelled(cancelReason, invokedCount);
        }

        return EventResult.success(invokedCount);
    }
}
