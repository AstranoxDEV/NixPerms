package de.astranox.nixperms.core.group;

import de.astranox.nixperms.api.group.IGroupPermissionData;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Map;

public final class NixGroupPermissionData implements IGroupPermissionData {

    private final Object2BooleanOpenHashMap<String> permissions;

    public NixGroupPermissionData(Map<String, Boolean> source) {
        this.permissions = new Object2BooleanOpenHashMap<>(source);
        this.permissions.defaultReturnValue(false);
    }

    @Override public Map<String, Boolean> asMap() { return permissions; }
    @Override public boolean contains(String node) { return permissions.containsKey(node); }
    @Override public boolean get(String node) { return permissions.getBoolean(node); }
}
