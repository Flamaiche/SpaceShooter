package markershape.editor.ui.control;

import markershape.config.ConfigParametres;
import markershape.editor.ui.framework.UIContainer;
import markershape.editor.ui.framework.UIRenderer;
import markershape.editor.ui.framework.UIText;
import markershape.editor.ui.menu.BlurBackground;
import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;

public class EntityListPanel {
    public static final int MODE_VERTEX = 0;
    public static final int MODE_EDGE = 1;

    private int px, pw = 260;
    private int y, h;
    private int width, height;
    private int activeMode = MODE_VERTEX;
    private int hoveredId = -1;
    private int scrollOffset;
    private ShapeData data;
    private final UIRenderer renderer = new UIRenderer();
    private final UIContainer root = new UIContainer(0, 0, 1, 1);

    private static final int HEADER_H = 28;
    private static final int ITEM_H = 22;
    private static final int NAV_W = 44;

    private static float ts(float designScale) { return designScale / 720f; }

    public EntityListPanel() {
        root.alpha = UIContainer.Alpha.NONE;
    }

    public void setSize(int w, int h) {
        this.width = w;
        this.height = h;
        renderer.setScreenSize(w, h);
    }
    public void setData(ShapeData d) { data = d; }
    public int getActiveMode() { return activeMode; }
    public void setActiveMode(int mode) { activeMode = mode; hoveredId = -1; scrollOffset = 0; }
    public int getHoveredId() { return hoveredId; }

    public boolean contains(float mx, float my) {
        return mx >= px && mx <= px + pw && my >= y && my <= y + h;
    }

    public int clickTab(float mx, float my) {
        if (my < y || my > y + HEADER_H || mx < px || mx > px + pw - NAV_W) return -1;
        int halfW = (pw - NAV_W) / 2;
        if (mx < px + halfW) return MODE_VERTEX;
        return MODE_EDGE;
    }

    public int getHoveredIdAt(float mx, float my) {
        if (!contains(mx, my) || data == null) return -1;
        if (my < y + HEADER_H) return -1;
        float ly = my - y - HEADER_H;
        int idx = (int) (ly / ITEM_H) + scrollOffset;
        if (activeMode == MODE_VERTEX) {
            Vertex[] vs = data.vertices.values().toArray(new Vertex[0]);
            if (idx >= 0 && idx < vs.length) return vs[idx].id;
        } else {
            Edge[] es = data.edges.values().toArray(new Edge[0]);
            if (idx >= 0 && idx < es.length) return es[idx].id;
        }
        return -1;
    }

    public int click(float mx, float my) {
        if (!contains(mx, my) || data == null) return -1;
        int arrow = clickArrow(mx, my);
        if (arrow == 0) { pagePrev(); return -2; }
        if (arrow == 1) { pageNext(); return -2; }
        int tab = clickTab(mx, my);
        if (tab >= 0) { setActiveMode(tab); return -2; }
        return getHoveredIdAt(mx, my);
    }

    public int clickArrow(float mx, float my) {
        if (my < y || my > y + HEADER_H || mx < px || mx > px + pw) return -1;
        float navX = px + pw - NAV_W;
        if (mx < navX) return -1;
        return mx < navX + NAV_W / 2 ? 0 : 1;
    }

    private int totalItems() {
        if (data == null) return 0;
        return activeMode == MODE_VERTEX ? data.vertices.size() : data.edges.size();
    }

    private int pageSize() { return Math.max(1, visibleItems()); }

    public void pagePrev() {
        scrollOffset = Math.max(0, scrollOffset - pageSize());
    }

    public void pageNext() {
        int max = Math.max(0, totalItems() - visibleItems());
        scrollOffset = Math.min(max, scrollOffset + pageSize());
    }

    private int visibleItems() {
        return (h - HEADER_H) / ITEM_H;
    }

    public void render(int screenW, int screenH) {
        if (data == null) return;
        int paneY = screenH * 58 / 100;
        int paneH = Math.min(HEADER_H + 10 * ITEM_H + 4, screenH * 35 / 100);
        this.y = paneY;
        this.h = paneH;
        root.clear();
        build(paneY, paneH);
        root.render(renderer);
    }

    private void build(int paneY, int paneH) {
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        UIContainer panel = new UIContainer((float) px / width, (float) paneY / height,
            (float) pw / width, (float) paneH / height);
        panel.alpha = UIContainer.Alpha.PANEL;
        panel.bgR = mr;
        panel.bgG = mg;
        panel.bgB = mb;
        root.add(panel);

        float midX = (pw - NAV_W) / 2f;
        for (int t = 0; t < 2; t++) {
            float tx = t == 0 ? 0 : midX;
            float tw = t == 0 ? midX : pw - NAV_W - midX;
            boolean act = (t == 0 && activeMode == MODE_VERTEX) || (t == 1 && activeMode == MODE_EDGE);
            if (act) {
                UIContainer underline = new UIContainer(tx / pw, (HEADER_H - 3f) / paneH,
                    (tw - 1f) / pw, 3f / paneH);
                underline.customAlpha = 0.8f;
                underline.bgR = Math.min(1f, mr + 0.25f);
                underline.bgG = Math.min(1f, mg + 0.45f);
                underline.bgB = Math.min(1f, mb + 0.8f);
                panel.add(underline);
            }
            UIText label = text(tx + 10f, 5f, t == 0 ? "Sommets" : "Aretes",
                tR * (act ? 1f : 0.8f), tG * (act ? 1f : 0.8f), tB * (act ? 1f : 0.8f), pw, paneH);
            panel.add(label);
        }

        int vis = visibleItems();
        int tot = totalItems();
        boolean canPrev = scrollOffset > 0;
        boolean canNext = scrollOffset + vis < tot;
        float navX = pw - NAV_W;
        panel.add(text(navX + 8f, 5f, "<", tR * (canPrev ? 1f : 0.4f), tG * (canPrev ? 1f : 0.4f), tB * (canPrev ? 1f : 0.4f), pw, paneH));
        panel.add(text(navX + 24f, 5f, ">", tR * (canNext ? 1f : 0.4f), tG * (canNext ? 1f : 0.4f), tB * (canNext ? 1f : 0.4f), pw, paneH));

        UIContainer sep = new UIContainer(8f / pw, (float) HEADER_H / paneH, (pw - 16f) / pw, 1f / paneH);
        sep.customAlpha = 1f;
        sep.bgR = Math.min(1f, mr + 0.1f);
        sep.bgG = Math.min(1f, mg + 0.1f);
        sep.bgB = Math.min(1f, mb + 0.1f);
        panel.add(sep);

        if (activeMode == MODE_VERTEX) {
            Vertex[] vs = data.vertices.values().toArray(new Vertex[0]);
            for (int i = scrollOffset; i < vs.length; i++) {
                float oy = HEADER_H + (i - scrollOffset) * ITEM_H;
                if (oy + ITEM_H > paneH) break;
                Vertex v = vs[i];
                boolean hover = v.id == hoveredId;
                if (hover) panel.add(highlight(oy, paneH));
                String label = "#" + v.id + "  (" + fmt(v.x) + ", " + fmt(v.y) + ", " + fmt(v.z) + ")";
                panel.add(text(10f, oy + 2f, label, tR * (hover ? 1f : 0.9f), tG * (hover ? 1f : 0.9f), tB * (hover ? 1f : 0.9f), pw, paneH));
            }
        } else {
            Edge[] es = data.edges.values().toArray(new Edge[0]);
            for (int i = scrollOffset; i < es.length; i++) {
                float oy = HEADER_H + (i - scrollOffset) * ITEM_H;
                if (oy + ITEM_H > paneH) break;
                Edge e = es[i];
                boolean hover = e.id == hoveredId;
                if (hover) panel.add(highlight(oy, paneH));
                String label = "#" + e.id + "  " + e.a + "->" + e.b + "  [" + e.mode + "]";
                panel.add(text(10f, oy + 2f, label, tR * (hover ? 1f : 0.9f), tG * (hover ? 1f : 0.9f), tB * (hover ? 1f : 0.9f), pw, paneH));
            }
        }
    }

    private UIContainer highlight(float oy, float paneH) {
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        UIContainer hl = new UIContainer(4f / pw, oy / paneH, (pw - 8f) / pw, (float) ITEM_H / paneH);
        hl.customAlpha = 0.25f;
        hl.bgR = Math.min(1f, mr + 0.15f);
        hl.bgG = Math.min(1f, mg + 0.3f);
        hl.bgB = Math.min(1f, mb + 0.7f);
        return hl;
    }

    private UIText text(float x, float y, String label, float r, float g, float b, float pw, float paneH) {
        UIText t = new UIText(x / pw, y / paneH, ts(1.5f), label);
        t.useConfigText = false;
        t.tR = r; t.tG = g; t.tB = b;
        return t;
    }

    public void updateHover(float mx, float my) {
        if (!contains(mx, my) || data == null) { hoveredId = -1; return; }
        hoveredId = getHoveredIdAt(mx, my);
    }

    private static String fmt(float v) {
        if (v == (int) v) return String.valueOf((int) v);
        return String.format("%.2f", v);
    }

    public void cleanup() {
        renderer.cleanup();
    }
}
