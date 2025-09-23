package learnGL.tools;

public class ComboTouche extends Touche {
    private Touche t1;

    public ComboTouche(Touche t1, int key, Runnable onPressAction, Runnable onReleaseAction, Runnable onHoldAction) {
        super(key, onPressAction, onReleaseAction, onHoldAction);
        this.t1 = t1;
    }

    @Override
    public boolean update(long window) {
        // Le combo est actif si t1 ET la touche principale sont pressées
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
