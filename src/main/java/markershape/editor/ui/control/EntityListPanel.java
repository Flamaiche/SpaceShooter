package markershape.editor.ui.control;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.config.ConfigParametres;
import markershape.editor.ui.menu.BlurBackground;
import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class EntityListPanel {
    public static final int MODE_VERTEX = 0;
    public static final int MODE_EDGE = 1;

    private int px, pw = 260;
    private int y, h;
    private int activeMode = MODE_VERTEX;
    private int hoveredId = -1;
    private int scrollOffset;
    private ShapeData data;
    private Shader shader, textShader;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    private static final int HEADER_H = 28;
    private static final int ITEM_H = 22;
    private static final int NAV_W = 44;

    public EntityListPanel(Shader shader, Shader textShader, int vao, int vbo) {
        this.shader = shader;
        this.textShader = textShader;
        this.vao = vao;
        this.vbo = vbo;
    }

    public void setSize(int w, int h) { this.h = h; ortho.setOrtho2D(0, w, h, 0); }
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

        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        shader.setUniformMat4f("projection", ortho);

        float panelAlpha = BlurBackground.panelAlpha();
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        buf.clear();
        buf.put(new float[]{
            px, paneY, mr, mg, mb, panelAlpha,
            px + pw, paneY, mr, mg, mb, panelAlpha,
            px + pw, paneY + paneH, mr, mg, mb, panelAlpha,
            px, paneY, mr, mg, mb, panelAlpha,
            px + pw, paneY + paneH, mr, mg, mb, panelAlpha,
            px, paneY + paneH, mr, mg, mb, panelAlpha,
        }).flip();
        drawQuad();

        shader.unbind();

        float midX = px + (pw - NAV_W) / 2;

        for (int t = 0; t < 2; t++) {
            float tx = t == 0 ? px : midX;
            float tw = t == 0 ? midX - px : px + pw - NAV_W - midX;
            boolean act = (t == 0 && activeMode == MODE_VERTEX) || (t == 1 && activeMode == MODE_EDGE);
            buf.clear();
            buf.put(new float[]{
                tx, paneY, mr, mg, mb, panelAlpha,
                tx+tw-1, paneY, mr, mg, mb, panelAlpha,
                tx+tw-1, paneY+HEADER_H, mr, mg, mb, panelAlpha,
                tx, paneY, mr, mg, mb, panelAlpha,
                tx+tw-1, paneY+HEADER_H, mr, mg, mb, panelAlpha,
                tx, paneY+HEADER_H, mr, mg, mb, panelAlpha,
            }).flip();
            drawQuad();
            if (act) {
                buf.clear();
                buf.put(new float[]{
                    tx, paneY+HEADER_H-3, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                    tx+tw-1, paneY+HEADER_H-3, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                    tx+tw-1, paneY+HEADER_H, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                    tx, paneY+HEADER_H-3, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                    tx+tw-1, paneY+HEADER_H, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                    tx, paneY+HEADER_H, mr+0.25f, mg+0.45f, mb+0.8f, 0.8f,
                }).flip();
                drawQuad();
            }
            shader.unbind();
            String label = t == 0 ? "Sommets" : "Ar\u00EAtes";
            Text.drawText(textShader, label, tx + 10, paneY + 5, 1.5f,
                tR * (act ? 1f : 0.6f), tG * (act ? 1f : 0.6f), tB * (act ? 1f : 0.6f));
            shader.bind();
            shader.setUniformMat4f("projection", ortho);
        }

        float navX = px + pw - NAV_W;
        buf.clear();
        buf.put(new float[]{
            navX, paneY, mr, mg, mb, panelAlpha,
            px+pw, paneY, mr, mg, mb, panelAlpha,
            px+pw, paneY+HEADER_H, mr, mg, mb, panelAlpha,
            navX, paneY, mr, mg, mb, panelAlpha,
            px+pw, paneY+HEADER_H, mr, mg, mb, panelAlpha,
            navX, paneY+HEADER_H, mr, mg, mb, panelAlpha,
        }).flip();
        drawQuad();

        {
            int vis = visibleItems();
            int tot = totalItems();
            boolean canPrev = scrollOffset > 0;
            boolean canNext = scrollOffset + vis < tot;
            shader.unbind();
            Text.drawText(textShader, "<", navX + 10, paneY + 5, 1.5f,
                tR * (canPrev ? 1f : 0.4f), tG * (canPrev ? 1f : 0.4f), tB * (canPrev ? 1f : 0.4f));
            Text.drawText(textShader, ">", navX + 28, paneY + 5, 1.5f,
                tR * (canNext ? 1f : 0.4f), tG * (canNext ? 1f : 0.4f), tB * (canNext ? 1f : 0.4f));
            shader.bind();
            shader.setUniformMat4f("projection", ortho);
        }

        float lineY = paneY + HEADER_H;
        buf.clear();
        buf.put(new float[]{
            px + 8, lineY, mr+0.1f, mg+0.1f, mb+0.1f, 1f,
            px + pw - 8, lineY, mr+0.1f, mg+0.1f, mb+0.1f, 1f,
        }).flip();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINES, 0, 2);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        shader.bind();
        shader.setUniformMat4f("projection", ortho);

        if (activeMode == MODE_VERTEX) {
            Vertex[] vs = data.vertices.values().toArray(new Vertex[0]);
            for (int i = scrollOffset; i < vs.length; i++) {
                float iy = paneY + HEADER_H + (i - scrollOffset) * ITEM_H;
                if (iy + ITEM_H > paneY + paneH) break;
                boolean hover = vs[i].id == hoveredId;
                if (hover) drawItemHighlight(iy);
                Vertex v = vs[i];
                shader.unbind();
                String label = "#" + v.id + "  (" + fmt(v.x) + ", " + fmt(v.y) + ", " + fmt(v.z) + ")";
                Text.drawText(textShader, label, px + 10, iy + 2, 1.5f,
                    tR * (hover ? 1f : 0.7f), tG * (hover ? 1f : 0.7f), tB * (hover ? 1f : 0.7f));
                shader.bind();
                shader.setUniformMat4f("projection", ortho);
            }
        } else {
            Edge[] es = data.edges.values().toArray(new Edge[0]);
            for (int i = scrollOffset; i < es.length; i++) {
                float iy = paneY + HEADER_H + (i - scrollOffset) * ITEM_H;
                if (iy + ITEM_H > paneY + paneH) break;
                boolean hover = es[i].id == hoveredId;
                if (hover) drawItemHighlight(iy);
                Edge e = es[i];
                shader.unbind();
                String label = "#" + e.id + "  " + e.a + "\u2192" + e.b + "  [" + e.mode + "]";
                Text.drawText(textShader, label, px + 10, iy + 2, 1.5f,
                    tR * (hover ? 1f : 0.7f), tG * (hover ? 1f : 0.7f), tB * (hover ? 1f : 0.7f));
                shader.bind();
                shader.setUniformMat4f("projection", ortho);
            }
        }
        shader.unbind();
        glDisable(GL_BLEND);
    }

    public void updateHover(float mx, float my) {
        if (!contains(mx, my) || data == null) { hoveredId = -1; return; }
        hoveredId = getHoveredIdAt(mx, my);
    }

    private void drawItemHighlight(float iy) {
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        buf.clear();
        buf.put(new float[]{
            px + 4, iy, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
            px + pw - 4, iy, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
            px + pw - 4, iy + ITEM_H, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
            px + 4, iy, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
            px + pw - 4, iy + ITEM_H, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
            px + 4, iy + ITEM_H, mr+0.15f, mg+0.3f, mb+0.7f, 0.25f,
        }).flip();
        drawQuad();
    }

    private void drawQuad() {
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private static String fmt(float v) {
        if (v == (int) v) return String.valueOf((int) v);
        return String.format("%.2f", v);
    }
}
