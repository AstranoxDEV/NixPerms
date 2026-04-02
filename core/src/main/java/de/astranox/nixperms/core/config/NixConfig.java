package de.astranox.nixperms.core.config;

import de.astranox.nixperms.api.annotation.config.ConfigSection;
import de.astranox.nixperms.api.annotation.config.Key;

public final class NixConfig {
    public final DatabaseConfig database = new DatabaseConfig();
    public final SyncConfig sync = new SyncConfig();
    public final WebConfig web = new WebConfig();
    public final PermissionConfig permissions = new PermissionConfig();
    public final MessageConfig messages = new MessageConfig();
}
