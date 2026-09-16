package markershape.editor.action;

import markershape.editor.Context;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import org.joml.Vector3f;

import java.util.function.IntConsumer;

/** Creates vertices (from clicks or resolved positions) and dispatches picks to the sibling picker. */
public class VertexAction {
    private final Context ctx;

    /** Creates the vertex action bound to the given editor context. */
    public VertexAction(Context ctx) { this.ctx = ctx; }

    /** Creates a vertex at the 3D position under the cursor, applying magnet/snap first. */
    public void create(float mx, float my) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        Vector3f pos = ctx.pick.getClickWorldPos(mx, my);
        ctx.magnetIfEnabled(pos, mx, my);
        ctx.snapIfEnabled(pos);
        createAt(pos);
    }

    /** Creates a vertex at an already-resolved position (magnet/snap applied by caller). */
    public void createAt(Vector3f pos) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        ctx.undoredo.snapshot(data);
        int newId = nextVertexId(data);
        Vertex v = new Vertex(newId, pos.x, pos.y, pos.z, 1f, 1f, 1f);
        data.addVertex(v);
        ctx.selection.selectVertex(newId);
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] created vertex %d at (%.3f, %.3f, %.3f)", newId, pos.x, pos.y, pos.z);
    }

    /** Returns the next free vertex id (highest id + 1). */
    private int nextVertexId(ShapeData data) {
        if (data.vertices.isEmpty()) return 0;
        int max = -1;
        for (int id : data.vertices.keySet()) if (id > max) max = id;
        return max + 1;
    }

    /** Creates a co-located sibling vertex at the same position as an existing one. */
    public void createSiblingAt(int sourceId) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        Vertex src = data.vertices.get(sourceId);
        if (src == null) return;
        ctx.undoredo.snapshot(data);
        int newId = nextVertexId(data);
        Vertex v = new Vertex(newId, src.x, src.y, src.z, src.r, src.g, src.b);
        data.addVertex(v);
        ctx.selection.selectVertex(newId);
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] created sibling vertex %d at (%.3f, %.3f, %.3f)",
            newId, src.x, src.y, src.z);
    }

    /** Handles a click on a vertex: opens the sibling picker if co-located vertices exist, else invokes onPicked. */
    public void handleClick(float mx, float my, int vertexId, IntConsumer onPicked) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        Vertex v = data.vertices.get(vertexId);
        if (v == null) return;
        int[] siblings = ctx.selection.findSiblings(data, v);
        if (siblings.length > 0) {
            ctx.selection.siblingPicker.show(data, siblings, mx, my,
                ctx.windowWidth, ctx.windowHeight, onPicked::accept);
        } else {
            onPicked.accept(vertexId);
        }
    }
}
