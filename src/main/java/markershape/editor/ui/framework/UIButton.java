package markershape.editor.ui.framework;

public class UIButton extends UIContainer {
    public String label;
    public float relScale;
    public boolean useConfigText = true;
    public float tR, tG, tB;

    public UIButton(float x, float y, float w, float h, String label, float relScale) {
        super(x, y, w, h);
        this.label = label;
        this.relScale = relScale;
    }

    @Override
    protected void renderSelf(UIRenderer r) {
        super.renderSelf(r);
        if (label == null || label.isEmpty()) return;
        float scale = relScale * r.getHeight();
        float[] ext = r.textExtent(label, scale);
        float tx = absX(r) + (absW(r) - ext[0]) / 2f;
        float ty = absY(r) + (absH(r) - ext[1]) / 2f;
        if (useConfigText) {
            tR = markershape.config.ConfigParametres.get().getFloat("textR") / 255f;
            tG = markershape.config.ConfigParametres.get().getFloat("textG") / 255f;
            tB = markershape.config.ConfigParametres.get().getFloat("textB") / 255f;
        }
        r.text(label, tx, ty, scale, tR, tG, tB);
    }
}
