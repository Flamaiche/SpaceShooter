package markershape.editor.action;

import markershape.editor.Context;
import markershape.shape.ShapeLoader;
import markershape.shape.ShapeData;

public class ShapeIO {
    private final Context ctx;

    public ShapeIO(Context ctx) { this.ctx = ctx; }

    public void save() {
        if (ctx.currentFilename == null || !ctx.renderer.hasShape()) return;
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        boolean ok = ShapeLoader.save(data, ctx.currentFilename);
        System.out.println("[MarkerShape] Save: " + ctx.currentFilename + " " + (ok ? "OK" : "FAILED"));
        if (ok) learngl.LogFile.logf("[MarkerShape] saved: %s | vertices=%d edges=%d faces=%d",
            ctx.currentFilename, data.vertices.size(), data.edges.size(), data.faces.size());
    }

    public void loadShapeData(ShapeData data) {
        ctx.renderer.setShapeData(data);
        ctx.renderer.rebuild();
        ctx.selection.reset();
        ctx.exitModes();
        ctx.ui.setActiveMode(-1);
    }

    public void load(String filename) {
        ctx.currentFilename = filename;
        boolean ok = ctx.renderer.loadShape(filename);
        if (!ok) {
            System.err.println("[App] failed to load shape: " + filename);
        }
        boolean[] fv = ctx.ui.getFilterValues();
        float[] sv = ctx.ui.getSliderValues();
        ctx.renderer.setShowFaces(fv[0]);
        ctx.renderer.setShowEdges(fv[1]);
        ctx.renderer.setShowPoints(fv[2]);
        ctx.renderer.setShowAxisX(fv[3]);
        ctx.renderer.setShowAxisY(fv[4]);
        ctx.renderer.setShowAxisZ(fv[5]);
        ctx.renderer.setPointSize(sv[0]);
        ctx.renderer.setLineWidth(sv[1]);
        ctx.renderer.setFaceAlpha(sv[2]);
    }
}
