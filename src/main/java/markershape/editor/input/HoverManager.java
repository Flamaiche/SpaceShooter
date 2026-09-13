package markershape.editor.input;

import markershape.editor.Context;
import markershape.editor.ui.control.EntityListPanel;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

public class HoverManager {
    private final Context ctx;

    public HoverManager(Context ctx) {
        this.ctx = ctx;
    }

    public void update(float mx, float my) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        int edgeId = -1, vertId = -1;

        ctx.ui.entityList.updateHover(mx, my);
        int listHoveredId = ctx.ui.entityList.getHoveredId();

        if (!ctx.ui.isOverUI(mx, my) && !ctx.selection.isOverOverlay(mx, my)) {
            vertId = ctx.pick.findVisibleVertexAt(mx, my);
            if (vertId < 0) edgeId = ctx.pick.pickVisibleEdge(mx, my);
        }

        if (listHoveredId >= 0) {
            if (ctx.ui.entityList.getActiveMode() == EntityListPanel.MODE_VERTEX) {
                vertId = listHoveredId;
                edgeId = -1;
            } else {
                edgeId = listHoveredId;
                vertId = -1;
            }
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
            ctx.renderer.setPlacementGhost(false, null);
        } else {
            ctx.hoveredPositionIds = new HashSet<>();
            if (!ctx.creatingVertex) ctx.renderer.setPlacementGhost(false, null);
        }
        ctx.renderer.setHoveredVertex(vertId);
        ctx.renderer.setHoveredEdge(edgeId);
        ctx.renderer.setHoveredPositionIds(ctx.hoveredPositionIds);

        if (vertId >= 0) {
            ctx.selection.crosshairPos.set(data.vertices.get(vertId).x,
                data.vertices.get(vertId).y,
                data.vertices.get(vertId).z);
            ctx.selection.crosshairValid = true;
        } else if (ctx.creatingVertex) {
            if (ctx.ui.isOverUI(mx, my) || ctx.selection.isOverOverlay(mx, my)) {
                ctx.renderer.setPlacementGhost(false, null);
            } else {
                Vector3f pos = ctx.pick.getClickWorldPos(mx, my);
                ctx.magnetIfEnabled(pos, mx, my);
                ctx.snapIfEnabled(pos);
                ctx.selection.crosshairPos.set(pos);
                ctx.selection.crosshairValid = true;
                ctx.renderer.setPlacementGhost(true, pos);
            }
        } else if (!ctx.selection.crosshairValid && ctx.selection.selectedVertex >= 0) {
            Vertex sv = data.vertices.get(ctx.selection.selectedVertex);
            if (sv != null) ctx.selection.crosshairPos.set(sv.x, sv.y, sv.z);
        }

        updateTracePreview(mx, my);

        ctx.renderer.setCrosshair(ctx.selection.crosshairValid, ctx.selection.crosshairPos);
    }

    private void updateTracePreview(float mx, float my) {
        if (ctx.creatingFace && ctx.renderer.getShapeData() != null) {
            if (ctx.ui.isOverUI(mx, my) || ctx.selection.isOverOverlay(mx, my)) {
                ctx.renderer.setTracePreview(null);
                return;
            }
            org.joml.Matrix4f mvp = new org.joml.Matrix4f(ctx.pick.getProjection());
            mvp.mul(ctx.pick.getView());
            java.util.List<Float> pts = new java.util.ArrayList<>();
            org.joml.Vector4f p = new org.joml.Vector4f();
            for (int id : ctx.traceVertices) {
                Vertex v = ctx.renderer.getShapeData().vertices.get(id);
                if (v == null) continue;
                p.set(v.x, v.y, v.z, 1f).mul(mvp);
                if (p.w <= 0) continue;
                pts.add((p.x / p.w * 0.5f + 0.5f) * ctx.windowWidth);
                pts.add((1f - (p.y / p.w * 0.5f + 0.5f)) * ctx.windowHeight);
            }
            Vector3f pos = ctx.pick.getClickWorldPos(mx, my);
            ctx.magnetIfEnabled(pos, mx, my);
            ctx.snapIfEnabled(pos);
            p.set(pos.x, pos.y, pos.z, 1f).mul(mvp);
            if (p.w > 0) {
                pts.add((p.x / p.w * 0.5f + 0.5f) * ctx.windowWidth);
                pts.add((1f - (p.y / p.w * 0.5f + 0.5f)) * ctx.windowHeight);
            }
            if (pts.size() < 4) { ctx.renderer.setTracePreview(null); return; }
            float[] arr = new float[pts.size()];
            for (int i = 0; i < arr.length; i++) arr[i] = pts.get(i);
            ctx.renderer.setTracePreview(arr);
        }
    }
}
