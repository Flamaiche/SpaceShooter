package markershape.editor.ui.overlay;

import markershape.config.ConfigParametres;
import markershape.editor.ui.UIElement;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.menu.BlurBackground;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;

import java.util.function.Consumer;

public class SiblingPicker extends UIElement {
    private int[] ids;
    private Vertex[] vertices;
    private static final float PW = 220;
    private static final float ROW_H = 22;
    private Consumer<Integer> callback;

    public SiblingPicker(UIResources res) {
        super(res);
        visible = false;
    }

    public void show(ShapeData data, int[] siblingIds, float mx, float my,
                     int screenW, int screenH, Consumer<Integer> cb) {
        ids = siblingIds;
        vertices = new Vertex[ids.length];
        for (int i = 0; i < ids.length; i++) vertices[i] = data.vertices.get(ids[i]);
        callback = cb;
        w = PW;
        h = ids.length * ROW_H + 30;
        x = Math.min(mx, screenW - PW - 10);
        y = Math.min(my, screenH - h - 10);
        if (x < 10) x = 10;
        if (y < 10) y = 10;
        visible = true;
    }

    @Override
    public void hide() { visible = false; callback = null; }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getW() { return w; }
    public float getH() { return h; }

    public int clickItem(float mx, float my) {
        if (!visible) return -1;
        if (mx < x || mx > x + w || my < y || my > y + h) {
            hide();
            return -1;
        }
        for (int i = 0; i < ids.length; i++) {
            float ry = y + 30 + i * ROW_H;
            if (my >= ry && my <= ry + ROW_H) {
                int picked = ids[i];
                if (callback != null) callback.accept(picked);
                hide();
                return picked;
            }
        }
        return -1;
    }

    @Override
    public void render() {
        if (!visible || vertices == null) return;

        res.begin2D();

        float[] c = res.menuColor();
        float alpha = BlurBackground.panelAlpha();
        res.drawQuad(x, y, w, h, c[0], c[1], c[2], alpha);

        float rowAlpha = BlurBackground.rowAlpha();
        for (int i = 0; i < ids.length; i++) {
            float ry = y + 30 + i * ROW_H;
            float mult = (i % 2 == 0) ? 1.15f : 0.95f;
            res.drawQuad(x + 2, ry, w - 2, ROW_H,
                Math.min(1f, c[0] * mult), Math.min(1f, c[1] * mult), Math.min(1f, c[2] * mult), rowAlpha);
        }

        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        res.drawText("Select vertex:", x + 8, y + 8, 1.2f, tR, tG, tB);

        for (int i = 0; i < vertices.length; i++) {
            Vertex v = vertices[i];
            if (v == null) continue;
            float ry = y + 30 + i * ROW_H + 4;
            float sw = 16;
            res.drawQuad(x + 8, ry, sw, sw, v.r, v.g, v.b, 1f);
            res.drawText("#" + v.id + " (" + String.format("%.2f,%.2f,%.2f", v.r, v.g, v.b) + ")",
                x + 30, ry, 1.2f, tR, tG, tB);
        }
    }
}