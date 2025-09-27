package de.syntaxjason.nixperms.api.event.group;

import de.syntaxjason.nixperms.api.event.ICancellable;
import de.syntaxjason.nixperms.api.event.INixEvent;
import de.syntaxjason.nixperms.api.group.IPermissionGroup;
import de.syntaxjason.nixperms.api.perms.IPermission;
import de.syntaxjason.nixperms.api.perms.PermissionResult;

public interface IGroupPermissionChangedEvent extends INixEvent, ICancellable {
    IPermissionGroup group();
    IPermission permission();
    PermissionResult permissionResult();

    void setResult(PermissionResult result);
    void setPermission(IPermission permission);
}
