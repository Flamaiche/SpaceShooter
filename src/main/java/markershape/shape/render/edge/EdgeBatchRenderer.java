package markershape.shape.render.edge;

import learngl.Shader;
import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.shape.render.Cam;
import markershape.shape.render.TriBuilder;
import markershape.shape.render.TriShape;
import markershape.shape.render.Renderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Renders the mesh edges as quads (two triangles each) whose thickness follows
 * the configured line width in screen pixels, through a {@link learngl.shape.Shape}.
 */
public class EdgeBatchRenderer implements Renderer {
    private final TriShape tri = new TriShape();
    private final Vector3f thick = new Vector3f();
    private float lineWidth = 3f;
    private Matrix4f lastView;
    private float lastWidth = -1f;

    public void setLineWidth(float w) { lineWidth = w; }

    @Override
    public void render(Shader shader, ShapeData data, Matrix4f view, Matrix4f projection, int screenW, int screenH) {
        if (data == null || data.edges.isEmpty()) return;

        boolean viewChanged = lastView == null || !lastView.equals(view);
        boolean widthChanged = lastWidth != lineWidth;
        if (viewChanged || widthChanged) {
            Cam cam = Cam.extract(view, projection, screenH);
            TriBuilder b = new TriBuilder();
            for (Edge e : data.edges.values()) {
                Vertex va = data.vertices.get(e.a);
                Vertex vb = data.vertices.get(e.b);
                if (va == null || vb == null) continue;
                float mx = (va.x + vb.x) * 0.5f, my = (va.y + vb.y) * 0.5f, mz = (va.z + vb.z) * 0.5f;
                float depth = cam.viewDepth(mx, my, mz);
                if (depth <= 1e-5f) continue;
                float half = cam.halfSize(lineWidth, depth);
                if (half <= 0f) continue;
                cam.thickness(va.x, va.y, va.z, vb.x, vb.y, vb.z, thick);
                b.quad(va.x, va.y, va.z, vb.x, vb.y, vb.z,
                    thick.x, thick.y, thick.z, half, e.r, e.g, e.bl);
            }
            tri.rebuild(b.isEmpty() ? null : b.toFloats());
            lastView = new Matrix4f(view);
            lastWidth = lineWidth;
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
        lastWidth = -1f;
    }
}