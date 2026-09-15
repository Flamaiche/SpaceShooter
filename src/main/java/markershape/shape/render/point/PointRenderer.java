package markershape.shape.render.point;

import learngl.Shader;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.shape.render.Cam;
import markershape.shape.render.Renderer;
import markershape.shape.render.TriBuilder;
import markershape.shape.render.TriShape;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Renders every mesh vertex as a camera-facing quad (two triangles), so the
 * points always keep their configured on-screen size regardless of depth —
 * through a {@link learngl.shape.Shape} exactly like the faces.
 */
public class PointRenderer implements Renderer {
    private final TriShape tri = new TriShape();
    private float pointSize = 5f;
    private Matrix4f lastView;
    private float lastSize = -1f;

    public void setPointSize(float s) { pointSize = s; }
    public float getPointSize() { return pointSize; }

    @Override
    public void render(Shader shader, ShapeData data, Matrix4f view, Matrix4f projection, int screenW, int screenH) {
        if (data == null || data.vertices.isEmpty()) return;

        boolean viewChanged = lastView == null || !lastView.equals(view);
        boolean sizeChanged = lastSize != pointSize;
        if (viewChanged || sizeChanged) {
            Cam cam = Cam.extract(view, projection, screenH);
            TriBuilder b = new TriBuilder();
            for (Vertex v : data.vertices.values()) {
                float depth = cam.viewDepth(v.x, v.y, v.z);
                if (depth <= 1e-5f) continue;
                float half = cam.halfSize(pointSize, depth);
                if (half <= 0f) continue;
                b.pquad(v.x, v.y, v.z,
                    cam.right.x, cam.right.y, cam.right.z,
                    cam.up.x, cam.up.y, cam.up.z,
                    half, 0.9f, 0.9f, 0.9f);
            }
            tri.rebuild(b.isEmpty() ? null : b.toFloats());
            lastView = new Matrix4f(view);
            lastSize = pointSize;
        }
        if (!tri.hasGeometry()) return;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);
        shader.setUniform1f("uAlpha", 1f);
        tri.render();
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    @Override
    public void cleanup() {
        tri.release();
        lastView = null;
        lastSize = -1f;
    }
}