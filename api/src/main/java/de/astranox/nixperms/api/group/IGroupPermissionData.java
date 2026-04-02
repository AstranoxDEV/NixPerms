package de.astranox.nixperms.api.group;

import java.util.Map;

public interface IGroupPermissionData {
    Map<String, Boolean> asMap();
    boolean contains(String node);
    boolean get(String node);
}
