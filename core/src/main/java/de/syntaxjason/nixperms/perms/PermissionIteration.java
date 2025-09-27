package de.syntaxjason.nixperms.perms;

import de.syntaxjason.nixperms.api.perms.IPermission;
import de.syntaxjason.nixperms.api.perms.IPermissionIteration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionIteration implements IPermissionIteration {

    private Set<IPermission> permissions;

    public PermissionIteration(List<IPermission> permissions) {
        this.permissions = new HashSet<>(permissions);
    }

    public PermissionIteration(IPermission permission) {
        this(List.of(permission));
    }

    public PermissionIteration() {
        this(List.of());
    }

    @Override
    public boolean has(IPermission permission) {
        return permissions.contains(permission);
    }

    @Override
    public Set<IPermission> all() {
        return permissions;
    }

    @Override
    public boolean add(IPermission permission) {
        return permissions.add(permission);
    }

    @Override
    public boolean remove(IPermission permission) {
        return permissions.remove(permission);
    }
}
