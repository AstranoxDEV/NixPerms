package de.syntaxjason.nixperms.api;

import de.syntaxjason.nixperms.api.event.INixEventBus;
import de.syntaxjason.nixperms.api.group.IPermissionGroup;
import de.syntaxjason.nixperms.api.perms.IPermissionManager;
import de.syntaxjason.nixperms.api.util.Ref;

import java.util.Set;

public interface INixPerms {

    Ref<INixPerms> ref = Ref.of();

    static INixPerms put(INixPerms nixPerms) {
        ref.set(nixPerms);
        return nixPerms;
    }

    static INixPerms get() {
        if(ref.isEmpty()) {
            throw new NullPointerException("INixPerms is not Initialized");
        }
        return ref.get();
    }

    INixEventBus eventBus();
    IPermissionManager permissionManager();
    Set<IPermissionGroup> allGroups();

}
