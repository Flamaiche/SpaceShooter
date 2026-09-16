package markershape.editor.action;

import markershape.config.ConfigParametres;
import markershape.editor.Context;
import markershape.shape.Edge;
import markershape.shape.ShapeData;

/** Creates edges between vertices and auto-detects triangular faces formed by the new edge. */
public class EdgeAction {
    private final Context ctx;
    private final FaceUtils faceUtils;

    /** Creates the edge action with the shared context and face utilities. */
    public EdgeAction(Context ctx, FaceUtils faceUtils) {
        this.ctx = ctx;
        this.faceUtils = faceUtils;
    }

    /** Creates an edge between two vertex ids (skipped if equal or duplicate), then auto-detects faces. */
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
        ConfigParametres cfg = ConfigParametres.get();
        Edge edge = new Edge(newId, a, b, "stun", 0.02f,
            cfg.getFloat("createColorR"),
            cfg.getFloat("createColorG"),
            cfg.getFloat("createColorB"));
        data.addEdge(edge);
        faceUtils.detectAndCreateFaces(data, a, b);
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] created edge %d: %d-%d", newId, a, b);
    }

    /** Handles a vertex pick during edge-creation mode: picks the first endpoint, then creates the edge. */
    public void onVertexPicked(int vertexId) {
        if (ctx.edgeFirstVertex < 0) {
            ctx.edgeFirstVertex = vertexId;
        } else {
            create(ctx.edgeFirstVertex, vertexId);
            if (ctx.edgeFirstVertex != vertexId) {
                ctx.edgeFirstVertex = vertexId;
            } else {
                ctx.edgeFirstVertex = -1;
                ctx.creatingEdge = false;
                ctx.ui.setActiveMode(-1);
            }
        }
    }
}
