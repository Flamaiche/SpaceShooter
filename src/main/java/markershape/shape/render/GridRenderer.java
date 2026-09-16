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

    /** Shows/hides the grid. */
    public void setGridVisible(boolean v) { gridVisible = v; }
    /** Returns true if the grid is visible. */
    public boolean isGridVisible() { return gridVisible; }

    /** Shows/hides the axis-aligned X grid lines. */
    public void setShowAxisX(boolean v) { showAxisX = v; dirty = true; }
    /** Shows/hides the axis-aligned Y grid lines. */
    public void setShowAxisY(boolean v) { showAxisY = v; dirty = true; }
    /** Shows/hides the axis-aligned Z grid lines. */
    public void setShowAxisZ(boolean v) { showAxisZ = v; dirty = true; }
    /** Returns true if the X grid lines are shown. */
    public boolean isShowAxisX() { return showAxisX; }
    /** Returns true if the Y grid lines are shown. */
    public boolean isShowAxisY() { return showAxisY; }
    /** Returns true if the Z grid lines are shown. */
    public boolean isShowAxisZ() { return showAxisZ; }
    /** Returns true if at least one axis is set to be shown. */
    public boolean anyVisible() { return showAxisX || showAxisY || showAxisZ; }

    /** Sets the grid spacing (clamped to a minimum of 0.1). */
    public void setGridStep(float step) {
        if (step <= 0f) step = 0.1f;
        gridStep = step;
        dirty = true;
    }

    /** Marks the grid geometry dirty so it is rebuilt on the next render. */
    public void rebuild() {
        dirty = true;
    }

    /** Builds the triangle geometry for the visible grid lines and colored axes. */
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

    /** Renders the grid, rebuilding the geometry when the view or grid state changed. */
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

    /** Releases the grid geometry and resets the dirty state. */
    public void cleanup() {
        tri.release();
        lastView = null;
        dirty = true;
    }
}