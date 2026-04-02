package de.astranox.nixperms.core.config;

import de.astranox.nixperms.api.annotation.config.ConfigSection;
import de.astranox.nixperms.api.annotation.config.Key;
import de.astranox.nixperms.api.permission.ResolutionPolicy;

@ConfigSection("permissions")
public final class PermissionConfig {
    @Key(value = "resolution-policy", comment = "PRIMARY_WINS | SECONDARY_WINS | DENY_WINS") public String resolutionPolicy = "PRIMARY_WINS";
    @Key(value = "default-group", comment = "Fallback group for users without a primary group") public String defaultGroup = "default";

    public ResolutionPolicy policy() { return ResolutionPolicy.valueOf(resolutionPolicy.toUpperCase()); }
}
