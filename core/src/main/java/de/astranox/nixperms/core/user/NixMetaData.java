package de.astranox.nixperms.core.user;

import de.astranox.nixperms.api.user.IMetaData;
import java.util.Map;

public record NixMetaData(String prefix, String suffix, Map<String, String> options) implements IMetaData {
    @Override public String option(String key) { return options.getOrDefault(key, ""); }
}
