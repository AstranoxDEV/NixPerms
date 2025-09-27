package de.syntaxjason.nixperms.api.perms;

public interface IPermissionCheck {
    boolean check(IPermission permission);
    boolean checkAny(IPermission... permissions);
    boolean checkAll(IPermission... permissions);
}
