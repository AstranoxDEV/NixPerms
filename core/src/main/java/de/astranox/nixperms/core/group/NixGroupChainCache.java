package de.astranox.nixperms.core.group;

import de.astranox.nixperms.api.group.IPermissionGroup;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class NixGroupChainCache {

    private static final int MAX_DEPTH = 16;

    private final ConcurrentHashMap<String, List<IPermissionGroup>> cache = new ConcurrentHashMap<>();

    public List<IPermissionGroup> get(IPermissionGroup root) {
        return cache.computeIfAbsent(root.name(), k -> resolve(root));
    }

    public void invalidate(String groupName) {
        cache.entrySet().removeIf(e -> e.getValue().stream().anyMatch(g -> g.name().equals(groupName)));
    }

    public void clear() { cache.clear(); }

    private List<IPermissionGroup> resolve(IPermissionGroup root) {
        List<IPermissionGroup> chain = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        IPermissionGroup current = root;
        while (current != null && chain.size() < MAX_DEPTH) {
            if (!visited.add(current.name())) break;
            chain.add(current);
            current = current.parent().orElse(null);
        }
        Collections.reverse(chain);
        return List.copyOf(chain);
    }
}
