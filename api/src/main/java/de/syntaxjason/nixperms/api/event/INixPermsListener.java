package de.syntaxjason.nixperms.api.event;

@FunctionalInterface
public interface INixPermsListener<T extends INixEvent> {

    void handle(T event) throws  Exception;

}
