package markershape.editor.action;

import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Geometry helpers for the angles between the edges meeting at a vertex.
 * An angle row pairs two incident edges in angular order around the vertex
 * (the "fan"), so each angle is the planar corner between two consecutive arms.
 */
public class AngleUtils {

    /** One angle of the fan: edges {@code edgeA}/{@code edgeB} share the vertex,
     *  and their outer endpoints are {@code otherA}/{@code otherB}. */
    public record AdjacentAngle(int edgeA, int edgeB, int otherA, int otherB, float degrees) {}

    private AngleUtils() {}

    /** Angle in degrees [0..180] between the two arms starting at {@code v}. */
    public static float angleDegrees(Vector3f v, Vector3f a, Vector3f b) {
        Vector3f ua = new Vector3f(a).sub(v);
        Vector3f ub = new Vector3f(b).sub(v);
        float la = ua.length(), lb = ub.length();
        if (la < 1e-6f || lb < 1e-6f) return 0f;
        float dot = ua.dot(ub) / (la * lb);
        return (float) Math.toDegrees(Math.acos(clamp(dot, -1f, 1f)));
    }

    /**
     * Builds the fan of incident edges around a vertex, sorted by their angular
     * position around the local normal, with the angle of each edge to the next.
     * Returns an empty list if the vertex has fewer than two edges.
     */
    public static List<AdjacentAngle> fan(ShapeData data, int vertexId) {
        ArrayList<AdjacentAngle> out = new ArrayList<>();
        Vertex v = data.vertices.get(vertexId);
        if (v == null) return out;

        ArrayList<int[]> rays = new ArrayList<>(); // {edgeId, otherId}
        for (int eid : v.edgeIds) {
            Edge e = data.edges.get(eid);
            if (e == null) continue;
            int other = e.a == vertexId ? e.b : e.a;
            if (data.vertices.get(other) == null) continue;
            rays.add(new int[]{eid, other});
        }
        if (rays.size() < 2) return out;

        Vector3f normal = averageNormal(data, vertexId, rays);
        Vector3f u = basisU(normal, new Vector3f(1, 0, 0));
        Vector3f w = new Vector3f(normal).cross(u);

        ArrayList<int[]> sorted = new ArrayList<>(rays);
        sorted.sort(Comparator.comparingDouble(r ->
            Math.atan2(dirOf(data, vertexId, r[1]).dot(w), dirOf(data, vertexId, r[1]).dot(u))));

        for (int i = 0; i < sorted.size(); i++) {
            int[] r0 = sorted.get(i);
            int[] r1 = sorted.get((i + 1) % sorted.size());
            float deg = angleDegrees(vec(v), vec(data.vertices.get(r0[1])), vec(data.vertices.get(r1[1])));
            out.add(new AdjacentAngle(r0[0], r1[0], r0[1], r1[1], deg));
        }
        return out;
    }

    /**
     * Rotates the outer endpoint {@code otherB} around the shared vertex, inside
     * the plane defined by the two arms, so that the angle between the pair of
     * edges becomes {@code targetDeg} (clamped to [1..179]).
     * The length of the moved arm is preserved; the reference arm stays fixed.
     */
    public static void setAngle(ShapeData data, int vertexId, int otherA, int otherB, float targetDeg) {
        Vertex v = data.vertices.get(vertexId);
        Vertex a = data.vertices.get(otherA);
        Vertex b = data.vertices.get(otherB);
        if (v == null || a == null || b == null) return;

        Vector3f va = vec(v);
        Vector3f ua = new Vector3f(vec(a)).sub(va);
        Vector3f ubRaw = new Vector3f(vec(b)).sub(va);
        float la = ua.length(), lb = ubRaw.length();
        if (la < 1e-6f || lb < 1e-6f) return;
        ua.normalize();
        Vector3f dir = new Vector3f(ubRaw).normalize();

        Vector3f axis = new Vector3f(ua).cross(dir);
        if (axis.lengthSquared() < 1e-8f) return; // colinéaire : plan indéfini
        axis.normalize();

        float cur = angleDegrees(va, vec(a), vec(b));
        float tgt = clamp(targetDeg, 1f, 179f);
        float delta = (float) Math.toRadians(tgt - cur);
        Quaternionf q = new Quaternionf().rotationAxis(delta, axis);
        Vector3f newDir = q.transform(dir);
        Vector3f nb = new Vector3f(va).add(new Vector3f(newDir).mul(lb));
        b.x = nb.x;
        b.y = nb.y;
        b.z = nb.z;
    }

    private static Vector3f averageNormal(ShapeData data, int vertexId, List<int[]> rays) {
        Vector3f normal = new Vector3f();
        int n = rays.size();
        for (int i = 0; i < n; i++) {
            Vector3f d1 = dirOf(data, vertexId, rays.get(i)[1]);
            Vector3f d2 = dirOf(data, vertexId, rays.get((i + 1) % n)[1]);
            if (d1.lengthSquared() < 1e-6f || d2.lengthSquared() < 1e-6f) continue;
            normal.add(new Vector3f(d1).normalize().cross(d2.normalize()));
        }
        if (normal.lengthSquared() < 1e-6f) normal.set(0, 1, 0);
        else normal.normalize();
        return normal;
    }

    private static Vector3f basisU(Vector3f n, Vector3f init) {
        Vector3f u = new Vector3f(init).sub(new Vector3f(n).mul(init.dot(n)));
        if (u.lengthSquared() < 1e-6f) u.set(0, 0, 1);
        return u.normalize();
    }

    private static Vector3f dirOf(ShapeData data, int vertexId, int otherId) {
        Vertex v = data.vertices.get(vertexId);
        Vertex o = data.vertices.get(otherId);
        if (v == null || o == null) return new Vector3f();
        return new Vector3f(vec(o)).sub(vec(v));
    }

    private static Vector3f vec(Vertex v) {
        return new Vector3f(v.x, v.y, v.z);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}