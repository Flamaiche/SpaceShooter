package learngl.tools.commandes;

/**
 * A composite key binding that is active only when a modifier Touche AND
 * a secondary key are both pressed simultaneously.
 */
public class ComboTouche extends Touche {
    private final Touche t1;

    /**
     * Creates a combo binding requiring the given modifier Touche and a
     * secondary key to be pressed together.
     *
     * @param t1              the modifier Touche that must also be held
     * @param key             the secondary GLFW key constant
     * @param onPressAction   action on combo press
     * @param onReleaseAction action on combo release
     * @param onHoldAction    action each frame while combo is held
     */
    public ComboTouche(Touche t1, int key, Runnable onPressAction, Runnable onReleaseAction, Runnable onHoldAction) {
        super(key, onPressAction, onReleaseAction, onHoldAction);
        this.t1 = t1;
    }

    @Override
    public boolean update(long window) {
        boolean comboActive = t1.isPressed(window) && isPressed(window);
        boolean inAction = false;

        if (comboActive) {
            if (onPressAction != null && !wasPressed) {
                onPressAction.run();
                inAction = true;
            }
            if (onHoldAction != null) {
                onHoldAction.run();
                inAction = true;
            }
        } else {
            if (onReleaseAction != null && wasPressed) {
                onReleaseAction.run();
                inAction = true;
            }
        }

        wasPressed = comboActive;
        return inAction;
    }

    @Override
    public boolean isPressed(long window) {
        return super.isPressed(window) && t1.isPressed(window);
    }
}
