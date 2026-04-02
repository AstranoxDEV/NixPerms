package de.astranox.nixperms.core.user;

import de.astranox.nixperms.api.group.GroupRole;
import de.astranox.nixperms.api.group.IPermissionGroup;
import de.astranox.nixperms.api.permission.PermissionDecision;
import de.astranox.nixperms.api.user.INixUser;
import de.astranox.nixperms.api.user.IUserSnapshot;
import de.astranox.nixperms.core.permission.NixPermissionData;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class NixUser implements INixUser {

    private final UUID uniqueId;
    private volatile IPermissionGroup primary;
    @Nullable private volatile IPermissionGroup secondaryExplicit;
    private final Object2BooleanOpenHashMap<String> ownPermissions;
    private volatile NixPermissionData permissionData;
    private volatile NixMetaData metaData;
    @Nullable private volatile String name;
    private final NixUserCallbacks callbacks;

    public NixUser(UUID uniqueId, IPermissionGroup primary, @Nullable IPermissionGroup secondaryExplicit, Map<String, Boolean> ownPermissions, NixPermissionData permissionData, NixMetaData metaData, NixUserCallbacks callbacks) {
        this.uniqueId = uniqueId;
        this.primary = primary;
        this.secondaryExplicit = secondaryExplicit;
        this.ownPermissions = new Object2BooleanOpenHashMap<>(ownPermissions);
        this.permissionData = permissionData;
        this.metaData = metaData;
        this.callbacks = callbacks;
    }

    @Override public UUID uniqueId() { return uniqueId; }
    @Override public @Nullable String name() { return name; }
    @Override public void updateName(String name) { this.name = name; }
    @Override public IPermissionGroup primary() { return primary; }
    @Override public @Nullable IPermissionGroup secondaryExplicit() { return secondaryExplicit; }

    @Override
    public @Nullable IPermissionGroup secondaryEffective() {
        IPermissionGroup explicit = secondaryExplicit;
        if (explicit != null) return explicit;
        return primary.parent().orElse(null);
    }

    @Override public Map<String, Boolean> ownPermissions() { return Collections.unmodifiableMap(ownPermissions); }
    @Override public boolean hasPermission(String node) { return permissionData.decision(node) == PermissionDecision.TRUE; }

    @Override
    public CompletableFuture<Void> setPermission(String node, boolean value) {
        ownPermissions.put(node, value);
        callbacks.onRefreshNeeded().run();
        return callbacks.onPermissionChange().apply(Collections.unmodifiableMap(ownPermissions));
    }

    @Override
    public CompletableFuture<Void> unsetPermission(String node) {
        ownPermissions.remove(node);
        callbacks.onRefreshNeeded().run();
        return callbacks.onPermissionChange().apply(Collections.unmodifiableMap(ownPermissions));
    }

    @Override
    public CompletableFuture<Void> setPrimary(IPermissionGroup group) {
        if (group.role() != GroupRole.PRIMARY) return CompletableFuture.failedFuture(new IllegalArgumentException("Group " + group.name() + " has role SECONDARY, expected PRIMARY"));
        this.primary = group;
        callbacks.onRefreshNeeded().run();
        return callbacks.onGroupChange().apply(group, secondaryExplicit);
    }

    @Override
    public CompletableFuture<Void> setSecondary(@Nullable IPermissionGroup group) {
        this.secondaryExplicit = group;
        callbacks.onRefreshNeeded().run();
        return callbacks.onGroupChange().apply(primary, group);
    }

    @Override
    public IUserSnapshot snapshot() {
        IPermissionGroup effectiveSec = secondaryEffective();
        return new NixUserSnapshot(uniqueId, primary.name(), secondaryExplicit != null ? secondaryExplicit.name() : null, effectiveSec != null ? effectiveSec.name() : null, permissionData, metaData);
    }

    public void refreshCache(NixPermissionData permData, NixMetaData meta) {
        this.permissionData = permData;
        this.metaData = meta;
    }

    public Object2BooleanOpenHashMap<String> ownPermissionsRaw() { return ownPermissions; }
}
