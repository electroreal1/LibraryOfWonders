package com.electro.libraryofwonders.client.rendering.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class VFXHelper {

    public static class MathHelper {
        /** Creates an organic floating bobbing motion over time. */
        public static float floatMotion(float time, float speed, float scale) {
            return (float) Math.sin(time * speed) * scale;
        }

        /** Generates a sharp, jagged pulse for critical hits or aggressive throbbing. */
        public static float sharpPulse(float time, float speed) {
            float sin = Mth.sin(time * speed);
            return sin > 0 ? (float) Math.pow(sin, 3.0) : 0.0F;
        }

        /** Clamps and normalizes a value between a custom min and max. */
        public static float remap(float value, float min1, float max1, float min2, float max2) {
            return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
        }
    }

    public class Colors {
        /** Compresses an alpha float and an RGB hex into a single ARGB integer. */
        public static int pack(float alpha, int rgb) {
            int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
            return (a << 24) | (rgb & 0xFFFFFF);
        }

        /** Generates a smooth shifting rainbow color based on time and an offset. */
        public static int rainbow(float time, float offset, float saturation, float brightness) {
            float hue = (time + offset) % 1.0F;
            if (hue < 0) hue += 1.0F;
            return Mth.hsvToRgb(hue, saturation, brightness);
        }

        /** Smoothly lerps between two ARGB colors. */
        public static int lerpColor(float partial, int colorA, int colorB) {
            int aA = (colorA >> 24) & 0xFF, rA = (colorA >> 16) & 0xFF, gA = (colorA >> 8) & 0xFF, bA = colorA & 0xFF;
            int aB = (colorB >> 24) & 0xFF, rB = (colorB >> 16) & 0xFF, gB = (colorB >> 8) & 0xFF, bB = colorB & 0xFF;

            int a = (int) Mth.lerp(partial, aA, aB);
            int r = (int) Mth.lerp(partial, rA, rB);
            int g = (int) Mth.lerp(partial, gA, gB);
            int b = (int) Mth.lerp(partial, bA, bB);

            return (a << 24) | (r << 16) | (g << 8) | b;
        }

    }

    public class Shapes {
        /**
         * Draws a 3D rectangular tube. Perfect for standard glowing lines, wireframes, and lasers.
         */
        public static void drawVolumetricLine(GeometryList list, Matrix4f m, Vec3 start, Vec3 end, float thickness, int argbColor) {
            drawTaperedLine(list, m, start, end, thickness, thickness, argbColor, argbColor);
        }

        public static void drawTaperedLine(GeometryList list, Matrix4f m, Vec3 start, Vec3 end, float thickStart, float thickEnd, int colorStart, int colorEnd) {
            Vec3 dir = end.subtract(start);
            if (dir.lengthSqr() < 0.0001) return;
            dir = dir.normalize();

            Vec3 up = new Vec3(0, 1, 0);
            if (Math.abs(dir.y) > 0.99) {
                up = new Vec3(1, 0, 0);
            }

            Vec3 rightStart = dir.cross(up).normalize().scale(thickStart / 2.0);
            Vec3 upStart = rightStart.cross(dir).normalize().scale(thickStart / 2.0);

            Vec3 rightEnd = dir.cross(up).normalize().scale(thickEnd / 2.0);
            Vec3 upEnd = rightEnd.cross(dir).normalize().scale(thickEnd / 2.0);

            Vec3 s1 = start.add(rightStart).add(upStart);
            Vec3 s2 = start.subtract(rightStart).add(upStart);
            Vec3 s3 = start.subtract(rightStart).subtract(upStart);
            Vec3 s4 = start.add(rightStart).subtract(upStart);

            Vec3 e1 = end.add(rightEnd).add(upEnd);
            Vec3 e2 = end.subtract(rightEnd).add(upEnd);
            Vec3 e3 = end.subtract(rightEnd).subtract(upEnd);
            Vec3 e4 = end.add(rightEnd).subtract(upEnd);

            drawQuad(list, m, s1, s2, e2, e1, colorStart, colorStart, colorEnd, colorEnd); // Top
            drawQuad(list, m, s4, s3, e3, e4, colorStart, colorStart, colorEnd, colorEnd); // Bottom
            drawQuad(list, m, s1, s4, e4, e1, colorStart, colorStart, colorEnd, colorEnd); // Right
            drawQuad(list, m, s2, s3, e3, e2, colorStart, colorStart, colorEnd, colorEnd); // Left
        }

        /**
         * Pushes a basic 4-point quad to the buffer with independent vertex colors.
         */
        public static void drawQuad(GeometryList list, Matrix4f m, Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4, int c1, int c2, int c3, int c4) {
            list.add(m, (float) v1.x, (float) v1.y, (float) v1.z, c1);
            list.add(m, (float) v2.x, (float) v2.y, (float) v2.z, c2);
            list.add(m, (float) v3.x, (float) v3.y, (float) v3.z, c3);
            list.add(m, (float) v4.x, (float) v4.y, (float) v4.z, c4);
        }

        /**
         * Generates the 12 spatial coordinates of a perfect Icosahedron (a 20-sided crystal).
         * Pair this with a loop to draw the connecting edges.
         */
        public static Vec3[] generateIcosahedron(float radius) {
            float phi = 1.61803398875F;
            Vec3[] vertices = new Vec3[12];

            vertices[0] = new Vec3(0, 1, phi).normalize().scale(radius);
            vertices[1] = new Vec3(0, 1, -phi).normalize().scale(radius);
            vertices[2] = new Vec3(0, -1, phi).normalize().scale(radius);
            vertices[3] = new Vec3(0, -1, -phi).normalize().scale(radius);

            vertices[4] = new Vec3(1, phi, 0).normalize().scale(radius);
            vertices[5] = new Vec3(-1, phi, 0).normalize().scale(radius);
            vertices[6] = new Vec3(1, -phi, 0).normalize().scale(radius);
            vertices[7] = new Vec3(-1, -phi, 0).normalize().scale(radius);

            vertices[8] = new Vec3(phi, 0, 1).normalize().scale(radius);
            vertices[9] = new Vec3(-phi, 0, 1).normalize().scale(radius);
            vertices[10] = new Vec3(phi, 0, -1).normalize().scale(radius);
            vertices[11] = new Vec3(-phi, 0, -1).normalize().scale(radius);

            return vertices;
        }

        /**
         * Draws a 3D Spiral / Helix.
         */
        public static void drawHelix(GeometryList list, Matrix4f m, float radius, float height, int turns, int segments, float thickness, int color) {
            for (int i = 0; i < segments; i++) {
                float p1 = i / (float) segments;
                float p2 = (i + 1) / (float) segments;

                float angle1 = p1 * Mth.TWO_PI * turns;
                float angle2 = p2 * Mth.TWO_PI * turns;

                Vec3 start = new Vec3(Mth.cos(angle1) * radius, p1 * height, Mth.sin(angle1) * radius);
                Vec3 end = new Vec3(Mth.cos(angle2) * radius, p2 * height, Mth.sin(angle2) * radius);

                drawVolumetricLine(list, m, start, end, thickness, color);
            }
        }

        /**
         * Draws a flat, solid circular ring/halo on the XZ plane.
         */
        public static void drawFlatRing(GeometryList list, Matrix4f m, float innerRadius, float outerRadius, int segments, int color) {
            for (int i = 0; i < segments; i++) {
                float a1 = (i / (float) segments) * Mth.TWO_PI;
                float a2 = ((i + 1) / (float) segments) * Mth.TWO_PI;

                float c1 = Mth.cos(a1), s1 = Mth.sin(a1);
                float c2 = Mth.cos(a2), s2 = Mth.sin(a2);

                Vec3 inner1 = new Vec3(c1 * innerRadius, 0, s1 * innerRadius);
                Vec3 inner2 = new Vec3(c2 * innerRadius, 0, s2 * innerRadius);
                Vec3 outer1 = new Vec3(c1 * outerRadius, 0, s1 * outerRadius);
                Vec3 outer2 = new Vec3(c2 * outerRadius, 0, s2 * outerRadius);

                drawQuad(list, m, inner1, inner2, outer2, outer1, color, color, color, color);
            }
        }

        /**
         * Draws a camera-facing square particle (billboard) that always looks straight at the viewer.
         */
        public static void drawBillboard(GeometryList list, Matrix4f m, Vec3 pos, Vec3 localCamPos, float size, int argbColor) {
            Vec3 toCam = localCamPos.subtract(pos);
            if (toCam.lengthSqr() < 0.0001) {
                toCam = new Vec3(0, 0, 1);
            } else {
                toCam = toCam.normalize();
            }

            Vec3 right = new Vec3(0, 1, 0).cross(toCam);
            if (right.lengthSqr() < 0.0001) {
                right = new Vec3(1, 0, 0);
            } else {
                right = right.normalize().scale(size / 2.0);
            }

            Vec3 up = toCam.cross(right).normalize().scale(size / 2.0);

            Vec3 v1 = pos.subtract(right).add(up); // Top-left
            Vec3 v2 = pos.add(right).add(up); // Top-right
            Vec3 v3 = pos.add(right).subtract(up); // Bottom-right
            Vec3 v4 = pos.subtract(right).subtract(up); // Bottom-left

            Shapes.drawQuad(list, m, v1, v2, v3, v4, argbColor, argbColor, argbColor, argbColor);
        }

        /**
         * Recursively generates a jagged, organic lightning arc between two points.
         * Call this inside your render loop with a time-based seed so the lightning violently crackles!
         */
        public static void drawLightningArc(GeometryList list, Matrix4f m, Vec3 start, Vec3 end, float displacement, int generations, float thickness, int color, long seed) {
            RandomSource random = RandomSource.create(seed);
            drawLightningRecursive(list, m, start, end, displacement, generations, thickness, color, random);
        }

        private static void drawLightningRecursive(GeometryList list, Matrix4f m, Vec3 start, Vec3 end, float displacement,
                                                   int gen, float thickness, int color, RandomSource random) {
            if (gen <= 0) {
                Shapes.drawVolumetricLine(list, m, start, end, thickness, color);
                return;
            }

            Vec3 mid = start.add(end).scale(0.5);

            if (displacement > 0.001f) {
                double offsetX = (random.nextDouble() - 0.5) * displacement;
                double offsetY = (random.nextDouble() - 0.5) * displacement;
                double offsetZ = (random.nextDouble() - 0.5) * displacement;
                mid = mid.add(offsetX, offsetY, offsetZ);
            }

            float nextDisp = displacement * 0.5f;
            drawLightningRecursive(list, m, start, mid, nextDisp, gen - 1, thickness, color, random);
            drawLightningRecursive(list, m, mid, end, nextDisp, gen - 1, thickness, color, random);
        }
    }
}
