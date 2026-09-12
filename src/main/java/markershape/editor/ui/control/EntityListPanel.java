package markershape.editor.ui.control;

import gamegl.gestion.texte.Text;
import markershape.config.ConfigParametres;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.menu.BlurBackground;
import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class EntityListPanel {
    public static final int MODE_VERTEX = 0;
    public static final int MODE_EDGE = 1;

    private final UIResources res;
    private int pw = 260;
    private int y, h;
    private int activeMode = MODE_VERTEX;
    private int hoveredId = -1;
    private int scrollOffset;
    private ShapeData data;

    private static final int HEADER_H = 28;
    private static final int ITEM_H = 22;
    private static final int NAV_W = 44;
    private static final int PANE_X = 0;

    public EntityListPanel(UIResources res) {
        this.res = res;
    }

    public void setSize(int w, int h) {
        this.h = h;
        res.setSize(w, h);
    }
    public void setData(ShapeData d) { data = d; }
    public int getActiveMode() { return activeMode; }
    public void setActiveMode(int mode) { activeMode = mode; hoveredId = -1; scrollOffset = 0; }
    public int getHoveredId() { return hoveredId; }

    public boolean contains(float mx, float my) {
        return mx >= PANE_X && mx <= PANE_X + pw && my >= y && my <= y + h;
    }

    public int clickTab(float mx, float my) {
        if (my < y || my > y + HEADER_H || mx < PANE_X || mx > PANE_X + pw - NAV_W) return -1;
        int halfW = (pw - NAV_W) / 2;
        if (mx < PANE_X + halfW) return MODE_VERTEX;
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

    public int clickList(float mx, float my) {
        return click(mx, my);
    }

    public int clickArrow(float mx, float my) {
        if (my < y || my > y + HEADER_H || mx < PANE_X || mx > PANE_X + pw) return -1;
        float navX = PANE_X + pw - NAV_W;
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

        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        res.begin2D();

        res.uiShader().bind();
        res.uiShader().setUniformMat4f("projection", res.ortho());

        float panelAlpha = BlurBackground.panelAlpha();
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        res.buf().clear();
        res.buf().put(new float[]{
            PANE_X, paneY, mr, mg, mb, panelAlpha,
            PANE_X + pw, paneY, mr, mg, mb, panelAlpha,
            PANE_X + pw, paneY + paneH, mr, mg, mb, panelAlpha,
            PANE_X, paneY, mr, mg, mb, panelAlpha,
            PANE_X + pw, paneY + paneH, mr, mg, mb, panelAlpha,
            PANE_X, paneY + paneH, mr, mg, mb, panelAlpha,
        }).flip();
        drawQuad();

        res.uiShader().unbind();

        float midX = PANE_X + (pw - NAV_W) / 2;

        for (int t = 0; t < 2; t++) {
            float tx = t == 0 ? PANE_X : midX;
            float tw = t == 0 ? midX - PANE_X : PANE_X + pw - NAV_W - midX;
            boolean act = (t == 0 && activeMode == MODE_VERTEX) || (t == 1 && activeMode == MODE_EDGE);
            res.buf().clear();
            res.buf().put(new float[]{
                tx, paneY, mr, mg, mb, panelAlpha,
                tx+tw-1, paneY, mr, mg, mb, panelAlpha,
                tx+tw-1, paneY+HEADER_H, mr, mg, mb, panelAlpha,
                tx, paneY, mr, mg, mb, panelAlpha,
                tx+tw-1, paneY+HEADER_H, mr, mg, mb, panelAlpha,
                tx, paneY+HEADER_H, mr, mg, mb, panelAlpha,
            }).flip();
            drawQuad();
            if (act) {
                res.buf().clear();
                res.buf().put(new float[]{
                    tx, paneY+HEADER_H-3, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                    tx+tw-1, paneY+HEADER_H-3, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                    tx+tw-1, paneY+HEADER_H, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                    tx, paneY+HEADER_H-3, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                    tx+tw-1, paneY+HEADER_H, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                    tx, paneY+HEADER_H, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                }).flip();
                drawQuad();
            }
            res.uiShader().unbind();
            String label = t == 0 ? "Sommets" : "Ar\u00EAtes";
            Text.drawText(res.textShader(), label, tx + 10, paneY + 5, 1.5f,
                tR * (act ? 1f : 0.6f), tG * (act ? 1f : 0.6f), tB * (act ? 1f : 0.6f));
            res.uiShader().bind();
            res.uiShader().setUniformMat4f("projection", res.ortho());
        }

        float navX = PANE_X + pw - NAV_W;
        res.buf().clear();
        res.buf().put(new float[]{
            navX, paneY, mr, mg, mb, panelAlpha,
            PANE_X+pw, paneY, mr, mg, mb, panelAlpha,
            PANE_X+pw, paneY+HEADER_H, mr, mg, mb, panelAlpha,
            navX, paneY, mr, mg, mb, panelAlpha,
            PANE_X+pw, paneY+HEADER_H, mr, mg, mb, panelAlpha,
            navX, paneY+HEADER_H, mr, mg, mb, panelAlpha,
        }).flip();
        drawQuad();

        {
            int vis = visibleItems();
            int tot = totalItems();
            boolean canPrev = scrollOffset > 0;
            boolean canNext = scrollOffset + vis < tot;
            res.uiShader().unbind();
            Text.drawText(res.textShader(), "<", navX + 10, paneY + 5, 1.5f,
                tR * (canPrev ? 1f : 0.4f), tG * (canPrev ? 1f : 0.4f), tB * (canPrev ? 1f : 0.4f));
            Text.drawText(res.textShader(), ">", navX + 28, paneY + 5, 1.5f,
                tR * (canNext ? 1f : 0.4f), tG * (canNext ? 1f : 0.4f), tB * (canNext ? 1f : 0.4f));
            res.uiShader().bind();
            res.uiShader().setUniformMat4f("projection", res.ortho());
        }

        float lineY = paneY + HEADER_H;
        res.buf().clear();
        res.buf().put(new float[]{
            PANE_X + 8, lineY, mr+0.1f, mg+0.1f, mb+0.1f, 1f,
            PANE_X + pw - 8, lineY, mr+0.1f, mg+0.1f, mb+0.1f, 1f,
        }).flip();
        glBindVertexArray(res.vao());
        glBindBuffer(GL_ARRAY_BUFFER, res.vbo());
        glBufferData(GL_ARRAY_BUFFER, res.buf(), GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINES, 0, 2);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        res.uiShader().bind();
        res.uiShader().setUniformMat4f("projection", res.ortho());

        if (activeMode == MODE_VERTEX) {
            Vertex[] vs = data.vertices.values().toArray(new Vertex[0]);
            for (int i = scrollOffset; i < vs.length; i++) {
                float iy = paneY + HEADER_H + (i - scrollOffset) * ITEM_H;
                if (iy + ITEM_H > paneY + paneH) break;
                boolean hover = vs[i].id == hoveredId;
                if (hover) drawItemHighlight(iy);
                Vertex v = vs[i];
                res.uiShader().unbind();
                String label = "#" + v.id + "  (" + fmt(v.x) + ", " + fmt(v.y) + ", " + fmt(v.z) + ")";
                Text.drawText(res.textShader(), label, PANE_X + 10, iy + 2, 1.5f,
                    tR * (hover ? 1f : 0.7f), tG * (hover ? 1f : 0.7f), tB * (hover ? 1f : 0.7f));
                res.uiShader().bind();
                res.uiShader().setUniformMat4f("projection", res.ortho());
            }
        } else {
            Edge[] es = data.edges.values().toArray(new Edge[0]);
            for (int i = scrollOffset; i < es.length; i++) {
                float iy = paneY + HEADER_H + (i - scrollOffset) * ITEM_H;
                if (iy + ITEM_H > paneY + paneH) break;
                boolean hover = es[i].id == hoveredId;
                if (hover) drawItemHighlight(iy);
                Edge e = es[i];
                res.uiShader().unbind();
                String label = "#" + e.id + "  " + e.a + "\u2192" + e.b + "  [" + e.mode + "]";
                Text.drawText(res.textShader(), label, PANE_X + 10, iy + 2, 1.5f,
                    tR * (hover ? 1f : 0.7f), tG * (hover ? 1f : 0.7f), tB * (hover ? 1f : 0.7f));
                res.uiShader().bind();
                res.uiShader().setUniformMat4f("projection", res.ortho());
            }
        }
        res.uiShader().unbind();
        glDisable(GL_BLEND);
    }

    public void updateHover(float mx, float my) {
        if (!contains(mx, my) || data == null) { hoveredId = -1; return; }
        hoveredId = getHoveredIdAt(mx, my);
    }

    private void drawItemHighlight(float iy) {
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        res.buf().clear();
        res.buf().put(new float[]{
            PANE_X + 4, iy, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
            PANE_X + pw - 4, iy, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
            PANE_X + pw - 4, iy + ITEM_H, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
            PANE_X + 4, iy, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
            PANE_X + pw - 4, iy + ITEM_H, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
            PANE_X + 4, iy + ITEM_H, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
        }).flip();
        drawQuad();
    }

    private void drawQuad() {
        glBindVertexArray(res.vao());
        glBindBuffer(GL_ARRAY_BUFFER, res.vbo());
        glBufferData(GL_ARRAY_BUFFER, res.buf(), GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private static String fmt(float v) {
        if (v == (int) v) return String.valueOf((int) v);
        return String.format("%.2f", v);
    }
}