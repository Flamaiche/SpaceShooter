package markershape.editor.input;

import markershape.editor.Context;
import markershape.editor.action.*;
import org.lwjgl.glfw.GLFW;

public class InputManager {
    private final Context ctx;
    private final HoverManager hover;
    private final DragAction drag;
    private final ClickHandler clicks;
    private boolean escDown, prevMouseLeft, mouseLeftDown;

    public InputManager(Context ctx, HoverManager hover, VertexAction vertex,
                        EdgeAction edge, DragAction drag, DeleteAction del, ShapeIO io) {
        this.ctx = ctx;
        this.hover = hover;
        this.drag = drag;
        this.clicks = new ClickHandler(ctx, hover, vertex, edge, drag, del, io);
    }

    public void process(float mx, float my) {
        if (ctx.ui.isConfirmSaveVisible()) {
            prevMouseLeft = mouseLeftDown;
            return;
        }

        hover.update(mx, my);

        if (drag.isDragging()) {
            drag.update(mx, my);
            if (!mouseLeftDown) drag.end();
        }

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
            if (key == GLFW.GLFW_KEY_Z) {
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
