package markershape.editor.ui.menu;

import markershape.config.ConfigParametres;
import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;

/** Dropdown under the "New" button offering the vertex and edge creation modes. */
public class NewMenu extends Panel {
    private boolean newMenuOpen;
    private int activeMode = -1; // -1=none, 0=vertex, 1=edge
    public static final float NEW_DROP_W = 130;
    public static final float NEW_ITEM_H = 22;

    /** Creates the new-mode dropdown, initially closed. */
    public NewMenu(UIResources res) {
        super(res);
    }

    /** @return true if the dropdown is open. */
    public boolean isOpen() { return newMenuOpen; }
    /** Opens or closes the dropdown. */
    public void setOpen(boolean v) { newMenuOpen = v; visible = v; }
    /** Toggles the dropdown open/closed state. */
    public void toggle() { newMenuOpen = !newMenuOpen; visible = newMenuOpen; }
    /** Closes the dropdown. */
    public void close() { newMenuOpen = false; visible = false; }

    /** Sets the active creation mode (0=vertex, 1=edge) and highlights its row. */
    public void setActiveMode(int mode) { activeMode = mode; }
    /** @return the currently active creation mode. */
    public int getActiveMode() { return activeMode; }

    /** Sets the window size used for rendering. */
    public void setSize(int w, int h) { res.setSize(w, h); }

    /** Positions the dropdown just below the "New" button. */
    public void setBtnPos(float x, float y) {
        this.x = x;
        this.y = y + 36;
        w = NEW_DROP_W;
        h = 2 * NEW_ITEM_H;
    }

    /** @return true if the point is inside the open dropdown. */
    @Override
    public boolean contains(float mx, float my) {
        return newMenuOpen && my >= y && my <= y + 2 * NEW_ITEM_H
            && mx >= x && mx <= x + NEW_DROP_W;
    }

    /** Draws the dropdown's outer panel with a small border. */
    @Override
    protected void drawBackground() {
        float border = 1f;

        float dropAlpha = BlurBackground.panelAlpha();
        float[] c = res.menuColor();
        res.drawQuad(x - border, y - border, NEW_DROP_W + 2 * border, h + 2 * border,
            c[0], c[1], c[2], dropAlpha);
    }

    /** Highlights the row of the active creation mode. */
    @Override
    protected void renderContent() {
        for (int i = 0; i < 2; i++) {
            float iy = y + i * NEW_ITEM_H;
            if (i == activeMode) {
                float[] c = res.menuColor();
                res.drawQuad(x, iy, NEW_DROP_W, NEW_ITEM_H,
                    c[0] + 0.15f, c[1] + 0.1f, c[2], 0.85f);
            }
        }
    }

    /** Draws the "Vertex" / "Edge" item labels. */
    @Override
    protected void renderText() {
        String[] items = {"Vertex", "Edge"};
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        for (int i = 0; i < 2; i++) {
            float iy = y + i * NEW_ITEM_H;
            String prefix = (i == activeMode) ? "> " : "  ";
            res.drawText(prefix + items[i], x + 8, iy + 4, 1.2f,
                i == activeMode ? tR * 1.3f : tR,
                i == activeMode ? tG * 1.3f : tG,
                i == activeMode ? tB * 1.3f : tB);
        }
    }

    /** Returns 0=Vertex, 1=Edge, -1=nothing. */
    public int clickItem(float mx, float my) {
        if (!newMenuOpen) return -1;
        if (mx < x || mx > x + NEW_DROP_W || my < y || my > y + 2 * NEW_ITEM_H) return -1;
        int idx = (int) ((my - y) / NEW_ITEM_H);
        if (idx < 0 || idx > 1) return -1;
        newMenuOpen = false;
        visible = false;
        return idx;
    }
}
