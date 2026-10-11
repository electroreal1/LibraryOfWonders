package com.electro.libraryofwonders.client.rendering.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShaderHelper {

    public static class Uniforms {
        /**
         * Safely injects a single float into the currently bound shader.
         * Fails silently if the shader is missing or the uniform was optimized out.
         */
        public static void setFloat(String uniformName, float value) {
            ShaderInstance shader = RenderSystem.getShader();
            if (shader != null) {
                Uniform uniform = shader.getUniform(uniformName);
                if (uniform != null) {
                    uniform.set(value);
                }
            }
        }

        /** Safely injects a 2D vector (e.g., UV coordinates or screen resolution). */
        public static void setVec2(String uniformName, float x, float y) {
            ShaderInstance shader = RenderSystem.getShader();
            if (shader != null) {
                Uniform uniform = shader.getUniform(uniformName);
                if (uniform != null) {
                    uniform.set(x, y);
                }
            }
        }

        /** Safely injects a 3D vector (e.g., RGB colors or 3D world coordinates). */
        public static void setVec3(String uniformName, float x, float y, float z) {
            ShaderInstance shader = RenderSystem.getShader();
            if (shader != null) {
                Uniform uniform = shader.getUniform(uniformName);
                if (uniform != null) {
                    uniform.set(new Vector3f(x, y, z));
                }
            }
        }

        /** Safely injects a 4D vector (e.g., ARGB colors). */
        public static void setVec4(String uniformName, float x, float y, float z, float w) {
            ShaderInstance shader = RenderSystem.getShader();
            if (shader != null) {
                Uniform uniform = shader.getUniform(uniformName);
                if (uniform != null) {
                    uniform.set(new Vector4f(x, y, z, w));
                }
            }
        }

        /**
         * Standardizes the "GameTime" uniform across custom shaders.
         */
        public static void applyStandardTime(float partialTick) {
            if (Minecraft.getInstance().level != null) {
                float time = Minecraft.getInstance().level.getGameTime() + partialTick;
                setFloat("u_GameTime", time);
            }
        }
    }

    public static class PostProcess {
        /**
         * Loads and activates a vanilla or custom JSON post-chain shader effect (e.g., a blur or CRT scanline overlay).
         * Note: This will override the current post-processing chain (like the vanilla Creeper or Spider vision).
         *
         * @param location The ResourceLocation of the shader JSON (e.g., new ResourceLocation("minecraft", "shaders/post/blur.json"))
         */
        public static void loadScreenEffect(net.minecraft.resources.ResourceLocation location) {
            Minecraft mc = Minecraft.getInstance();
            mc.tell(() -> {
                if (mc.gameRenderer.currentEffect() != null) {
                    mc.gameRenderer.shutdownEffect();
                }
                mc.gameRenderer.loadEffect(location);
            });
        }

        /**
         * Clears all active post-processing effects and returns the screen to normal.
         */
        public static void clearScreenEffect() {
            Minecraft mc = Minecraft.getInstance();
            mc.tell(() -> {
                if (mc.gameRenderer.currentEffect() != null) {
                    mc.gameRenderer.shutdownEffect();
                }
            });
        }
    }
}
