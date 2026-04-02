package de.astranox.nixperms.api.group;

import java.util.Optional;

public interface IPermissionGroup {
    String name();
    int weight();
    GroupRole role();
    IGroupPermissionData permissions();
    IGroupMeta meta();
    Optional<IPermissionGroup> parent();
}
