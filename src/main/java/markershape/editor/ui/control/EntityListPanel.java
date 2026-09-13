package markershape.editor.ui.control;

import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.menu.BlurBackground;
import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;

public class EntityListPanel extends Panel {
    public static final int MODE_VERTEX = 0;
    public static final int MODE_EDGE = 1;

    private int height;
    private static final int PW = 260;

    private int activeMode = MODE_VERTEX;
    private int hoveredId = -1;
    private int scrollOffset;
    private ShapeData data;

    private static final int HEADER_H = 28;
    private static final int ITEM_H = 22;
    private static final int NAV_W = 44;

    public EntityListPanel(UIResources res) {
        super(res);
    }

    public void setSize(int w, int h) {
        height = h;
        res.setSize(w, h);
    }
    public void setData(ShapeData d) { data = d; }
    public int getActiveMode() { return activeMode; }
    public void setActiveMode(int mode) { activeMode = mode; hoveredId = -1; scrollOffset = 0; }
    public int getHoveredId() { return hoveredId; }

    @Override
    public boolean contains(float mx, float my) {
        return mx >= x && mx <= x + PW && my >= y && my <= y + h;
    }

    public int clickTab(float mx, float my) {
        if (my < y || my > y + HEADER_H || mx < x || mx > x + PW - NAV_W) return -1;
        int halfW = (PW - NAV_W) / 2;
        if (mx < x + halfW) return MODE_VERTEX;
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

    public int clickItem(float mx, float my) {
        if (!contains(mx, my) || data == null) return -1;
        int arrow = clickArrow(mx, my);
        if (arrow == 0) { pagePrev(); return -2; }
        if (arrow == 1) { pageNext(); return -2; }
        int tab = clickTab(mx, my);
        if (tab >= 0) { setActiveMode(tab); return -2; }
        return getHoveredIdAt(mx, my);
    }

    public int clickList(float mx, float my) {
        return clickItem(mx, my);
    }

    public int clickArrow(float mx, float my) {
        if (my < y || my > y + HEADER_H || mx < x || mx > x + PW) return -1;
        float navX = x + PW - NAV_W;
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
        return (int) (h - HEADER_H) / ITEM_H;
    }

    @Override
    protected void drawBackground() {
        if (data == null) return;
        int paneY = height * 58 / 100;
        int paneH = Math.min(HEADER_H + 10 * ITEM_H + 4, height * 35 / 100);
        x = 0;
        y = paneY;
        w = PW;
        h = paneH;
        float[] c = res.menuColor();
        res.drawQuad(x, y, w, h, c[0], c[1], c[2], BlurBackground.panelAlpha());
    }

    @Override
    protected void renderContent() {
        float panelAlpha = BlurBackground.panelAlpha();
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        float midX = x + (PW - NAV_W) / 2;

        for (int t = 0; t < 2; t++) {
            float tx = t == 0 ? x : midX;
            float tw = t == 0 ? midX - x : x + PW - NAV_W - midX;
            boolean act = (t == 0 && activeMode == MODE_VERTEX) || (t == 1 && activeMode == MODE_EDGE);
            res.drawQuad(tx, y, tw - 1, HEADER_H, mr, mg, mb, panelAlpha);
            if (act) {
                res.drawQuad(tx, y + HEADER_H - 3, tw - 1, 3, mr + 0.25f, mg + 0.45f, mb + 0.8f, 0.8f);
            }
        }

        float navX = x + PW - NAV_W;
        res.drawQuad(navX, y, NAV_W, HEADER_H, mr, mg, mb, panelAlpha);

        res.drawLine(x + 8, y + HEADER_H, x + PW - 8, y + HEADER_H,
            mr + 0.1f, mg + 0.1f, mb + 0.1f, 1f);

        if (activeMode == MODE_VERTEX) {
            Vertex[] vs = data.vertices.values().toArray(new Vertex[0]);
            for (int i = scrollOffset; i < vs.length; i++) {
                float iy = y + HEADER_H + (i - scrollOffset) * ITEM_H;
                if (iy + ITEM_H > y + h) break;
                if (vs[i].id == hoveredId) {
                    res.drawQuad(x + 4, iy, PW - 8, ITEM_H, mr + 0.15f, mg + 0.3f, mb + 0.7f, 0.25f);
                }
            }
        } else {
            Edge[] es = data.edges.values().toArray(new Edge[0]);
            for (int i = scrollOffset; i < es.length; i++) {
                float iy = y + HEADER_H + (i - scrollOffset) * ITEM_H;
                if (iy + ITEM_H > y + h) break;
                if (es[i].id == hoveredId) {
                    res.drawQuad(x + 4, iy, PW - 8, ITEM_H, mr + 0.15f, mg + 0.3f, mb + 0.7f, 0.25f);
                }
            }
        }
    }

    @Override
    protected void renderText() {
        float[] tc = res.textColor();
        float tR = tc[0], tG = tc[1], tB = tc[2];
        float midX = x + (PW - NAV_W) / 2;

        for (int t = 0; t < 2; t++) {
            float tx = t == 0 ? x : midX;
            boolean act = (t == 0 && activeMode == MODE_VERTEX) || (t == 1 && activeMode == MODE_EDGE);
            String label = t == 0 ? "Sommets" : "Ar\u00EAtes";
            res.drawText(label, tx + 10, y + 5, 1.5f,
                tR * (act ? 1f : 0.6f), tG * (act ? 1f : 0.6f), tB * (act ? 1f : 0.6f));
        }

        float navX = x + PW - NAV_W;
        int vis = visibleItems();
        int tot = totalItems();
        boolean canPrev = scrollOffset > 0;
        boolean canNext = scrollOffset + vis < tot;
        res.drawText("<", navX + 10, y + 5, 1.5f,
            tR * (canPrev ? 1f : 0.4f), tG * (canPrev ? 1f : 0.4f), tB * (canPrev ? 1f : 0.4f));
        res.drawText(">", navX + 28, y + 5, 1.5f,
            tR * (canNext ? 1f : 0.4f), tG * (canNext ? 1f : 0.4f), tB * (canNext ? 1f : 0.4f));

        if (data == null) return;

        if (activeMode == MODE_VERTEX) {
            Vertex[] vs = data.vertices.values().toArray(new Vertex[0]);
            for (int i = scrollOffset; i < vs.length; i++) {
                float iy = y + HEADER_H + (i - scrollOffset) * ITEM_H;
                if (iy + ITEM_H > y + h) break;
                boolean hover = vs[i].id == hoveredId;
                Vertex v = vs[i];
                String label = "#" + v.id + "  (" + fmt(v.x) + ", " + fmt(v.y) + ", " + fmt(v.z) + ")";
                res.drawText(label, x + 10, iy + 2, 1.5f,
                    tR * (hover ? 1f : 0.7f), tG * (hover ? 1f : 0.7f), tB * (hover ? 1f : 0.7f));
            }
        } else {
            Edge[] es = data.edges.values().toArray(new Edge[0]);
            for (int i = scrollOffset; i < es.length; i++) {
                float iy = y + HEADER_H + (i - scrollOffset) * ITEM_H;
                if (iy + ITEM_H > y + h) break;
                boolean hover = es[i].id == hoveredId;
                Edge e = es[i];
                String label = "#" + e.id + "  " + e.a + "\u2192" + e.b + "  [" + e.mode + "]";
                res.drawText(label, x + 10, iy + 2, 1.5f,
                    tR * (hover ? 1f : 0.7f), tG * (hover ? 1f : 0.7f), tB * (hover ? 1f : 0.7f));
            }
        }
    }

    public void updateHover(float mx, float my) {
        if (!contains(mx, my) || data == null) { hoveredId = -1; return; }
        hoveredId = getHoveredIdAt(mx, my);
    }

    private static String fmt(float v) {
        if (v == (int) v) return String.valueOf((int) v);
        return String.format("%.2f", v);
    }
}