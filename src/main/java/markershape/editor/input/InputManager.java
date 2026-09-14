package markershape.editor.input;

import markershape.camera.EditorCamera;
import markershape.config.ConfigParametres;
import markershape.editor.Context;
import markershape.editor.action.*;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class InputManager {
    private final Context ctx;
    private final HoverManager hover;
    private final ClickHandler clicks;
    private final DragAction drag;
    private final EdgeAction edge;
    private final ShapeTools tools;
    private final EditorCamera camera;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private boolean escDown, prevMouseLeft, mouseLeftDown;
    private boolean rightDown, middleDown, shiftDown;
    private boolean grabMode;
    private float lastMouseX, lastMouseY;
    private float rightPressX, rightPressY;

    private int pendingDragVertex = -1;
    private float pressX, pressY;
    private static final float DRAG_THRESHOLD_PX = 5f;
    private static final int NAV_NONE = 0, NAV_PAN = 1, NAV_ORBIT = 2;
    private int navMode = NAV_NONE;

    private boolean marqueeActive;
    private float marqueeEndX, marqueeEndY;

    private boolean rubberActive;
    private int rubberVertex = -1;
    private int rubberTarget = -1;

    public InputManager(Context ctx, HoverManager hover, VertexAction vertex,
                        EdgeAction edge, DeleteAction del, ShapeIO io,
                        ShapeTools tools, EditorCamera camera) {
        this.ctx = ctx;
        this.hover = hover;
        this.camera = camera;
        this.tools = tools;
        this.edge = edge;
        this.drag = new DragAction(ctx);
        this.clicks = new ClickHandler(ctx, hover, vertex, edge, del, io, tools);
    }

    public void setKeyState(int key, int action) {
        boolean press = action == GLFW.GLFW_PRESS;
        if (key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT) shiftDown = press;
        if (press) pressedKeys.add(key);
        else if (action == GLFW.GLFW_RELEASE) pressedKeys.remove(key);
    }

    public void processFrameKeys() {
        if (ctx.selection.vertexOverlay.isTyping() || ctx.selection.edgeOverlay.isTyping()) return;
        for (int k : pressedKeys) {
            switch (k) {
                case GLFW.GLFW_KEY_UP    -> camera.rotate(0f, 1f);
                case GLFW.GLFW_KEY_DOWN  -> camera.rotate(0f, -1f);
                case GLFW.GLFW_KEY_LEFT  -> camera.rotate(1f, 0f);
                case GLFW.GLFW_KEY_RIGHT -> camera.rotate(-1f, 0f);
                case GLFW.GLFW_KEY_O     -> camera.zoom(1f);
                case GLFW.GLFW_KEY_P     -> camera.zoom(-1f);
            }
        }
    }

    public void process(float mx, float my) {
        if (ctx.ui.isConfirmSaveVisible()) {
            prevMouseLeft = mouseLeftDown;
            lastMouseX = mx;
            lastMouseY = my;
            return;
        }

        if (rightDown || middleDown) {
            float dx = mx - lastMouseX;
            float dy = my - lastMouseY;
            lastMouseX = mx;
            lastMouseY = my;
            boolean panWanted = middleDown || (shiftDown && rightDown);
            if (panWanted) {
                if (navMode == NAV_NONE) navMode = NAV_PAN;
                if (navMode == NAV_ORBIT) endOrbit();
                navMode = NAV_PAN;
            } else if (navMode == NAV_NONE) {
                navMode = NAV_ORBIT;
                beginOrbit(mx, my);
            } else if (navMode == NAV_PAN) {
                navMode = NAV_ORBIT;
                beginOrbit(mx, my);
            }
            if (navMode == NAV_PAN) {
                camera.pan(dx, dy);
            } else {
                camera.rotate(-dx * 0.15f, -dy * 0.15f);
                updateOrbitMarkerAxes();
            }
        } else {
            lastMouseX = mx;
            lastMouseY = my;
            navMode = NAV_NONE;
            endOrbit();
        }

        processDrag(mx, my);

        hover.update(mx, my);

        boolean escDownNow = GLFW.glfwGetKey(ctx.window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS;
        if (escDownNow && !escDown) {
            if (drag.isDragging() && grabMode) {
                drag.cancel();
                grabMode = false;
            } else if (!ctx.selection.vertexOverlay.isTyping()
                && !ctx.selection.edgeOverlay.isTyping()) {
                clicks.handleEscape();
            }
        }
        escDown = escDownNow;
    }

    public void onMouseButton(int btn, int action, float mx, float my) {
        if (btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (action == GLFW.GLFW_PRESS) {
                rightDown = true;
                rightPressX = mx;
                rightPressY = my;
            } else {
                rightDown = false;
                if (!ctx.ui.isOverUI(mx, my) && !ctx.selection.isOverOverlay(mx, my)) {
                    float ddx = mx - rightPressX, ddy = my - rightPressY;
                    if (ddx * ddx + ddy * ddy <= 36f && ctx.hoveredVertexId >= 0) {
                        pickColor(ctx.hoveredVertexId);
                    }
                }
            }
            lastMouseX = mx;
            lastMouseY = my;
            return;
        }
        if (btn == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            middleDown = action == GLFW.GLFW_PRESS;
            lastMouseX = mx;
            lastMouseY = my;
            return;
        }
        if (btn != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        mouseLeftDown = action == GLFW.GLFW_PRESS;
        if (action == GLFW.GLFW_PRESS) {
            if (ctx.ui.isConfirmSaveVisible()) {
                clicks.mouseClicked(mx, my);
                return;
            }
            if (grabMode) { endDrag(); return; }
            if (ctx.ui.isOverUI(mx, my) || ctx.selection.isOverOverlay(mx, my)
                || ctx.selection.siblingPicker.isVisible()) {
                clicks.mouseClicked(mx, my);
                return;
            }
            if (!ctx.creatingVertex && !ctx.creatingEdge && !ctx.creatingFace) {
                int vert = ctx.pick.findVisibleVertexAt(mx, my);
                if (vert >= 0) {
                    if (shiftDown) {
                        rubberVertex = vert;
                        rubberActive = true;
                        rubberTarget = -1;
                    } else {
                        pendingDragVertex = vert;
                        pressX = mx;
                        pressY = my;
                    }
                } else {
                    marqueeActive = true;
                    pressX = mx;
                    pressY = my;
                    marqueeEndX = mx;
                    marqueeEndY = my;
                    ctx.renderer.setMarquee(true, mx, my, mx, my);
                }
            }
            clicks.mouseClicked(mx, my);
        } else {
            endDrag();
        }
    }

    /** Right-click tap on a vertex: its color becomes the creation color. */
    private void pickColor(int vertexId) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        Vertex v = data.vertices.get(vertexId);
        if (v == null) return;
        ConfigParametres cfg = ConfigParametres.get();
        cfg.setFloat("defaultVertexColorR", v.r);
        cfg.setFloat("defaultVertexColorG", v.g);
        cfg.setFloat("defaultVertexColorB", v.b);
        ConfigParametres.sauvegarder();
        System.out.printf("[MarkerShape] couleur pipette #%d -> (R=%.2f G=%.2f B=%.2f) (creation)%n",
            vertexId, v.r, v.g, v.b);
    }

    private void processDrag(float mx, float my) {
        if (!mouseLeftDown && !grabMode) return;
        if (grabMode) {
            if (drag.isDragging()) drag.update(mx, my);
            return;
        }
        if (!mouseLeftDown) return;
        if (rubberActive) {
            updateRubber(mx, my);
            return;
        }
        if (drag.isDragging()) {
            drag.update(mx, my);
            return;
        }
        if (marqueeActive) {
            marqueeEndX = mx;
            marqueeEndY = my;
            ctx.renderer.setMarquee(true, pressX, pressY, mx, my);
            return;
        }
        if (pendingDragVertex < 0) return;
        if (ctx.selection.selectedVertex != pendingDragVertex
            && !ctx.selection.multiVertices.contains(pendingDragVertex)) { endDrag(); return; }
        if (ctx.selection.siblingPicker.isVisible()) { endDrag(); return; }
        float ddx = mx - pressX, ddy = my - pressY;
        if (ddx * ddx + ddy * ddy >= DRAG_THRESHOLD_PX * DRAG_THRESHOLD_PX) {
            drag.start(pendingDragVertex, pressX, pressY);
        }
    }

    private void updateRubber(float mx, float my) {
        ShapeData d = ctx.renderer.getShapeData();
        if (d == null) return;
        rubberTarget = ctx.pick.findVisibleVertexAt(mx, my);
        Vertex src = d.vertices.get(rubberVertex);
        if (src == null) return;
        Matrix4f mvp = new Matrix4f(ctx.pick.getProjection());
        mvp.mul(ctx.pick.getView());
        Vector4f p = new Vector4f(src.x, src.y, src.z, 1f).mul(mvp);
        float sx = -1000f, sy = -1000f;
        if (p.w > 0) {
            sx = (p.x / p.w * 0.5f + 0.5f) * ctx.windowWidth;
            sy = (1f - (p.y / p.w * 0.5f + 0.5f)) * ctx.windowHeight;
        }
        float tx = mx, ty = my;
        if (rubberTarget >= 0) {
            Vertex tv = d.vertices.get(rubberTarget);
            if (tv != null) {
                p.set(tv.x, tv.y, tv.z, 1f).mul(mvp);
                if (p.w > 0) {
                    tx = (p.x / p.w * 0.5f + 0.5f) * ctx.windowWidth;
                    ty = (1f - (p.y / p.w * 0.5f + 0.5f)) * ctx.windowHeight;
                }
            }
        }
        ctx.renderer.setRubberBand(sx, sy, tx, ty);
    }

    private void endRubber() {
        rubberActive = false;
        ctx.renderer.clearRubberBand();
        if (rubberVertex >= 0 && rubberTarget >= 0 && rubberVertex != rubberTarget) {
            edge.create(rubberVertex, rubberTarget);
        }
        rubberVertex = -1;
        rubberTarget = -1;
    }

    private void endMarquee() {
        marqueeActive = false;
        ctx.renderer.setMarquee(false, 0f, 0f, 0f, 0f);
        float x1 = Math.min(pressX, marqueeEndX), x2 = Math.max(pressX, marqueeEndX);
        float y1 = Math.min(pressY, marqueeEndY), y2 = Math.max(pressY, marqueeEndY);
        if (x2 - x1 < 4f || y2 - y1 < 4f) return;
        ShapeData d = ctx.renderer.getShapeData();
        if (d == null) return;
        Matrix4f mvp = new Matrix4f(ctx.pick.getProjection());
        mvp.mul(ctx.pick.getView());
        TreeSet<Integer> sel = new TreeSet<>();
        Vector4f p = new Vector4f();
        for (Vertex v : d.vertices.values()) {
            p.set(v.x, v.y, v.z, 1f).mul(mvp);
            if (p.w <= 0) continue;
            float sx = (p.x / p.w * 0.5f + 0.5f) * ctx.windowWidth;
            float sy = (1f - (p.y / p.w * 0.5f + 0.5f)) * ctx.windowHeight;
            if (sx >= x1 && sx <= x2 && sy >= y1 && sy <= y2) sel.add(v.id);
        }
        if (sel.isEmpty()) return;
        ctx.selection.multiVertices.clear();
        ctx.selection.multiEdges.clear();
        ctx.selection.selectedEdge = -1;
        ctx.selection.multiVertices.addAll(sel);
        ctx.selection.selectedVertex = sel.first();
        ctx.selection.refreshSelectionVisual();
    }

    private void endDrag() {
        pendingDragVertex = -1;
        grabMode = false;
        if (drag.isDragging()) { drag.end(); return; }
        if (rubberActive) { endRubber(); return; }
        if (marqueeActive) { endMarquee(); return; }
    }

    private final org.joml.Vector3f orbitPivot = new org.joml.Vector3f();
    private boolean orbitPivotSet;

    private void beginOrbit(float mx, float my) {
        orbitPivot.set(ctx.pick.getClickWorldPos(mx, my));
        orbitPivotSet = true;
        float size = camera.getRadius() * 0.3f;
        ctx.renderer.setOrbitPivotMarker(true, orbitPivot, Math.max(0.8f, size));
        updateOrbitMarkerAxes();
    }

    private void updateOrbitMarkerAxes() {
        if (!orbitPivotSet) return;
        org.joml.Vector3f right = camera.getRight();
        org.joml.Vector3f up = camera.getUp();
        ctx.renderer.setOrbitPivotMarkerAxes(right.x, right.y, right.z, up.x, up.y, up.z);
    }

    private void endOrbit() {
        if (orbitPivotSet) {
            orbitPivotSet = false;
            ctx.renderer.setOrbitPivotMarker(false, null, 0f);
        }
    }

    private boolean hasVertexSelection() {
        return ctx.selection.selectedVertex >= 0 || !ctx.selection.multiVertices.isEmpty();
    }

    /** G grab: the selection follows the cursor until LMB confirms / ESC cancels. */
    private void startGrab() {
        ShapeData d = ctx.renderer.getShapeData();
        if (d == null) return;
        int id = ctx.selection.selectedVertex;
        if (id < 0 && !ctx.selection.multiVertices.isEmpty()) id = ctx.selection.multiVertices.first();
        if (id < 0) return;
        grabMode = true;
        drag.start(id, lastMouseX, lastMouseY);
    }

    private boolean hasEdgeSelection() {
        return ctx.selection.selectedEdge >= 0 || !ctx.selection.multiEdges.isEmpty();
    }

    public void handleKey(int key, int scancode, int action, int mods) {
        if (action != GLFW.GLFW_PRESS) return;
        boolean ctrl = (mods & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean alt = (mods & GLFW.GLFW_MOD_ALT) != 0;
        boolean shift = (mods & GLFW.GLFW_MOD_SHIFT) != 0;

        if (ctrl) {
            if (key == GLFW.GLFW_KEY_Z || key == GLFW.GLFW_KEY_W) {
                if (shift) clicks.redo();
                else clicks.undo();
                return;
            }
            if (key == GLFW.GLFW_KEY_Y) { clicks.redo(); return; }
            if (key == GLFW.GLFW_KEY_S) { clicks.save(); return; }
            if (key == GLFW.GLFW_KEY_D) { tools.duplicateSelected(); return; }
            if (key == GLFW.GLFW_KEY_C) { tools.copySelected(); return; }
            if (key == GLFW.GLFW_KEY_V) { tools.pasteSelected(); return; }
            if (key == GLFW.GLFW_KEY_A) { tools.selectAll(); return; }
            if (key == GLFW.GLFW_KEY_F) {
                camera.captureFront();
                saveFrontToConfig();
                System.out.printf("[MarkerShape] front capturé : yaw=%.1f pitch=%.1f%n",
                    camera.getFrontYaw(), camera.getFrontPitch());
                return;
            }
        }

        if (key == GLFW.GLFW_KEY_H) {
            ctx.help.toggle();
            return;
        }

        if (key == GLFW.GLFW_KEY_T && !ctx.creatingFace && ctx.renderer.getShapeData() != null) {
            clicks.onNewTrace();
            return;
        }
        if (key == GLFW.GLFW_KEY_ENTER && ctx.creatingFace) {
            clicks.closeTrace();
            return;
        }
        if (key == GLFW.GLFW_KEY_G && !ctx.isInMode() && !ctx.creatingFace
            && !drag.isDragging() && !grabMode) {
            startGrab();
            return;
        }

        if (drag.isDragging()) {
            if (key == GLFW.GLFW_KEY_X) { drag.axis = 1; return; }
            if (key == GLFW.GLFW_KEY_Y) { drag.axis = 2; return; }
            if (key == GLFW.GLFW_KEY_Z) { drag.axis = 3; return; }
            if (key == GLFW.GLFW_KEY_G || key == GLFW.GLFW_KEY_ESCAPE) { drag.axis = 0; return; }
        }

        if (key == GLFW.GLFW_KEY_K) {
            System.out.println("[MarkerShape] " + tools.cleanupShape());
            return;
        }
        if (key == GLFW.GLFW_KEY_S && hasEdgeSelection()) {
            tools.splitEdge(ctx.selection.selectedEdge >= 0 ? ctx.selection.selectedEdge : ctx.selection.multiEdges.first());
            return;
        }
        if (key == GLFW.GLFW_KEY_E && hasEdgeSelection()) {
            tools.extrudeSelectedEdge();
            return;
        }
        if (key == GLFW.GLFW_KEY_M && hasVertexSelection()) {
            tools.weldSelected();
            return;
        }
        if (key == GLFW.GLFW_KEY_F) {
            if (hasEdgeSelection() || alt) {
                tools.fillSelection();
                return;
            }
            camera.captureFront();
            saveFrontToConfig();
            System.out.printf("[MarkerShape] front capturé : yaw=%.1f pitch=%.1f%n",
                camera.getFrontYaw(), camera.getFrontPitch());
            return;
        }
        if (key == GLFW.GLFW_KEY_R && !drag.isDragging()) {
            resetViewToFront();
            return;
        }
        if ((key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE)
            && (hasVertexSelection() || hasEdgeSelection()))
            clicks.deleteSelected();
    }

    private void resetViewToFront() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null || data.vertices.isEmpty()) return;
        float[] bb = bounds(data);
        float cx = (bb[0] + bb[3]) / 2f, cy = (bb[1] + bb[4]) / 2f, cz = (bb[2] + bb[5]) / 2f;
        float size = Math.max(bb[3] - bb[0], Math.max(bb[4] - bb[1], bb[5] - bb[2]));
        camera.resetToFront(new org.joml.Vector3f(cx, cy, cz), size);
    }

    private float[] bounds(ShapeData data) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        for (Vertex v : data.vertices.values()) {
            minX = Math.min(minX, v.x); maxX = Math.max(maxX, v.x);
            minY = Math.min(minY, v.y); maxY = Math.max(maxY, v.y);
            minZ = Math.min(minZ, v.z); maxZ = Math.max(maxZ, v.z);
        }
        return new float[]{minX, minY, minZ, maxX, maxY, maxZ};
    }

    private void saveFrontToConfig() {
        ConfigParametres cfg = ConfigParametres.get();
        cfg.setFloat("frontYaw", camera.getFrontYaw());
        cfg.setFloat("frontPitch", camera.getFrontPitch());
        ConfigParametres.sauvegarder();
    }
}