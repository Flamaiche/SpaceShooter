package markershape.editor.action;

import markershape.editor.Context;
import markershape.shape.ShapeData;

public class DeleteAction {
    private final Context ctx;
    private final FaceUtils faceUtils;

    public DeleteAction(Context ctx, FaceUtils faceUtils) {
        this.ctx = ctx;
        this.faceUtils = faceUtils;
    }

    public void deleteSelected() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        ctx.undoredo.snapshot(data);
        java.util.TreeSet<Integer> vv = new java.util.TreeSet<>(ctx.selection.multiVertices);
        if (vv.isEmpty() && ctx.selection.selectedVertex >= 0) vv.add(ctx.selection.selectedVertex);
        java.util.TreeSet<Integer> ee = new java.util.TreeSet<>(ctx.selection.multiEdges);
        if (ee.isEmpty() && ctx.selection.selectedEdge >= 0) ee.add(ctx.selection.selectedEdge);
        if (vv.isEmpty() && ee.isEmpty()) return;
        for (int id : vv) data.removeVertex(id);
        for (int id : ee) data.removeEdge(id);
        faceUtils.cleanupFaces(data);
        data.purgeOrphanVertices();
        ctx.selection.reset();
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] deleted %d vertices, %d edges (cascade)", vv.size(), ee.size());
    }

    public void deleteVertexFromOverlay() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        ctx.undoredo.snapshot(data);
        if (ctx.selection.selectedVertex >= 0) {
            data.removeVertex(ctx.selection.selectedVertex);
            faceUtils.cleanupFaces(data);
            data.purgeOrphanVertices();
            ctx.selection.reset();
            ctx.renderer.rebuild();
        }
    }

    public void deleteEdgeFromOverlay() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        ctx.undoredo.snapshot(data);
        if (ctx.selection.selectedEdge >= 0) {
            data.removeEdge(ctx.selection.selectedEdge);
            faceUtils.cleanupFaces(data);
            data.purgeOrphanVertices();
            ctx.selection.reset();
            ctx.renderer.rebuild();
        }
    }
}
