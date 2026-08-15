package markershape.editor.ui.framework;

import markershape.config.ConfigParametres;

public class UIText extends UIElement {
    public String text;
    public float relScale;
    public boolean centered = false;
    public boolean useConfigText = true;
    public float tR, tG, tB;

    public UIText(float x, float y, float relScale, String text) {
        this.x = x;
        this.y = y;
        this.relScale = relScale;
        this.text = text;
    }

    @Override
    protected void renderSelf(UIRenderer r) {
        if (text == null || text.isEmpty()) return;
        float scale = relScale * r.getHeight();
        float[] ext = r.textExtent(text, scale);
        float tx = absX(r);
        float ty = absY(r);
        if (centered) {
            tx = absX(r) + (absW(r) - ext[0]) / 2f;
            ty = absY(r) + (absH(r) - ext[1]) / 2f;
        }
        if (useConfigText) {
            ConfigParametres cfg = ConfigParametres.get();
            tR = cfg.getFloat("textR") / 255f;
            tG = cfg.getFloat("textG") / 255f;
            tB = cfg.getFloat("textB") / 255f;
        }
        r.text(text, tx, ty, scale, tR, tG, tB);
    }
}
