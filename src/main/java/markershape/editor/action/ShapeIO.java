package markershape.editor.action;

import markershape.editor.Context;
import markershape.shape.ShapeLoader;
import markershape.shape.ShapeData;
import markershape.shape.render.ShapeRenderer;

/** Loads and saves shape files, and applies shared render-filter settings. */
public class ShapeIO {
    private final Context ctx;

    /** Creates the shape I/O bound to the given editor context. */
    public ShapeIO(Context ctx) { this.ctx = ctx; }

    /** Applies the UI filter/slider settings to the renderer (shared by load paths). */
    public static void applyFilters(ShapeRenderer renderer, boolean[] fv, float[] sv) {
        renderer.setShowFaces(fv[0]);
        renderer.setShowEdges(fv[1]);
        renderer.setShowPoints(fv[2]);
        renderer.setShowAxisX(fv[3]);
        renderer.setShowAxisY(fv[4]);
        renderer.setShowAxisZ(fv[5]);
        renderer.setShowFrontArrow(fv[8]);
        renderer.setPointSize(sv[0]);
        renderer.setLineWidth(sv[1]);
        renderer.setFaceAlpha(sv[2]);
        renderer.setGridStep(sv[3]);
    }

    /** Saves the current shape under the context's current filename, if any. */
    public void save() {
        if (ctx.currentFilename == null || !ctx.renderer.hasShape()) return;
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        boolean ok = ShapeLoader.save(data, ctx.currentFilename);
        System.out.println("[MarkerShape] Save: " + ctx.currentFilename + " " + (ok ? "OK" : "FAILED"));
        if (ok) learngl.LogFile.logf("[MarkerShape] saved: %s | vertices=%d edges=%d faces=%d",
            ctx.currentFilename, data.vertices.size(), data.edges.size(), data.faces.size());
    }

    /** Loads an already-parsed shape into the renderer and resets the edit state. */
    public void loadShapeData(ShapeData data) {
        ctx.renderer.setShapeData(data);
        ctx.renderer.rebuild();
        ctx.selection.reset();
        ctx.exitModes();
        ctx.ui.setActiveMode(-1);
    }

    /** Loads a shape from disk and applies the current UI filter settings. */
    public void load(String filename) {
        ctx.currentFilename = filename;
        boolean ok = ctx.renderer.loadShape(filename);
        if (!ok) {
            System.err.println("[App] failed to load shape: " + filename);
        }
        boolean[] fv = ctx.ui.getFilterValues();
        float[] sv = ctx.ui.getSliderValues();
        applyFilters(ctx.renderer, fv, sv);
    }
}
