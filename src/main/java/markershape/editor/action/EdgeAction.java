package markershape.editor.action;

import markershape.editor.Context;
import markershape.shape.Edge;
import markershape.shape.ShapeData;

public class EdgeAction {
    private final Context ctx;
    private final FaceUtils faceUtils;

    public EdgeAction(Context ctx, FaceUtils faceUtils) {
        this.ctx = ctx;
        this.faceUtils = faceUtils;
    }

    public void create(int a, int b) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null || a == b) return;
        ctx.undoredo.snapshot(data);
        for (Edge e : data.edges.values()) {
            if ((e.a == a && e.b == b) || (e.a == b && e.b == a)) {
                System.out.println("[MarkerShape] edge " + a + "-" + b + " already exists (id=" + e.id + ")");
                return;
            }
        }
        int newId = data.edges.isEmpty() ? 0 : data.edges.keySet().stream().max(Integer::compareTo).get() + 1;
        Edge edge = new Edge();
        edge.id = newId;
        edge.a = a;
        edge.b = b;
        edge.mode = "stun";
        edge.thickness = 0.02f;
        data.addEdge(edge);
        faceUtils.detectAndCreateFaces(data, a, b);
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] created edge %d: %d-%d", newId, a, b);
    }

    public void onVertexPicked(int vertexId) {
        if (ctx.edgeFirstVertex < 0) {
            ctx.edgeFirstVertex = vertexId;
        } else {
            create(ctx.edgeFirstVertex, vertexId);
            ctx.edgeFirstVertex = -1;
            ctx.creatingEdge = false;
            ctx.ui.setActiveMode(-1);
        }
    }
}
