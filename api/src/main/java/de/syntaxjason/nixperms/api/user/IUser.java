package de.syntaxjason.nixperms.api.user;

import de.syntaxjason.nixperms.api.group.IPermissionGroup;
import de.syntaxjason.nixperms.api.perms.IPermissionCheck;
import de.syntaxjason.nixperms.api.util.IMetaHolder;

import java.util.UUID;

public interface IUser extends IMetaHolder {
    UUID uniqueId();
    String name();

    IPermissionCheck permissions();

    IPermissionGroup primaryGroup();
    void primaryGroup(IPermissionGroup group);

    IPermissionGroup secondaryGroup();
    void secondaryGroup(IPermissionGroup group);
    void clearSecondaryGroup();

    String prefix();
    String suffix();

    default boolean hasSecondary() { return secondaryGroup() != null; }
}
