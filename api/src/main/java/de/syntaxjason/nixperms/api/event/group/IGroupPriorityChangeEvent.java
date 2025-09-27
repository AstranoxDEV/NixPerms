package de.syntaxjason.nixperms.api.event.group;

import de.syntaxjason.nixperms.api.event.ICancellable;
import de.syntaxjason.nixperms.api.event.INixEvent;
import de.syntaxjason.nixperms.api.group.IPermissionGroup;

public interface IGroupPriorityChangeEvent extends INixEvent, ICancellable {
    IPermissionGroup group();
    int oldPriority();
    int newPriority();
    void setNewPriority(int priority);
}
