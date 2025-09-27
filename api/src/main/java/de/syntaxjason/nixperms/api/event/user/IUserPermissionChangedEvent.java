package de.syntaxjason.nixperms.api.event.user;

import de.syntaxjason.nixperms.api.event.ICancellable;
import de.syntaxjason.nixperms.api.event.INixEvent;
import de.syntaxjason.nixperms.api.perms.IPermission;
import de.syntaxjason.nixperms.api.perms.PermissionResult;
import de.syntaxjason.nixperms.api.user.IUser;

public interface IUserPermissionChangedEvent extends INixEvent, ICancellable {

    IUser user();
    IPermission permission();
    PermissionResult permissionResult();
    void setResult(PermissionResult result);
    void setPermission(IPermission permission);


}
