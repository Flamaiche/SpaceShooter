package markershape.editor.ui.framework;

import markershape.editor.ui.menu.BlurBackground;

public class UIContainer extends UIElement {
    public enum Alpha { PANEL, ROW, BTN, BOX, DIM, OPAQUE, HIGHLIGHT, NONE }

    public Alpha alpha = Alpha.PANEL;
    public float bgR = Float.NaN;
    public float bgG = Float.NaN;
    public float bgB = Float.NaN;
    public Runnable onClickAction;

    public UIContainer() {}

    public UIContainer(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    @Override
    protected void renderSelf(UIRenderer r) {
        float a = switch (alpha) {
            case PANEL -> BlurBackground.panelAlpha();
            case ROW   -> BlurBackground.rowAlpha();
            case BTN   -> BlurBackground.btnAlpha();
            case BOX   -> BlurBackground.boxAlpha();
            case DIM   -> BlurBackground.dimAlpha();
            case OPAQUE -> 1f;
            case HIGHLIGHT -> 0.85f;
            case NONE  -> 0f;
        };
        if (a <= 0f) return;
        float cr = Float.isNaN(bgR) ? BlurBackground.menuR : bgR;
        float cg = Float.isNaN(bgG) ? BlurBackground.menuG : bgG;
        float cb = Float.isNaN(bgB) ? BlurBackground.menuB : bgB;
        r.quad(absX(r), absY(r), absW(r), absH(r), cr, cg, cb, a);
    }

    @Override
    protected boolean onClickSelf(float px, float py, UIRenderer r) {
        if (onClickAction != null) {
            onClickAction.run();
            return true;
        }
        return false;
    }
}
