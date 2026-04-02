package de.astranox.nixperms.core.permission;

import de.astranox.nixperms.api.permission.IPermissionData;
import de.astranox.nixperms.api.permission.PermissionDecision;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.Map;

public final class NixPermissionData implements IPermissionData {

    private static final NixPermissionData WILDCARD = new NixPermissionData(Map.of(), true);

    private final Object2BooleanOpenHashMap<String> permissions;
    private final boolean wildcard;

    public NixPermissionData(Map<String, Boolean> source) {
        this(source, false);
    }

    private NixPermissionData(Map<String, Boolean> source, boolean wildcard) {
        this.permissions = new Object2BooleanOpenHashMap<>(source);
        this.permissions.defaultReturnValue(false);
        this.wildcard = wildcard;
    }

    public static NixPermissionData wildcard() { return WILDCARD; }

    @Override
    public PermissionDecision decision(String node) {
        if (wildcard) return PermissionDecision.TRUE;
        if (!permissions.containsKey(node)) return PermissionDecision.UNSET;
        return permissions.getBoolean(node) ? PermissionDecision.TRUE : PermissionDecision.FALSE;
    }

    @Override public Map<String, Boolean> flattened() { return permissions; }
}
