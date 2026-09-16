package markershape.editor.ui.menu;

/**
 * Holds the alpha values used by the menu/panel UI, which switch between a
 * transparent and a solid look depending on the transparent-UI toggle.
 */
public class BlurBackground {
    public static boolean transparentUI = true;
    public static float menuR = 0.12f, menuG = 0.12f, menuB = 0.18f;

    /** @return the alpha used for menu panels. */
    public static float panelAlpha() { return transparentUI ? 0.3f : 0.85f; }
    /** @return the alpha used for highlighted/alternate rows inside panels. */
    public static float rowAlpha()   { return transparentUI ? 0.35f : 0.85f; }
    /** @return the alpha used for buttons. */
    public static float btnAlpha()   { return transparentUI ? 0.45f : 0.95f; }
    /** @return the alpha used for the dark overlay dimming the background. */
    public static float dimAlpha()   { return transparentUI ? 0.45f : 0.55f; }
    /** @return the alpha used for modal message/confirm boxes. */
    public static float boxAlpha()   { return transparentUI ? 0.7f : 0.9f; }
}
