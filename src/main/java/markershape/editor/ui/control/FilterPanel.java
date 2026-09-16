package markershape.editor.ui.control;

import markershape.config.ConfigParametres;
import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.menu.BlurBackground;

/**
 * Panel listing the view/filter toggles (checkboxes) and the display sliders,
 * opened below the filter button.
 */
public class FilterPanel extends Panel {
    private boolean filterOpen;

    public String[] filterLabels = {"Faces", "Arêtes", "Sommets", "Axe X", "Axe Y", "Axe Z", "Accrochage grille", "Aimantation", "Flèche avant"};
    public boolean[] filterValues = {true, true, true, true, true, true, false, false, true};

    public String[] sliderLabels = {"Taille des points", "Épaisseur des lignes", "Opacité des faces", "Pas de l'accrochage"};
    public float[] sliderValues = {5f, 3f, 1f, 1f};
    private float[] sliderMin = {1f, 1f, 0f, 0.1f};
    private float[] sliderMax = {20f, 10f, 1f, 5f};
    private float[] sliderStep = {1f, 0.5f, 0.05f, 0.1f};

    public static final int CHECKBOX_H = 20;
    public static final int SLIDER_H = 25;
    public static final int PANEL_GAP = 4;
    public static final float PANEL_W = 210;
    public static final float TRACK_W = 70;
    public static final float TRACK_X = 58;
    public static final float VAL_X = 80;
    public static final float MINUS_X = 155;
    public static final float PLUS_X = 173;
    public static final float BTN_SM_W = 16;
    public static final int SLIDER_DECIMALS = 1;

    private Runnable filterCallback;

    /** Creates the filter panel, initially hidden. */
    public FilterPanel(UIResources res) {
        super(res);
        visible = false;
    }

    /** Sets the window size used for rendering. */
    public void setSize(int w, int h) {
        res.setSize(w, h);
    }

    /** @return true if the panel is open. */
    public boolean isOpen() { return filterOpen; }
    /** Opens or closes the panel. */
    public void setOpen(boolean v) { filterOpen = v; visible = v; }
    /** Toggles the panel open/closed state. */
    public void toggle() { filterOpen = !filterOpen; visible = filterOpen; }

    /** Sets the callback fired whenever a filter or slider value changes. */
    public void setFilterCallback(Runnable cb) { filterCallback = cb; }

    /** @return the total panel height for the checkboxes and sliders. */
    public float panelHeight() {
        return filterLabels.length * CHECKBOX_H + PANEL_GAP + sliderLabels.length * SLIDER_H;
    }

    /** @return the Y offset of the given slider row. */
    public float sliderItemY(int i) {
        return y + filterLabels.length * CHECKBOX_H + PANEL_GAP + i * SLIDER_H;
    }

    /** @return true if the point is inside the visible panel. */
    @Override
    public boolean contains(float mx, float my) {
        return visible && mx >= x && mx <= x + PANEL_W
            && my >= y && my <= y + panelHeight();
    }

    /** Positions the panel below the filter button. */
    public void setPosition(float btnX, float btnY) {
        x = btnX + (130 - PANEL_W) / 2;
        y = btnY;
        w = PANEL_W;
        h = panelHeight();
    }

    /** No extra content is drawn for this panel. */
    @Override
    protected void renderContent() {
    }

    /** Draws the checkbox labels and the slider labels/values. */
    @Override
    protected void renderText() {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        for (int i = 0; i < filterLabels.length; i++) {
            float iy = y + i * CHECKBOX_H;
            String prefix = filterValues[i] ? "[x] " : "[ ] ";
            float brightness = filterValues[i] ? 1f : 0.6f;
            res.drawText(prefix + filterLabels[i],
                x + 8, iy + 3, 1.2f, tR * brightness, tG * brightness, tB * brightness);
        }

        for (int i = 0; i < sliderLabels.length; i++) {
            float iy = sliderItemY(i);

            String valStr = String.format("%." + SLIDER_DECIMALS + "f", sliderValues[i]);
            res.drawText(sliderLabels[i] + ":",
                x + 8, iy + 2, 1.2f, tR, tG, tB);
            res.drawText(valStr,
                x + VAL_X, iy + 2, 1.2f, tR, tG, tB);
            res.drawText("[-]",
                x + MINUS_X, iy + 2, 1.2f, tR, tG, tB);
            res.drawText("[+]",
                x + PLUS_X, iy + 2, 1.2f, tR, tG, tB);
        }
    }

    /**
     * Handles a click on a checkbox or slider.
     * @return the checkbox index (0-8) or 3 + the slider index (3-6),
     *         or -1 if nothing was hit.
     */
    public int clickFilter(float mx, float my) {
        if (!visible) return -1;

        for (int i = 0; i < sliderLabels.length; i++) {
            float iy = sliderItemY(i);
            if (my >= iy && my <= iy + SLIDER_H) {
                if (mx >= x + MINUS_X && mx <= x + MINUS_X + BTN_SM_W) {
                    sliderValues[i] = Math.max(sliderMin[i], sliderValues[i] - sliderStep[i]);
                    fireCallback();
                    return 3 + i;
                }
                if (mx >= x + PLUS_X && mx <= x + PLUS_X + BTN_SM_W) {
                    sliderValues[i] = Math.min(sliderMax[i], sliderValues[i] + sliderStep[i]);
                    fireCallback();
                    return 3 + i;
                }
                return 3 + i;
            }
        }

        for (int i = 0; i < filterLabels.length; i++) {
            float iy = y + i * CHECKBOX_H;
            if (mx >= x && mx <= x + PANEL_W
                && my >= iy && my <= iy + CHECKBOX_H) {
                filterValues[i] = !filterValues[i];
                fireCallback();
                return i;
            }
        }
        return -1;
    }

    /** @return true if grid snapping is enabled. */
    public boolean isSnapEnabled() { return filterValues[6]; }
    /** @return the grid snap step. */
    public float getSnapStep() { return sliderValues[3]; }
    /** Enables or disables grid snapping. */
    public void setSnapEnabled(boolean v) { filterValues[6] = v; }
    /** Sets the grid snap step. */
    public void setSnapStep(float v) { sliderValues[3] = v; }
    /** @return true if vertex magnet capture is enabled. */
    public boolean isMagnetEnabled() { return filterValues[7]; }
    /** Enables or disables vertex magnet capture. */
    public void setMagnetEnabled(boolean v) { filterValues[7] = v; }
    /** @return the magnet capture radius. */
    public float getMagnetRadius() { return magnetRadius; }
    /** Sets the magnet capture radius. */
    public void setMagnetRadius(float v) { magnetRadius = v; }
    private float magnetRadius = 14f;
    /** @return true if the front arrow is displayed. */
    public boolean isFrontArrowEnabled() { return filterValues[8]; }
    /** Shows or hides the front arrow. */
    public void setFrontArrowEnabled(boolean v) { filterValues[8] = v; }

    /** Runs the filter callback when one is set. */
    private void fireCallback() { if (filterCallback != null) filterCallback.run(); }
}