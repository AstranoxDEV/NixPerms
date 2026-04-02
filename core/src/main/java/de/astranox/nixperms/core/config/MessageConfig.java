package de.astranox.nixperms.core.config;

import de.astranox.nixperms.api.annotation.config.ConfigSection;
import de.astranox.nixperms.api.annotation.config.Key;

@ConfigSection("messages")
public final class MessageConfig {
    @Key(value = "default-locale", comment = "Fallback locale: en_us | de_de | lol_us") public String defaultLocale = "en_us";
}
