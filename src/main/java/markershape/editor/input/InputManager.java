package markershape.editor.input;

import markershape.camera.EditorCamera;
import markershape.editor.Context;
import markershape.editor.action.*;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class InputManager {
    private final Context ctx;
    private final HoverManager hover;
    private final ClickHandler clicks;
    private final EditorCamera camera;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private boolean escDown, prevMouseLeft, mouseLeftDown;

    public InputManager(Context ctx, HoverManager hover, VertexAction vertex,
                        EdgeAction edge, DeleteAction del, ShapeIO io, EditorCamera camera) {
        this.ctx = ctx;
        this.hover = hover;
        this.camera = camera;
        this.clicks = new ClickHandler(ctx, hover, vertex, edge, del, io);
    }

    public void setKeyState(int key, int action) {
        if (action == GLFW.GLFW_PRESS) pressedKeys.add(key);
        else if (action == GLFW.GLFW_RELEASE) pressedKeys.remove(key);
    }

    public void processFrameKeys() {
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
            return;
        }

        hover.update(mx, my);

        boolean escDownNow = GLFW.glfwGetKey(ctx.window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS;
        if (escDownNow && !escDown) clicks.handleEscape();
        escDown = escDownNow;
    }

    public void onMouseButton(int btn, int action, float mx, float my) {
        if (btn != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        mouseLeftDown = action == GLFW.GLFW_PRESS;
        if (action == GLFW.GLFW_PRESS) {
            if (ctx.ui.isConfirmSaveVisible()) {
                clicks.mouseClicked(mx, my);
                return;
            }
            clicks.mouseClicked(mx, my);
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
        if ((key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE)
            && (ctx.selection.selectedVertex >= 0 || ctx.selection.selectedEdge >= 0))
            clicks.deleteSelected();
    }
}
