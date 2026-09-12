package markershape.editor.ui.menu;

import gamegl.gestion.texte.Text;
import markershape.config.ConfigParametres;
import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;

public class NewMenu extends Panel {
    private boolean newMenuOpen;
    private int activeMode = -1; // -1=none, 0=vertex, 1=edge
    public static final float NEW_DROP_W = 130;
    public static final float NEW_ITEM_H = 26;

    private float btnX, btnY;

    public NewMenu(UIResources res) {
        super(res);
    }

    public boolean isOpen() { return newMenuOpen; }
    public void setOpen(boolean v) { newMenuOpen = v; visible = v; }
    public void toggle() { newMenuOpen = !newMenuOpen; visible = newMenuOpen; }
    public void close() { newMenuOpen = false; visible = false; }

    public void setActiveMode(int mode) { activeMode = mode; }
    public int getActiveMode() { return activeMode; }

    public void setSize(int w, int h) { res.setSize(w, h); }

    public void setBtnPos(float x, float y) {
        btnX = x;
        btnY = y;
        this.x = x;
        this.y = y + 36;
        w = NEW_DROP_W;
        h = 2 * NEW_ITEM_H;
    }

    @Override
    public boolean contains(float mx, float my) {
        return newMenuOpen && my >= btnY + 36 && my <= btnY + 36 + 2 * NEW_ITEM_H
            && mx >= btnX && mx <= btnX + NEW_DROP_W;
    }

    @Override
    protected void drawBackground() {
        float dx = btnX;
        float dy = btnY + 36;
        float dh = 2 * NEW_ITEM_H;
        float border = 1f;

        float dropAlpha = BlurBackground.panelAlpha();
        float[] c = res.menuColor();
        res.drawQuad(dx - border, dy - border, NEW_DROP_W + 2 * border, dh + 2 * border,
            c[0], c[1], c[2], dropAlpha);
    }

    @Override
    protected void renderContent() {
        float dx = btnX;
        float dy = btnY + 36;
        for (int i = 0; i < 2; i++) {
            float iy = dy + i * NEW_ITEM_H;
            if (i == activeMode) {
                float[] c = res.menuColor();
                res.drawQuad(dx, iy, NEW_DROP_W, NEW_ITEM_H,
                    c[0] + 0.15f, c[1] + 0.1f, c[2], 0.85f);
            }
        }
    }

    @Override
    protected void renderText() {
        String[] items = {"Vertex", "Edge"};
        float dx = btnX;
        float dy = btnY + 36;
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        for (int i = 0; i < 2; i++) {
            float iy = dy + i * NEW_ITEM_H;
            String prefix = (i == activeMode) ? "> " : "  ";
            res.drawText(prefix + items[i], dx + 8, iy + 4, 1.5f,
                i == activeMode ? tR * 1.3f : tR,
                i == activeMode ? tG * 1.3f : tG,
                i == activeMode ? tB * 1.3f : tB);
        }
    }

    /** Returns 0=Vertex, 1=Edge, -1=nothing. */
    public int clickItem(float mx, float my) {
        if (!newMenuOpen) return -1;
        float dx = btnX;
        float dy = btnY + 36;
        if (mx < dx || mx > dx + NEW_DROP_W || my < dy || my > dy + 2 * NEW_ITEM_H) return -1;
        int idx = (int) ((my - dy) / NEW_ITEM_H);
        if (idx < 0 || idx > 1) return -1;
        newMenuOpen = false;
        visible = false;
        return idx;
    }
}
