package de.astranox.nixperms.core.user;

import de.astranox.nixperms.api.group.IPermissionGroup;
import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public record NixUserCallbacks(BiFunction<IPermissionGroup, @Nullable IPermissionGroup, CompletableFuture<Void>> onGroupChange, Function<Map<String, Boolean>, CompletableFuture<Void>> onPermissionChange, Runnable onRefreshNeeded) {}
