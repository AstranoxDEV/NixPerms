package de.syntaxjason.nixperms.api.perms;

import de.syntaxjason.nixperms.api.group.IPermissionGroup;
import de.syntaxjason.nixperms.api.user.IUser;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IPermissionManager {

    Optional<IUser> user(UUID uniqueId);
    Optional<IPermissionGroup> group(UUID uniqueId);
    Optional<IPermissionGroup> group(String name);

    Optional<Set<IPermissionGroup>> registeredGroups();

    IPermission permission(String permission);
    IPermission permission(String permission, boolean value);

    boolean permission(UUID uniqueId, IPermission permission);
    void grantPermission(UUID uniqueId, IPermission permission);
    void revokePermission(UUID uniqueId, IPermission permission);

    void primaryGroup(UUID uniqueId, IPermissionGroup group);
    void secondaryGroup(UUID uniqueId, IPermissionGroup group);
    void clearSecondaryGroup(UUID uniqueId);

    void switchPrimaryGroup(UUID uniqueId, IPermissionGroup group);
    void swapGroups(UUID uniqueId);
    void replacePrimaryGroup(UUID uniqueId);
}
