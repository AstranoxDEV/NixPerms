package de.syntaxjason.nixperms.perms;

import de.syntaxjason.nixperms.api.perms.IPermission;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

public final class SimplePermission implements IPermission {

    private final String name;
    private final boolean value;
    private final OptionalLong expiresAt;
    private final Map<String, String> meta = new HashMap<>();

    public SimplePermission(String name, boolean value, OptionalLong expiresAt) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("permission name must not be empty");
        }
        this.name = name;
        this.value = value;
        this.expiresAt = (expiresAt != null) ? expiresAt : OptionalLong.empty();
    }

    public SimplePermission(String name, boolean value) {
        this(name, value, null);
    }

    public SimplePermission(String name) {
        this(name, true, null);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean value() {
        return value;
    }

    @Override
    public OptionalLong expiresAt() {
        return expiresAt;
    }

    @Override
    public boolean meta(String key, String value) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        meta.put(key, value);
        return true;
    }

    @Override
    public boolean removeMeta(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        return meta.remove(key) != null;
    }

    @Override
    public java.util.Optional<String> meta(String key) {
        if (key == null || key.isEmpty()) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.ofNullable(meta.get(key));
    }

    @Override
    public Map<String, String> allMeta() {
        return Collections.unmodifiableMap(meta);
    }
}
