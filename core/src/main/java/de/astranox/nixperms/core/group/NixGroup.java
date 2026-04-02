package de.astranox.nixperms.core.group;

import de.astranox.nixperms.api.group.*;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.function.Function;

public final class NixGroup implements IPermissionGroup {

    private final String name;
    private final GroupRole role;
    private final int weight;
    private final NixGroupPermissionData permissions;
    private final NixGroupMeta meta;
    @Nullable private final String parentName;
    private final Function<String, IPermissionGroup> groupLookup;

    public NixGroup(String name, GroupRole role, int weight, NixGroupPermissionData permissions, NixGroupMeta meta, @Nullable String parentName, Function<String, IPermissionGroup> groupLookup) {
        this.name = name;
        this.role = role;
        this.weight = weight;
        this.permissions = permissions;
        this.meta = meta;
        this.parentName = parentName;
        this.groupLookup = groupLookup;
    }

    @Override public String name() { return name; }
    @Override public GroupRole role() { return role; }
    @Override public int weight() { return weight; }
    @Override public NixGroupPermissionData permissions() { return permissions; }
    @Override public NixGroupMeta meta() { return meta; }

    @Override
    public Optional<IPermissionGroup> parent() {
        if (parentName == null) return Optional.empty();
        return Optional.ofNullable(groupLookup.apply(parentName));
    }
}
