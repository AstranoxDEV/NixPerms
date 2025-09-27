package de.syntaxjason.nixperms.group;

import de.syntaxjason.nixperms.api.group.IPermissionGroup;
import de.syntaxjason.nixperms.api.perms.IPermission;
import de.syntaxjason.nixperms.api.perms.IPermissionIteration;
import de.syntaxjason.nixperms.api.user.IUserList;
import de.syntaxjason.nixperms.api.util.IPriority;
import de.syntaxjason.nixperms.perms.PermissionIteration;
import de.syntaxjason.nixperms.perms.SimplePermission;

import java.util.*;

public final class SimplePermissionGroup implements IPermissionGroup {

    private final UUID uniqueId;
    private String name;
    private String prefix;
    private String suffix;
    private final IPriority priority;
    private final IPermissionIteration permissions;
    private final IUserList assignedUsers;
    private final Map<String, String> meta = new HashMap<>();

    public SimplePermissionGroup(String name, IPriority priority, IPermissionIteration permissions, IUserList assignedUsers) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.priority = priority;
        this.permissions = (permissions != null) ? permissions : new PermissionIteration();
        this.assignedUsers = assignedUsers;
    }

    public SimplePermissionGroup(String name, IPermissionIteration permissions) {
        this(name, null, permissions, null);
    }

    public SimplePermissionGroup(String name, IPermission... permissions) {
        this(name, null, new PermissionIteration(List.of(permissions)), null);
    }

    public SimplePermissionGroup(String name, IPermission permission) {
        this(name, null, new PermissionIteration(List.of(permission)), null);
    }

    public SimplePermissionGroup(String name) {
        this(name, null, new PermissionIteration(List.of()), null);
    }

    @Override
    public UUID uniqueId() {
        return uniqueId;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public IPermissionIteration permissions() {
        return permissions;
    }

    @Override
    public String prefix() {
        return prefix;
    }

    @Override
    public String suffix() {
        return suffix;
    }

    @Override
    public IPriority priority() {
        return priority;
    }

    @Override
    public IUserList assignedUsers() {
        return assignedUsers;
    }

    @Override
    public void name(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        this.name = name;
    }

    @Override
    public void priority(int priority) {
        if (this.priority == null) {
            return;
        }
        this.priority.priority(priority);
    }

    @Override
    public void prefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void suffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public boolean addPermission(String permission) {
        return permissions.add(new SimplePermission(permission));
    }

    @Override
    public boolean removePermission(String permission) {
        return permissions.remove(new SimplePermission(permission));
    }

    public boolean addPermission(IPermission permission) {
        if (permissions instanceof PermissionIteration concrete) {
            return concrete.add(permission);
        }
        if (permission == null) {
            return false;
        }
        return permissions.add(permission);
    }

    public boolean removePermission(IPermission permission) {
        if (permissions instanceof PermissionIteration concrete) {
            return concrete.remove(permission);
        }
        if (permission == null) {
            return false;
        }
        return permissions.remove(permission);
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
    public Optional<String> meta(String key) {
        if (key == null || key.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(meta.get(key));
    }

    @Override
    public Map<String, String> allMeta() {
        return Collections.unmodifiableMap(meta);
    }

    @Override
    public boolean create() {
        return true;
    }

    @Override
    public boolean delete() {
        return true;
    }
}
