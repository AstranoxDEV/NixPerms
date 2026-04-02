package de.astranox.nixperms.core.config;

import de.astranox.nixperms.api.annotation.config.ConfigSection;
import de.astranox.nixperms.api.annotation.config.Key;
import de.astranox.nixperms.api.annotation.config.Reload;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AnnotationConfigProcessor {

    private final Path configFile;
    private final Yaml yaml;

    public AnnotationConfigProcessor(Path configFile) {
        this.configFile = configFile;
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        this.yaml = new Yaml(options);
    }

    public void load(NixConfig config) {
        if (!configFile.toFile().exists()) { save(config); return; }
        try (Reader reader = new FileReader(configFile.toFile())) {
            Map<String, Object> root = yaml.load(reader);
            if (root == null) return;
            applySection(config.database, root);
            applySection(config.sync, root);
            applySection(config.web, root);
            applySection(config.permissions, root);
            applySection(config.messages, root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + e.getMessage(), e);
        }
    }

    public void save(NixConfig config) {
        configFile.toFile().getParentFile().mkdirs();
        Map<String, Object> root = new LinkedHashMap<>();
        collectSection(config.database, root);
        collectSection(config.sync, root);
        collectSection(config.web, root);
        collectSection(config.permissions, root);
        collectSection(config.messages, root);
        try (Writer writer = new FileWriter(configFile.toFile())) {
            yaml.dump(root, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config: " + e.getMessage(), e);
        }
    }

    public void reload(NixConfig config) {
        if (!configFile.toFile().exists()) return;
        try (Reader reader = new FileReader(configFile.toFile())) {
            Map<String, Object> root = yaml.load(reader);
            if (root == null) return;
            reloadSection(config.database, root);
            reloadSection(config.sync, root);
            reloadSection(config.web, root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reload config: " + e.getMessage(), e);
        }
    }

    private void applySection(Object section, Map<String, Object> root) {
        ConfigSection annotation = section.getClass().getAnnotation(ConfigSection.class);
        if (annotation == null) return;
        Object sectionData = root.get(annotation.value());
        if (!(sectionData instanceof Map<?, ?> sectionMap)) return;
        for (Field field : section.getClass().getDeclaredFields()) {
            Key keyAnnotation = field.getAnnotation(Key.class);
            if (keyAnnotation == null) continue;
            Object value = sectionMap.get(keyAnnotation.value());
            if (value == null) continue;
            setField(field, section, value);
        }
    }

    private void reloadSection(Object section, Map<String, Object> root) {
        ConfigSection annotation = section.getClass().getAnnotation(ConfigSection.class);
        if (annotation == null) return;
        Object sectionData = root.get(annotation.value());
        if (!(sectionData instanceof Map<?, ?> sectionMap)) return;
        for (Field field : section.getClass().getDeclaredFields()) {
            if (field.getAnnotation(Reload.class) == null) continue;
            Key keyAnnotation = field.getAnnotation(Key.class);
            if (keyAnnotation == null) continue;
            Object value = sectionMap.get(keyAnnotation.value());
            if (value == null) continue;
            setField(field, section, value);
        }
    }

    private void collectSection(Object section, Map<String, Object> root) {
        ConfigSection annotation = section.getClass().getAnnotation(ConfigSection.class);
        if (annotation == null) return;
        Map<String, Object> sectionMap = new LinkedHashMap<>();
        for (Field field : section.getClass().getDeclaredFields()) {
            Key keyAnnotation = field.getAnnotation(Key.class);
            if (keyAnnotation == null) continue;
            try { field.setAccessible(true); sectionMap.put(keyAnnotation.value(), field.get(section)); }
            catch (IllegalAccessException ignored) {}
        }
        root.put(annotation.value(), sectionMap);
    }

    private void setField(Field field, Object target, Object value) {
        try {
            field.setAccessible(true);
            if (field.getType() == int.class) { field.set(target, ((Number) value).intValue()); return; }
            if (field.getType() == long.class) { field.set(target, ((Number) value).longValue()); return; }
            if (field.getType() == double.class) { field.set(target, ((Number) value).doubleValue()); return; }
            if (field.getType() == boolean.class) { field.set(target, value); return; }
            field.set(target, String.valueOf(value));
        } catch (IllegalAccessException ignored) {}
    }
}
