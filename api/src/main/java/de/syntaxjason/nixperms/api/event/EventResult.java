package de.syntaxjason.nixperms.api.event;


import java.util.Objects;
import java.util.Optional;

public final class EventResult {
    private final boolean cancelled;
    private final String cancelReason;
    private final Throwable error;
    private final int listenersInvoked;
    private final boolean handled;

    private EventResult(boolean cancelled,
                        String cancelReason,
                        Throwable error,
                        int listenersInvoked,
                        boolean handled) {
        this.cancelled = cancelled;
        this.cancelReason = cancelReason;
        this.error = error;
        this.listenersInvoked = Math.max(0, listenersInvoked);
        this.handled = handled || this.listenersInvoked > 0;
    }

    public static EventResult success(int listenersInvoked) {
        return new EventResult(false, null, null, listenersInvoked, true);
    }

    public static EventResult cancelled(String reason, int listenersInvoked) {
        return new EventResult(true, reason, null, listenersInvoked, true);
    }

    public static EventResult failure(Throwable error, int listenersInvoked) {
        return new EventResult(false, Objects.requireNonNullElse(null, null), Objects.requireNonNull(error), listenersInvoked, true);
    }

    public static EventResult empty() {
        return new EventResult(false, null, null, 0, false);
    }

    public boolean isSuccess() {
        return !cancelled && error == null;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Optional<String> getCancelReason() {
        return Optional.ofNullable(cancelReason);
    }

    public Optional<Throwable> getError() {
        return Optional.ofNullable(error);
    }

    public int getListenersInvoked() {
        return listenersInvoked;
    }

    public boolean isHandled() {
        return handled;
    }

    public EventResult withAdditionalError(Throwable t) {
        if (t == null) return this;
        if (this.error == null) {
            return new EventResult(this.cancelled, this.cancelReason, t, this.listenersInvoked, this.handled);
        }
        this.error.addSuppressed(t);
        return this;
    }

    public static EventResult combine(EventResult a, EventResult b) {
        if (a == null) return b == null ? EventResult.empty() : b;
        if (b == null) return a;
        boolean cancelled = a.cancelled || b.cancelled;
        String reason = a.cancelReason != null ? a.cancelReason : b.cancelReason;
        Throwable err = a.error != null ? a.error : b.error;
        EventResult combined = new EventResult(cancelled, reason, err, a.listenersInvoked + b.listenersInvoked, a.handled || b.handled);
        if (a.error != null && b.error != null && a.error != b.error) {
            combined.error.addSuppressed(a == combined ? b.error : a.error);
        }
        return combined;
    }

    @Override
    public String toString() {
        return "EventResult{" +
                "cancelled=" + cancelled +
                ", cancelReason='" + cancelReason + '\'' +
                ", error=" + (error == null ? "null" : (error.getClass().getSimpleName() + ": " + error.getMessage())) +
                ", listenersInvoked=" + listenersInvoked +
                ", handled=" + handled +
                '}';
    }
}
