package markershape.editor.ui.overlay;

import markershape.editor.ui.framework.UIContainer;
import markershape.editor.ui.framework.UIButton;
import markershape.editor.ui.framework.UIRenderer;
import markershape.editor.ui.framework.UIText;
import markershape.editor.ui.menu.BlurBackground;

public abstract class Overlay {
    protected boolean visible;
    protected float px, py;
    protected final float pw, ph;
    protected int selectedField = -1;
    protected Runnable editCallback;
    protected Runnable preEditCallback;
    protected Runnable deleteCallback;
    protected final UIRenderer renderer = new UIRenderer();
    protected final UIContainer root = new UIContainer(0, 0, 1, 1);
    protected int width, height;

    protected static final float CLOSE_BTN_X = 28;
    protected static final float CLOSE_BTN_W = 24;
    protected static final float CLOSE_BTN_H = 24;
    protected static final float DELETE_BTN_H = 28;

    private static float ts(float designScale) { return designScale / 720f; }

    public Overlay(float pw, float ph) {
        this.pw = pw;
        this.ph = ph;
        px = 100; py = 100;
        root.alpha = UIContainer.Alpha.NONE;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        renderer.setScreenSize(w, h);
    }

    public void setDeleteCallback(Runnable cb) { deleteCallback = cb; }
    public void hide() { visible = false; selectedField = -1; }
    public boolean isVisible() { return visible; }
    public boolean isCloseClicked(float mx, float my) {
        return visible && mx >= px + pw - CLOSE_BTN_X && mx <= px + pw - CLOSE_BTN_X + CLOSE_BTN_W
            && my >= py + 4 && my <= py + 4 + CLOSE_BTN_H;
    }
    public boolean isDeleteClicked(float mx, float my) {
        return visible && mx >= px + 10 && mx <= px + 10 + pw - 20
            && my >= py + ph - DELETE_BTN_H - 10 && my <= py + ph - 10;
    }
    public boolean contains(float mx, float my) {
        return mx >= px && mx <= px + pw && my >= py && my <= py + ph;
    }
    public void setEditCallback(Runnable cb) { editCallback = cb; }
    public void setPreEditCallback(Runnable cb) { preEditCallback = cb; }

    protected abstract boolean hasEntity();

    public void render() {
        if (!visible || !hasEntity()) return;
        root.clear();
        build();
        root.render(renderer);
    }

    private void build() {
        UIContainer panel = new UIContainer(px / width, py / height, pw / width, ph / height);
        panel.alpha = UIContainer.Alpha.PANEL;
        panel.bgR = BlurBackground.menuR;
        panel.bgG = BlurBackground.menuG;
        panel.bgB = BlurBackground.menuB;
        root.add(panel);

        buildContent(panel);

        UIContainer delete = new UIContainer(10f / pw, (ph - DELETE_BTN_H - 10f) / ph,
            (pw - 20f) / pw, DELETE_BTN_H / ph);
        delete.alpha = UIContainer.Alpha.BTN;
        delete.bgR = 0.5f; delete.bgG = 0.1f; delete.bgB = 0.1f;
        delete.onClickAction = () -> { if (deleteCallback != null) deleteCallback.run(); };
        panel.add(delete);
        UIButton delLabel = new UIButton(0, 0, 1, 1, "Delete", ts(1.5f));
        delLabel.alpha = UIContainer.Alpha.NONE;
        delLabel.useConfigText = false;
        delLabel.tR = 1f; delLabel.tG = 1f; delLabel.tB = 1f;
        delete.add(delLabel);

        UIContainer close = new UIContainer((pw - CLOSE_BTN_X) / pw, 4f / ph,
            CLOSE_BTN_W / pw, CLOSE_BTN_H / ph);
        close.alpha = UIContainer.Alpha.NONE;
        close.onClickAction = this::hide;
        panel.add(close);
        UIText closeLabel = new UIText(0.5f, 0.5f, ts(1.5f), "X");
        closeLabel.centered = true;
        closeLabel.useConfigText = false;
        closeLabel.tR = 1f; closeLabel.tG = 0.3f; closeLabel.tB = 0.3f;
        close.add(closeLabel);
    }

    protected abstract void buildContent(UIContainer panel);

    protected UIText label(float x, float y, String s, float r, float g, float b) {
        UIText t = new UIText(x / pw, y / ph, ts(1.5f), s);
        t.useConfigText = false;
        t.tR = r; t.tG = g; t.tB = b;
        return t;
    }

    protected UIContainer box(float x, float y, float w, float h, float r, float g, float b, float a) {
        UIContainer c = new UIContainer(x / pw, y / ph, w / pw, h / ph);
        c.customAlpha = a;
        c.bgR = r; c.bgG = g; c.bgB = b;
        return c;
    }

    public void setPosition(float x, float y) {
        px = x;
        py = y;
    }

    public float getPx() { return px; }
    public float getPy() { return py; }
    public float getPw() { return pw; }
    public float getPh() { return ph; }

    public void cleanup() {
        renderer.cleanup();
    }
}
