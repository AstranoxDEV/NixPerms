package de.syntaxjason.nixperms;

import de.syntaxjason.nixperms.api.INixPerms;
import de.syntaxjason.nixperms.api.event.INixEventBus;
import de.syntaxjason.nixperms.api.group.IPermissionGroup;
import de.syntaxjason.nixperms.api.perms.IPermissionManager;

import java.util.Set;

public class NixPerms implements INixPerms {



    @Override
    public INixEventBus eventBus() {
        return null;
    }

    @Override
    public IPermissionManager permissionManager() {
        return null;
    }

    @Override
    public Set<IPermissionGroup> allGroups() {
        return Set.of();
    }
}
