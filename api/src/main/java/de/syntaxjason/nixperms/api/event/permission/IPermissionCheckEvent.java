package de.syntaxjason.nixperms.api.event.permission;

import de.syntaxjason.nixperms.api.event.INixEvent;
import de.syntaxjason.nixperms.api.perms.IPermission;

import java.util.UUID;

public interface IPermissionCheckEvent extends INixEvent {
    UUID uniqueId();
    IPermission node();
    boolean allowed();
    void allow();
    void deny();
    boolean handled();
}
