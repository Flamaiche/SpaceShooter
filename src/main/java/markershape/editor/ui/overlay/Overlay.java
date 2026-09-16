package markershape.editor.ui.overlay;

import markershape.config.ConfigParametres;
import markershape.editor.ui.UIElement;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.control.Button;
import markershape.editor.ui.menu.BlurBackground;

/**
 * Base class for modal editing overlays that edit a single shape entity.
 * Provides the close/delete buttons and the semi-transparent panel rendering.
 */
public abstract class Overlay extends UIElement {
    protected final Button closeBtn;
    protected final Button deleteBtn;
    protected int selectedField = -1;
    protected Runnable editCallback;
    protected Runnable preEditCallback;
    protected Runnable deleteCallback;

    /** Creates an overlay of the given size, with close and delete buttons. */
    public Overlay(UIResources res, float pw, float ph) {
        super(res);
        this.w = pw;
        this.h = ph;
        x = 100; y = 100;
        visible = false;
        closeBtn = new Button(res, "X", x + w - 28, y + 4, 24, 24, null);
        closeBtn.showBackground = false;
        closeBtn.textScale = 1.2f;
        closeBtn.textR = 1f; closeBtn.textG = 0.3f; closeBtn.textB = 0.3f;
        deleteBtn = new Button(res, "Delete", x + 10, y + h - 38, w - 20, 28,
            () -> { if (deleteCallback != null) deleteCallback.run(); });
        deleteBtn.bgR = 0.5f; deleteBtn.bgG = 0.1f; deleteBtn.bgB = 0.1f;
        deleteBtn.textR = 1f; deleteBtn.textG = 1f; deleteBtn.textB = 1f;
    }

    /** Sets the callback invoked when the delete button is clicked. */
    public void setDeleteCallback(Runnable cb) { deleteCallback = cb; }
    /** Hides the overlay and resets the selected field. */
    public void hide() { visible = false; selectedField = -1; }
    /** @return true if the overlay is currently visible. */
    public boolean isVisible() { return visible; }
    /** @return true if the given point is inside the close button while visible. */
    public boolean isCloseClicked(float mx, float my) { return visible && closeBtn.contains(mx, my); }
    /** @return true if the point lies within the overlay bounds. */
    @Override
    public boolean contains(float mx, float my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
    /** Sets the callback invoked after an edit has been applied. */
    public void setEditCallback(Runnable cb) { editCallback = cb; }
    /** Sets the callback invoked just before an edit is applied. */
    public void setPreEditCallback(Runnable cb) { preEditCallback = cb; }

    /** @return true if the overlay has a target entity to edit. */
    protected abstract boolean hasEntity();

    /**
     * Renders the overlay panel, its content and text, plus the close and
     * delete buttons, when visible and an entity is present.
     */
    @Override
    public final void render() {
        if (!visible || !hasEntity()) return;

        res.begin2D();

        float[] c = res.menuColor();
        res.drawQuad(x, y, w, h, c[0], c[1], c[2], BlurBackground.panelAlpha());

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

    /** Renders overlay-specific decorative content behind the text. */
    protected abstract void renderContent();

    /** Renders overlay-specific textual labels. */
    protected abstract void renderText();

    /** Moves the overlay and its buttons to the given position. */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        closeBtn.x = x + w - 28;
        closeBtn.y = y + 4;
        deleteBtn.x = x + 10;
        deleteBtn.y = y + h - 38;
    }
}