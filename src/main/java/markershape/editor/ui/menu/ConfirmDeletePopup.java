package markershape.editor.ui.menu;

import markershape.config.ConfigParametres;
import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;

public class ConfirmDeletePopup extends Panel {
    private int width, height;
    private Runnable confirmAction;

    public static final float CONFIRM_W = 220;
    public static final float CONFIRM_H = 100;
    public static final float CONFIRM_BTN_W = 70;
    public static final float CONFIRM_BTN_H = 28;

    public ConfirmDeletePopup(UIResources res) {
        super(res);
        visible = false;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        x = (w - CONFIRM_W) / 2;
        y = (36 + (h - 36) / 2) - CONFIRM_H / 2;
        this.w = CONFIRM_W;
        this.h = CONFIRM_H;
    }

    public boolean isVisible() { return visible; }
    public void show() { visible = true; }
    public void close() { visible = false; }
    public void setConfirmAction(Runnable r) { confirmAction = r; }
    public Runnable getConfirmAction() { return confirmAction; }

    @Override
    public boolean contains(float mx, float my) {
        if (!visible) return false;
        return mx >= x && mx <= x + CONFIRM_W && my >= y && my <= y + CONFIRM_H;
    }

    @Override
    protected void drawBackground() {
        float[] c = res.menuColor();
        res.drawQuad(0, 0, width, height, c[0], c[1], c[2], BlurBackground.dimAlpha());
        res.drawQuad(x, y, CONFIRM_W, CONFIRM_H, c[0], c[1], c[2], BlurBackground.boxAlpha());
    }

    @Override
    protected void renderContent() {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        res.drawText("Supprimer ?",
            x + CONFIRM_W / 2 - 36, y + 18, 1.2f, tR, tG, tB);
        res.drawText("[Oui]",
            x + 30, btnY() + 2, 1.2f, tR, tG, tB);
        res.drawText("[Non]",
            x + CONFIRM_W - 64, btnY() + 2, 1.2f, tR, tG, tB);
    }

    private float btnY() { return y + CONFIRM_H - CONFIRM_BTN_H - 12; }
    private float ouiX() { return x + 20; }
    private float nonX() { return x + CONFIRM_W - 20 - CONFIRM_BTN_W; }

    /** Returns 1=Oui, 2=Non, 0=click on popup (no btn), -1=not on popup. */
    public int clickBtn(float mx, float my) {
        if (!visible) return -1;
        float by = btnY();
        if (my >= by && my <= by + CONFIRM_BTN_H) {
            if (mx >= ouiX() && mx <= ouiX() + CONFIRM_BTN_W) return 1;
            if (mx >= nonX() && mx <= nonX() + CONFIRM_BTN_W) return 2;
        }
        return 0;
    }
}