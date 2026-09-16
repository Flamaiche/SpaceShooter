package markershape.shape.render.point;

import learngl.Shader;
import markershape.shape.ShapeData;
import markershape.shape.render.Cam;
import markershape.shape.render.Renderer;
import markershape.shape.render.TriBuilder;
import markershape.shape.render.TriShape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/** Cursor cross: three axis lines drawn as triangles via the engine. */
public class CrosshairRenderer implements Renderer {
    private static final float PIXELS = 2.5f;

    private final TriShape tri = new TriShape();
    private boolean visible;
    private Vector3f pos;
    private boolean showAxisX = true, showAxisY = true, showAxisZ = true;
    private Matrix4f lastView;
    private boolean dirty = true;

    /** Shows/hides the crosshair. */
    public void setVisible(boolean v) { visible = v; }
    /** Sets the crosshair world position (rebuilds it on the next render). */
    public void setPosition(Vector3f p) {
        pos = p;
        dirty = true;
    }
    /** Shows/hides the X axis line of the crosshair. */
    public void setShowAxisX(boolean v) { showAxisX = v; dirty = true; }
    /** Shows/hides the Y axis line of the crosshair. */
    public void setShowAxisY(boolean v) { showAxisY = v; dirty = true; }
    /** Shows/hides the Z axis line of the crosshair. */
    public void setShowAxisZ(boolean v) { showAxisZ = v; dirty = true; }

    /** Rebuilds and draws the axis lines when the view or crosshair state changed. */
    @Override
    public void render(Shader shader, ShapeData data, Matrix4f view, Matrix4f projection, int screenW, int screenH) {
        if (!visible || pos == null || (!showAxisX && !showAxisY && !showAxisZ)) return;
        boolean viewChanged = lastView == null || !lastView.equals(view);
        if (viewChanged || dirty) {
            dirty = false;
            lastView = new Matrix4f(view);
            Cam cam = Cam.extract(view, projection, screenH);
            float x = pos.x, y = pos.y, z = pos.z;
            float len = 100f;
            float half = cam.halfSize(PIXELS, cam.viewDepth(x, y, z));
            TriBuilder b = new TriBuilder();
            if (half > 0f) {
                if (showAxisX) {
                    b.quad(x - len, y, z, x + len, y, z, 0, 1, 0, half, 1f, 0f, 0f);
                }
                if (showAxisY) {
                    b.quad(x, y - len, z, x, y + len, z, 0, 0, 1, half, 0f, 1f, 0f);
                }
                if (showAxisZ) {
                    b.quad(x, y, z - len, x, y, z + len, 0, 1, 0, half, 0f, 0f, 1f);
                }
            }
            tri.rebuild(b.isEmpty() ? null : b.toFloats());
        }
        if (!tri.hasGeometry()) return;

        glDepthMask(false);
        tri.render();
        glDepthMask(true);
    }

    /** Releases the crosshair geometry. */
    @Override
    public void cleanup() {
        tri.release();
        lastView = null;
        dirty = true;
    }
}