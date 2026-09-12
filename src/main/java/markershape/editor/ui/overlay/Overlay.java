package markershape.editor.ui.overlay;

import markershape.config.ConfigParametres;
import markershape.editor.ui.UIElement;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.control.Button;
import markershape.editor.ui.menu.BlurBackground;

public abstract class Overlay extends UIElement {
    protected float px, py;
    protected final float pw, ph;
    protected final Button closeBtn;
    protected final Button deleteBtn;
    protected int selectedField = -1;
    protected Runnable editCallback;
    protected Runnable preEditCallback;
    protected Runnable deleteCallback;

    public Overlay(UIResources res, float pw, float ph) {
        super(res);
        this.pw = pw;
        this.ph = ph;
        px = 100; py = 100;
        visible = false;
        closeBtn = new Button(res, "X", px + pw - 28, py + 4, 24, 24, null);
        closeBtn.showBackground = false;
        closeBtn.textScale = 1.5f;
        closeBtn.textR = 1f; closeBtn.textG = 0.3f; closeBtn.textB = 0.3f;
        deleteBtn = new Button(res, "Delete", px + 10, py + ph - 38, pw - 20, 28,
            () -> { if (deleteCallback != null) deleteCallback.run(); });
        deleteBtn.bgR = 0.5f; deleteBtn.bgG = 0.1f; deleteBtn.bgB = 0.1f;
        deleteBtn.textR = 1f; deleteBtn.textG = 1f; deleteBtn.textB = 1f;
    }

    public void setDeleteCallback(Runnable cb) { deleteCallback = cb; }
    public void hide() { visible = false; selectedField = -1; }
    public boolean isVisible() { return visible; }
    public boolean isCloseClicked(float mx, float my) { return visible && closeBtn.contains(mx, my); }
    public boolean contains(float mx, float my) {
        return mx >= px && mx <= px + pw && my >= py && my <= py + ph;
    }
    public void setEditCallback(Runnable cb) { editCallback = cb; }
    public void setPreEditCallback(Runnable cb) { preEditCallback = cb; }

    protected abstract boolean hasEntity();

    @Override
    public final void render() {
        if (!visible || !hasEntity()) return;

        res.begin2D();

        float[] c = res.menuColor();
        res.drawQuad(px, py, pw, ph, c[0], c[1], c[2], BlurBackground.panelAlpha());

        renderContent();

        renderText();

        ConfigParametres cfg = ConfigParametres.get();
        deleteBtn.textR = cfg.getFloat("textR") / 255f;
        deleteBtn.textG = cfg.getFloat("textG") / 255f;
        deleteBtn.textB = cfg.getFloat("textB") / 255f;
        deleteBtn.bgA = BlurBackground.btnAlpha();
        deleteBtn.render();
        closeBtn.render();
    }

    protected abstract void renderContent();

    protected abstract void renderText();

    public void setPosition(float x, float y) {
        px = x;
        py = y;
        closeBtn.x = px + pw - 28;
        closeBtn.y = py + 4;
        deleteBtn.x = px + 10;
        deleteBtn.y = py + ph - 38;
    }

    public float getPx() { return px; }
    public float getPy() { return py; }
    public float getPw() { return pw; }
    public float getPh() { return ph; }
}