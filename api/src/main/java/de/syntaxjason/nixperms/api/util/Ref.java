package de.syntaxjason.nixperms.api.util;

import java.util.Optional;

public final class Ref<E> {

    private E value;
    private boolean locked = false;

    private Ref(final E value) {
        this.value = value;
    }

    public boolean isLocked() {
        return locked;
    }

    public E get() {
        return value;
    }

    public Ref<E> set(final E value) {
        if (!locked) {
            this.value = value;
        }
        return this;
    }

    public Ref<E> lock() {
        this.locked = true;
        return this;
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public Optional<E> asOptional() {
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    public static <E> Ref<E> of() {
        return new Ref<>(null);
    }

    public static <E> Ref<E> of(final E value) {
        return new Ref<>(value);
    }

}
