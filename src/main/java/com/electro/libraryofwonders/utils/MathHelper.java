package com.electro.libraryofwonders.utils;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/**
 * Maths for motion and timing: easing ({@link #easeOutCubic}, {@link #smoothstep}), envelopes ({@link #grow},
 * {@link #fadeInOut}), curves for things that travel ({@link #quadraticBezier}, {@link #cubicBezier},
 * {@link #catmullRom}), random directions ({@link #randomUnit}) and {@link Basis}, a frame round a direction that
 * beams and anything aimed are built on.
 */
public class MathHelper {
    private MathHelper() {}

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

    /** Hermite smoothstep between two edges. */
    public static float smoothstep(float edge0, float edge1, float x) {
        float k = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return k * k * (3.0F - 2.0F * k);
    }

    /** Fast start, soft landing. Great for flashes and shockwave radii. */
    public static float easeOutCubic(float x) {
        float k = 1.0F - Mth.clamp(x, 0.0F, 1.0F);
        return 1.0F - k * k * k;
    }

    /** Gentle in-and-out curve for breathing glows. */
    public static float easeInOutSine(float x) {
        return -(Mth.cos(Mth.PI * Mth.clamp(x, 0.0F, 1.0F)) - 1.0F) / 2.0F;
    }

    /** 0 → 1 over {@code ticks}. The standard "beam grows in" factor. */
    public static float grow(float t, float ticks) {
        return ticks <= 0.0F ? 1.0F : Mth.clamp(t / ticks, 0.0F, 1.0F);
    }

    /**
     * Alpha envelope: fades in over {@code fadeIn}, holds, then fades out over the last {@code fadeOut} ticks of
     * {@code life}.
     */
    public static float fadeInOut(float t, float fadeIn, float life, float fadeOut) {
        float in = fadeIn <= 0.0F ? 1.0F : Mth.clamp(t / fadeIn, 0.0F, 1.0F);
        float out = fadeOut <= 0.0F ? 1.0F : Mth.clamp((life - t) / fadeOut, 0.0F, 1.0F);
        return Math.min(in, out);
    }

    /** Point on a quadratic Bézier (start, control, end) at {@code k} ∈ [0,1]. Used by curved beams. */
    public static Vec3 quadraticBezier(Vec3 a, Vec3 control, Vec3 b, double k) {
        double u = 1.0D - k;
        return a.scale(u * u).add(control.scale(2.0D * u * k)).add(b.scale(k * k));
    }

    /** Point on a cubic Bézier at {@code k} ∈ [0,1]. */
    public static Vec3 cubicBezier(Vec3 a, Vec3 c1, Vec3 c2, Vec3 b, double k) {
        double u = 1.0D - k;
        return a.scale(u * u * u).add(c1.scale(3.0D * u * u * k)).add(c2.scale(3.0D * u * k * k)).add(b.scale(k * k * k));
    }

    /**
     * Point on a Catmull-Rom spline through every point of {@code pts}, at {@code k} ∈ [0,1] along the whole
     * chain. Unlike a Bézier it passes through each control point: lay out a serpent, tail or ribbon by its joints.
     */
    public static Vec3 catmullRom(Vec3[] pts, double k) {
        int n = pts.length - 1;
        double f = Mth.clamp(k, 0.0D, 1.0D) * n;
        int i = Math.min((int) f, n - 1);
        double u = f - i;
        Vec3 p0 = pts[Math.max(i - 1, 0)], p1 = pts[i], p2 = pts[i + 1], p3 = pts[Math.min(i + 2, n)];
        double u2 = u * u, u3 = u2 * u;
        return p1.scale(2.0D)
                .add(p2.subtract(p0).scale(u))
                .add(p0.scale(2.0D).subtract(p1.scale(5.0D)).add(p2.scale(4.0D)).subtract(p3).scale(u2))
                .add(p3.subtract(p0).add(p1.scale(3.0D)).subtract(p2.scale(3.0D)).scale(u3))
                .scale(0.5D);
    }

    /** Uniformly distributed random unit vector. */
    public static Vec3 randomUnit(RandomSource r) {
        double z = r.nextDouble() * 2.0D - 1.0D;
        double a = r.nextDouble() * Math.PI * 2.0D;
        double s = Math.sqrt(1.0D - z * z);
        return new Vec3(s * Math.cos(a), z, s * Math.sin(a));
    }

    /** Rotates {@code v} around a unit {@code axis} by {@code angle} radians (Rodrigues). */
    public static Vec3 rotateAround(Vec3 v, Vec3 axis, double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return v.scale(c).add(axis.cross(v).scale(s)).add(axis.scale(axis.dot(v) * (1.0D - c)));
    }

    /** Linear cone: {@code startRadius} at the muzzle widening to {@code radius} over {@code coneLength} blocks. */
    public static double coneRadius(double startRadius, double radius, double coneLength, double distance) {
        double k = coneLength <= 0.0D ? 1.0D : Mth.clamp(distance / coneLength, 0.0D, 1.0D);
        return startRadius + (radius - startRadius) * k;
    }

    /**
     * An orthonormal frame around a direction: {@code dir} forward, {@code side} and {@code up} spanning the
     * cross-section. Every beam helper uses one of these.
     */
    public record Basis(Vec3 dir, Vec3 side, Vec3 up) {
        /** Builds a stable frame for any direction (falls back to +X when pointing straight up/down). */
        public static Basis of(Vec3 direction) {
            Vec3 d = direction.normalize();
            Vec3 s = d.cross(new Vec3(0.0D, 1.0D, 0.0D));
            if (s.lengthSqr() < 1.0E-4D) {
                s = new Vec3(1.0D, 0.0D, 0.0D);
            }
            s = s.normalize();
            return new Basis(d, s, s.cross(d).normalize());
        }

        /** Offset of length {@code radius} at {@code angle} around the forward axis. */
        public Vec3 around(double angle, double radius) {
            return side.scale(Math.cos(angle) * radius).add(up.scale(Math.sin(angle) * radius));
        }

        /** Point {@code along} blocks forward from {@code origin}, pushed out by {@code around(angle, radius)}. */
        public Vec3 at(Vec3 origin, double along, double angle, double radius) {
            return origin.add(dir.scale(along)).add(around(angle, radius));
        }
    }
}
