package learnGL.tools.commandes;

import org.lwjgl.glfw.GLFW;

public class Touche {
    protected int keyOrButton;
    protected boolean isMouse = false;        // false = clavier, true = souris
    protected Runnable onPressAction;
    protected Runnable onReleaseAction;
    protected Runnable onHoldAction;

    protected boolean wasPressed = false;
    private boolean active = true;
    private boolean ignoreNextPress = false;

    // Constructeur clavier (par défaut)
    public Touche(int key, Runnable onPressAction, Runnable onReleaseAction, Runnable onHoldAction) {
        this.keyOrButton = key;
        this.onPressAction = onPressAction;
        this.onReleaseAction = onReleaseAction;
        this.onHoldAction = onHoldAction;
    }

    // Nouveau constructeur souris
    public Touche(int button, boolean isMouse, Runnable onPressAction, Runnable onReleaseAction, Runnable onHoldAction) {
        this.keyOrButton = button;
        this.isMouse = isMouse;
        this.onPressAction = onPressAction;
        this.onReleaseAction = onReleaseAction;
        this.onHoldAction = onHoldAction;
    }

    public boolean update(long window) {
        boolean inAction = false;
        if (!active) return false;

        boolean pressed;
        if (isMouse) {
            pressed = GLFW.glfwGetMouseButton(window, keyOrButton) == GLFW.GLFW_PRESS;
        } else {
            pressed = GLFW.glfwGetKey(window, keyOrButton) == GLFW.GLFW_PRESS;
        }

        if (ignoreNextPress) {
            wasPressed = pressed;
            ignoreNextPress = false;
            return false;
        }

        if (pressed) {
            if (!wasPressed && onPressAction != null) {
                onPressAction.run();
                inAction = true;
            }
            if (onHoldAction != null) {
                onHoldAction.run();
                inAction = true;
            }
        } else {
            if (wasPressed && onReleaseAction != null) {
                onReleaseAction.run();
                inAction = true;
            }
        }

        wasPressed = pressed;
        return inAction;
    }

    public boolean isPressed(long window) {
        if (!active) return false;
        if (isMouse) {
            return GLFW.glfwGetMouseButton(window, keyOrButton) == GLFW.GLFW_PRESS;
        } else {
            return GLFW.glfwGetKey(window, keyOrButton) == GLFW.GLFW_PRESS;
        }
    }

    public void reset() {
        wasPressed = false;
    }

    public int getKey() { return keyOrButton; }
    public void setKey(int keyOrButton) { this.keyOrButton = keyOrButton; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }
    public boolean wasPressed() { return wasPressed; }
    public void setIgnoreNextPress(boolean ignoreNextPress) {
        this.ignoreNextPress = ignoreNextPress;
    }
}
