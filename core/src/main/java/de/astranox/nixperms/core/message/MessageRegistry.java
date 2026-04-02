package de.astranox.nixperms.core.message;

import de.astranox.nixperms.api.annotation.message.*;
import de.astranox.nixperms.api.message.MessageProvider;
import de.astranox.nixperms.api.platform.Platform;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.reflect.Field;
import java.util.Map;

public final class MessageRegistry {

    private static final String GLOBAL = "global";

    private final Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, String>>> store = new Object2ObjectOpenHashMap<>();
    private String defaultLocale = "en_us";
    private String globalPrefix = "";

    public void register(Class<?> messageClass) {
        Locale localeAnnotation = messageClass.getAnnotation(Locale.class);
        String locale = localeAnnotation != null ? localeAnnotation.value().toLowerCase() : defaultLocale;
        MessagePrefix prefixAnnotation = messageClass.getAnnotation(MessagePrefix.class);
        String classPrefix = prefixAnnotation != null ? prefixAnnotation.value() : "";
        if (prefixAnnotation != null && prefixAnnotation.global()) globalPrefix = prefixAnnotation.value();
        for (Field field : messageClass.getDeclaredFields()) {
            Message annotation = field.getAnnotation(Message.class);
            if (annotation == null || !MessageProvider.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                MessageProvider provider = (MessageProvider) field.get(null);
                if (provider == null) continue;
                String raw = annotation.prefix() ? classPrefix + provider.raw() : provider.raw();
                if (annotation.platforms().length == 0) { set(locale, GLOBAL, annotation.value(), raw); continue; }
                for (Platform platform : annotation.platforms()) set(locale, platform.name().toLowerCase(), annotation.value(), raw);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to register message field: " + field.getName(), e);
            }
        }
    }

    public void set(String locale, String platformKey, String id, String raw) {
        store.computeIfAbsent(locale.toLowerCase(), k -> new Object2ObjectOpenHashMap<>())
                .computeIfAbsent(platformKey, k -> new Object2ObjectOpenHashMap<>())
                .put(id, raw);
    }

    public String getRaw(String locale, Platform platform, String id) {
        String platformKey = platform.name().toLowerCase();
        String found = lookup(locale, platformKey, id);
        if (found != null) return found;
        found = lookup(locale, GLOBAL, id);
        if (found != null) return found;
        found = lookup(defaultLocale, platformKey, id);
        if (found != null) return found;
        found = lookup(defaultLocale, GLOBAL, id);
        return found != null ? found : id;
    }

    private String lookup(String locale, String platformKey, String id) {
        Object2ObjectOpenHashMap<String, Object2ObjectOpenHashMap<String, String>> byLocale = store.get(locale.toLowerCase());
        if (byLocale == null) return null;
        Object2ObjectOpenHashMap<String, String> byPlatform = byLocale.get(platformKey);
        if (byPlatform == null) return null;
        return byPlatform.get(id);
    }

    public void setDefaultLocale(String locale) { this.defaultLocale = locale.toLowerCase(); }
    public String globalPrefix() { return globalPrefix; }
}
