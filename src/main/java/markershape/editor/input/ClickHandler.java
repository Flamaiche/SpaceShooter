package markershape.editor.input;

import markershape.editor.Context;
import markershape.editor.action.*;
import markershape.editor.ui.control.EntityListPanel;
import markershape.shape.ShapeData;
import org.lwjgl.glfw.GLFW;

/** Handles mouse click dispatching to the appropriate editor action (view, UI, confirm dialog, or tool). */
public class ClickHandler {
    private final Context ctx;
    private final HoverManager hover;
    private final VertexAction vertex;
    private final EdgeAction edge;
    private final DeleteAction del;
    private final ShapeIO io;
    private final ShapeTools tools;

    /** Constructor without shape tools (keyboard shortcuts requiring tools are unavailable). */
    public ClickHandler(Context ctx, HoverManager hover, VertexAction vertex,
                        EdgeAction edge, DeleteAction del, ShapeIO io) {
        this(ctx, hover, vertex, edge, del, io, null);
    }

    /**
     * Full constructor for the click handler.
     *
     * @param ctx   the shared editor context
     * @param hover the hover manager
     * @param vertex vertex action
     * @param edge   edge action
     * @param del    delete action
     * @param io     shape I/O
     * @param tools  shape tools (may be null)
     */
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

    /** Returns whether either Ctrl key is currently held down. */
    private boolean isCtrlDown() {
        return GLFW.glfwGetKey(ctx.window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(ctx.window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    /** Returns whether either Shift key is currently held down. */
    private boolean isShiftDown() {
        return GLFW.glfwGetKey(ctx.window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(ctx.window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    /**
     * Dispatches a mouse click: confirm dialogs first, then UI, overlays,
     * record the sibling picker, and finally the 3D view.
     *
     * @param mx mouse X in screen coordinates
     * @param my mouse Y in screen coordinates
     */
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
        if (ctx.ui.isConfirmDeleteVisible()) {
            int cs = ctx.ui.clickConfirmDelete(mx, my);
            if (cs == 1) {
                ctx.ui.closeConfirmDelete();
                Runnable action = ctx.ui.getConfirmDeleteAction();
                if (action != null) action.run();
            } else if (cs == 2) {
                ctx.ui.closeConfirmDelete();
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

    /** Routes clicks on the top bar UI: save, quit, new, filter, origin, tools, and entity list. */
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

        int originResult = ctx.ui.clickOrigin(mx, my);
        if (originResult != 0) {
            if (ctx.renderer.getShapeData() != null && !ctx.renderer.getShapeData().vertices.isEmpty()) {
                float step = markershape.editor.ui.control.OriginPanel.STEP;
                float sign = originResult > 0 ? step : -step;
                int axis = Math.abs(originResult) - 1;
                float dx = axis == 0 ? sign : 0f;
                float dy = axis == 1 ? sign : 0f;
                float dz = axis == 2 ? sign : 0f;
                ctx.ui.origin.addOffset(dx, dy, dz);
                tools.translateAll(dx, dy, dz);
            }
            return;
        }

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

    /**
     * Routes a click in the 3D viewport: vertex/edge creation, selection,
     * face tracing, and multi-selection handling.
     *
     * @param mx mouse X in screen coordinates
     * @param my mouse Y in screen coordinates
     */
    private void handleViewClick(float mx, float my) {
        if (ctx.renderer.getShapeData() == null) return;

        if (ctx.creatingFace) {
            int v = ctx.pick.findVisibleVertexAt(mx, my);
            if (v < 0) {
                // Snap onto an existing vertex near the cursor (aimantation) so the
                // trace reuses it instead of stacking a duplicate at the same spot.
                float radius = ctx.ui != null && ctx.ui.isMagnetEnabled()
                    ? ctx.ui.getMagnetRadius() : 8f;
                markershape.shape.Vertex near = ctx.pick.findVertexNearCursor(mx, my, radius);
                if (near != null) v = near.id;
            }
            if (v >= 0) {
                appendTraceVertex(v);
                ctx.renderer.setPlacementGhost(false, null);
                return;
            }
            // No existing vertex matches: resolve the target position and only create
            // a new vertex if no vertex already occupies it exactly.
            org.joml.Vector3f pos = ctx.pick.getClickWorldPos(mx, my);
            ctx.magnetIfEnabled(pos, mx, my);
            ctx.snapIfEnabled(pos);
            markershape.shape.ShapeData data = ctx.renderer.getShapeData();
            for (markershape.shape.Vertex vo : data.vertices.values()) {
                if (vo.x == pos.x && vo.y == pos.y && vo.z == pos.z) {
                    appendTraceVertex(vo.id);
                    ctx.renderer.setPlacementGhost(false, null);
                    return;
                }
            }
            vertex.create(mx, my);
            int nv = ctx.selection.selectedVertex;
            if (nv >= 0) appendTraceVertex(nv);
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

        // 3. Empty space: clear the selection
        ctx.selection.reset();
    }

    /** Cancels the current modal state (dialog, menu, mode, or selection) step by step. */
    public void handleEscape() {
        if (ctx.ui.isConfirmSaveVisible()) { ctx.ui.closeConfirmSave(); return; }
        if (ctx.ui.isConfirmDeleteVisible()) { ctx.ui.closeConfirmDelete(); return; }
        if (ctx.help != null && ctx.help.isVisible()) { ctx.help.hide(); return; }
        if (ctx.ui.newMenu.isOpen()) { ctx.ui.newMenu.close(); return; }
        if (ctx.ui.isToolsOpen()) { ctx.ui.closeToolsPal(); return; }
        if (ctx.ui.filter.isOpen()) { ctx.ui.filter.setOpen(false); return; }
        if (ctx.ui.isOriginOpen()) { ctx.ui.closeOrigin(); return; }
        if (ctx.faceSelectPending) { tools.cancelFaceFromSelection(); return; }
        if (ctx.creatingVertex || ctx.creatingEdge) { ctx.exitModes(); ctx.ui.setActiveMode(-1); ctx.renderer.setTracePreview(null); return; }
        if (ctx.selection.selectedVertex >= 0 || ctx.selection.selectedEdge >= 0) {
            ctx.selection.reset();
            ctx.hoveredVertexId = -1;
            ctx.hoveredEdgeId = -1;
            return;
        }
    }

    /** Saves the current shape to disk. */
    public void save() { io.save(); }

    /** Deletes the currently selected vertices or edges. */
    public void deleteSelected() { del.deleteSelected(); }

    /** Applies one undo step, restoring the previous shape and selection. */
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

    /** Applies one redo step, restoring the next shape and selection. */
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

    /** Restores the given vertex (or fallback edge) selection after an undo/redo, if it still exists. */
    private void restoreSelection(int sv, int se) {
        if (sv >= 0 && ctx.renderer.getShapeData().vertices.containsKey(sv)) {
            ctx.selection.selectVertex(sv);
        } else if (se >= 0 && ctx.renderer.getShapeData().edges.containsKey(se)) {
            ctx.selection.selectEdge(se);
        }
    }

    /** Enters the vertex creation mode and resets the UI state. */
    private void onNewVertex() {
        ctx.exitModes();
        ctx.creatingVertex = true; ctx.ui.setActiveMode(0);
        ctx.ui.closeNewMenu(); ctx.selection.hideOverlays();
        ctx.selection.reset();
    }

    /** Enters the edge creation mode and resets the UI state. */
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

    /** Adds a traced point (reusing an existing vertex) to the current face loop,
     *  connecting it to the previous one. Closes the loop when the first vertex
     *  is clicked again with 3+ points. */
    private void appendTraceVertex(int vid) {
        if (vid < 0) return;
        if (ctx.traceVertices.isEmpty()) {
            ctx.traceVertices.add(vid);
            ctx.selection.selectVertex(vid);
            return;
        }
        int last = ctx.traceVertices.get(ctx.traceVertices.size() - 1);
        if (vid == last) return;
        if (ctx.traceVertices.size() >= 3 && vid == ctx.traceVertices.get(0)) {
            closeTrace();
            return;
        }
        edge.create(last, vid);
        ctx.traceVertices.add(vid);
        ctx.selection.selectVertex(vid);
        ctx.renderer.setPlacementGhost(false, null);
    }

    /** Returns whether any vertex (single or multi) is currently selected. */
    private boolean hasVertexSelection() {
        return ctx.selection.selectedVertex >= 0 || !ctx.selection.multiVertices.isEmpty();
    }

    /** Returns whether any edge (single or multi) is currently selected. */
    private boolean hasEdgeSelection() {
        return ctx.selection.selectedEdge >= 0 || !ctx.selection.multiEdges.isEmpty();
    }

    /** Dispatches a tool palette row to the corresponding action. */
    private void handleTool(int tool) {
        if (ctx.renderer.getShapeData() == null) return;
        if (ctx.faceSelectPending && tool != markershape.editor.ui.menu.ToolPalette.TOOL_CREATE_FACE) {
            tools.cancelFaceFromSelection();
        }
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
            case markershape.editor.ui.menu.ToolPalette.TOOL_CREATE_FACE -> tools.prepareFaceFromSelection();
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
