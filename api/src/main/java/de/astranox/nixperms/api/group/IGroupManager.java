package de.astranox.nixperms.api.group;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface IGroupManager {
    IPermissionGroup defaultGroup();
    @Nullable IPermissionGroup group(String name);
    Collection<IPermissionGroup> loaded();
    CompletableFuture<IPermissionGroup> create(String name, GroupRole role);
    CompletableFuture<Void> delete(IPermissionGroup group);
    CompletableFuture<Void> setParent(IPermissionGroup group, @Nullable IPermissionGroup parent);
    CompletableFuture<Void> setPermission(IPermissionGroup group, String node, boolean value);
    CompletableFuture<Void> unsetPermission(IPermissionGroup group, String node);
    CompletableFuture<Void> setOption(IPermissionGroup group, String key, String value);
    CompletableFuture<Void> unsetOption(IPermissionGroup group, String key);
    CompletableFuture<Void> addPrefix(IPermissionGroup group, int priority, String value);
    CompletableFuture<Void> removePrefix(IPermissionGroup group, int priority, String value);
    CompletableFuture<Void> addSuffix(IPermissionGroup group, int priority, String value);
    CompletableFuture<Void> removeSuffix(IPermissionGroup group, int priority, String value);
    CompletableFuture<Void> setWeight(IPermissionGroup group, int weight);

    CompletableFuture<Void> loadAll();
}
