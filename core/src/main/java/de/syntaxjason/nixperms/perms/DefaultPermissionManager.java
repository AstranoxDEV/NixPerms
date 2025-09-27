package de.syntaxjason.nixperms.perms;

import de.syntaxjason.nixperms.api.group.IPermissionGroup;
import de.syntaxjason.nixperms.api.perms.IPermission;
import de.syntaxjason.nixperms.api.perms.IPermissionManager;
import de.syntaxjason.nixperms.api.user.IUser;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultPermissionManager implements IPermissionManager {

    public interface UserRepository {
        Optional<IUser> find(UUID uniqueId);
        void save(IUser user);
    }

    public interface GroupRepository {
        Optional<IPermissionGroup> findById(UUID id);
        Optional<IPermissionGroup> findByName(String name);
        Optional<Set<IPermissionGroup>> all();
        void save(IPermissionGroup group);
    }

    public interface PermissionResolver {
        boolean has(IUser user, String node);
        void invalidate(UUID userId);
    }

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PermissionResolver resolver;
    private final PermissionStore store = new PermissionStore();

    public DefaultPermissionManager(UserRepository userRepository, GroupRepository groupRepository, PermissionResolver resolver) {
        if (userRepository == null) throw new IllegalArgumentException("userRepository must not be null");
        if (groupRepository == null) throw new IllegalArgumentException("groupRepository must not be null");
        if (resolver == null) throw new IllegalArgumentException("resolver must not be null");
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.resolver = resolver;
    }

    @Override
    public Optional<IUser> user(UUID uniqueId) {
        if (uniqueId == null) return Optional.empty();
        return userRepository.find(uniqueId);
    }

    @Override
    public Optional<IPermissionGroup> group(UUID uniqueId) {
        if (uniqueId == null) return Optional.empty();
        return groupRepository.findById(uniqueId);
    }

    @Override
    public Optional<IPermissionGroup> group(String name) {
        if (name == null || name.isEmpty()) return Optional.empty();
        return groupRepository.findByName(name);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Optional<Set<IPermissionGroup>> registeredGroups() {
        Optional<Set<IPermissionGroup>> all = groupRepository.all();
        if (all.isEmpty()) return Optional.empty();
        return Optional.of(all.get());
    }

    @Override
    public IPermission permission(String permission) {
        return null;
    }

    @Override
    public IPermission permission(String permission, boolean value) {
        return null;
    }

    @Override
    public boolean permission(UUID uniqueId, IPermission permission) {
        if (uniqueId == null || permission == null) return false;
        Optional<IUser> user = userRepository.find(uniqueId);
        if (user.isEmpty()) return false;
        String node = permission.name();
        if (node == null || node.isEmpty()) return false;
        return resolver.has(user.get(), node);
    }

    @Override
    public void grantPermission(UUID uniqueId, IPermission permission) {
        if (uniqueId == null || permission == null) return;
        Optional<IUser> user = userRepository.find(uniqueId);
        if (user.isEmpty()) return;

        store.addUserNode(uniqueId, permission);
        resolver.invalidate(uniqueId);
    }

    @Override
    public void revokePermission(UUID uniqueId, IPermission permission) {
        if (uniqueId == null || permission == null) return;
        Optional<IUser> user = userRepository.find(uniqueId);
        if (user.isEmpty()) return;

        store.removeUserNode(uniqueId, permission.name());
        resolver.invalidate(uniqueId);
    }

    @Override
    public void primaryGroup(UUID uniqueId, IPermissionGroup group) {
        if (uniqueId == null || group == null) return;
        Optional<IUser> user = userRepository.find(uniqueId);
        if (user.isEmpty()) return;

        IUser target = user.get();
        IPermissionGroup current = target.primaryGroup();
        if (current != null && current.uniqueId().equals(group.uniqueId())) return;

        target.primaryGroup(group);
        userRepository.save(target);
        resolver.invalidate(uniqueId);
    }

    @Override
    public void secondaryGroup(UUID uniqueId, IPermissionGroup group) {
        if (uniqueId == null || group == null) return;
        Optional<IUser> user = userRepository.find(uniqueId);
        if (user.isEmpty()) return;

        IUser target = user.get();
        IPermissionGroup primary = target.primaryGroup();
        if (primary != null && primary.uniqueId().equals(group.uniqueId())) {
            target.clearSecondaryGroup();
            userRepository.save(target);
            resolver.invalidate(uniqueId);
            return;
        }

        target.secondaryGroup(group);
        userRepository.save(target);
        resolver.invalidate(uniqueId);
    }

    @Override
    public void clearSecondaryGroup(UUID uniqueId) {
        if (uniqueId == null) return;
        Optional<IUser> user = userRepository.find(uniqueId);
        if (user.isEmpty()) return;

        IUser target = user.get();
        if (target.secondaryGroup() == null) return;

        target.clearSecondaryGroup();
        userRepository.save(target);
        resolver.invalidate(uniqueId);
    }

    @Override
    public void switchPrimaryGroup(UUID uniqueId, IPermissionGroup group) {
        if (uniqueId == null || group == null) return;
        Optional<IUser> user = userRepository.find(uniqueId);
        if (user.isEmpty()) return;

        IUser target = user.get();
        IPermissionGroup current = target.primaryGroup();
        if (current != null && current.uniqueId().equals(group.uniqueId())) return;

        target.primaryGroup(group);
        userRepository.save(target);
        resolver.invalidate(uniqueId);
    }

    @Override
    public void swapGroups(UUID uniqueId) {
        if (uniqueId == null) return;
        Optional<IUser> user = userRepository.find(uniqueId);
        if (user.isEmpty()) return;

        IUser target = user.get();
        IPermissionGroup secondary = target.secondaryGroup();
        if (secondary == null) return;

        IPermissionGroup newPrimary = secondary;
        target.secondaryGroup(target.primaryGroup());
        target.primaryGroup(newPrimary);

        userRepository.save(target);
        resolver.invalidate(uniqueId);
    }

    @Override
    public void replacePrimaryGroup(UUID uniqueId) {
        if (uniqueId == null) return;
        Optional<IUser> user = userRepository.find(uniqueId);
        if (user.isEmpty()) return;

        IUser target = user.get();
        IPermissionGroup secondary = target.secondaryGroup();
        if (secondary == null) return;

        target.primaryGroup(secondary);
        target.clearSecondaryGroup();

        userRepository.save(target);
        resolver.invalidate(uniqueId);
    }

    private static final class PermissionStore {
        private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<IPermission>> user = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<IPermission>> group = new ConcurrentHashMap<>();

        List<IPermission> userNodes(UUID id) {
            CopyOnWriteArrayList<IPermission> list = user.get(id);
            if (list == null) return java.util.Collections.emptyList();
            return List.copyOf(list);
        }

        List<IPermission> groupNodes(UUID id) {
            CopyOnWriteArrayList<IPermission> list = group.get(id);
            if (list == null) return java.util.Collections.emptyList();
            return List.copyOf(list);
        }

        void addUserNode(UUID id, IPermission node) {
            CopyOnWriteArrayList<IPermission> list = user.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>());
            list.add(node);
        }

        void removeUserNode(UUID id, String nodeName) {
            CopyOnWriteArrayList<IPermission> list = user.get(id);
            if (list == null) return;
            list.removeIf(p -> nodeName.equalsIgnoreCase(p.name()));
        }

        void addGroupNode(UUID id, IPermission node) {
            CopyOnWriteArrayList<IPermission> list = group.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>());
            list.add(node);
        }

        void removeGroupNode(UUID id, String nodeName) {
            CopyOnWriteArrayList<IPermission> list = group.get(id);
            if (list == null) return;
            list.removeIf(p -> nodeName.equalsIgnoreCase(p.name()));
        }
    }
}
