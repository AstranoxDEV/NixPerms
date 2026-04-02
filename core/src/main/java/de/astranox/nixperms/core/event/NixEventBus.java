package de.astranox.nixperms.core.event;

import de.astranox.nixperms.api.event.IEventBus;
import de.astranox.nixperms.api.event.INixEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class NixEventBus implements IEventBus {

    private final Object2ObjectOpenHashMap<Class<?>, List<Consumer<INixEvent>>> listeners = new Object2ObjectOpenHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INixEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add((Consumer<INixEvent>) listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INixEvent> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<INixEvent>> list = listeners.get(eventType);
        if (list != null) list.remove(listener);
    }

    @Override
    public void post(INixEvent event) {
        List<Consumer<INixEvent>> direct = listeners.get(event.getClass());
        if (direct != null) direct.forEach(l -> l.accept(event));
        listeners.forEach((type, list) -> {
            if (type == event.getClass()) return;
            if (type.isInstance(event)) list.forEach(l -> l.accept(event));
        });
    }
}
