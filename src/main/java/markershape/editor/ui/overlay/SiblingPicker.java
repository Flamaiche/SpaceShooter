package markershape.editor.ui.overlay;

import markershape.config.ConfigParametres;
import markershape.editor.ui.framework.UIContainer;
import markershape.editor.ui.framework.UIRenderer;
import markershape.editor.ui.framework.UIText;
import markershape.editor.ui.menu.BlurBackground;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;

import java.util.function.Consumer;

public class SiblingPicker {
    private boolean visible;
    private int[] ids;
    private Vertex[] vertices;
    private float px, py, ph;
    private static final float PW = 220;
    private static final float ROW_H = 26;
    private Consumer<Integer> callback;
    private int width, height;
    private final UIRenderer renderer = new UIRenderer();
    private final UIContainer root = new UIContainer(0, 0, 1, 1);

    private static float ts(float designScale) { return designScale / 720f; }

    public SiblingPicker() {
        root.alpha = UIContainer.Alpha.NONE;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        renderer.setScreenSize(w, h);
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
        root.clear();
        build();
        root.render(renderer);
    }

    private void build() {
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        UIContainer panel = new UIContainer(px / width, py / height, PW / width, ph / height);
        panel.alpha = UIContainer.Alpha.PANEL;
        panel.bgR = mr;
        panel.bgG = mg;
        panel.bgB = mb;
        root.add(panel);

        UIText title = new UIText(8f / PW, 8f / ph, ts(1.5f), "Select vertex:");
        title.useConfigText = false;
        title.tR = tR; title.tG = tG; title.tB = tB;
        panel.add(title);

        float rowAlpha = BlurBackground.rowAlpha();
        for (int i = 0; i < ids.length; i++) {
            Vertex v = vertices[i];
            if (v == null) continue;
            float ry = 30 + i * ROW_H;
            float mult = (i % 2 == 0) ? 1.15f : 0.95f;

            UIContainer row = new UIContainer(2f / PW, ry / ph, (PW - 4f) / PW, ROW_H / ph);
            row.customAlpha = rowAlpha;
            row.bgR = Math.min(1f, mr * mult);
            row.bgG = Math.min(1f, mg * mult);
            row.bgB = Math.min(1f, mb * mult);
            panel.add(row);

            UIContainer swatch = new UIContainer(8f / PW, (ry + 4f) / ph, 16f / PW, 16f / ph);
            swatch.customAlpha = 1f;
            swatch.bgR = v.r; swatch.bgG = v.g; swatch.bgB = v.b;
            panel.add(swatch);

            UIText t = new UIText(30f / PW, (ry + 4f) / ph, ts(1.5f),
                "#" + v.id + " (" + String.format("%.2f,%.2f,%.2f", v.r, v.g, v.b) + ")");
            t.useConfigText = false;
            t.tR = tR; t.tG = tG; t.tB = tB;
            panel.add(t);
        }
    }

    public void cleanup() {
        renderer.cleanup();
    }
}
