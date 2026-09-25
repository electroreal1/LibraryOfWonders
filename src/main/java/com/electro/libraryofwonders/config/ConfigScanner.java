package com.electro.libraryofwonders.config;

import com.electro.libraryofwonders.LibraryOfWonders;
import com.electro.libraryofwonders.config.annotations.Config;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import org.objectweb.asm.Type;

@EventBusSubscriber(modid = LibraryOfWonders.MODID)
public class ConfigScanner {

    @SubscribeEvent
    public static void onModConstruct(FMLConstructModEvent event) {
        Type configAnnotationType = Type.getType(Config.class);

        ModList.get().getAllScanData().forEach(scanData -> {
            scanData.getAnnotations().forEach(annotationData -> {

                if (annotationData.annotationType().equals(configAnnotationType)) {
                    try {
                        String className = annotationData.clazz().getClassName();

                        Class configClass = Class.forName(className, true, Thread.currentThread().getContextClassLoader());

                        String fileName = (String) annotationData.annotationData().get("value");
                        if (fileName == null || fileName.isEmpty()) {
                            fileName = configClass.getSimpleName().toLowerCase() + ".toml";
                        }

                        AutoConfigManager.syncInstance(configClass, fileName);

                    } catch (ClassNotFoundException e) {
                        LibraryOfWonders.LOGGER.error("Library of Wonders: Failed to load config class {}", annotationData.clazz().getClassName(), e);
                    }
                }
            });
        });
    }
}