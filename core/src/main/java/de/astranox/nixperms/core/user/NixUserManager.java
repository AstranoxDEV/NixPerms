package de.astranox.nixperms.core.user;

import de.astranox.nixperms.api.event.EventCause;
import de.astranox.nixperms.api.event.IEventBus;
import de.astranox.nixperms.api.event.user.UserLoadEvent;
import de.astranox.nixperms.api.event.user.UserUnloadEvent;
import de.astranox.nixperms.api.group.GroupRole;
import de.astranox.nixperms.api.group.IPermissionGroup;
import de.astranox.nixperms.api.permission.ResolutionPolicy;
import de.astranox.nixperms.api.user.*;
import de.astranox.nixperms.core.group.NixGroupManager;
import de.astranox.nixperms.core.model.UserModel;
import de.astranox.nixperms.core.permission.NixPermissionData;
import de.astranox.nixperms.core.permission.NixPermissionResolver;
import de.astranox.nixperms.core.storage.IUserStorage;
import de.astranox.nixperms.core.util.MojangProfileService;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public final class NixUserManager implements IUserManager {

    private static final long NEGATIVE_TTL_MS = 5 * 60 * 1000L;

    private final IUserStorage storage;
    private final NixGroupManager groupManager;
    private final NixPermissionResolver resolver;
    private final IEventBus eventBus;
    private final ResolutionPolicy defaultPolicy;
    private final Executor dbExecutor;
    private final Object2ObjectOpenHashMap<UUID, NixUser> cache = new Object2ObjectOpenHashMap<>();
    private final ConcurrentHashMap<UUID, SoftReference<NixUserSnapshot>> snapshotCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> negativeNameCache = new ConcurrentHashMap<>();

    public NixUserManager(IUserStorage storage, NixGroupManager groupManager, NixPermissionResolver resolver, IEventBus eventBus, ResolutionPolicy defaultPolicy, Executor dbExecutor) {
        this.storage = storage;
        this.groupManager = groupManager;
        this.resolver = resolver;
        this.eventBus = eventBus;
        this.defaultPolicy = defaultPolicy;
        this.dbExecutor = dbExecutor;
    }

    @Override public @Nullable INixUser getUser(UUID uniqueId) { synchronized (cache) { return cache.get(uniqueId); } }

    @Override
    public CompletableFuture<INixUser> loadUser(UUID uniqueId) {
        synchronized (cache) { NixUser cached = cache.get(uniqueId); if (cached != null) return CompletableFuture.completedFuture(cached); }
        return storage.load(uniqueId).thenApply(model -> {
            UserModel resolved = model != null ? model : new UserModel(uniqueId, groupManager.defaultGroup().name(), null, Map.of());
            NixUser user = buildUser(resolved);
            synchronized (cache) { cache.put(uniqueId, user); }
            eventBus.post(new UserLoadEvent(user, EventCause.API));
            return (INixUser) user;
        });
    }

    @Override
    public CompletableFuture<Void> saveUser(INixUser user) {
        return storage.save(new UserModel(user.uniqueId(), user.primary().name(), user.secondaryExplicit() != null ? user.secondaryExplicit().name() : null, user.ownPermissions()));
    }

    @Override
    public CompletableFuture<@Nullable IUserSnapshot> fetchSnapshot(UUID uniqueId) {
        synchronized (cache) { NixUser online = cache.get(uniqueId); if (online != null) return CompletableFuture.completedFuture(online.snapshot()); }
        SoftReference<NixUserSnapshot> ref = snapshotCache.get(uniqueId);
        if (ref != null) { NixUserSnapshot snap = ref.get(); if (snap != null) return CompletableFuture.completedFuture(snap); }
        return storage.load(uniqueId).thenApply(model -> {
            if (model == null) return null;
            NixUserSnapshot snap = (NixUserSnapshot) buildUser(model).snapshot();
            snapshotCache.put(uniqueId, new SoftReference<>(snap));
            return (IUserSnapshot) snap;
        });
    }

    @Override
    public CompletableFuture<@Nullable INixUser> resolveUser(String nameOrUuid) {
        try { return loadUser(UUID.fromString(nameOrUuid)); } catch (IllegalArgumentException ignored) {}
        synchronized (cache) { INixUser byName = cache.values().stream().filter(u -> nameOrUuid.equalsIgnoreCase(u.name())).findFirst().orElse(null); if (byName != null) return CompletableFuture.completedFuture(byName); }
        Long negAt = negativeNameCache.get(nameOrUuid.toLowerCase());
        if (negAt != null && System.currentTimeMillis() - negAt < NEGATIVE_TTL_MS) return CompletableFuture.completedFuture(null);
        return CompletableFuture.supplyAsync(() -> MojangProfileService.getUniqueId(nameOrUuid), dbExecutor).thenCompose(uuid -> {
            if (uuid == null) { negativeNameCache.put(nameOrUuid.toLowerCase(), System.currentTimeMillis()); return CompletableFuture.completedFuture(null); }
            return loadUser(uuid);
        });
    }

    @Override
    public void unloadUser(UUID uniqueId) {
        NixUser user;
        synchronized (cache) { user = cache.remove(uniqueId); }
        if (user != null) eventBus.post(new UserUnloadEvent(user.snapshot(), EventCause.API));
    }

    @Override public Collection<INixUser> loaded() { synchronized (cache) { return List.copyOf(cache.values()); } }

    public void invalidateGroup(String groupName) {
        synchronized (cache) { cache.values().stream().filter(u -> isInChain(u, groupName)).forEach(this::refreshUser); }
    }

    private boolean isInChain(NixUser user, String groupName) {
        if (groupManager.getChain(user.primary()).stream().anyMatch(g -> g.name().equals(groupName))) return true;
        IPermissionGroup sec = user.secondaryEffective();
        if (sec == null) return false;
        return groupManager.getChain(sec).stream().anyMatch(g -> g.name().equals(groupName));
    }

    private void refreshUser(NixUser user) {
        NixPermissionData permData = resolver.compute(user.primary(), user.secondaryEffective(), user.ownPermissionsRaw(), List.of(), defaultPolicy);
        NixMetaData metaData = buildMetaData(user.primary(), user.secondaryEffective());
        user.refreshCache(permData, metaData);
    }

    private NixUser buildUser(UserModel model) {
        IPermissionGroup primary = resolvePrimary(model.primaryGroupName());
        IPermissionGroup secondary = model.secondaryGroupName() != null ? groupManager.group(model.secondaryGroupName()) : null;
        IPermissionGroup effectiveSec = secondary != null ? secondary : primary.parent().orElse(null);
        NixPermissionData permData = resolver.compute(primary, effectiveSec, model.permissions(), List.of(), defaultPolicy);
        NixMetaData metaData = buildMetaData(primary, effectiveSec);
        AtomicReference<NixUser> ref = new AtomicReference<>();
        NixUser user = new NixUser(model.uniqueId(), primary, secondary, model.permissions(), permData, metaData, new NixUserCallbacks(
                (p, s) -> storage.save(new UserModel(model.uniqueId(), p.name(), s != null ? s.name() : null, ref.get().ownPermissionsRaw())),
                perms -> storage.save(new UserModel(model.uniqueId(), ref.get().primary().name(), ref.get().secondaryExplicit() != null ? ref.get().secondaryExplicit().name() : null, perms)),
                () -> refreshUser(ref.get())));
        ref.set(user);
        return user;
    }

    private NixMetaData buildMetaData(IPermissionGroup primary, @Nullable IPermissionGroup secondary) {
        String prefix = secondary != null && !secondary.meta().primaryPrefix().isEmpty() ? secondary.meta().primaryPrefix() : primary.meta().primaryPrefix();
        String suffix = secondary != null && !secondary.meta().primarySuffix().isEmpty() ? secondary.meta().primarySuffix() : primary.meta().primarySuffix();
        Map<String, String> options = new HashMap<>(primary.meta().options());
        if (secondary != null) options.putAll(secondary.meta().options());
        return new NixMetaData(prefix, suffix, Map.copyOf(options));
    }

    private IPermissionGroup resolvePrimary(String name) {
        IPermissionGroup group = groupManager.group(name);
        if (group != null && group.role() == GroupRole.PRIMARY) return group;
        return groupManager.defaultGroup();
    }
}
