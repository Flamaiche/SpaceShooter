package markershape.shape.render;

import org.joml.Matrix4f;

/** Renders the editor grid and axis lines as real triangles via the engine. */
public class GridRenderer {
    private static final float GRID_PIXELS = 1.2f;
    private static final float AXIS_PIXELS = 2.5f;

    private final TriShape tri = new TriShape();
    private boolean showAxisX = true, showAxisY = true, showAxisZ = true;
    private boolean gridVisible = true;
    private float gridStep = 1.0f;
    private Matrix4f lastView;
    private boolean dirty = true;

    public void setGridVisible(boolean v) { gridVisible = v; }
    public boolean isGridVisible() { return gridVisible; }

    public void setShowAxisX(boolean v) { showAxisX = v; dirty = true; }
    public void setShowAxisY(boolean v) { showAxisY = v; dirty = true; }
    public void setShowAxisZ(boolean v) { showAxisZ = v; dirty = true; }
    public boolean isShowAxisX() { return showAxisX; }
    public boolean isShowAxisY() { return showAxisY; }
    public boolean isShowAxisZ() { return showAxisZ; }
    public boolean anyVisible() { return showAxisX || showAxisY || showAxisZ; }

    public void setGridStep(float step) {
        if (step <= 0f) step = 0.1f;
        gridStep = step;
        dirty = true;
    }

    public void rebuild() {
        dirty = true;
    }

    private void buildTriangles(Cam cam, float gridPx, float axisPx) {
        final float targetHalfSize = 10f;
        final float step = gridStep;
        int n = (int) (targetHalfSize / step);
        final float halfSize = n * step;

        TriBuilder b = new TriBuilder();
        float cR = 0.2f, cG = 0.2f, cB = 0.3f;

        // YZ plane at X=0 (Axe Z)
        if (showAxisZ) {
            for (int i = -n; i <= n; i++) {
                float z = i * step;
                b.quad(0, -halfSize, z, 0, halfSize, z, 0, 0, 1, halfPx(cam, 0, 0, z, gridPx), cR, cG, cB);
            }
            for (int i = -n; i <= n; i++) {
                float y = i * step;
                b.quad(0, y, -halfSize, 0, y, halfSize, 0, 1, 0, halfPx(cam, 0, y, 0, gridPx), cR, cG, cB);
            }
        }

        // XZ plane at Y=0 (Axe X)
        if (showAxisX) {
            for (int i = -n; i <= n; i++) {
                float z = i * step;
                b.quad(-halfSize, 0, z, halfSize, 0, z, 0, 0, 1, halfPx(cam, 0, 0, z, gridPx), cR, cG, cB);
            }
            for (int i = -n; i <= n; i++) {
                float x = i * step;
                b.quad(x, 0, -halfSize, x, 0, halfSize, 1, 0, 0, halfPx(cam, x, 0, 0, gridPx), cR, cG, cB);
            }
        }

        // XY plane at Z=0 (Axe Y)
        if (showAxisY) {
            for (int i = -n; i <= n; i++) {
                float y = i * step;
                b.quad(-halfSize, y, 0, halfSize, y, 0, 0, 1, 0, halfPx(cam, 0, y, 0, gridPx), cR, cG, cB);
            }
            for (int i = -n; i <= n; i++) {
                float x = i * step;
                b.quad(x, -halfSize, 0, x, halfSize, 0, 1, 0, 0, halfPx(cam, x, 0, 0, gridPx), cR, cG, cB);
            }
        }

        // Axis lines (thicker, colored)
        if (showAxisX) {
            b.quad(-halfSize, 0, 0, halfSize, 0, 0, 0, 1, 0, halfPx(cam, 0, 0, 0, axisPx), 1f, 0.2f, 0.2f);
        }
        if (showAxisY) {
            b.quad(0, -halfSize, 0, 0, halfSize, 0, 0, 0, 1, halfPx(cam, 0, 0, 0, axisPx), 0.2f, 1f, 0.2f);
        }
        if (showAxisZ) {
            b.quad(0, 0, -halfSize, 0, 0, halfSize, 0, 1, 0, halfPx(cam, 0, 0, 0, axisPx), 0.2f, 0.2f, 1f);
        }

        tri.rebuild(b.isEmpty() ? null : b.toFloats());
    }

    /** World half-extent corresponding to the given on-screen pixels at a point. */
    private float halfPx(Cam cam, float mx, float my, float mz, float pixels) {
        float depth = cam.viewDepth(mx, my, mz);
        return depth > 1e-5f ? cam.halfSize(pixels, depth) : 0f;
    }

    public void render(Matrix4f view, Matrix4f projection, int screenW, int screenH) {
        if (!gridVisible || !anyVisible()) return;
        boolean viewChanged = lastView == null || !lastView.equals(view);
        if (viewChanged || dirty) {
            dirty = false;
            lastView = new Matrix4f(view);
            Cam cam = Cam.extract(view, projection, screenH);
            buildTriangles(cam, GRID_PIXELS, AXIS_PIXELS);
        }
        if (!tri.hasGeometry()) return;
        tri.render();
    }

    public void cleanup() {
        tri.release();
        lastView = null;
        dirty = true;
    }
}