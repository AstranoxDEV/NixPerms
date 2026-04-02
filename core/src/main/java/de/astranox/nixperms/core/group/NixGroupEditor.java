package de.astranox.nixperms.core.group;

import de.astranox.nixperms.api.event.EventCause;
import de.astranox.nixperms.api.event.IEventBus;
import de.astranox.nixperms.api.event.group.*;
import de.astranox.nixperms.api.group.IPermissionGroup;
import de.astranox.nixperms.core.model.*;
import de.astranox.nixperms.core.storage.IGroupStorage;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

final class NixGroupEditor {

    private final NixGroupManager manager;
    private final IGroupStorage storage;
    private final IEventBus eventBus;

    NixGroupEditor(NixGroupManager manager, IGroupStorage storage, IEventBus eventBus) {
        this.manager = manager;
        this.storage = storage;
        this.eventBus = eventBus;
    }

    CompletableFuture<Void> setParent(IPermissionGroup group, @Nullable IPermissionGroup parent) {
        return mutate(group.name(), m -> new GroupModel(m.name(), m.role(), m.weight(), parent != null ? parent.name() : null, m.permissions(), m.prefixes(), m.suffixes(), m.options()), () -> eventBus.post(new GroupMetaChangeEvent(manager.group(group.name()), GroupMetaChangeEvent.MetaChangeType.PARENT_CHANGED, EventCause.API)));
    }

    CompletableFuture<Void> setPermission(IPermissionGroup group, String node, boolean value) {
        Boolean previous = group.permissions().asMap().get(node);
        return mutate(group.name(), m -> { Map<String, Boolean> p = new HashMap<>(m.permissions()); p.put(node, value); return new GroupModel(m.name(), m.role(), m.weight(), m.parentName(), Collections.unmodifiableMap(p), m.prefixes(), m.suffixes(), m.options()); }, () -> eventBus.post(new GroupPermissionChangeEvent(manager.group(group.name()), node, value, previous, EventCause.API)));
    }

    CompletableFuture<Void> unsetPermission(IPermissionGroup group, String node) {
        Boolean previous = group.permissions().asMap().get(node);
        return mutate(group.name(), m -> { Map<String, Boolean> p = new HashMap<>(m.permissions()); p.remove(node); return new GroupModel(m.name(), m.role(), m.weight(), m.parentName(), Collections.unmodifiableMap(p), m.prefixes(), m.suffixes(), m.options()); }, () -> eventBus.post(new GroupPermissionChangeEvent(manager.group(group.name()), node, null, previous, EventCause.API)));
    }

    CompletableFuture<Void> setOption(IPermissionGroup group, String key, String value) {
        return mutate(group.name(), m -> { Map<String, String> o = new HashMap<>(m.options()); o.put(key, value); return new GroupModel(m.name(), m.role(), m.weight(), m.parentName(), m.permissions(), m.prefixes(), m.suffixes(), Collections.unmodifiableMap(o)); }, () -> eventBus.post(new GroupMetaChangeEvent(manager.group(group.name()), GroupMetaChangeEvent.MetaChangeType.OPTION_SET, EventCause.API)));
    }

    CompletableFuture<Void> unsetOption(IPermissionGroup group, String key) {
        return mutate(group.name(), m -> { Map<String, String> o = new HashMap<>(m.options()); o.remove(key); return new GroupModel(m.name(), m.role(), m.weight(), m.parentName(), m.permissions(), m.prefixes(), m.suffixes(), Collections.unmodifiableMap(o)); }, () -> eventBus.post(new GroupMetaChangeEvent(manager.group(group.name()), GroupMetaChangeEvent.MetaChangeType.OPTION_UNSET, EventCause.API)));
    }

    CompletableFuture<Void> addPrefix(IPermissionGroup group, int priority, String value) {
        return mutate(group.name(), m -> new GroupModel(m.name(), m.role(), m.weight(), m.parentName(), m.permissions(), withEntry(m.prefixes(), priority, value), m.suffixes(), m.options()), () -> eventBus.post(new GroupMetaChangeEvent(manager.group(group.name()), GroupMetaChangeEvent.MetaChangeType.PREFIX_ADDED, EventCause.API)));
    }

    CompletableFuture<Void> removePrefix(IPermissionGroup group, int priority, String value) {
        return mutate(group.name(), m -> new GroupModel(m.name(), m.role(), m.weight(), m.parentName(), m.permissions(), withoutEntry(m.prefixes(), priority, value), m.suffixes(), m.options()), () -> eventBus.post(new GroupMetaChangeEvent(manager.group(group.name()), GroupMetaChangeEvent.MetaChangeType.PREFIX_REMOVED, EventCause.API)));
    }

    CompletableFuture<Void> addSuffix(IPermissionGroup group, int priority, String value) {
        return mutate(group.name(), m -> new GroupModel(m.name(), m.role(), m.weight(), m.parentName(), m.permissions(), m.prefixes(), withEntry(m.suffixes(), priority, value), m.options()), () -> eventBus.post(new GroupMetaChangeEvent(manager.group(group.name()), GroupMetaChangeEvent.MetaChangeType.SUFFIX_ADDED, EventCause.API)));
    }

    CompletableFuture<Void> removeSuffix(IPermissionGroup group, int priority, String value) {
        return mutate(group.name(), m -> new GroupModel(m.name(), m.role(), m.weight(), m.parentName(), m.permissions(), m.prefixes(), withoutEntry(m.suffixes(), priority, value), m.options()), () -> eventBus.post(new GroupMetaChangeEvent(manager.group(group.name()), GroupMetaChangeEvent.MetaChangeType.SUFFIX_REMOVED, EventCause.API)));
    }

    CompletableFuture<Void> setWeight(IPermissionGroup group, int weight) {
        return mutate(group.name(), m -> new GroupModel(m.name(), m.role(), weight, m.parentName(), m.permissions(), m.prefixes(), m.suffixes(), m.options()), () -> eventBus.post(new GroupMetaChangeEvent(manager.group(group.name()), GroupMetaChangeEvent.MetaChangeType.WEIGHT_CHANGED, EventCause.API)));
    }

    private CompletableFuture<Void> mutate(String name, UnaryOperator<GroupModel> updater, Runnable postEvent) {
        GroupModel current = manager.modelFor(name);
        if (current == null) return CompletableFuture.completedFuture(null);
        GroupModel updated = updater.apply(current);
        manager.applyModel(updated);
        return storage.save(updated).thenRun(postEvent);
    }

    private List<MetaEntryModel> withEntry(List<MetaEntryModel> existing, int priority, String value) {
        List<MetaEntryModel> list = new ArrayList<>(existing);
        list.add(new MetaEntryModel(priority, value));
        list.sort(Comparator.comparingInt(MetaEntryModel::priority).reversed());
        return List.copyOf(list);
    }

    private List<MetaEntryModel> withoutEntry(List<MetaEntryModel> existing, int priority, String value) {
        List<MetaEntryModel> list = new ArrayList<>(existing);
        list.removeIf(e -> e.priority() == priority && e.value().equals(value));
        return List.copyOf(list);
    }
}
