package markershape.editor.ui.control;

import markershape.config.ConfigParametres;
import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.menu.BlurBackground;

public class FilterPanel extends Panel {
    private boolean filterOpen;

    public String[] filterLabels = {"Faces", "Arêtes", "Points", "Axe X", "Axe Y", "Axe Z", "Snap", "Magnet", "Vecteur avant"};
    public boolean[] filterValues = {true, true, true, true, true, true, false, false, true};

    public String[] sliderLabels = {"Taille points", "Epaisseur", "Opacite", "Pas de snap"};
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

    public FilterPanel(UIResources res) {
        super(res);
        visible = false;
    }

    public void setSize(int w, int h) {
        res.setSize(w, h);
    }

    public boolean isOpen() { return filterOpen; }
    public void setOpen(boolean v) { filterOpen = v; visible = v; }
    public void toggle() { filterOpen = !filterOpen; visible = filterOpen; }

    public void setFilterCallback(Runnable cb) { filterCallback = cb; }

    public float panelHeight() {
        return filterLabels.length * CHECKBOX_H + PANEL_GAP + sliderLabels.length * SLIDER_H;
    }

    public float sliderItemY(int i) {
        return y + filterLabels.length * CHECKBOX_H + PANEL_GAP + i * SLIDER_H;
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
    protected void renderContent() {
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;

        for (int i = 0; i < sliderLabels.length; i++) {
            float iy = sliderItemY(i);
            float trackY = iy + (SLIDER_H - 8) * 0.5f + 4;
            float trackX = x + TRACK_X;
            float val = sliderValues[i];
            float frac = (val - sliderMin[i]) / (sliderMax[i] - sliderMin[i]);

            float trackA = BlurBackground.transparentUI ? 0.6f : 1f;
            float trackR = Math.min(1f, mr * 0.75f), trackG = Math.min(1f, mg * 0.75f), trackB = Math.min(1f, mb * 0.75f);
            float fillR = Math.min(1f, mr + 0.35f), fillG = Math.min(1f, mg + 0.35f), fillB = Math.min(1f, mb + 0.45f);
            float thumbR = Math.min(1f, mr + 0.6f), thumbG = Math.min(1f, mg + 0.6f), thumbB = Math.min(1f, mb + 0.6f);

            float tx = trackX, ty = trackY, tw = TRACK_W, th = 6;
            res.drawQuad(tx, ty, tw, th, trackR, trackG, trackB, trackA);

            float fw = Math.max(2, frac * tw);
            res.drawQuad(tx, ty, fw, th, fillR, fillG, fillB, 1f);

            float thumbX = tx + frac * tw - 3;
            float thumbY = ty - 1;
            res.drawQuad(thumbX, thumbY, 6, 8, thumbR, thumbG, thumbB, 1f);
        }
    }

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

    public boolean isSnapEnabled() { return filterValues[6]; }
    public float getSnapStep() { return sliderValues[3]; }
    public void setSnapEnabled(boolean v) { filterValues[6] = v; }
    public void setSnapStep(float v) { sliderValues[3] = v; }
    public boolean isMagnetEnabled() { return filterValues[7]; }
    public void setMagnetEnabled(boolean v) { filterValues[7] = v; }
    public float getMagnetRadius() { return magnetRadius; }
    public void setMagnetRadius(float v) { magnetRadius = v; }
    private float magnetRadius = 14f;
    public boolean isFrontArrowEnabled() { return filterValues[8]; }
    public void setFrontArrowEnabled(boolean v) { filterValues[8] = v; }

    private void fireCallback() { if (filterCallback != null) filterCallback.run(); }
}