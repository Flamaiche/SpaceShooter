package markershape.editor;

import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.camera.EditorCamera;
import markershape.shape.render.ShapeRenderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;

public class PickUtils {
    private ShapeRenderer renderer;
    private EditorCamera camera;
    private int width, height;

    private final FloatBuffer depthBuf = BufferUtils.createFloatBuffer(1);

    public void setRenderer(ShapeRenderer r) { this.renderer = r; }
    public void setCamera(EditorCamera c) { this.camera = c; }
    public void setSize(int w, int h) { width = w; height = h; }
    public EditorCamera getCamera() { return camera; }
    public Matrix4f getProjection() { return camera.getProjection(); }
    public Matrix4f getView() { return camera.getViewMatrix(); }

    public int findVertexAt(float mx, float my) {
        return findVertexAtImpl(mx, my, false);
    }

    public int findVisibleVertexAt(float mx, float my) {
        return findVertexAtImpl(mx, my, true);
    }

    private int findVertexAtImpl(float mx, float my, boolean checkVisibility) {
        if (!renderer.hasShape()) return -1;
        ShapeData data = renderer.getShapeData();
        if (data == null || data.vertices.isEmpty()) return -1;

        Matrix4f mvp = new Matrix4f(camera.getProjection());
        mvp.mul(camera.getViewMatrix());

        float surfaceDepth = -1f;
        if (checkVisibility) {
            surfaceDepth = readDepth(mx, my);
        }

        float bestDist2 = 64f;
        int bestId = -1;
        Vector4f p = new Vector4f();

        for (Vertex v : data.vertices.values()) {
            p.set(v.x, v.y, v.z, 1f).mul(mvp);
            if (p.w <= 0) continue;
            float sx = (p.x / p.w * 0.5f + 0.5f) * width;
            float sy = (1f - (p.y / p.w * 0.5f + 0.5f)) * height;
            float dx = sx - mx;
            float dy = sy - my;
            float d2 = dx * dx + dy * dy;
            if (d2 < bestDist2) {
                if (!checkVisibility) {
                    bestDist2 = d2;
                    bestId = v.id;
                } else {
                    float ndcZ = p.z / p.w;
                    float myDepth = ndcZ * 0.5f + 0.5f;
                    if (myDepth <= surfaceDepth + 0.001f) {
                        bestDist2 = d2;
                        bestId = v.id;
                    }
                }
            }
        }

        return bestId;
    }

    private float readDepth(float mx, float my) {
        int px = Math.round(mx);
        int py = height - Math.round(my) - 1;
        if (px < 0 || px >= width || py < 0 || py >= height) return 1f;
        depthBuf.clear();
        glReadPixels(px, py, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, depthBuf);
        return depthBuf.get(0);
    }

    public int pickEdge(float mx, float my) {
        return pickEdgeImpl(mx, my, false);
    }

    public int pickVisibleEdge(float mx, float my) {
        return pickEdgeImpl(mx, my, true);
    }

    private int pickEdgeImpl(float mx, float my, boolean checkVisibility) {
        if (!renderer.hasShape()) return -1;
        ShapeData data = renderer.getShapeData();
        if (data == null || data.edges.isEmpty()) return -1;

        Matrix4f mvp = new Matrix4f(camera.getProjection());
        mvp.mul(camera.getViewMatrix());

        float surfaceDepth = -1f;
        if (checkVisibility) {
            surfaceDepth = readDepth(mx, my);
        }

        float bestDist = 14f;
        int bestId = -1;
        Vector4f pa = new Vector4f(), pb = new Vector4f(), pm = new Vector4f();

        for (Edge e : data.edges.values()) {
            Vertex va = data.vertices.get(e.a);
            Vertex vb = data.vertices.get(e.b);
            if (va == null || vb == null) continue;

            pa.set(va.x, va.y, va.z, 1f).mul(mvp);
            pb.set(vb.x, vb.y, vb.z, 1f).mul(mvp);
            if (pa.w <= 0 || pb.w <= 0) continue;

            float ax = (pa.x / pa.w * 0.5f + 0.5f) * width;
            float ay = (1f - (pa.y / pa.w * 0.5f + 0.5f)) * height;
            float bx = (pb.x / pb.w * 0.5f + 0.5f) * width;
            float by = (1f - (pb.y / pb.w * 0.5f + 0.5f)) * height;

            float d = pointToSegDist(mx, my, ax, ay, bx, by);
            if (d < bestDist) {
                if (!checkVisibility) {
                    bestDist = d;
                    bestId = e.id;
                } else {
                    pm.set(
                        (va.x + vb.x) * 0.5f,
                        (va.y + vb.y) * 0.5f,
                        (va.z + vb.z) * 0.5f,
                        1f
                    ).mul(mvp);
                    if (pm.w <= 0) continue;
                    float ndcZ = pm.z / pm.w;
                    float myDepth = ndcZ * 0.5f + 0.5f;
                    if (myDepth <= surfaceDepth + 0.001f) {
                        bestDist = d;
                        bestId = e.id;
                    }
                }
            }
        }

        return bestId;
    }

    private float pointToSegDist(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay;
        float lenSq = dx * dx + dy * dy;
        if (lenSq < 1e-8f) return (float) Math.hypot(px - ax, py - ay);
        float t = ((px - ax) * dx + (py - ay) * dy) / lenSq;
        t = Math.max(0f, Math.min(1f, t));
        return (float) Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    public Vector3f getClickWorldPos(float mx, float my) {
        Matrix4f invProjView = new Matrix4f(camera.getProjection());
        invProjView.mul(camera.getViewMatrix());
        invProjView.invert();

        float ndcX = (2f * mx) / width - 1f;
        float ndcY = 1f - (2f * my) / height;

        Vector4f nearP = new Vector4f(ndcX, ndcY, -1f, 1f).mul(invProjView);
        nearP.div(nearP.w);
        Vector4f farP = new Vector4f(ndcX, ndcY, 1f, 1f).mul(invProjView);
        farP.div(farP.w);

        Vector3f rayOrig = new Vector3f(nearP.x, nearP.y, nearP.z);
        Vector3f rayDir = new Vector3f(farP.x - nearP.x, farP.y - nearP.y, farP.z - nearP.z);
        rayDir.normalize();

        if (renderer.hasShape()) {
            ShapeData data = renderer.getShapeData();
            if (data != null && data.faces != null) {
                float bestT = Float.MAX_VALUE;
                Vector3f e1 = new Vector3f(), e2 = new Vector3f(), P = new Vector3f();
                Vector3f T = new Vector3f(), Q = new Vector3f();
                for (int[] poly : data.faces) {
                    for (int j = 0; j + 2 < poly.length; j += 3) {
                        Vertex va = data.vertices.get(poly[j]);
                        Vertex vb = data.vertices.get(poly[j + 1]);
                        Vertex vc = data.vertices.get(poly[j + 2]);
                        if (va == null || vb == null || vc == null) continue;
                        Vector3f a = new Vector3f(va.x, va.y, va.z);
                        Vector3f b = new Vector3f(vb.x, vb.y, vb.z);
                        Vector3f c = new Vector3f(vc.x, vc.y, vc.z);
                        e1.set(b).sub(a);
                        e2.set(c).sub(a);
                        P.set(rayDir).cross(e2);
                        float det = e1.dot(P);
                        if (Math.abs(det) < 1e-12f) continue;
                        float invDet = 1f / det;
                        T.set(rayOrig).sub(a);
                        float u = T.dot(P) * invDet;
                        if (u < 0 || u > 1) continue;
                        Q.set(T).cross(e1);
                        float w = rayDir.dot(Q) * invDet;
                        if (w < 0 || u + w > 1) continue;
                        float t = e2.dot(Q) * invDet;
                        if (t > 1e-5f && t < bestT) bestT = t;
                    }
                }
                if (bestT < Float.MAX_VALUE) {
                    return new Vector3f(rayDir).mul(bestT).add(rayOrig);
                }
            }
        }

        Vector3f target = camera.getTarget();
        Vector3f viewDir = new Vector3f(target).sub(camera.getPosition()).normalize();

        float denom = rayDir.dot(viewDir);
        if (Math.abs(denom) < 1e-6f) return new Vector3f(target);

        float t = (target.dot(viewDir) - rayOrig.dot(viewDir)) / denom;
        return new Vector3f(rayOrig).add(rayDir.mul(t));
    }

    public boolean isNearCrosshair(float mx, float my, Vector3f crosshairPos, float radiusPx) {
        if (crosshairPos == null) return false;
        Matrix4f mvp = new Matrix4f(camera.getProjection());
        mvp.mul(camera.getViewMatrix());
        Vector4f p = new Vector4f(crosshairPos.x, crosshairPos.y, crosshairPos.z, 1f).mul(mvp);
        if (p.w <= 0) return false;
        float sx = (p.x / p.w * 0.5f + 0.5f) * width;
        float sy = (1f - (p.y / p.w * 0.5f + 0.5f)) * height;
        float dx = sx - mx;
        float dy = sy - my;
        return dx * dx + dy * dy <= radiusPx * radiusPx;
    }

    public Vector3f unprojectAtDepth(float mx, float my, float ndcZ) {
        Matrix4f invProjView = new Matrix4f(camera.getProjection());
        invProjView.mul(camera.getViewMatrix());
        invProjView.invert();

        float ndcX = (2f * mx) / width - 1f;
        float ndcY = 1f - (2f * my) / height;

        Vector4f worldP = new Vector4f(ndcX, ndcY, ndcZ, 1f).mul(invProjView);
        worldP.div(worldP.w);
        return new Vector3f(worldP.x, worldP.y, worldP.z);
    }
}
