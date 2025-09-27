package de.syntaxjason.nixperms.api.event.group;

import de.syntaxjason.nixperms.api.event.ICancellable;
import de.syntaxjason.nixperms.api.event.INixEvent;
import de.syntaxjason.nixperms.api.group.IPermissionGroup;
import de.syntaxjason.nixperms.api.user.IUser;

public interface IPrimaryGroupChangeEvent extends INixEvent, ICancellable {
    IUser user();
    IPermissionGroup oldGroup();
    IPermissionGroup newGroup();
    void setNewGroup(IPermissionGroup group);
}
