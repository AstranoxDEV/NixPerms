package de.syntaxjason.nixperms.api.util;

import java.util.Map;
import java.util.Optional;

public interface IMetaHolder {
    boolean meta(String key, String value);
    boolean removeMeta(String key);
    Optional<String> meta(String key);
    Map<String, String> allMeta();
}
