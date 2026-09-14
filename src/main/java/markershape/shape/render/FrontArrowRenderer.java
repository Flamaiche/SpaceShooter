package markershape.shape.render;

import learngl.Shader;
import markershape.shape.ShapeData;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Renders a 3D arrow showing the shape's fixed "front" direction, built as
 * triangles (shaft quad + pyramid head) through the engine.
 */
public class FrontArrowRenderer implements Renderer {
    private final TriShape tri = new TriShape();
    private final Vector3f thick = new Vector3f();
    private boolean visible;
    private float cx, cy, cz;
    private Vector3f dir = new Vector3f(0, 0, -1);
    private float length = 1f;
    private Matrix4f lastView;
    private boolean dirty = true;

    public void setVisible(boolean v) { visible = v; }
    public boolean isVisible() { return visible; }

    public void setArrow(float cx, float cy, float cz, Vector3f direction, float arrowLength) {
        this.cx = cx; this.cy = cy; this.cz = cz;
        this.dir = new Vector3f(direction);
        if (dir.lengthSquared() > 1e-6f) dir.normalize();
        this.length = arrowLength > 0 ? arrowLength : 1f;
        dirty = true;
    }

    @Override
    public void render(Shader shader, ShapeData data, Matrix4f view, Matrix4f projection, int screenW, int screenH) {
        if (!visible) return;
        boolean viewChanged = lastView == null || !lastView.equals(view);
        if (viewChanged || dirty) {
            dirty = false;
            Cam cam = Cam.extract(view, projection, screenH);
            TriBuilder b = new TriBuilder();

            float ex = cx + dir.x * length;
            float ey = cy + dir.y * length;
            float ez = cz + dir.z * length;

            Vector3f perp = new Vector3f(dir).cross(new Vector3f(0, 1, 0));
            if (perp.lengthSquared() < 1e-6f) perp.set(dir).cross(new Vector3f(1, 0, 0));
            perp.normalize();
            Vector3f upV = new Vector3f(perp).cross(dir).normalize();

            float s = length * 0.18f;
            cam.thickness(cx, cy, cz, ex, ey, ez, thick);
            float shaftHalf = cam.halfSize(3f, cam.viewDepth((cx + ex) * 0.5f, (cy + ey) * 0.5f, (cz + ez) * 0.5f));
            b.quad(cx, cy, cz, ex, ey, ez, thick.x, thick.y, thick.z, shaftHalf, 1f, 0.8f, 0.2f);

            float headLen = length * 0.25f;
            float tx = ex + dir.x * headLen, ty = ey + dir.y * headLen, tz = ez + dir.z * headLen;
            float p1x = ex + perp.x * s, p1y = ey + perp.y * s, p1z = ez + perp.z * s;
            float p2x = ex - perp.x * s, p2y = ey - perp.y * s, p2z = ez - perp.z * s;
            float q1x = ex + upV.x * s, q1y = ey + upV.y * s, q1z = ez + upV.z * s;
            float q2x = ex - upV.x * s, q2y = ey - upV.y * s, q2z = ez - upV.z * s;

            b.tri(p1x, p1y, p1z, tx, ty, tz, q1x, q1y, q1z, 1f, 0.8f, 0.2f);
            b.tri(q1x, q1y, q1z, tx, ty, tz, p2x, p2y, p2z, 1f, 0.8f, 0.2f);
            b.tri(p2x, p2y, p2z, tx, ty, tz, q2x, q2y, q2z, 1f, 0.8f, 0.2f);
            b.tri(q2x, q2y, q2z, tx, ty, tz, p1x, p1y, p1z, 1f, 0.8f, 0.2f);

            tri.rebuild(b.isEmpty() ? null : b.toFloats());
            lastView = new Matrix4f(view);
        }
        if (!tri.hasGeometry()) return;

        glDepthMask(false);
        tri.render();
        glDepthMask(true);
    }

    @Override
    public void cleanup() {
        tri.release();
        lastView = null;
        dirty = true;
    }
}