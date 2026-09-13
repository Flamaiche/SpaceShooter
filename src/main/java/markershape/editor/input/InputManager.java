package markershape.editor.input;

import markershape.camera.EditorCamera;
import markershape.config.ConfigParametres;
import markershape.editor.Context;
import markershape.editor.action.*;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class InputManager {
    private final Context ctx;
    private final HoverManager hover;
    private final ClickHandler clicks;
    private final DragAction drag;
    private final EditorCamera camera;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private boolean escDown, prevMouseLeft, mouseLeftDown;
    private boolean rightDown, middleDown, shiftDown;
    private float lastMouseX, lastMouseY;

    private int pendingDragVertex = -1;
    private float pressX, pressY;
    private static final float DRAG_THRESHOLD_PX = 5f;
    private static final int NAV_NONE = 0, NAV_PAN = 1, NAV_ORBIT = 2;
    private int navMode = NAV_NONE;

    public InputManager(Context ctx, HoverManager hover, VertexAction vertex,
                        EdgeAction edge, DeleteAction del, ShapeIO io, EditorCamera camera) {
        this.ctx = ctx;
        this.hover = hover;
        this.camera = camera;
        this.drag = new DragAction(ctx);
        this.clicks = new ClickHandler(ctx, hover, vertex, edge, del, io);
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
        if (escDownNow && !escDown && !ctx.selection.vertexOverlay.isTyping()
            && !ctx.selection.edgeOverlay.isTyping()) clicks.handleEscape();
        escDown = escDownNow;
    }

    public void onMouseButton(int btn, int action, float mx, float my) {
        if (btn == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            rightDown = action == GLFW.GLFW_PRESS;
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
            if (ctx.ui.isOverUI(mx, my) || ctx.selection.isOverOverlay(mx, my)
                || ctx.selection.siblingPicker.isVisible()) {
                clicks.mouseClicked(mx, my);
                return;
            }
            int vert = ctx.pick.findVisibleVertexAt(mx, my);
            if (vert >= 0 && !ctx.creatingVertex && !ctx.creatingEdge) {
                pendingDragVertex = vert;
                pressX = mx;
                pressY = my;
            }
            clicks.mouseClicked(mx, my);
        } else {
            endDrag();
        }
    }

    private void processDrag(float mx, float my) {
        if (!mouseLeftDown) return;
        if (drag.isDragging()) {
            drag.update(mx, my);
            return;
        }
        if (pendingDragVertex < 0) return;
        if (ctx.selection.selectedVertex != pendingDragVertex) { endDrag(); return; }
        if (ctx.selection.siblingPicker.isVisible()) { endDrag(); return; }
        float ddx = mx - pressX, ddy = my - pressY;
        if (ddx * ddx + ddy * ddy >= DRAG_THRESHOLD_PX * DRAG_THRESHOLD_PX) {
            drag.start(pendingDragVertex, pressX, pressY);
        }
    }

    private void endDrag() {
        pendingDragVertex = -1;
        if (drag.isDragging()) drag.end();
    }

    private final org.joml.Vector3f orbitPivot = new org.joml.Vector3f();
    private boolean orbitPivotSet;

    private void beginOrbit(float mx, float my) {
        orbitPivot.set(ctx.pick.getClickWorldPos(mx, my));
        camera.setOrbitPivot(orbitPivot);
        orbitPivotSet = true;
        float size = camera.getRadius() * 0.12f;
        ctx.renderer.setOrbitPivotMarker(true, orbitPivot, Math.max(0.15f, size));
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

    public void handleKey(int key, int scancode, int action, int mods) {
        if (action != GLFW.GLFW_PRESS) return;
        if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
            if (key == GLFW.GLFW_KEY_Z || key == GLFW.GLFW_KEY_W) {
                if ((mods & GLFW.GLFW_MOD_SHIFT) != 0) clicks.redo();
                else clicks.undo();
                return;
            }
            if (key == GLFW.GLFW_KEY_S) { clicks.save(); return; }
        }
        if ((key == GLFW.GLFW_KEY_F) && !drag.isDragging()) {
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
        if (drag.isDragging()) {
            if (key == GLFW.GLFW_KEY_X) { drag.axis = 1; return; }
            if (key == GLFW.GLFW_KEY_Y) { drag.axis = 2; return; }
            if (key == GLFW.GLFW_KEY_Z) { drag.axis = 3; return; }
            if (key == GLFW.GLFW_KEY_G || key == GLFW.GLFW_KEY_ESCAPE) { drag.axis = 0; return; }
        }
        if ((key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE)
            && (ctx.selection.selectedVertex >= 0 || ctx.selection.selectedEdge >= 0))
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