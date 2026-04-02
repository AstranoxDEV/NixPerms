package de.astranox.nixperms.core.model;

import de.astranox.nixperms.api.group.GroupRole;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public record GroupModel(String name, GroupRole role, int weight, @Nullable String parentName, Map<String, Boolean> permissions, List<MetaEntryModel> prefixes, List<MetaEntryModel> suffixes, Map<String, String> options) {

    public static GroupModel empty(String name, GroupRole role) {
        return new GroupModel(name, role, 0, null, Map.of(), List.of(), List.of(), Map.of());
    }

    public static final class Builder {
        private final String name;
        private final GroupRole role;
        private final int weight;
        @Nullable private final String parentName;
        private final Map<String, Boolean> permissions = new LinkedHashMap<>();
        private final List<MetaEntryModel> prefixes = new ArrayList<>();
        private final List<MetaEntryModel> suffixes = new ArrayList<>();
        private final Map<String, String> options = new LinkedHashMap<>();

        public Builder(String name, GroupRole role, int weight, @Nullable String parentName) {
            this.name = name;
            this.role = role;
            this.weight = weight;
            this.parentName = parentName;
        }

        public void permission(String node, boolean value) { permissions.put(node, value); }
        public void prefix(MetaEntryModel entry) { prefixes.add(entry); }
        public void suffix(MetaEntryModel entry) { suffixes.add(entry); }
        public void option(String key, String value) { options.put(key, value); }

        public GroupModel build() {
            return new GroupModel(name, role, weight, parentName,
                    Collections.unmodifiableMap(permissions),
                    Collections.unmodifiableList(prefixes),
                    Collections.unmodifiableList(suffixes),
                    Collections.unmodifiableMap(options));
        }
    }
}
