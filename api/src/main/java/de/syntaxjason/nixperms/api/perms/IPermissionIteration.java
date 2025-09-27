package de.syntaxjason.nixperms.api.perms;

import java.util.Set;

public interface IPermissionIteration {
    boolean has(IPermission permission);
    Set<IPermission> all();
    boolean add(IPermission permission);
    boolean remove(IPermission permission);
}
