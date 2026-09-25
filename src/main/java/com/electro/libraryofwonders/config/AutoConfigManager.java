package com.electro.libraryofwonders.config;

import com.electro.libraryofwonders.LibraryOfWonders;
import com.electro.libraryofwonders.config.annotations.ConfigValue;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.neoforged.fml.loading.FMLPaths;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

public class AutoConfigManager {

    /**
     * Synchronizes a specific object instance's non-static fields with a TOML file.
     * Use this for classes that do not use the @Config annotation.
     *
     * @param instance The object instance containing @ConfigValue fields.
     * @param fileName The name of the config file (e.g., "my-instance.toml").
     */
    public static void syncInstance(Object instance, String fileName) {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(fileName);

        try (CommentedFileConfig configData = CommentedFileConfig.builder(configPath)
                .sync()
                .preserveInsertionOrder()
                .autosave()
                .build()) {

            configData.load();
            boolean fileModified = false;

            for (Field field : instance.getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(ConfigValue.class)) continue;

                ConfigValue annotation = field.getAnnotation(ConfigValue.class);
                String tomlKey = annotation.name().isEmpty() ? field.getName() : annotation.name();

                field.setAccessible(true);

                try {
                    if (!configData.contains(tomlKey)) {
                        Object defaultValue = field.get(instance);
                        configData.set(tomlKey, defaultValue);
                        fileModified = true;
                    } else {
                        Object tomlValue = configData.get(tomlKey);
                        setFieldValueSafely(field, instance, tomlValue);
                    }

                    if (!annotation.comment().isEmpty()) {
                        configData.setComment(tomlKey, annotation.comment());
                        fileModified = true;
                    }
                } catch (IllegalAccessException e) {
                    LibraryOfWonders.LOGGER.error("Failed to access config field: {}", field.getName(), e);
                }
            }

            if (fileModified) {
                configData.save();
            }
        }
    }

    /**
     * Helper to safely cast NightConfig types to Java field types.
     */
    private static void setFieldValueSafely(Field field, Object target, Object tomlValue) throws IllegalAccessException {
        if (tomlValue == null) return;
        Class type = field.getType();

        if (type == int.class || type == Integer.class) {
            field.set(target, ((Number) tomlValue).intValue());
        } else if (type == double.class || type == Double.class) {
            field.set(target, ((Number) tomlValue).doubleValue());
        } else if (type == float.class || type == Float.class) {
            field.set(target, ((Number) tomlValue).floatValue());
        } else if (type == long.class || type == Long.class) {
            field.set(target, ((Number) tomlValue).longValue());
        } else {
            // Fallback for Booleans, Strings, and Lists
            field.set(target, tomlValue);
        }
    }
}