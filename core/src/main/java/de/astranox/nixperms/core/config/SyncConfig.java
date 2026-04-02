package de.astranox.nixperms.core.config;

import de.astranox.nixperms.api.annotation.config.ConfigSection;
import de.astranox.nixperms.api.annotation.config.Key;
import de.astranox.nixperms.api.annotation.config.Reload;
import java.util.UUID;

@ConfigSection("sync")
public final class SyncConfig {
    @Key(value = "poll-interval-ms", comment = "How often to poll DB for sync messages") @Reload public long pollIntervalMs = 3000L;
    @Key(value = "server-id", comment = "Unique ID for this server instance") public String serverId = UUID.randomUUID().toString();
}
