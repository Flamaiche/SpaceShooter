package markershape.editor.ui.control;

import markershape.config.ConfigParametres;
import markershape.editor.ui.framework.UIContainer;
import markershape.editor.ui.framework.UIRenderer;
import markershape.editor.ui.framework.UIText;
import markershape.editor.ui.menu.BlurBackground;

public class FilterPanel {
    private int width, height;
    private boolean filterOpen;
    private float filterX, filterY;
    private final UIRenderer renderer = new UIRenderer();
    private final UIContainer root = new UIContainer(0, 0, 1, 1);

    public String[] filterLabels = {"Faces", "Aretes", "Points", "Axe X", "Axe Y", "Axe Z", "Snap"};
    public boolean[] filterValues = {true, true, true, true, true, true, false};

    public String[] sliderLabels = {"Taille pts", "Lignes", "Opacite", "Snap pas"};
    public float[] sliderValues = {5f, 3f, 1f, 1f};
    private float[] sliderMin = {1f, 1f, 0f, 0.1f};
    private float[] sliderMax = {20f, 10f, 1f, 5f};
    private float[] sliderStep = {1f, 0.5f, 0.05f, 0.1f};

    public static final int CHECKBOX_H = 24;
    public static final int SLIDER_H = 30;
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

    private static float ts(float designScale) { return designScale / 720f; }

    public FilterPanel() {
        root.alpha = UIContainer.Alpha.NONE;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        renderer.setScreenSize(w, h);
    }

    public boolean isOpen() { return filterOpen; }
    public void setOpen(boolean v) { filterOpen = v; }
    public void toggle() { filterOpen = !filterOpen; }

    public void setFilterCallback(Runnable cb) { filterCallback = cb; }

    public float panelHeight() {
        return filterLabels.length * CHECKBOX_H + PANEL_GAP + sliderLabels.length * SLIDER_H;
    }

    public float sliderItemY(int i) {
        return filterY + filterLabels.length * CHECKBOX_H + PANEL_GAP + i * SLIDER_H;
    }

    public boolean contains(float mx, float my) {
        return filterOpen && my >= filterY && my <= filterY + panelHeight()
            && mx >= filterX && mx <= filterX + PANEL_W;
    }

    public void render(float btnX, float btnY) {
        if (!filterOpen) return;
        filterX = btnX + (130 - PANEL_W) / 2;
        filterY = btnY;
        root.clear();
        build();
        root.render(renderer);
    }

    private void build() {
        float ph = panelHeight();
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        UIContainer panel = new UIContainer(filterX / width, filterY / height, PANEL_W / width, ph / height);
        panel.alpha = UIContainer.Alpha.PANEL;
        panel.bgR = mr;
        panel.bgG = mg;
        panel.bgB = mb;
        root.add(panel);

        for (int i = 0; i < filterLabels.length; i++) {
            float oy = i * CHECKBOX_H;
            String prefix = filterValues[i] ? "[x] " : "[ ] ";
            float brightness = filterValues[i] ? 1f : 0.6f;
            UIText t = new UIText(8f / PANEL_W, (oy + 4f) / ph, ts(1.5f), prefix + filterLabels[i]);
            t.useConfigText = false;
            t.tR = tR * brightness;
            t.tG = tG * brightness;
            t.tB = tB * brightness;
            panel.add(t);
        }

        float cbH = filterLabels.length * CHECKBOX_H + PANEL_GAP;
        for (int i = 0; i < sliderLabels.length; i++) {
            float oy = cbH + i * SLIDER_H;
            float trackY = oy + (SLIDER_H - 8) * 0.5f + 4;
            float val = sliderValues[i];
            float frac = (val - sliderMin[i]) / (sliderMax[i] - sliderMin[i]);

            String valStr = String.format("%." + SLIDER_DECIMALS + "f", sliderValues[i]);
            panel.add(text(8f, oy + 2f, sliderLabels[i] + ":", tR, tG, tB, ph));
            panel.add(text(VAL_X, oy + 2f, valStr, tR, tG, tB, ph));
            panel.add(text(MINUS_X, oy + 2f, "[-]", tR, tG, tB, ph));
            panel.add(text(PLUS_X, oy + 2f, "[+]", tR, tG, tB, ph));

            float trackA = BlurBackground.transparentUI ? 0.6f : 1f;
            float trackR = Math.min(1f, mr * 0.75f), trackG = Math.min(1f, mg * 0.75f), trackB = Math.min(1f, mb * 0.75f);
            float fillR = Math.min(1f, mr + 0.35f), fillG = Math.min(1f, mg + 0.35f), fillB = Math.min(1f, mb + 0.45f);
            float thumbR = Math.min(1f, mr + 0.6f), thumbG = Math.min(1f, mg + 0.6f), thumbB = Math.min(1f, mb + 0.6f);

            UIContainer track = new UIContainer(TRACK_X / PANEL_W, trackY / ph, TRACK_W / PANEL_W, 6f / ph);
            track.customAlpha = trackA;
            track.bgR = trackR; track.bgG = trackG; track.bgB = trackB;
            panel.add(track);

            float fw = Math.max(2, frac * TRACK_W);
            UIContainer fill = new UIContainer(TRACK_X / PANEL_W, trackY / ph, fw / PANEL_W, 6f / ph);
            fill.customAlpha = 1f;
            fill.bgR = fillR; fill.bgG = fillG; fill.bgB = fillB;
            panel.add(fill);

            float thumbX = TRACK_X + frac * TRACK_W - 3;
            UIContainer thumb = new UIContainer(thumbX / PANEL_W, (trackY - 1f) / ph, 6f / PANEL_W, 8f / ph);
            thumb.customAlpha = 1f;
            thumb.bgR = thumbR; thumb.bgG = thumbG; thumb.bgB = thumbB;
            panel.add(thumb);
        }
    }

    private UIText text(float x, float y, String label, float r, float g, float b, float ph) {
        UIText t = new UIText(x / PANEL_W, y / ph, ts(1.5f), label);
        t.useConfigText = false;
        t.tR = r; t.tG = g; t.tB = b;
        return t;
    }

    public int clickFilter(float mx, float my, float btnX) {
        if (!filterOpen) return -1;
        filterX = btnX + (130 - PANEL_W) / 2;
        filterY = 36;

        for (int i = 0; i < sliderLabels.length; i++) {
            float iy = sliderItemY(i);
            if (my >= iy && my <= iy + SLIDER_H) {
                if (mx >= filterX + MINUS_X && mx <= filterX + MINUS_X + BTN_SM_W) {
                    sliderValues[i] = Math.max(sliderMin[i], sliderValues[i] - sliderStep[i]);
                    fireCallback();
                    return 3 + i;
                }
                if (mx >= filterX + PLUS_X && mx <= filterX + PLUS_X + BTN_SM_W) {
                    sliderValues[i] = Math.min(sliderMax[i], sliderValues[i] + sliderStep[i]);
                    fireCallback();
                    return 3 + i;
                }
                return 3 + i;
            }
        }

        for (int i = 0; i < filterLabels.length; i++) {
            float iy = filterY + i * CHECKBOX_H;
            if (mx >= filterX && mx <= filterX + PANEL_W
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

    private void fireCallback() { if (filterCallback != null) filterCallback.run(); }

    public void cleanup() {
        renderer.cleanup();
    }
}
