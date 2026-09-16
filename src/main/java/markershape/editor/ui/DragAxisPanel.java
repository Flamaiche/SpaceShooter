package markershape.editor.ui;

import markershape.config.ConfigParametres;
import markershape.editor.ui.menu.BlurBackground;

/**
 * Small floating panel that appears during a vertex drag, allowing the user
 * to set the movement axis by hovering (not clicking) one of the four buttons:
 * X, Y, Z, or Libre (free).  The axis is applied live while the LMB stays
 * held down.
 */
public class DragAxisPanel extends UIElement {

    public static final float PANEL_W = 140;
    public static final float PANEL_H = 30;
    public static final float BTN_W = 28;
    public static final float BTN_H = 22;
    public static final float GAP = 4;
    public static final float PAD = 4;

    private int axis = 0;        // 0=free, 1=X, 2=Y, 3=Z
    private boolean active;

    private static final String[] LABELS = {"Libre", "X", "Y", "Z"};

    /** @param res the shared UI resources used to render the panel */
    public DragAxisPanel(UIResources res) {
        super(res);
        visible = false;
        w = PANEL_W;
        h = PANEL_H;
    }

    /** Called each frame to position and show/hide the panel. */
    public void update(float mx, float my, boolean dragActive) {
        active = dragActive;
        if (!dragActive) { visible = false; return; }
        visible = true;
        x = mx + 18;
        y = my - PANEL_H - 8;
    }

    /** Sets the currently active axis (0=Libre, 1=X, 2=Y, 3=Z). */
    public void setAxis(int axis) { this.axis = axis; }

    /**
     * Returns the button index (0=Libre, 1=X, 2=Y, 3=Z) under the cursor,
     * or -1 if none. Called while dragging to apply the hover axis.
     */
    public int hoverAxis(float mx, float my) {
        if (!visible) return -1;
        for (int i = 0; i < LABELS.length; i++) {
            float bx = x + PAD + i * (BTN_W + GAP);
            float by = y + PAD;
            if (mx >= bx && mx <= bx + BTN_W && my >= by && my <= by + BTN_H)
                return i;
        }
        return -1;
    }

    /** Draws the panel background, the axis buttons (highlighting the active one), and the Axis label. */
    @Override
    public void render() {
        if (!visible) return;
        res.begin2D();

        // Panel background
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        float bgA = BlurBackground.transparentUI ? 0.85f : 1f;
        float bgR = Math.min(1f, mr + 0.15f), bgG = Math.min(1f, mg + 0.15f), bgB = Math.min(1f, mb + 0.15f);
        res.drawQuad(x, y, PANEL_W, PANEL_H, bgR, bgG, bgB, bgA);

        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        // Axis index mapping: 0=Libre(0), 1=X(1), 2=Y(2), 3=Z(3)
        for (int i = 0; i < LABELS.length; i++) {
            float bx = x + PAD + i * (BTN_W + GAP);
            float by = y + PAD;

            boolean selected = (i == axis);
            float r, g, b;
            if (selected) {
                r = 0.4f; g = 0.85f; b = 0.5f;   // bright green for selected
            } else {
                r = Math.min(1f, mr + 0.45f); g = Math.min(1f, mg + 0.45f); b = Math.min(1f, mb + 0.45f);
            }
            float a = selected ? 0.95f : 0.7f;
            res.drawQuad(bx, by, BTN_W, BTN_H, r, g, b, a);

            float[] tc = res.textColor();
            float brightness = selected ? 1f : 0.8f;
            float tw = res.getTextExtent(LABELS[i], 1.1f)[0];
            float tx = bx + (BTN_W - tw) / 2;
            float ty = by + 3;
            res.drawText(LABELS[i], tx, ty, 1.1f,
                tc[0] * brightness, tc[1] * brightness, tc[2] * brightness);
        }

        // Label
        float[] tc = res.textColor();
        float labelScale = 1.1f;
        res.drawText("Axe:", x + PAD, y - 3, labelScale, tc[0], tc[1], tc[2]);
    }
}