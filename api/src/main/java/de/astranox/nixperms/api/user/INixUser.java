package de.astranox.nixperms.api.user;

import de.astranox.nixperms.api.group.IPermissionGroup;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface INixUser {
    UUID uniqueId();
    @Nullable String name();
    void updateName(String name);
    IPermissionGroup primary();
    @Nullable IPermissionGroup secondaryExplicit();
    @Nullable IPermissionGroup secondaryEffective();
    Map<String, Boolean> ownPermissions();
    boolean hasPermission(String node);
    CompletableFuture<Void> setPermission(String node, boolean value);
    CompletableFuture<Void> unsetPermission(String node);
    CompletableFuture<Void> setPrimary(IPermissionGroup group);
    CompletableFuture<Void> setSecondary(@Nullable IPermissionGroup group);
    IUserSnapshot snapshot();
}
