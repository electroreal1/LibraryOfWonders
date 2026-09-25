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
     * Synchronizes a static configuration class with a TOML file.
     *
     * @param configClass The class containing your @ConfigValue fields.
     * @param fileName    The name of the config file (e.g., "mythos-common.toml").
     */
    public static void sync(Class configClass, String fileName) {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(fileName);

        try (CommentedFileConfig configData = CommentedFileConfig.builder(configPath)
                .sync()
                .preserveInsertionOrder()
                .autosave()
                .build()) {

            configData.load();
            boolean fileModified = false;

            for (Field field : configClass.getDeclaredFields()) {
                if (!field.isAnnotationPresent(ConfigValue.class)) continue;
                if (!Modifier.isStatic(field.getModifiers())) {
                    LibraryOfWonders.LOGGER.error("@ConfigValue applied to non-static field: {}", field.getName());
                    continue;
                }

                ConfigValue annotation = field.getAnnotation(ConfigValue.class);
                String tomlKey = annotation.name().isEmpty() ? field.getName() : annotation.name();

                field.setAccessible(true);

                try {
                    if (!configData.contains(tomlKey)) {
                        Object defaultValue = field.get(null);
                        configData.set(tomlKey, defaultValue);
                        fileModified = true;
                    } else {
                        Object tomlValue = configData.get(tomlKey);
                        setFieldValueSafely(field, tomlValue);
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
    private static void setFieldValueSafely(Field field, Object tomlValue) throws IllegalAccessException {
        if (tomlValue == null) return;

        Class type = field.getType();

        if (type == int.class || type == Integer.class) {
            field.set(null, ((Number) tomlValue).intValue());
        } else if (type == double.class || type == Double.class) {
            field.set(null, ((Number) tomlValue).doubleValue());
        } else if (type == float.class || type == Float.class) {
            field.set(null, ((Number) tomlValue).floatValue());
        } else if (type == long.class || type == Long.class) {
            field.set(null, ((Number) tomlValue).longValue());
        } else {
            // Fallback for Booleans, Strings, and Lists
            field.set(null, tomlValue);
        }
    }
}