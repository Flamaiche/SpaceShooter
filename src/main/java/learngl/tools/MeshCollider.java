package learngl.tools;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class MeshCollider {

    private static final float EPSILON = 1e-6f;

    private MeshCollider() {}

    public static boolean intersectsOptimized(float[] vertsA, float[] vertsB, Matrix4f modelA, Matrix4f modelB) {
        float[] ta = applyTransform(vertsA, modelA);
        float[] tb = applyTransform(vertsB, modelB);

        int countA = ta.length / VertexUtils.FLOATS_PER_VERTEX;
        int countB = tb.length / VertexUtils.FLOATS_PER_VERTEX;

        float[] minA = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] maxA = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        float[] minB = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] maxB = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};

        for (int i = 0; i < countA; i++) {
            for (int j = 0; j < 3; j++) {
                float v = ta[i * VertexUtils.FLOATS_PER_VERTEX + j];
                minA[j] = Math.min(minA[j], v);
                maxA[j] = Math.max(maxA[j], v);
            }
        }
        for (int i = 0; i < countB; i++) {
            for (int j = 0; j < 3; j++) {
                float v = tb[i * VertexUtils.FLOATS_PER_VERTEX + j];
                minB[j] = Math.min(minB[j], v);
                maxB[j] = Math.max(maxB[j], v);
            }
        }

        if (maxA[0] < minB[0] || minA[0] > maxB[0] ||
            maxA[1] < minB[1] || minA[1] > maxB[1] ||
            maxA[2] < minB[2] || minA[2] > maxB[2]) return false;

        for (int i = 0; i < countA; i += 3) {
            for (int j = 0; j < countB; j += 3) {
                if (triTriIntersectRaw(ta, i, tb, j)) return true;
            }
        }
        return false;
    }

    public static float intersectRayDistance(float[] vertices, Vector3f origin, Vector3f dir, Matrix4f model) {
        float[] transformed = applyTransform(vertices, model);
        int count = transformed.length / VertexUtils.FLOATS_PER_VERTEX;

        float[] min = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        float[] max = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < 3; j++) {
                float v = transformed[i * VertexUtils.FLOATS_PER_VERTEX + j];
                min[j] = Math.min(min[j], v);
                max[j] = Math.max(max[j], v);
            }
        }

        if (!rayIntersectsAABB(origin, dir, min, max)) return -1f;

        float minT = Float.MAX_VALUE;
        boolean hit = false;
        for (int i = 0; i < count; i += 3) {
            float[] v0 = {transformed[i * VertexUtils.FLOATS_PER_VERTEX],     transformed[i * VertexUtils.FLOATS_PER_VERTEX + 1], transformed[i * VertexUtils.FLOATS_PER_VERTEX + 2]};
            float[] v1 = {transformed[(i+1) * VertexUtils.FLOATS_PER_VERTEX], transformed[(i+1) * VertexUtils.FLOATS_PER_VERTEX + 1], transformed[(i+1) * VertexUtils.FLOATS_PER_VERTEX + 2]};
            float[] v2 = {transformed[(i+2) * VertexUtils.FLOATS_PER_VERTEX], transformed[(i+2) * VertexUtils.FLOATS_PER_VERTEX + 1], transformed[(i+2) * VertexUtils.FLOATS_PER_VERTEX + 2]};
            float t = rayIntersectsTriangleDistance(origin, dir, v0, v1, v2);
            if (t >= 0 && t < minT) {
                minT = t;
                hit = true;
            }
        }
        return hit ? minT : -1f;
    }

    private static float[] applyTransform(float[] vertices, Matrix4f model) {
        float[] transformed = vertices.clone();
        Vector3f tmp = new Vector3f();
        for (int i = 0; i < vertices.length / VertexUtils.FLOATS_PER_VERTEX; i++) {
            tmp.set(vertices[i * VertexUtils.FLOATS_PER_VERTEX],
                    vertices[i * VertexUtils.FLOATS_PER_VERTEX + 1],
                    vertices[i * VertexUtils.FLOATS_PER_VERTEX + 2]);
            tmp.mulPosition(model);
            transformed[i * VertexUtils.FLOATS_PER_VERTEX]     = tmp.x;
            transformed[i * VertexUtils.FLOATS_PER_VERTEX + 1] = tmp.y;
            transformed[i * VertexUtils.FLOATS_PER_VERTEX + 2] = tmp.z;
        }
        return transformed;
    }

    private static float[] sub(float[] a, float[] b) {
        return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static float[] cross(float[] a, float[] b) {
        return new float[]{a[1]*b[2] - a[2]*b[1],
                           a[2]*b[0] - a[0]*b[2],
                           a[0]*b[1] - a[1]*b[0]};
    }

    private static float dot(float[] a, float[] b) {
        return a[0]*b[0] + a[1]*b[1] + a[2]*b[2];
    }

    private static float vecLength(float[] v) {
        return (float) Math.sqrt(dot(v, v));
    }

    private static float[] projectOnAxisRaw(int axis, float[] v0, float[] v1, float[] v2) {
        float p0 = v0[axis], p1 = v1[axis], p2 = v2[axis];
        return new float[]{Math.min(p0, Math.min(p1, p2)), Math.max(p0, Math.max(p1, p2))};
    }

    private static boolean intervalsOverlap(float min1, float max1, float min2, float max2) {
        return !(max1 < min2 || max2 < min1);
    }

    private static boolean triTriIntersectRaw(float[] V, int vi, float[] U, int ui) {
        float[] V0 = {V[vi * VertexUtils.FLOATS_PER_VERTEX],     V[vi * VertexUtils.FLOATS_PER_VERTEX + 1],     V[vi * VertexUtils.FLOATS_PER_VERTEX + 2]};
        float[] V1 = {V[(vi+1) * VertexUtils.FLOATS_PER_VERTEX], V[(vi+1) * VertexUtils.FLOATS_PER_VERTEX + 1], V[(vi+1) * VertexUtils.FLOATS_PER_VERTEX + 2]};
        float[] V2 = {V[(vi+2) * VertexUtils.FLOATS_PER_VERTEX], V[(vi+2) * VertexUtils.FLOATS_PER_VERTEX + 1], V[(vi+2) * VertexUtils.FLOATS_PER_VERTEX + 2]};
        float[] U0 = {U[ui * VertexUtils.FLOATS_PER_VERTEX],     U[ui * VertexUtils.FLOATS_PER_VERTEX + 1],     U[ui * VertexUtils.FLOATS_PER_VERTEX + 2]};
        float[] U1 = {U[(ui+1) * VertexUtils.FLOATS_PER_VERTEX], U[(ui+1) * VertexUtils.FLOATS_PER_VERTEX + 1], U[(ui+1) * VertexUtils.FLOATS_PER_VERTEX + 2]};
        float[] U2 = {U[(ui+2) * VertexUtils.FLOATS_PER_VERTEX], U[(ui+2) * VertexUtils.FLOATS_PER_VERTEX + 1], U[(ui+2) * VertexUtils.FLOATS_PER_VERTEX + 2]};

        float[] N1 = cross(sub(V1, V0), sub(V2, V0));
        float[] N2 = cross(sub(U1, U0), sub(U2, U0));

        if (vecLength(N1) == 0 || vecLength(N2) == 0) return false;

        float du0 = dot(N1, sub(U0, V0));
        float du1 = dot(N1, sub(U1, V0));
        float du2 = dot(N1, sub(U2, V0));
        float dv0 = dot(N2, sub(V0, U0));
        float dv1 = dot(N2, sub(V1, U0));
        float dv2 = dot(N2, sub(V2, U0));

        if (Math.abs(du0) < EPSILON) du0 = 0;
        if (Math.abs(du1) < EPSILON) du1 = 0;
        if (Math.abs(du2) < EPSILON) du2 = 0;
        if (Math.abs(dv0) < EPSILON) dv0 = 0;
        if (Math.abs(dv1) < EPSILON) dv1 = 0;
        if (Math.abs(dv2) < EPSILON) dv2 = 0;

        if (du0 * du1 > 0 && du0 * du2 > 0) return false;
        if (dv0 * dv1 > 0 && dv0 * dv2 > 0) return false;

        float[] D = cross(N1, N2);
        int max = Math.abs(D[0]) > Math.abs(D[1]) ? 0 : 1;
        max = Math.abs(D[2]) > Math.abs(D[max]) ? 2 : max;

        float[] tri1 = projectOnAxisRaw(max, V0, V1, V2);
        float[] tri2 = projectOnAxisRaw(max, U0, U1, U2);
        return intervalsOverlap(tri1[0], tri1[1], tri2[0], tri2[1]);
    }

    private static boolean rayIntersectsAABB(Vector3f origin, Vector3f dir, float[] min, float[] max) {
        float tmin = (min[0] - origin.x) / dir.x;
        float tmax = (max[0] - origin.x) / dir.x;
        if (tmin > tmax) { float tmp = tmin; tmin = tmax; tmax = tmp; }

        float tymin = (min[1] - origin.y) / dir.y;
        float tymax = (max[1] - origin.y) / dir.y;
        if (tymin > tymax) { float tmp = tymin; tymin = tymax; tymax = tmp; }

        if ((tmin > tymax) || (tymin > tmax)) return false;
        tmin = Math.max(tmin, tymin);
        tmax = Math.min(tmax, tymax);

        float tzmin = (min[2] - origin.z) / dir.z;
        float tzmax = (max[2] - origin.z) / dir.z;
        if (tzmin > tzmax) { float tmp = tzmin; tzmin = tzmax; tzmax = tmp; }

        return !(tmin > tzmax || tzmin > tmax);
    }

    private static float rayIntersectsTriangleDistance(Vector3f origin, Vector3f dir, float[] v0, float[] v1, float[] v2) {
        Vector3f edge1 = new Vector3f(v1[0]-v0[0], v1[1]-v0[1], v1[2]-v0[2]);
        Vector3f edge2 = new Vector3f(v2[0]-v0[0], v2[1]-v0[1], v2[2]-v0[2]);
        Vector3f h = new Vector3f();
        dir.cross(edge2, h);
        float a = edge1.dot(h);
        if (Math.abs(a) < 1e-6f) return -1;

        Vector3f s = new Vector3f(origin.x - v0[0], origin.y - v0[1], origin.z - v0[2]);
        float f = 1.0f / a;
        float u = f * s.dot(h);
        if (u < 0.0f || u > 1.0f) return -1;

        Vector3f q = new Vector3f();
        s.cross(edge1, q);
        float v = f * dir.dot(q);
        if (v < 0.0f || u + v > 1.0f) return -1;

        float t = f * edge2.dot(q);
        return t > 0 ? t : -1;
    }
}
