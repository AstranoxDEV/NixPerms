package de.syntaxjason.nixperms.api.event.group;

import de.syntaxjason.nixperms.api.event.ICancellable;
import de.syntaxjason.nixperms.api.event.INixEvent;
import de.syntaxjason.nixperms.api.group.IPermissionGroup;

public interface IGroupRenameEvent extends INixEvent, ICancellable {
    IPermissionGroup group();
    String oldName();
    String newName();
    void setNewName(String name);
}
