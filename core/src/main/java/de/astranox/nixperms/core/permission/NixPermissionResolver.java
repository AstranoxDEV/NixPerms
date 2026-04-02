package de.astranox.nixperms.core.permission;

import de.astranox.nixperms.api.attachment.IPermissionAttachment;
import de.astranox.nixperms.api.group.IPermissionGroup;
import de.astranox.nixperms.api.permission.ResolutionPolicy;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Function;

public final class NixPermissionResolver {

    private final Function<IPermissionGroup, List<IPermissionGroup>> chainProvider;

    public NixPermissionResolver(Function<IPermissionGroup, List<IPermissionGroup>> chainProvider) {
        this.chainProvider = chainProvider;
    }

    public NixPermissionData compute(IPermissionGroup primary, @Nullable IPermissionGroup secondaryEffective, Map<String, Boolean> ownPermissions, Collection<IPermissionAttachment> attachments, ResolutionPolicy policy) {
        Map<String, Boolean> primaryFlat = flatten(chainProvider.apply(primary));
        Map<String, Boolean> secondaryFlat = secondaryEffective != null ? flatten(chainProvider.apply(secondaryEffective)) : Map.of();
        Map<String, Boolean> merged = merge(primaryFlat, secondaryFlat, policy);
        attachments.forEach(a -> merged.putAll(a.permissions()));
        merged.putAll(ownPermissions);
        if (Boolean.TRUE.equals(merged.get("*"))) return NixPermissionData.wildcard();
        return new NixPermissionData(merged);
    }

    private Map<String, Boolean> flatten(List<IPermissionGroup> chain) {
        Map<String, Boolean> result = new LinkedHashMap<>();
        chain.forEach(g -> result.putAll(g.permissions().asMap()));
        return result;
    }

    private Map<String, Boolean> merge(Map<String, Boolean> primary, Map<String, Boolean> secondary, ResolutionPolicy policy) {
        Map<String, Boolean> result = new HashMap<>(primary.size() + secondary.size());
        switch (policy) {
            case PRIMARY_WINS -> { result.putAll(secondary); result.putAll(primary); }
            case SECONDARY_WINS -> { result.putAll(primary); result.putAll(secondary); }
            case DENY_WINS -> { result.putAll(primary); secondary.forEach((node, value) -> result.merge(node, value, (a, b) -> a && b)); }
        }
        return result;
    }
}
