package de.syntaxjason.nixperms.api.event;

import java.util.function.Predicate;

public interface INixEventBus {

    <T extends INixEvent> AutoCloseable register(Class<T> type, NixPriority prio, INixPermsListener<T> listener,  Predicate<T> filter);
    <T extends INixEvent> AutoCloseable register(Class<T> type, INixPermsListener<T> listener);
    EventResult post(INixEvent event);
}
