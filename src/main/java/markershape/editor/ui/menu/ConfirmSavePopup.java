package markershape.editor.ui.menu;

import markershape.editor.ui.framework.UIButton;
import markershape.editor.ui.framework.UIContainer;
import markershape.editor.ui.framework.UIRenderer;
import markershape.editor.ui.framework.UIText;

public class ConfirmSavePopup {
    private int width, height;
    private boolean visible;
    private Runnable confirmAction;
    private final UIRenderer renderer = new UIRenderer();
    private final UIContainer root = new UIContainer(0, 0, 1, 1);

    public static final float CONFIRM_W = 220;
    public static final float CONFIRM_H = 100;
    public static final float CONFIRM_BTN_W = 70;
    public static final float CONFIRM_BTN_H = 28;

    private static float ts(float designScale) { return designScale / 720f; }

    public ConfirmSavePopup() {
        root.alpha = UIContainer.Alpha.NONE;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        renderer.setScreenSize(w, h);
    }

    public boolean isVisible() { return visible; }
    public void show() { visible = true; }
    public void close() { visible = false; }
    public void setConfirmAction(Runnable r) { confirmAction = r; }
    public Runnable getConfirmAction() { return confirmAction; }

    public boolean contains(float mx, float my) {
        if (!visible) return false;
        float cx = (width - CONFIRM_W) / 2;
        float cy = (36 + (height - 36) / 2) - CONFIRM_H / 2;
        return mx >= cx && mx <= cx + CONFIRM_W && my >= cy && my <= cy + CONFIRM_H;
    }

    /** Returns 1=Oui, 2=Non, 0=click on popup (no btn), -1=not on popup. */
    public int click(float mx, float my) {
        if (!visible) return -1;
        float cx = (width - CONFIRM_W) / 2;
        float cy = (36 + (height - 36) / 2) - CONFIRM_H / 2;
        float btnY = cy + CONFIRM_H - CONFIRM_BTN_H - 12;
        float ouiX = cx + 20;
        float nonX = cx + CONFIRM_W - 20 - CONFIRM_BTN_W;
        if (my >= btnY && my <= btnY + CONFIRM_BTN_H) {
            if (mx >= ouiX && mx <= ouiX + CONFIRM_BTN_W) return 1;
            if (mx >= nonX && mx <= nonX + CONFIRM_BTN_W) return 2;
        }
        return 0;
    }

    public void render() {
        if (!visible) return;
        root.clear();
        build();
        root.render(renderer);
    }

    private void build() {
        float cx = (width - CONFIRM_W) / 2;
        float cy = (36 + (height - 36) / 2) - CONFIRM_H / 2;

        UIContainer box = new UIContainer(cx / width, cy / height,
            CONFIRM_W / width, CONFIRM_H / height);
        box.alpha = UIContainer.Alpha.BOX;
        box.bgR = BlurBackground.menuR;
        box.bgG = BlurBackground.menuG;
        box.bgB = BlurBackground.menuB;
        root.add(box);

        UIText title = new UIText(0.5f, 16f / CONFIRM_H, ts(1.5f), "Sauvegarder ?");
        title.centered = true;
        box.add(title);

        float btnY = (CONFIRM_H - CONFIRM_BTN_H - 10f) / CONFIRM_H;
        UIButton oui = new UIButton(20f / CONFIRM_W, btnY,
            CONFIRM_BTN_W / CONFIRM_W, CONFIRM_BTN_H / CONFIRM_H, "Oui", ts(1.5f));
        oui.alpha = UIContainer.Alpha.NONE;
        box.add(oui);

        UIButton non = new UIButton((CONFIRM_W - 20f - CONFIRM_BTN_W) / CONFIRM_W, btnY,
            CONFIRM_BTN_W / CONFIRM_W, CONFIRM_BTN_H / CONFIRM_H, "Non", ts(1.5f));
        non.alpha = UIContainer.Alpha.NONE;
        box.add(non);
    }

    public void cleanup() {
        renderer.cleanup();
    }
}
