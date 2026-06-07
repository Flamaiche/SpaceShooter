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
        if (ctx.selection.selectedVertex >= 0) {
            data.removeVertex(ctx.selection.selectedVertex);
            faceUtils.cleanupFaces(data);
            ctx.selection.selectedVertex = -1;
            ctx.selection.vertexOverlay.hide();
            ctx.renderer.rebuild();
            learngl.LogFile.logf("[MarkerShape] deleted vertex (cascade)");
        } else if (ctx.selection.selectedEdge >= 0) {
            data.removeEdge(ctx.selection.selectedEdge);
            faceUtils.cleanupFaces(data);
            ctx.selection.selectedEdge = -1;
            ctx.selection.edgeOverlay.hide();
            ctx.renderer.rebuild();
            learngl.LogFile.logf("[MarkerShape] deleted edge (cascade)");
        }
    }

    public void deleteVertexFromOverlay() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        ctx.undoredo.snapshot(data);
        if (ctx.selection.selectedVertex >= 0) {
            data.removeVertex(ctx.selection.selectedVertex);
            faceUtils.cleanupFaces(data);
            ctx.selection.selectedVertex = -1;
            ctx.selection.vertexOverlay.hide();
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
            ctx.selection.selectedEdge = -1;
            ctx.selection.edgeOverlay.hide();
            ctx.renderer.rebuild();
        }
    }
}
