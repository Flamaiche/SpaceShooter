package markershape.shape.render.point;

import learngl.Shader;
import markershape.shape.ShapeData;
import markershape.shape.render.Cam;
import markershape.shape.render.Renderer;
import markershape.shape.render.TriBuilder;
import markershape.shape.render.TriShape;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Semi-transparent placement ghost rendered as a camera-facing quad
 * (two triangles), shown before the user validates a vertex placement click.
 */
public class GhostPointRenderer implements Renderer {
    private final TriShape tri = new TriShape();
    private boolean visible;
    private float px, py, pz;
    private float pointSize = 5f;
    private Matrix4f lastView;
    private float lastSize = -1f;
    private boolean lastVisible = false;

    public void setVisible(boolean v) { visible = v; }
    public void setPosition(float x, float y, float z) { px = x; py = y; pz = z; }
    public void setPointSize(float s) { pointSize = s; }

    private boolean positionChanged(float x, float y, float z) {
        return x != lastPx || y != lastPy || z != lastPz;
    }
    private float lastPx = Float.MAX_VALUE, lastPy = Float.MAX_VALUE, lastPz = Float.MAX_VALUE;

    @Override
    public void render(Shader shader, ShapeData data, Matrix4f view, Matrix4f projection, int screenW, int screenH) {
        if (!visible) return;

        boolean viewChanged = lastView == null || !lastView.equals(view);
        boolean sizeChanged = lastSize != pointSize || !lastVisible;
        boolean posChanged = positionChanged(px, py, pz);
        if (viewChanged || sizeChanged || posChanged) {
            Cam cam = Cam.extract(view, projection, screenH);
            float depth = cam.viewDepth(px, py, pz);
            if (depth <= 1e-5f) {
                tri.rebuild(null);
            } else {
                float half = cam.halfSize(pointSize, depth);
                TriBuilder b = new TriBuilder();
                if (half > 0f) {
                    b.pquad(px, py, pz,
                        cam.right.x, cam.right.y, cam.right.z,
                        cam.up.x, cam.up.y, cam.up.z,
                        half, 1f, 0.85f, 0.3f);
                }
                tri.rebuild(b.isEmpty() ? null : b.toFloats());
            }
            lastView = new Matrix4f(view);
            lastSize = pointSize;
            lastVisible = true;
            lastPx = px;
            lastPy = py;
            lastPz = pz;
        }
        if (!tri.hasGeometry()) return;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);
        shader.setUniform1f("uAlpha", 0.55f);
        tri.render();
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    @Override
    public void cleanup() {
        tri.release();
        lastView = null;
        lastSize = -1f;
        lastVisible = false;
        lastPx = Float.MAX_VALUE;
        lastPy = Float.MAX_VALUE;
        lastPz = Float.MAX_VALUE;
    }
}