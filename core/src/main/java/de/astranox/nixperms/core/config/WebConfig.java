package de.astranox.nixperms.core.config;

import de.astranox.nixperms.api.annotation.config.ConfigSection;
import de.astranox.nixperms.api.annotation.config.Key;
import de.astranox.nixperms.api.annotation.config.Reload;

@ConfigSection("web")
public final class WebConfig {
    @Key("enabled") public boolean enabled = false;
    @Key("port") public int port = 6432;
    @Key(value = "public-address", comment = "Override auto-detected IP. Leave empty for auto.") @Reload public String publicAddress = "";
    @Key("session-ttl-minutes") public int sessionTtlMinutes = 30;
}
