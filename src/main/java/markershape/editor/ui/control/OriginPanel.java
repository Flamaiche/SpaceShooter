package markershape.editor.ui.control;

import markershape.config.ConfigParametres;
import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;

/**
 * Small panel with X / Y / Z rows, each row showing the accumulated offset
 * with [-] and [+] buttons. Each click moves ALL the model's vertices by a
 * fixed step, i.e. moves the shape relative to the world origin.
 */
public class OriginPanel extends Panel {
    private boolean open;

    public static final float ROW_H = 26;
    public static final float ROW_GAP = 4;
    public static final float PANEL_W = 190;
    public static final float VAL_X = 44;
    public static final float MINUS_X = 130;
    public static final float PLUS_X = 158;
    public static final float BTN_SM_W = 26;
    public static final float STEP = 0.1f;

    private final String[] axisLabels = {"X", "Y", "Z"};
    private final float[] offsets = {0f, 0f, 0f};

    public OriginPanel(UIResources res) {
        super(res);
        visible = false;
    }

    public boolean isOpen() { return open; }
    public void setOpen(boolean v) { open = v; visible = v; }
    public void toggle() { open = !open; visible = open; }

    /** Tracks the accumulated translation (the displaced "origin"). */
    public void addOffset(float dx, float dy, float dz) {
        offsets[0] += dx;
        offsets[1] += dy;
        offsets[2] += dz;
    }

    public void resetOffsets() {
        offsets[0] = 0f;
        offsets[1] = 0f;
        offsets[2] = 0f;
    }

    public float panelHeight() {
        return ROW_GAP + axisLabels.length * ROW_H;
    }

    @Override
    public boolean contains(float mx, float my) {
        return visible && mx >= x && mx <= x + PANEL_W
            && my >= y && my <= y + panelHeight();
    }

    public void setPosition(float btnX, float btnY) {
        x = btnX + (130 - PANEL_W) / 2;
        y = btnY;
        w = PANEL_W;
        h = panelHeight();
    }

    @Override
    protected void renderText() {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        for (int i = 0; i < axisLabels.length; i++) {
            float iy = y + ROW_GAP + i * ROW_H;
            String val = String.format("%+.2f", offsets[i]);
            res.drawText(axisLabels[i], x + 8, iy + 2, 1.2f, tR, tG, tB);
            res.drawText(val, x + VAL_X, iy + 2, 1.2f, tR, tG, tB);
            res.drawText("[-]", x + MINUS_X, iy + 2, 1.2f, tR, tG, tB);
            res.drawText("[+]", x + PLUS_X, iy + 2, 1.2f, tR, tG, tB);
        }
    }

    /**
     * Returns 0 if not clicked. Otherwise +/- (axis+1): X=1, Y=2, Z=3 with the
     * sign giving the direction (positive = [+], negative = [-]).
     */
    public int clickOrigin(float mx, float my) {
        if (!visible) return 0;
        for (int i = 0; i < axisLabels.length; i++) {
            float iy = y + ROW_GAP + i * ROW_H;
            if (my >= iy && my <= iy + ROW_H) {
                if (mx >= x + MINUS_X && mx <= x + MINUS_X + BTN_SM_W) return -(i + 1);
                if (mx >= x + PLUS_X && mx <= x + PLUS_X + BTN_SM_W) return (i + 1);
                return 0;
            }
        }
        return 0;
    }
}