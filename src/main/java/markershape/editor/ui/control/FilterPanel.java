package markershape.editor.ui.control;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.config.ConfigParametres;
import markershape.editor.ui.menu.BlurBackground;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class FilterPanel {
    private int width, height;
    private boolean filterOpen;
    private float filterX, filterY;

    public String[] filterLabels = {"Faces", "Arêtes", "Points", "Axe X", "Axe Y", "Axe Z", "Snap"};
    public boolean[] filterValues = {true, true, true, true, true, true, false};

    public String[] sliderLabels = {"Taille pts", "Lignes", "Opacité", "Snap pas"};
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
    private Shader shader;
    private Shader textShader;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    public FilterPanel(Shader shader, Shader textShader, int vao, int vbo) {
        this.shader = shader;
        this.textShader = textShader;
        this.vao = vao;
        this.vbo = vbo;
    }

    public void setSize(int w, int h) { width = w; height = h; ortho.setOrtho2D(0, w, h, 0); }

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

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        filterX = btnX + (130 - PANEL_W) / 2;
        filterY = btnY;

        float ph = panelHeight();

        float panelAlpha = BlurBackground.panelAlpha();
        shader.bind();
        shader.setUniformMat4f("projection", ortho);
        buf.clear();
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        buf.put(new float[]{
            filterX, filterY, mr, mg, mb, panelAlpha,
            filterX+PANEL_W, filterY, mr, mg, mb, panelAlpha,
            filterX+PANEL_W, filterY+ph, mr, mg, mb, panelAlpha,
            filterX, filterY, mr, mg, mb, panelAlpha,
            filterX+PANEL_W, filterY+ph, mr, mg, mb, panelAlpha,
            filterX, filterY+ph, mr, mg, mb, panelAlpha,
        }).flip();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        shader.unbind();

        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        shader.bind();
        shader.setUniformMat4f("projection", ortho);

        for (int i = 0; i < filterLabels.length; i++) {
            float iy = filterY + i * CHECKBOX_H;
            String prefix = filterValues[i] ? "[x] " : "[ ] ";
            float brightness = filterValues[i] ? 1f : 0.6f;
            Text.drawText(textShader, prefix + filterLabels[i],
                filterX + 8, iy + 4, 1.5f, tR * brightness, tG * brightness, tB * brightness);
        }

        for (int i = 0; i < sliderLabels.length; i++) {
            float iy = sliderItemY(i);
            float trackY = iy + (SLIDER_H - 8) * 0.5f + 4;
            float trackX = filterX + TRACK_X;
            float val = sliderValues[i];
            float frac = (val - sliderMin[i]) / (sliderMax[i] - sliderMin[i]);

            String valStr = String.format("%." + SLIDER_DECIMALS + "f", sliderValues[i]);
            Text.drawText(textShader, sliderLabels[i] + ":",
                filterX + 8, iy + 2, 1.5f, tR, tG, tB);
            Text.drawText(textShader, valStr,
                filterX + VAL_X, iy + 2, 1.5f, tR, tG, tB);
            Text.drawText(textShader, "[-]",
                filterX + MINUS_X, iy + 2, 1.5f, tR, tG, tB);
            Text.drawText(textShader, "[+]",
                filterX + PLUS_X, iy + 2, 1.5f, tR, tG, tB);

            float trackA = BlurBackground.transparentUI ? 0.6f : 1f;
            float trackR = Math.min(1f, mr * 0.75f), trackG = Math.min(1f, mg * 0.75f), trackB = Math.min(1f, mb * 0.75f);
            float fillR = Math.min(1f, mr + 0.35f), fillG = Math.min(1f, mg + 0.35f), fillB = Math.min(1f, mb + 0.45f);
            float thumbR = Math.min(1f, mr + 0.6f), thumbG = Math.min(1f, mg + 0.6f), thumbB = Math.min(1f, mb + 0.6f);

            buf.clear();
            float tx = trackX, ty = trackY, tw = TRACK_W, th = 6;
            buf.put(new float[]{
                tx, ty, trackR, trackG, trackB, trackA,
                tx+tw, ty, trackR, trackG, trackB, trackA,
                tx+tw, ty+th, trackR, trackG, trackB, trackA,
                tx, ty, trackR, trackG, trackB, trackA,
                tx+tw, ty+th, trackR, trackG, trackB, trackA,
                tx, ty+th, trackR, trackG, trackB, trackA,
            }).flip();
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
            glDrawArrays(GL_TRIANGLES, 0, 6);

            float fw = Math.max(2, frac * tw);
            buf.clear();
            buf.put(new float[]{
                tx, ty, fillR, fillG, fillB, 1f,
                tx+fw, ty, fillR, fillG, fillB, 1f,
                tx+fw, ty+th, fillR, fillG, fillB, 1f,
                tx, ty, fillR, fillG, fillB, 1f,
                tx+fw, ty+th, fillR, fillG, fillB, 1f,
                tx, ty+th, fillR, fillG, fillB, 1f,
            }).flip();
            glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
            glDrawArrays(GL_TRIANGLES, 0, 6);

            float thumbX = tx + frac * tw - 3;
            float thumbY = ty - 1;
            buf.clear();
            buf.put(new float[]{
                thumbX, thumbY, thumbR, thumbG, thumbB, 1f,
                thumbX+6, thumbY, thumbR, thumbG, thumbB, 1f,
                thumbX+6, thumbY+8, thumbR, thumbG, thumbB, 1f,
                thumbX, thumbY, thumbR, thumbG, thumbB, 1f,
                thumbX+6, thumbY+8, thumbR, thumbG, thumbB, 1f,
                thumbX, thumbY+8, thumbR, thumbG, thumbB, 1f,
            }).flip();
            glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }

        shader.unbind();
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
}
