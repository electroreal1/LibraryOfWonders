package com.electro.libraryofwonders.client.rendering.annotation;

import com.electro.libraryofwonders.LibraryOfWonders;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.objectweb.asm.Type;

@EventBusSubscriber(modid = LibraryOfWonders.MODID, value = Dist.CLIENT)
public class RendererAutomator {

    /**
     * Reads the @BindRenderer annotation from the provided classes and automatically
     * registers them to Minecraft's rendering engine.
     */
    @SubscribeEvent
    @SuppressWarnings({"unchecked"})
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        Type annotationType = Type.getType(BindRenderer.class);

        ModList.get().getAllScanData().forEach(scanData -> scanData.getAnnotations().forEach(annotationData -> {

            if (annotationData.annotationType().equals(annotationType)) {
                try {
                    String className = annotationData.clazz().getClassName();
                    Class<?> clazz = Class.forName(className);

                    if (EntityRenderer.class.isAssignableFrom(clazz)) {
                        BindRenderer bind = clazz.getAnnotation(BindRenderer.class);

                        ResourceLocation entityId = ResourceLocation.parse(bind.value());

                        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);

                        EntityRendererProvider<Entity> provider = context -> {
                            try {
                                return (EntityRenderer<Entity>) clazz.getConstructor(EntityRendererProvider.Context.class).newInstance(context);
                            } catch (Exception e) {
                                throw new RuntimeException("[Library of Wonders] Failed to construct renderer: " + clazz.getSimpleName(), e);
                            }
                        };

                        event.registerEntityRenderer(entityType, provider);
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("[Library of Wonders] Failed to load renderer class: " + annotationData.clazz().getClassName());
                }
            }
        }));
    }
}
