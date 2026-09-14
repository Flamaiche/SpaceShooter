package markershape.shape.render;

import learngl.VertexUtils;
import learngl.shape.Shape;

/**
 * Wraps a {@link Shape} built from triangle vertices so the editor's helper
 * overlays rasterize as real triangles through the engine. Re-generates the
 * Shape only when the geometry actually changes.
 */
public final class TriShape {
    private Shape shape;

    public void rebuild(float[] xyzRgb) {
        if (xyzRgb == null || xyzRgb.length < 18) {
            release();
            return;
        }
        if (shape != null) {
            shape.cleanup();
            shape = null;
        }
        shape = new Shape(VertexUtils.autoAddSlotTexture(xyzRgb));
    }

    public void render() {
        if (shape != null) shape.render();
    }

    public boolean hasGeometry() { return shape != null; }

    public void release() {
        if (shape != null) {
            shape.cleanup();
            shape = null;
        }
    }
}