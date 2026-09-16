package markershape.editor.ui.menu;

import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.control.Button;
import markershape.editor.ui.widgets.AutoWindow;

/** Modal popup confirming the deletion of the selected entity. */
public class ConfirmDeletePopup extends Panel {
    private int width, height;
    private Runnable confirmAction;

    public static final float CONFIRM_BTN_W = 70;
    public static final float CONFIRM_BTN_H = 28;

    private final AutoWindow box;
    private final Button ouiBtn;
    private final Button nonBtn;

    /** Creates the confirm popup with its Oui/Non buttons. */
    public ConfirmDeletePopup(UIResources res) {
        super(res);
        visible = false;
        ouiBtn = new Button(res, "[Oui]", 0, 0, CONFIRM_BTN_W, CONFIRM_BTN_H, () -> {});
        nonBtn = new Button(res, "[Non]", 0, 0, CONFIRM_BTN_W, CONFIRM_BTN_H, () -> {});
        box = new AutoWindow(res);
        box.setTitle("Supprimer ?");
        box.addChildRow(ouiBtn, nonBtn);
        box.showBackground = false;
        box.autoSize();
        this.w = box.w;
        this.h = box.h;
    }

    /** Sets the window size and centers the popup box. */
    public void setSize(int w, int h) {
        width = w;
        height = h;
        box.centerOn(w / 2f, 36 + (h - 36) / 2f);
        x = box.x;
        y = box.y;
    }

    /** @return true if the popup is visible. */
    public boolean isVisible() { return visible; }
    /** Shows the popup. */
    public void show() { visible = true; }
    /** Hides the popup. */
    public void close() { visible = false; }
    /** Sets the action run when the user confirms. */
    public void setConfirmAction(Runnable r) { confirmAction = r; }
    /** @return the action run when the user confirms. */
    public Runnable getConfirmAction() { return confirmAction; }

    /** @return true if the point is inside the visible popup box. */
    @Override
    public boolean contains(float mx, float my) {
        if (!visible) return false;
        return box.contains(mx, my);
    }

    /** Draws the dimming overlay and the popup box. */
    @Override
    protected void drawBackground() {
        float[] c = res.menuColor();
        res.drawQuad(0, 0, width, height, 0, 0, 0, BlurBackground.dimAlpha());
        res.drawQuad(box.x, box.y, box.w, box.h, c[0], c[1], c[2], BlurBackground.boxAlpha());
    }

    /** Renders the popup box contents. */
    @Override
    protected void renderContent() {
        box.render();
    }

    /** Returns 1=Oui, 2=Non, 0=click on popup (no btn), -1=not on popup. */
    public int clickBtn(float mx, float my) {
        if (!visible) return -1;
        if (ouiBtn.contains(mx, my)) return 1;
        if (nonBtn.contains(mx, my)) return 2;
        if (box.contains(mx, my)) return 0;
        return -1;
    }
}