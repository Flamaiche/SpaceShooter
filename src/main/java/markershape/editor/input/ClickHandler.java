package markershape.editor.input;

import markershape.editor.Context;
import markershape.editor.action.*;
import markershape.editor.ui.control.EntityListPanel;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;

public class ClickHandler {
    private final Context ctx;
    private final HoverManager hover;
    private final VertexAction vertex;
    private final EdgeAction edge;
    private final DeleteAction del;
    private final ShapeIO io;

    public ClickHandler(Context ctx, HoverManager hover, VertexAction vertex,
                        EdgeAction edge, DeleteAction del, ShapeIO io) {
        this.ctx = ctx;
        this.hover = hover;
        this.vertex = vertex;
        this.edge = edge;
        this.del = del;
        this.io = io;
    }

    public void mouseClicked(float mx, float my) {
        if (ctx.ui.isOverUI(mx, my)) {
            handleUIClick(mx, my);
            return;
        }
        if (ctx.selection.isOverOverlay(mx, my)) {
            ctx.selection.vertexOverlay.clickField(mx, my);
            ctx.selection.edgeOverlay.clickField(mx, my);
            return;
        }
        if (ctx.selection.siblingPicker.isVisible()) {
            int picked = ctx.selection.siblingPicker.click(mx, my);
            if (picked >= 0) {
                if (ctx.creatingEdge) edge.onVertexPicked(picked);
                else ctx.selection.selectVertex(picked);
                return;
            }
            return;
        }
        handleViewClick(mx, my);
    }

    private void handleUIClick(float mx, float my) {
        if (ctx.ui.isSaveClicked(mx, my)) { io.save(); return; }
        if (ctx.ui.isQuitClicked(mx, my)) {
            ctx.ui.filter.setOpen(false);
            ctx.ui.closeNewMenu();
            ctx.exitModes();
            ctx.ui.setActiveMode(-1);
            ctx.selection.hideOverlays();
            ctx.selection.siblingPicker.hide();
            ctx.selection.reset();
            ctx.ui.showConfirmSave();
            ctx.ui.setConfirmSaveAction(() -> { io.save(); if (ctx.onGoToMenu != null) ctx.onGoToMenu.run(); });
            return;
        }
        int newResult = ctx.ui.clickNew(mx, my);
        if (newResult == 0) { onNewVertex(); return; }
        else if (newResult == 1) { onNewEdge(); return; }
        else if (newResult == -2) return;

        int filterResult = ctx.ui.clickFilter(mx, my);
        if (filterResult >= 0) {
            if (filterResult == 6) {
                boolean[] fv = ctx.ui.getFilterValues();
                ctx.renderer.setShowFaces(fv[0]); ctx.renderer.setShowEdges(fv[1]);
                ctx.renderer.setShowPoints(fv[2]); ctx.renderer.setShowAxisX(fv[3]);
                ctx.renderer.setShowAxisY(fv[4]); ctx.renderer.setShowAxisZ(fv[5]);
            }
            if (filterResult == 3) ctx.renderer.setGridStep(ctx.ui.getSliderValues()[3]);
        }

        int elResult = ctx.ui.clickEntityList(mx, my);
        if (elResult == -2) return;
        if (elResult >= 0) {
            if (ctx.ui.entityList.getActiveMode() == EntityListPanel.MODE_VERTEX) {
                ctx.selection.selectVertex(elResult);
            } else {
                ctx.selection.selectEdge(elResult);
            }
            return;
        }

        if (ctx.ui.isConfirmSaveVisible()) {
            int cs = ctx.ui.clickConfirmSave(mx, my);
            if (cs == 1) {
                ctx.ui.closeConfirmSave();
                Runnable action = ctx.ui.getConfirmSaveAction();
                if (action != null) action.run();
                else io.save();
            } else if (cs == 2) {
                ctx.ui.closeConfirmSave();
                if (ctx.onGoToMenu != null) ctx.onGoToMenu.run();
            }
        }
    }

    private void handleViewClick(float mx, float my) {
        if (ctx.renderer.getShapeData() == null) return;
        if (ctx.creatingVertex) { vertex.create(mx, my); return; }

        // 1. Pick visible vertex (depth-checked)
        int vertId = ctx.pick.findVisibleVertexAt(mx, my);
        if (vertId >= 0) {
            vertex.handleClick(mx, my, vertId, picked -> {
                if (ctx.creatingEdge) edge.onVertexPicked(picked);
                else ctx.selection.selectVertex(picked);
            });
            return;
        }

        // 2. Pick visible edge (depth-checked)
        int edgeId = ctx.pick.pickVisibleEdge(mx, my);
        if (edgeId >= 0) {
            ctx.selection.selectEdge(edgeId);
            return;
        }

        // 3. Click near crosshair → select vertex at that position
        if (ctx.selection.crosshairValid) {
            ShapeData data = ctx.renderer.getShapeData();
            if (data != null) {
                for (Vertex v : data.vertices.values()) {
                    if (v.x == ctx.selection.crosshairPos.x
                        && v.y == ctx.selection.crosshairPos.y
                        && v.z == ctx.selection.crosshairPos.z) {
                        if (ctx.pick.isNearCrosshair(mx, my, ctx.selection.crosshairPos, 40f)) {
                            ctx.selection.selectVertex(v.id);
                        }
                        break;
                    }
                }
            }
        }
    }

    public void handleEscape() {
        if (ctx.ui.isConfirmSaveVisible()) { ctx.ui.closeConfirmSave(); return; }
        if (ctx.ui.newMenu.isOpen()) { ctx.ui.newMenu.close(); return; }
        if (ctx.ui.filter.isOpen()) { ctx.ui.filter.setOpen(false); return; }
        if (ctx.creatingVertex || ctx.creatingEdge) { ctx.exitModes(); ctx.ui.setActiveMode(-1); return; }
        if (ctx.selection.selectedVertex >= 0 || ctx.selection.selectedEdge >= 0) {
            ctx.selection.reset();
            ctx.hoveredVertexId = -1;
            ctx.hoveredEdgeId = -1;
            return;
        }
    }

    public void save() { io.save(); }

    public void deleteSelected() { del.deleteSelected(); }

    public void undo() {
        var cur = ctx.renderer.getShapeData();
        if (cur == null) return;
        var prev = ctx.undoredo.undo(cur);
        if (prev != null) io.loadShapeData(prev);
    }

    public void redo() {
        var cur = ctx.renderer.getShapeData();
        if (cur == null) return;
        var next = ctx.undoredo.redo(cur);
        if (next != null) io.loadShapeData(next);
    }

    private void onNewVertex() {
        ctx.creatingVertex = true; ctx.ui.setActiveMode(0);
        ctx.creatingEdge = false; ctx.edgeFirstVertex = -1;
        ctx.ui.closeNewMenu(); ctx.selection.hideOverlays();
        ctx.selection.reset();
    }

    private void onNewEdge() {
        ctx.creatingEdge = true; ctx.ui.setActiveMode(1);
        ctx.edgeFirstVertex = -1; ctx.creatingVertex = false;
        ctx.ui.closeNewMenu(); ctx.selection.hideOverlays();
        ctx.selection.reset();
    }
}
