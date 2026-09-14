package markershape.editor.ui.menu;

import markershape.config.ConfigParametres;
import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;

/**
 * Tool palette dropdown under the "Outils" button: selection/creation modes
 * plus the one-shot editing tools (split, extrude, fill, weld, duplicate,
 * copy/paste, clean, help). Uses the same row pattern as NewMenu.
 */
public class ToolPalette extends Panel {
    private boolean open;
    private int activeMode = -1; // -1=selection, 0=vertex, 1=edge, 2=trace

    public static final float PAL_W = 200;
    public static final float ITEM_H = 25;

    // Row codes (returned by clickItem). Values MUST stay aligned with LABELS/KEYS.
    public static final int TOOL_SELECT = 0;
    public static final int TOOL_VERTEX = 1;
    public static final int TOOL_EDGE = 2;
    public static final int TOOL_TRACE = 3;
    public static final int TOOL_SPLIT = 4;
    public static final int TOOL_EXTRUDE = 5;
    public static final int TOOL_FILL = 6;
    public static final int TOOL_WELD = 7;
    public static final int TOOL_CLEAN = 8;
    public static final int TOOL_DUPLICATE = 9;
    public static final int TOOL_COPY = 10;
    public static final int TOOL_PASTE = 11;
    public static final int TOOL_HELP = 12;

    private static final String[] LABELS = {
        "Selection", "Sommet", "Arete", "Tracé (face)",
        "Subdiviser", "Extruder", "Remplir", "Fusionner",
        "Nettoyer", "Dupliquer", "Copier", "Coller", "Aide"
    };

    private static final String[] KEYS = {
        "", "", "", "T",
        "S", "E", "F", "M",
        "K", "Ctrl+D", "Ctrl+C", "Ctrl+V", "H"
    };

    // index of the last row of each group (a divider is drawn after it)
    private static final int[] GROUP_END = {3, 8, 11};

    public ToolPalette(UIResources res) {
        super(res);
        visible = false;
    }

    public boolean isOpen() { return open; }
    public void setOpen(boolean v) { open = v; visible = v; }
    public void toggle() { open = !open; visible = open; }
    public void close() { open = false; visible = false; }

    public void setActiveMode(int mode) { activeMode = mode; }

    public void setSize(int w, int h) { res.setSize(w, h); }

    public void setBtnPos(float btnX, float btnY) {
        x = btnX + (130 - PAL_W) / 2f;
        y = btnY + 36;
        w = PAL_W;
        h = LABELS.length * ITEM_H;
    }

    @Override
    public boolean contains(float mx, float my) {
        return open && my >= y && my <= y + h && mx >= x && mx <= x + PAL_W;
    }

    @Override
    protected void drawBackground() {
        float border = 1f;
        float[] c = res.menuColor();
        res.drawQuad(x - border, y - border, PAL_W + 2 * border, h + 2 * border,
            c[0], c[1], c[2], BlurBackground.panelAlpha());
    }

    private boolean isGroupEnd(int i) {
        for (int g : GROUP_END) if (g == i) return true;
        return false;
    }

    @Override
    protected void renderContent() {
        float[] c = res.menuColor();
        int activeRow = activeMode + 1;
        for (int i = 0; i < LABELS.length; i++) {
            float iy = y + i * ITEM_H;
            if (i == activeRow) {
                res.drawQuad(x, iy, PAL_W, ITEM_H,
                    Math.min(1f, c[0] + 0.15f), Math.min(1f, c[1] + 0.1f), Math.min(1f, c[2] + 0f), 0.85f);
            }
            if (isGroupEnd(i)) {
                res.drawQuad(x + 8, iy + ITEM_H, PAL_W - 16, 1f,
                    Math.min(1f, c[0] + 0.3f), Math.min(1f, c[1] + 0.3f), Math.min(1f, c[2] + 0.3f), 0.5f);
            }
        }
    }

    @Override
    protected void renderText() {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        int activeRow = activeMode + 1;
        for (int i = 0; i < LABELS.length; i++) {
            float iy = y + i * ITEM_H;
            float scale = 1.45f;
            boolean active = i == activeRow;
            res.drawText((active ? "> " : "  ") + LABELS[i], x + 8, iy + 4, scale,
                active ? Math.min(1f, tR * 1.3f) : tR,
                active ? Math.min(1f, tG * 1.3f) : tG,
                active ? Math.min(1f, tB * 1.3f) : tB);
            if (!KEYS[i].isEmpty()) {
                float[] ext = res.getTextExtent(KEYS[i], 1.2f);
                res.drawText(KEYS[i], x + PAL_W - 8 - ext[0], iy + 5, 1.2f,
                    tR * 0.75f, tG * 0.75f, tB * 0.75f);
            }
        }
    }

    /** Returns the tool row code, or -1 if the click was outside the palette. */
    public int clickItem(float mx, float my) {
        if (!open) return -1;
        if (mx < x || mx > x + PAL_W || my < y || my > y + h) return -1;
        int idx = (int) ((my - y) / ITEM_H);
        if (idx < 0 || idx >= LABELS.length) return -1;
        close();
        return idx;
    }
}