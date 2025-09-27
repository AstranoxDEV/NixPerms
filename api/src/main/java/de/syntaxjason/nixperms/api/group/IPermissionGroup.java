package de.syntaxjason.nixperms.api.group;

import de.syntaxjason.nixperms.api.perms.IPermission;
import de.syntaxjason.nixperms.api.perms.IPermissionIteration;
import de.syntaxjason.nixperms.api.user.IUserList;
import de.syntaxjason.nixperms.api.util.IMetaHolder;
import de.syntaxjason.nixperms.api.util.IPriority;

import java.util.UUID;

public interface IPermissionGroup extends IMetaHolder {
    UUID uniqueId();
    String name();
    IPermissionIteration permissions();
    String prefix();
    String suffix();
    IPriority priority();
    IUserList assignedUsers();

    void name(String name);
    void priority(int priority);
    void prefix(String prefix);
    void suffix(String suffix);

    boolean addPermission(String permission);
    boolean removePermission(String permission);

    boolean create();
    boolean delete();
}
