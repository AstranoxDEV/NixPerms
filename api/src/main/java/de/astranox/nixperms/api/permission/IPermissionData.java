package de.astranox.nixperms.api.permission;

import java.util.Map;

public interface IPermissionData {
    Map<String, Boolean> flattened();
    PermissionDecision decision(String node);
}
