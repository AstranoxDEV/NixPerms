package de.astranox.nixperms.core.group;

import de.astranox.nixperms.api.event.EventCause;
import de.astranox.nixperms.api.event.IEventBus;
import de.astranox.nixperms.api.event.group.GroupCreateEvent;
import de.astranox.nixperms.api.event.group.GroupDeleteEvent;
import de.astranox.nixperms.api.group.*;
import de.astranox.nixperms.core.model.*;
import de.astranox.nixperms.core.storage.IGroupStorage;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class NixGroupManager implements IGroupManager {

    private final IGroupStorage storage;
    private final IEventBus eventBus;
    private final NixGroupChainCache chainCache = new NixGroupChainCache();
    private final Object2ObjectOpenHashMap<String, NixGroup> groupCache = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, GroupModel> modelCache = new Object2ObjectOpenHashMap<>();
    private final NixGroupEditor editor;
    private volatile IPermissionGroup defaultGroup;

    public NixGroupManager(IGroupStorage storage, IEventBus eventBus) {
        this.storage = storage;
        this.eventBus = eventBus;
        this.editor = new NixGroupEditor(this, storage, eventBus);
    }

    public CompletableFuture<Void> loadAll() {
        return storage.loadAll().thenAccept(models -> {
            synchronized (groupCache) {
                groupCache.clear(); modelCache.clear(); chainCache.clear();
                models.forEach(m -> { modelCache.put(m.name(), m); groupCache.put(m.name(), fromModel(m)); });
            }
            IPermissionGroup def = groupCache.get("default");
            if (def != null) { defaultGroup = def; return; }
            create("default", GroupRole.PRIMARY).thenAccept(g -> defaultGroup = g);
        });
    }

    public CompletableFuture<Void> reloadGroup(String name) {
        return storage.load(name).thenAccept(model -> {
            if (model == null) { synchronized (groupCache) { groupCache.remove(name); modelCache.remove(name); } chainCache.invalidate(name); return; }
            synchronized (groupCache) { modelCache.put(name, model); groupCache.put(name, fromModel(model)); }
            chainCache.invalidate(name);
        });
    }

    public List<IPermissionGroup> getChain(IPermissionGroup root) { return chainCache.get(root); }

    void applyModel(GroupModel model) {
        synchronized (groupCache) { modelCache.put(model.name(), model); groupCache.put(model.name(), fromModel(model)); }
        chainCache.invalidate(model.name());
    }

    @Nullable GroupModel modelFor(String name) { synchronized (groupCache) { return modelCache.get(name); } }

    @Override public IPermissionGroup defaultGroup() { return defaultGroup; }
    @Override public @Nullable IPermissionGroup group(String name) { return groupCache.get(name); }
    @Override public Collection<IPermissionGroup> loaded() { return List.copyOf(groupCache.values()); }

    @Override
    public CompletableFuture<IPermissionGroup> create(String name, GroupRole role) {
        GroupModel model = GroupModel.empty(name, role);
        return storage.save(model).thenApply(v -> {
            applyModel(model);
            IPermissionGroup group = groupCache.get(name);
            eventBus.post(new GroupCreateEvent(group, EventCause.API));
            return group;
        });
    }

    @Override
    public CompletableFuture<Void> delete(IPermissionGroup group) {
        return storage.delete(group.name()).thenRun(() -> {
            synchronized (groupCache) { groupCache.remove(group.name()); modelCache.remove(group.name()); }
            chainCache.invalidate(group.name());
            eventBus.post(new GroupDeleteEvent(group.name(), EventCause.API));
        });
    }

    @Override public CompletableFuture<Void> setParent(IPermissionGroup g, @Nullable IPermissionGroup p) { return editor.setParent(g, p); }
    @Override public CompletableFuture<Void> setPermission(IPermissionGroup g, String node, boolean v) { return editor.setPermission(g, node, v); }
    @Override public CompletableFuture<Void> unsetPermission(IPermissionGroup g, String node) { return editor.unsetPermission(g, node); }
    @Override public CompletableFuture<Void> setOption(IPermissionGroup g, String key, String v) { return editor.setOption(g, key, v); }
    @Override public CompletableFuture<Void> unsetOption(IPermissionGroup g, String key) { return editor.unsetOption(g, key); }
    @Override public CompletableFuture<Void> addPrefix(IPermissionGroup g, int p, String v) { return editor.addPrefix(g, p, v); }
    @Override public CompletableFuture<Void> removePrefix(IPermissionGroup g, int p, String v) { return editor.removePrefix(g, p, v); }
    @Override public CompletableFuture<Void> addSuffix(IPermissionGroup g, int p, String v) { return editor.addSuffix(g, p, v); }
    @Override public CompletableFuture<Void> removeSuffix(IPermissionGroup g, int p, String v) { return editor.removeSuffix(g, p, v); }
    @Override public CompletableFuture<Void> setWeight(IPermissionGroup g, int weight) { return editor.setWeight(g, weight); }

    NixGroup fromModel(GroupModel model) {
        NixGroupPermissionData permData = new NixGroupPermissionData(model.permissions());
        List<NixMetaEntry> prefixes = model.prefixes().stream().map(e -> new NixMetaEntry(e.priority(), e.value())).sorted(Comparator.comparingInt(NixMetaEntry::priority).reversed()).toList();
        List<NixMetaEntry> suffixes = model.suffixes().stream().map(e -> new NixMetaEntry(e.priority(), e.value())).sorted(Comparator.comparingInt(NixMetaEntry::priority).reversed()).toList();
        NixGroupMeta meta = new NixGroupMeta(model.options(), List.copyOf(prefixes), List.copyOf(suffixes));
        return new NixGroup(model.name(), model.role(), model.weight(), permData, meta, model.parentName(), groupCache::get);
    }
}
