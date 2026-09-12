package markershape.editor.ui.overlay;

import markershape.config.ConfigParametres;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.menu.BlurBackground;
import markershape.shape.*;

import java.util.function.Consumer;

public class SiblingPicker {
    private final UIResources res;
    private boolean visible;
    private int[] ids;
    private Vertex[] vertices;
    private float px, py, ph;
    private static final float PW = 220;
    private static final float ROW_H = 26;
    private Consumer<Integer> callback;

    public SiblingPicker(UIResources res) {
        this.res = res;
    }

    public boolean isVisible() { return visible; }

    public void show(ShapeData data, int[] siblingIds, float mx, float my,
                     int screenW, int screenH, Consumer<Integer> cb) {
        ids = siblingIds;
        vertices = new Vertex[ids.length];
        for (int i = 0; i < ids.length; i++) vertices[i] = data.vertices.get(ids[i]);
        callback = cb;
        ph = ids.length * ROW_H + 30;
        px = Math.min(mx, screenW - PW - 10);
        py = Math.min(my, screenH - ph - 10);
        if (px < 10) px = 10;
        if (py < 10) py = 10;
        visible = true;
    }

    public void hide() { visible = false; callback = null; }

    public float getX() { return px; }
    public float getY() { return py; }
    public float getW() { return PW; }
    public float getH() { return ph; }

    public int click(float mx, float my) {
        if (!visible) return -1;
        float h = ph;
        if (mx < px || mx > px + PW || my < py || my > py + h) {
            hide();
            return -1;
        }
        for (int i = 0; i < ids.length; i++) {
            float ry = py + 30 + i * ROW_H;
            if (my >= ry && my <= ry + ROW_H) {
                int picked = ids[i];
                if (callback != null) callback.accept(picked);
                hide();
                return picked;
            }
        }
        return -1;
    }

    public void render() {
        if (!visible || vertices == null) return;

        res.begin2D();

        float[] c = res.menuColor();
        float alpha = BlurBackground.panelAlpha();
        res.drawQuad(px, py, PW, ph, c[0], c[1], c[2], alpha);

        float rowAlpha = BlurBackground.rowAlpha();
        for (int i = 0; i < ids.length; i++) {
            float ry = py + 30 + i * ROW_H;
            float mult = (i % 2 == 0) ? 1.15f : 0.95f;
            res.drawQuad(px + 2, ry, PW - 2, ROW_H,
                Math.min(1f, c[0] * mult), Math.min(1f, c[1] * mult), Math.min(1f, c[2] * mult), rowAlpha);
        }

        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        res.drawText("Select vertex:", px + 8, py + 8, 1.5f, tR, tG, tB);

        for (int i = 0; i < vertices.length; i++) {
            Vertex v = vertices[i];
            if (v == null) continue;
            float ry = py + 30 + i * ROW_H + 4;
            float sw = 16;
            res.drawQuad(px + 8, ry, sw, sw, v.r, v.g, v.b, 1f);
            res.drawText("#" + v.id + " (" + String.format("%.2f,%.2f,%.2f", v.r, v.g, v.b) + ")",
                px + 30, ry, 1.5f, tR, tG, tB);
        }
    }
}