package markershape.editor.action;

import markershape.editor.Context;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import org.joml.Vector3f;

import java.util.function.IntConsumer;

public class VertexAction {
    private final Context ctx;

    public VertexAction(Context ctx) { this.ctx = ctx; }

    public void create(float mx, float my) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        ctx.undoredo.snapshot(data);
        Vector3f pos = ctx.pick.getClickWorldPos(mx, my);
        ctx.snapIfEnabled(pos);
        int newId = data.vertices.isEmpty() ? 0
            : data.vertices.keySet().stream().max(Integer::compareTo).get() + 1;
        Vertex v = new Vertex(newId, pos.x, pos.y, pos.z, 1f, 1f, 1f);
        data.addVertex(v);
        ctx.creatingVertex = false;
        ctx.ui.setActiveMode(-1);
        ctx.selection.selectVertex(newId);
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] created vertex %d at (%.3f, %.3f, %.3f)", newId, pos.x, pos.y, pos.z);
    }

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
