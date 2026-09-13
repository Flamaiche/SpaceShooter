package markershape.editor.input;

import markershape.editor.Context;
import markershape.editor.action.*;
import markershape.editor.ui.control.EntityListPanel;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import org.lwjgl.glfw.GLFW;

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

    private boolean isCtrlDown() {
        return GLFW.glfwGetKey(ctx.window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(ctx.window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    private boolean isShiftDown() {
        return GLFW.glfwGetKey(ctx.window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(ctx.window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    public void mouseClicked(float mx, float my) {
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
            return;
        }
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
            int picked = ctx.selection.siblingPicker.clickItem(mx, my);
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
            ctx.ui.showConfirmSave();
            ctx.ui.setConfirmSaveAction(() -> { io.save(); if (ctx.onGoToMenu != null) ctx.onGoToMenu.run(); });
            return;
        }
        int newResult = ctx.ui.clickNew(mx, my);
        if (newResult == 0) { onNewVertex(); return; }
        else if (newResult == 1) { onNewEdge(); return; }
        else if (newResult == -2) return;

        ctx.ui.clickFilter(mx, my);

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
    }

    private void handleViewClick(float mx, float my) {
        if (ctx.renderer.getShapeData() == null) return;

        int vertId = -1;
        if (ctx.creatingVertex) {
            vertId = ctx.pick.findVisibleVertexAt(mx, my);
            if (vertId >= 0) {
                if (isShiftDown()) {
                    vertex.createSiblingAt(vertId);
                    ctx.edgeFirstVertex = ctx.selection.selectedVertex;
                    return;
                }
                if (ctx.edgeFirstVertex >= 0 && ctx.edgeFirstVertex != vertId) {
                    edge.create(ctx.edgeFirstVertex, vertId);
                    ctx.edgeFirstVertex = vertId;
                } else if (ctx.edgeFirstVertex < 0) {
                    ctx.edgeFirstVertex = vertId;
                    ctx.selection.selectVertex(vertId);
                }
                return;
            }
            vertex.create(mx, my);
            ctx.edgeFirstVertex = ctx.selection.selectedVertex;
            return;
        }

        // 1. Pick visible vertex (depth-checked)
        int vertId2 = ctx.pick.findVisibleVertexAt(mx, my);
        if (vertId2 >= 0) {
            if (ctx.creatingEdge) {
                vertex.handleClick(mx, my, vertId2, picked -> edge.onVertexPicked(picked));
                return;
            }
            if (isCtrlDown()) {
                ctx.selection.toggleVertexMulti(vertId2);
                return;
            }
            if (ctx.selection.multiVertices.contains(vertId2)) {
                vertex.handleClick(mx, my, vertId2, picked -> ctx.selection.makePrimaryVertex(picked));
                return;
            }
            vertex.handleClick(mx, my, vertId2, picked -> ctx.selection.selectVertex(picked));
            return;
        }

        // 2. Pick visible edge (depth-checked)
        int edgeId = ctx.pick.pickVisibleEdge(mx, my);
        if (edgeId >= 0) {
            if (isCtrlDown()) {
                ctx.selection.toggleEdgeMulti(edgeId);
                return;
            }
            if (ctx.selection.multiEdges.contains(edgeId)) {
                ctx.selection.makePrimaryEdge(edgeId);
                return;
            }
            ctx.selection.selectEdge(edgeId);
            return;
        }

        // 3. Click near crosshair → select vertex at that position
        if (ctx.selection.crosshairValid && !isCtrlDown()) {
            ShapeData data = ctx.renderer.getShapeData();
            if (data != null) {
                for (Vertex v : data.vertices.values()) {
                    if (v.x == ctx.selection.crosshairPos.x
                        && v.y == ctx.selection.crosshairPos.y
                        && v.z == ctx.selection.crosshairPos.z) {
                        if (ctx.pick.isNearCrosshair(mx, my, ctx.selection.crosshairPos, 40f)) {
                            ctx.selection.selectVertex(v.id);
                            return;
                        }
                        break;
                    }
                }
            }
        }

        // 4. Empty space: clear the selection
        if (isCtrlDown()) {
            ctx.selection.multiVertices.clear();
            ctx.selection.multiEdges.clear();
            ctx.selection.selectedVertex = -1;
            ctx.selection.selectedEdge = -1;
            ctx.selection.refreshSelectionVisual();
        } else {
            ctx.selection.reset();
        }
    }

    public void handleEscape() {
        if (ctx.ui.isConfirmSaveVisible()) { ctx.ui.closeConfirmSave(); return; }
        if (ctx.help != null && ctx.help.isVisible()) { ctx.help.hide(); return; }
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
        int sv = ctx.selection.selectedVertex, se = ctx.selection.selectedEdge;
        var prev = ctx.undoredo.undo(cur);
        if (prev != null) {
            io.loadShapeData(prev);
            restoreSelection(sv, se);
        }
    }

    public void redo() {
        var cur = ctx.renderer.getShapeData();
        if (cur == null) return;
        int sv = ctx.selection.selectedVertex, se = ctx.selection.selectedEdge;
        var next = ctx.undoredo.redo(cur);
        if (next != null) {
            io.loadShapeData(next);
            restoreSelection(sv, se);
        }
    }

    private void restoreSelection(int sv, int se) {
        if (sv >= 0 && ctx.renderer.getShapeData().vertices.containsKey(sv)) {
            ctx.selection.selectVertex(sv);
        } else if (se >= 0 && ctx.renderer.getShapeData().edges.containsKey(se)) {
            ctx.selection.selectEdge(se);
        }
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
