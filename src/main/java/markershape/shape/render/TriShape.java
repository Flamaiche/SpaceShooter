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

    /** (Re)builds the engine Shape from xyz+rgb vertex data; a null or too-short array releases the geometry. */
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

    /** Draws the current geometry through the engine if present. */
    public void render() {
        if (shape != null) shape.render();
    }

    /** Returns true if a shape is currently built. */
    public boolean hasGeometry() { return shape != null; }

    /** Releases the current engine geometry, if any. */
    public void release() {
        if (shape != null) {
            shape.cleanup();
            shape = null;
        }
    }
}