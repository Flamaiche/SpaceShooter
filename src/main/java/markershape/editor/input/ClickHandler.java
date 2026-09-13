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
    private final ShapeTools tools;

    public ClickHandler(Context ctx, HoverManager hover, VertexAction vertex,
                        EdgeAction edge, DeleteAction del, ShapeIO io) {
        this(ctx, hover, vertex, edge, del, io, null);
    }

    public ClickHandler(Context ctx, HoverManager hover, VertexAction vertex,
                        EdgeAction edge, DeleteAction del, ShapeIO io, ShapeTools tools) {
        this.ctx = ctx;
        this.hover = hover;
        this.vertex = vertex;
        this.edge = edge;
        this.del = del;
        this.io = io;
        this.tools = tools;
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

        int toolResult = ctx.ui.clickTools(mx, my);
        if (toolResult == -2) return;
        if (toolResult >= 0) { handleTool(toolResult); return; }

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

        if (ctx.creatingFace) {
            int v = ctx.pick.findVisibleVertexAt(mx, my);
            if (v >= 0) {
                if (ctx.traceVertices.size() >= 3 && v == ctx.traceVertices.get(0)) {
                    closeTrace();
                }
                return;
            }
            vertex.create(mx, my);
            int nv = ctx.selection.selectedVertex;
            if (nv >= 0) {
                if (ctx.traceVertices.isEmpty()) {
                    ctx.traceVertices.add(nv);
                } else {
                    int prev = ctx.traceVertices.get(ctx.traceVertices.size() - 1);
                    edge.create(prev, nv);
                    ctx.traceVertices.add(nv);
                }
            }
            ctx.renderer.setPlacementGhost(false, null);
            return;
        }

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
        if (ctx.ui.isToolsOpen()) { ctx.ui.closeToolsPal(); return; }
        if (ctx.ui.filter.isOpen()) { ctx.ui.filter.setOpen(false); return; }
        if (ctx.creatingVertex || ctx.creatingEdge) { ctx.exitModes(); ctx.ui.setActiveMode(-1); ctx.renderer.setTracePreview(null); return; }
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
        ctx.exitModes();
        ctx.creatingVertex = true; ctx.ui.setActiveMode(0);
        ctx.ui.closeNewMenu(); ctx.selection.hideOverlays();
        ctx.selection.reset();
    }

    private void onNewEdge() {
        ctx.exitModes();
        ctx.creatingEdge = true; ctx.ui.setActiveMode(1);
        ctx.ui.closeNewMenu(); ctx.selection.hideOverlays();
        ctx.selection.reset();
    }

    /** Enters the "Tracé (face)" mode: chained vertex placement closed by a fill. */
    public void onNewTrace() {
        ctx.exitModes();
        ctx.creatingFace = true;
        ctx.creatingVertex = true;
        ctx.ui.setActiveMode(2);
        ctx.ui.closeNewMenu(); ctx.selection.hideOverlays();
        ctx.selection.reset();
    }

    /** Closes the traced loop, fills it with faces and exits the mode. */
    public void closeTrace() {
        if (ctx.renderer.getShapeData() == null) return;
        if (ctx.traceVertices.size() < 3) { ctx.exitModes(); ctx.ui.setActiveMode(-1); return; }
        ctx.undoredo.snapshot(ctx.renderer.getShapeData());
        int added = tools.closeTraceLoop(ctx.traceVertices);
        ctx.renderer.rebuild();
        int n = ctx.traceVertices.size();
        ctx.exitModes();
        ctx.ui.setActiveMode(-1);
        ctx.renderer.setTracePreview(null);
        System.out.println("[MarkerShape] Trace cloturee : " + n + " sommets, " + added + " faces");
    }

    private boolean hasVertexSelection() {
        return ctx.selection.selectedVertex >= 0 || !ctx.selection.multiVertices.isEmpty();
    }

    private boolean hasEdgeSelection() {
        return ctx.selection.selectedEdge >= 0 || !ctx.selection.multiEdges.isEmpty();
    }

    /** Dispatches a tool palette row to the corresponding action. */
    private void handleTool(int tool) {
        if (ctx.renderer.getShapeData() == null) return;
        switch (tool) {
            case markershape.editor.ui.menu.ToolPalette.TOOL_SELECT -> {
                ctx.exitModes();
                ctx.ui.setActiveMode(-1);
                ctx.ui.closeNewMenu();
            }
            case markershape.editor.ui.menu.ToolPalette.TOOL_VERTEX -> onNewVertex();
            case markershape.editor.ui.menu.ToolPalette.TOOL_EDGE -> onNewEdge();
            case markershape.editor.ui.menu.ToolPalette.TOOL_TRACE -> onNewTrace();
            case markershape.editor.ui.menu.ToolPalette.TOOL_SPLIT -> {
                if (hasEdgeSelection()) {
                    tools.splitEdge(ctx.selection.selectedEdge >= 0
                        ? ctx.selection.selectedEdge : ctx.selection.multiEdges.first());
                } else {
                    System.out.println("[MarkerShape] Subdiviser demande une arete selectionnee");
                }
            }
            case markershape.editor.ui.menu.ToolPalette.TOOL_EXTRUDE -> tools.extrudeSelectedEdge();
            case markershape.editor.ui.menu.ToolPalette.TOOL_FILL -> tools.fillSelection();
            case markershape.editor.ui.menu.ToolPalette.TOOL_WELD -> tools.weldSelected();
            case markershape.editor.ui.menu.ToolPalette.TOOL_DUPLICATE -> tools.duplicateSelected();
            case markershape.editor.ui.menu.ToolPalette.TOOL_COPY -> tools.copySelected();
            case markershape.editor.ui.menu.ToolPalette.TOOL_PASTE -> tools.pasteSelected();
            case markershape.editor.ui.menu.ToolPalette.TOOL_CLEAN ->
                System.out.println("[MarkerShape] " + tools.cleanupShape());
            case markershape.editor.ui.menu.ToolPalette.TOOL_HELP -> ctx.help.toggle();
            default -> { }
        }
    }
}
