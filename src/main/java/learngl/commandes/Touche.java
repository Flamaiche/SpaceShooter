package learngl.commandes;

import org.lwjgl.glfw.GLFW;

/**
 * Represents a single key or mouse button binding with press, release, and
 * hold action callbacks. Supports both keyboard and mouse input sources.
 */
public class Touche {
    protected int keyOrButton;
    protected boolean isMouse = false;
    protected Runnable onPressAction;
    protected Runnable onReleaseAction;
    protected Runnable onHoldAction;

    protected boolean wasPressed = false;
    private boolean active = true;
    private boolean ignoreNextPress = false;

    /**
     * Creates a keyboard key binding.
     *
     * @param key             the GLFW key constant
     * @param onPressAction   action to run on initial press
     * @param onReleaseAction action to run on release
     * @param onHoldAction    action to run each frame while held
     */
    public Touche(int key, Runnable onPressAction, Runnable onReleaseAction, Runnable onHoldAction) {
        this.keyOrButton = key;
        this.onPressAction = onPressAction;
        this.onReleaseAction = onReleaseAction;
        this.onHoldAction = onHoldAction;
    }

    /**
     * Creates a mouse button binding.
     *
     * @param button          the GLFW mouse button constant
     * @param isMouse         must be true to indicate a mouse binding
     * @param onPressAction   action to run on initial press
     * @param onReleaseAction action to run on release
     * @param onHoldAction    action to run each frame while held
     */
    public Touche(int button, boolean isMouse, Runnable onPressAction, Runnable onReleaseAction, Runnable onHoldAction) {
        this.keyOrButton = button;
        this.isMouse = isMouse;
        this.onPressAction = onPressAction;
        this.onReleaseAction = onReleaseAction;
        this.onHoldAction = onHoldAction;
    }

    /**
     * Updates the binding state for the current frame: checks whether the key
     * or button is pressed, fires the appropriate callbacks, and returns whether
     * any action was executed.
     *
     * @param window the GLFW window handle
     * @return true if any callback was invoked this frame
     */
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

    /**
     * Returns whether the key or button is currently pressed.
     *
     * @param window the GLFW window handle
     * @return true if currently pressed
     */
    public boolean isPressed(long window) {
        if (!active) return false;
        if (isMouse) {
            return GLFW.glfwGetMouseButton(window, keyOrButton) == GLFW.GLFW_PRESS;
        } else {
            return GLFW.glfwGetKey(window, keyOrButton) == GLFW.GLFW_PRESS;
        }
    }

    /**
     * Resets the pressed state tracking.
     */
    public void reset() {
        wasPressed = false;
    }

    /**
     * Returns the bound key or button code.
     *
     * @return the GLFW key or mouse button constant
     */
    public int getKey() { return keyOrButton; }

    /**
     * Sets the bound key or button code.
     *
     * @param keyOrButton the GLFW key or mouse button constant
     */
    public void setKey(int keyOrButton) { this.keyOrButton = keyOrButton; }

    /**
     * Enables or disables this binding.
     *
     * @param active true to enable, false to disable
     */
    public void setActive(boolean active) { this.active = active; }

    /**
     * Returns whether this binding is active.
     *
     * @return true if active
     */
    public boolean isActive() { return active; }

    /**
     * Returns whether the key was pressed in the previous frame.
     *
     * @return true if it was pressed last frame
     */
    public boolean wasPressed() { return wasPressed; }

    /**
     * Sets whether to ignore the next press detection (used during input mode switches).
     *
     * @param ignoreNextPress true to skip the next press event
     */
    public void setIgnoreNextPress(boolean ignoreNextPress) {
        this.ignoreNextPress = ignoreNextPress;
    }
}
