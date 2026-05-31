package learngl.shape;

import learngl.VertexUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Provides static methods for mesh-level collision detection.
 * Supports AABB-based broad-phase culling followed by triangle-triangle
 * intersection tests, as well as ray-mesh intersection queries.
 */
public final class MeshCollider {

    private static final float EPSILON = 1e-6f;

    private MeshCollider() {}

    /**
     * Tests whether two meshes, each transformed by their respective model matrices,
     * intersect. First performs an AABB overlap check, then a detailed
     * triangle-triangle intersection for potentially overlapping triangles.
     *
     * @param vertsA vertex data of the first mesh
     * @param vertsB vertex data of the second mesh
     * @param modelA model matrix for the first mesh
     * @param modelB model matrix for the second mesh
     * @return true if any triangles intersect
     */
    public static boolean intersectsOptimized(float[] vertsA, float[] vertsB,
                                               Matrix4f modelA, Matrix4f modelB) {
        float[] mbA = new float[6];
        float[] mbB = new float[6];
        computeBounds(vertsA, mbA);
        computeBounds(vertsB, mbB);

        float[] wbA = new float[6];
        float[] wbB = new float[6];
        transformBounds(mbA, modelA, wbA);
        transformBounds(mbB, modelB, wbB);

        if (wbA[3] < wbB[0] || wbA[0] > wbB[3] ||
            wbA[4] < wbB[1] || wbA[1] > wbB[4] ||
            wbA[5] < wbB[2] || wbA[2] > wbB[5]) return false;

        int countA = vertsA.length / VertexUtils.FLOATS_PER_VERTEX;
        int countB = vertsB.length / VertexUtils.FLOATS_PER_VERTEX;
        Vector3f[] triA = {new Vector3f(), new Vector3f(), new Vector3f()};
        Vector3f[] triB = {new Vector3f(), new Vector3f(), new Vector3f()};
        for (int i = 0; i < countA; i += 3) {
            readTri(vertsA, i, modelA, triA);
            for (int j = 0; j < countB; j += 3) {
                readTri(vertsB, j, modelB, triB);
                if (triTriIntersect(triA[0], triA[1], triA[2], triB[0], triB[1], triB[2]))
                    return true;
            }
        }
        return false;
    }

    /**
     * Casts a ray against a mesh transformed by the given model matrix and returns
     * the distance to the nearest intersection, or -1 if no intersection occurs.
     *
     * @param vertices vertex data of the mesh
     * @param origin   ray origin in world space
     * @param dir      ray direction in world space
     * @param model    model matrix for the mesh
     * @return the distance to the nearest intersection, or -1 if none
     */
    public static float intersectRayDistance(float[] vertices, Vector3f origin,
                                               Vector3f dir, Matrix4f model) {
        float[] mb = new float[6];
        computeBounds(vertices, mb);
        float[] wb = new float[6];
        transformBounds(mb, model, wb);

        float[] min = {wb[0], wb[1], wb[2]};
        float[] max = {wb[3], wb[4], wb[5]};
        if (!rayIntersectsAABB(origin, dir, min, max)) return -1f;

        int count = vertices.length / VertexUtils.FLOATS_PER_VERTEX;
        float minT = Float.MAX_VALUE;
        boolean hit = false;
        Vector3f[] tri = {new Vector3f(), new Vector3f(), new Vector3f()};
        for (int i = 0; i < count; i += 3) {
            readTri(vertices, i, model, tri);
            float t = rayIntersectsTriangleDistance(origin, dir, tri[0], tri[1], tri[2]);
            if (t >= 0 && t < minT) {
                minT = t;
                hit = true;
            }
        }
        return hit ? minT : -1f;
    }

    private static void computeBounds(float[] verts, float[] out) {
        out[0] = out[1] = out[2] = Float.MAX_VALUE;
        out[3] = out[4] = out[5] = -Float.MAX_VALUE;
        int count = verts.length / VertexUtils.FLOATS_PER_VERTEX;
        for (int i = 0; i < count; i++) {
            int off = i * VertexUtils.FLOATS_PER_VERTEX;
            float x = verts[off], y = verts[off + 1], z = verts[off + 2];
            if (x < out[0]) out[0] = x;
            if (y < out[1]) out[1] = y;
            if (z < out[2]) out[2] = z;
            if (x > out[3]) out[3] = x;
            if (y > out[4]) out[4] = y;
            if (z > out[5]) out[5] = z;
        }
    }

    private static void transformBounds(float[] mb, Matrix4f model, float[] wb) {
        Vector3f tmp = new Vector3f();
        wb[0] = wb[1] = wb[2] = Float.MAX_VALUE;
        wb[3] = wb[4] = wb[5] = -Float.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            float x = (i & 1) == 0 ? mb[0] : mb[3];
            float y = (i & 2) == 0 ? mb[1] : mb[4];
            float z = (i & 4) == 0 ? mb[2] : mb[5];
            tmp.set(x, y, z).mulPosition(model);
            if (tmp.x < wb[0]) wb[0] = tmp.x;
            if (tmp.y < wb[1]) wb[1] = tmp.y;
            if (tmp.z < wb[2]) wb[2] = tmp.z;
            if (tmp.x > wb[3]) wb[3] = tmp.x;
            if (tmp.y > wb[4]) wb[4] = tmp.y;
            if (tmp.z > wb[5]) wb[5] = tmp.z;
        }
    }

    private static void readTri(float[] verts, int triIndex, Matrix4f model, Vector3f[] tri) {
        int off = triIndex * VertexUtils.FLOATS_PER_VERTEX;
        tri[0].set(verts[off], verts[off + 1], verts[off + 2]).mulPosition(model);
        off += VertexUtils.FLOATS_PER_VERTEX;
        tri[1].set(verts[off], verts[off + 1], verts[off + 2]).mulPosition(model);
        off += VertexUtils.FLOATS_PER_VERTEX;
        tri[2].set(verts[off], verts[off + 1], verts[off + 2]).mulPosition(model);
    }

    private static float comp(Vector3f v, int axis) {
        switch (axis) {
            case 0: return v.x;
            case 1: return v.y;
            default: return v.z;
        }
    }

    private static boolean triTriIntersect(Vector3f V0, Vector3f V1, Vector3f V2,
                                            Vector3f U0, Vector3f U1, Vector3f U2) {
        Vector3f e1 = new Vector3f();
        Vector3f e2 = new Vector3f();
        Vector3f n1 = new Vector3f();
        Vector3f n2 = new Vector3f();
        Vector3f tmp = new Vector3f();

        V1.sub(V0, e1);
        V2.sub(V0, e2);
        e1.cross(e2, n1);
        if (n1.length() < EPSILON) return false;

        U1.sub(U0, e1);
        U2.sub(U0, e2);
        e1.cross(e2, n2);
        if (n2.length() < EPSILON) return false;

        U0.sub(V0, tmp);
        float du0 = n1.dot(tmp);
        U1.sub(V0, tmp);
        float du1 = n1.dot(tmp);
        U2.sub(V0, tmp);
        float du2 = n1.dot(tmp);
        if (Math.abs(du0) < EPSILON) du0 = 0;
        if (Math.abs(du1) < EPSILON) du1 = 0;
        if (Math.abs(du2) < EPSILON) du2 = 0;
        if (du0 * du1 > 0 && du0 * du2 > 0) return false;

        V0.sub(U0, tmp);
        float dv0 = n2.dot(tmp);
        V1.sub(U0, tmp);
        float dv1 = n2.dot(tmp);
        V2.sub(U0, tmp);
        float dv2 = n2.dot(tmp);
        if (Math.abs(dv0) < EPSILON) dv0 = 0;
        if (Math.abs(dv1) < EPSILON) dv1 = 0;
        if (Math.abs(dv2) < EPSILON) dv2 = 0;
        if (dv0 * dv1 > 0 && dv0 * dv2 > 0) return false;

        n1.cross(n2, tmp);
        int max = Math.abs(tmp.x) > Math.abs(tmp.y) ? 0 : 1;
        max = Math.abs(comp(tmp, 2)) > Math.abs(comp(tmp, max)) ? 2 : max;

        float vMin, vMax, uMin, uMax;
        vMin = vMax = comp(V0, max);
        vMin = Math.min(vMin, comp(V1, max));
        vMin = Math.min(vMin, comp(V2, max));
        vMax = Math.max(vMax, comp(V1, max));
        vMax = Math.max(vMax, comp(V2, max));
        uMin = uMax = comp(U0, max);
        uMin = Math.min(uMin, comp(U1, max));
        uMin = Math.min(uMin, comp(U2, max));
        uMax = Math.max(uMax, comp(U1, max));
        uMax = Math.max(uMax, comp(U2, max));

        return vMin <= uMax && uMin <= vMax;
    }

    private static boolean rayIntersectsAABB(Vector3f origin, Vector3f dir, float[] min, float[] max) {
        float tmin = (min[0] - origin.x) / dir.x;
        float tmax = (max[0] - origin.x) / dir.x;
        if (tmin > tmax) { float t = tmin; tmin = tmax; tmax = t; }

        float tymin = (min[1] - origin.y) / dir.y;
        float tymax = (max[1] - origin.y) / dir.y;
        if (tymin > tymax) { float t = tymin; tymin = tymax; tymax = t; }

        if ((tmin > tymax) || (tymin > tmax)) return false;
        if (tymin > tmin) tmin = tymin;
        if (tymax < tmax) tmax = tymax;

        float tzmin = (min[2] - origin.z) / dir.z;
        float tzmax = (max[2] - origin.z) / dir.z;
        if (tzmin > tzmax) { float t = tzmin; tzmin = tzmax; tzmax = t; }

        return !(tmin > tzmax || tzmin > tmax);
    }

    private static float rayIntersectsTriangleDistance(Vector3f origin, Vector3f dir,
                                                        Vector3f v0, Vector3f v1, Vector3f v2) {
        Vector3f edge1 = new Vector3f();
        Vector3f edge2 = new Vector3f();
        v1.sub(v0, edge1);
        v2.sub(v0, edge2);

        Vector3f h = new Vector3f();
        dir.cross(edge2, h);
        float a = edge1.dot(h);
        if (Math.abs(a) < 1e-6f) return -1;

        float f = 1.0f / a;
        Vector3f s = new Vector3f(origin).sub(v0);
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
