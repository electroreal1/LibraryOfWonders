package com.electro.libraryofwonders.client.rendering.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import java.util.Arrays;

public class GeometryList {
    private float[] pos;
    private int[] col;
    private Matrix4f[] matrices;
    private int size;

    public GeometryList() {
        this(32678);
    }

    public GeometryList(int initialCapacity) {
        this.pos = new float[3 * initialCapacity];
        this.col = new int[initialCapacity];
        this.matrices = new Matrix4f[initialCapacity];
        this.size = 0;
    }

    public void add(Matrix4f m, float x, float y, float z, int argb) {
        if (size == col.length) {
            pos = Arrays.copyOf(pos, pos.length * 2);
            col = Arrays.copyOf(col, col.length * 2);
            matrices = Arrays.copyOf(matrices, matrices.length * 2);
        }
        pos[size * 3] = x;
        pos[size * 3 + 1] = y;
        pos[size * 3 + 2] = z;
        col[size] = argb;
        matrices[size] = m;
        size++;
    }

    public void emit(VertexConsumer vc, Matrix4f fallbackMatrix) {
        for (int i = 0; i < size; i++) {
            int c = col[i];
            Matrix4f m = matrices[i] != null ? matrices[i] : fallbackMatrix;
            vc.addVertex(m, pos[i * 3], pos[i * 3 + 1], pos[i * 3 + 2])
                    .setColor((c >> 16) & 255, (c >> 8) & 255, c & 255, (c >>> 24) & 255);
        }
    }

    public void clear() {
        this.size = 0;
    }
}
