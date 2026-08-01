package markershape.editor.ui.menu;

public class BlurBackground {
    public static boolean transparentUI = true;
    public static float menuR = 0.12f, menuG = 0.12f, menuB = 0.18f;

    public static float panelAlpha() { return transparentUI ? 0.3f : 0.85f; }
    public static float rowAlpha()   { return transparentUI ? 0.35f : 0.85f; }
    public static float btnAlpha()   { return transparentUI ? 0.45f : 0.95f; }
    public static float dimAlpha()   { return transparentUI ? 0.45f : 0.55f; }
    public static float boxAlpha()   { return transparentUI ? 0.7f : 0.9f; }
}
