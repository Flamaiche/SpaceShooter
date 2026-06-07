package markershape.editor.input;

import markershape.editor.Context;
import markershape.editor.action.DragAction;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;

import java.util.HashSet;
import java.util.Set;

public class HoverManager {
    private final Context ctx;
    private final DragAction drag;

    public HoverManager(Context ctx, DragAction drag) {
        this.ctx = ctx;
        this.drag = drag;
    }

    public void update(float mx, float my) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        int edgeId = -1, vertId = -1;

        if (!ctx.ui.isOverUI(mx, my) && !ctx.selection.isOverOverlay(mx, my)) {
            vertId = ctx.pick.findVertexAt(mx, my);
            if (vertId < 0) edgeId = ctx.pick.pickEdge(mx, my);
        }

        ctx.selection.hoveredVertex = vertId;
        ctx.selection.hoveredEdge = edgeId;
        ctx.hoveredVertexId = vertId;
        ctx.hoveredEdgeId = edgeId;

        if (vertId >= 0) {
            Vertex v = data.vertices.get(vertId);
            if (v != null) {
                Set<Integer> ids = new HashSet<>();
                for (Vertex vo : data.vertices.values()) {
                    if (vo.x == v.x && vo.y == v.y && vo.z == v.z) ids.add(vo.id);
                }
                ctx.hoveredPositionIds = ids;
            }
        } else {
            ctx.hoveredPositionIds = new HashSet<>();
        }
        ctx.renderer.setHoveredVertex(vertId);
        ctx.renderer.setHoveredEdge(edgeId);
        ctx.renderer.setHoveredPositionIds(ctx.hoveredPositionIds);

        if (!drag.isDragging() || vertId < 0) {
            if (vertId >= 0) {
                ctx.selection.crosshairPos.set(data.vertices.get(vertId).x,
                    data.vertices.get(vertId).y,
                    data.vertices.get(vertId).z);
                ctx.selection.crosshairValid = true;
            } else if (!ctx.selection.crosshairValid && ctx.selection.selectedVertex >= 0) {
                Vertex sv = data.vertices.get(ctx.selection.selectedVertex);
                if (sv != null) ctx.selection.crosshairPos.set(sv.x, sv.y, sv.z);
            }
        }
        ctx.renderer.setCrosshair(ctx.selection.crosshairValid, ctx.selection.crosshairPos);
    }
}
