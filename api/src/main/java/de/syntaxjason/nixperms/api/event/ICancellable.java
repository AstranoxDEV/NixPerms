package de.syntaxjason.nixperms.api.event;

public interface ICancellable {

    boolean isCancelled();
    void cancelled(boolean cancelled);
}
