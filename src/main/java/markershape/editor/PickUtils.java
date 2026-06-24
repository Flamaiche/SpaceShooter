package markershape.editor;

import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.camera.EditorCamera;
import markershape.shape.render.ShapeRenderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class PickUtils {
    private ShapeRenderer renderer;
    private EditorCamera camera;
    private int width, height;

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

    private int findVertexAtImpl(float mx, float my, boolean checkDepth) {
        if (!renderer.hasShape()) return -1;
        ShapeData data = renderer.getShapeData();
        if (data == null || data.vertices.isEmpty()) return -1;

        Matrix4f mvp = new Matrix4f(camera.getProjection());
        mvp.mul(camera.getViewMatrix());

        float bestDist2 = 64f; // 8^2
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
                if (!checkDepth || isDepthAtOrInFront(sx, sy, p)) {
                    bestDist2 = d2;
                    bestId = v.id;
                }
            }
        }

        return bestId;
    }

    public int pickEdge(float mx, float my) {
        return pickEdgeImpl(mx, my, false);
    }

    public int pickVisibleEdge(float mx, float my) {
        return pickEdgeImpl(mx, my, true);
    }

    private int pickEdgeImpl(float mx, float my, boolean checkDepth) {
        if (!renderer.hasShape()) return -1;
        ShapeData data = renderer.getShapeData();
        if (data == null || data.edges.isEmpty()) return -1;

        Matrix4f mvp = new Matrix4f(camera.getProjection());
        mvp.mul(camera.getViewMatrix());

        float bestDist = 14f;
        int bestId = -1;
        Vector4f pa = new Vector4f(), pb = new Vector4f();

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
                if (!checkDepth || isEdgeSegmentVisibleAt(mx, my, ax, ay, bx, by, pa, pb)) {
                    bestDist = d;
                    bestId = e.id;
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

    private float segmentClampT(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay;
        float lenSq = dx * dx + dy * dy;
        if (lenSq < 1e-8f) return 0f;
        float t = ((px - ax) * dx + (py - ay) * dy) / lenSq;
        return Math.max(0f, Math.min(1f, t));
    }

    private boolean isDepthAtOrInFront(float screenX, float screenY, Vector4f clipPos) {
        float ndcZ = clipPos.z / clipPos.w;
        float myDepth = ndcZ * 0.5f + 0.5f;
        int px = Math.round(screenX);
        int py = height - Math.round(screenY) - 1;
        if (px < 0 || px >= width || py < 0 || py >= height) return false;
        FloatBuffer depthBuf = BufferUtils.createFloatBuffer(1);
        glReadPixels(px, py, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, depthBuf);
        float storedDepth = depthBuf.get(0);
        return myDepth <= storedDepth + 0.002f;
    }

    private boolean isEdgeSegmentVisibleAt(float mx, float my,
                                            float ax, float ay, float bx, float by,
                                            Vector4f ca, Vector4f cb) {
        // Check both endpoints first
        if (isDepthAtOrInFront(ax, ay, ca)) return true;
        if (isDepthAtOrInFront(bx, by, cb)) return true;
        // Check at interpolated closest point
        float t = segmentClampT(mx, my, ax, ay, bx, by);
        float px = ax + t * (bx - ax);
        float py = ay + t * (by - ay);
        float ndcZ = (ca.z / ca.w) + t * (cb.z / cb.w - ca.z / ca.w);
        Vector4f interpolated = new Vector4f(0, 0, ndcZ, 1f);
        return isDepthAtOrInFront(px, py, interpolated);
    }

    public Vector3f getClickWorldPos(float mx, float my) {
        Matrix4f invProjView = new Matrix4f(camera.getProjection());
        invProjView.mul(camera.getViewMatrix());
        invProjView.invert();

        float ndcX = (2f * mx) / width - 1f;
        float ndcY = 1f - (2f * my) / height;

        FloatBuffer depthBuf = BufferUtils.createFloatBuffer(1);
        glReadPixels((int) mx, height - (int) my - 1, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, depthBuf);
        float depth = depthBuf.get(0);

        if (depth < 1.0f) {
            Vector4f worldP = new Vector4f(ndcX, ndcY, depth * 2f - 1f, 1f).mul(invProjView);
            worldP.div(worldP.w);
            return new Vector3f(worldP.x, worldP.y, worldP.z);
        }

        Vector4f nearP = new Vector4f(ndcX, ndcY, -1f, 1f).mul(invProjView);
        nearP.div(nearP.w);
        Vector4f farP = new Vector4f(ndcX, ndcY, 1f, 1f).mul(invProjView);
        farP.div(farP.w);

        Vector3f rayOrig = new Vector3f(nearP.x, nearP.y, nearP.z);
        Vector3f rayDir = new Vector3f(farP.x - nearP.x, farP.y - nearP.y, farP.z - nearP.z).normalize();

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
