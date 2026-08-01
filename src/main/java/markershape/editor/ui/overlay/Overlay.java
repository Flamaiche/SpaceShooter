package markershape.editor.ui.overlay;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.config.ConfigParametres;
import markershape.editor.ui.control.Button;
import markershape.editor.ui.menu.BlurBackground;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public abstract class Overlay {
    protected boolean visible;
    protected float px, py;
    protected final float pw, ph;
    protected final Button closeBtn;
    protected final Button deleteBtn;
    protected int selectedField = -1;
    protected Runnable editCallback;
    protected Runnable preEditCallback;
    protected Runnable deleteCallback;

    public Overlay(float pw, float ph) {
        this.pw = pw;
        this.ph = ph;
        px = 100; py = 100;
        closeBtn = new Button("X", px + pw - 28, py + 4, 24, 24, null);
        closeBtn.showBackground = false;
        closeBtn.textScale = 1.5f;
        closeBtn.textR = 1f; closeBtn.textG = 0.3f; closeBtn.textB = 0.3f;
        deleteBtn = new Button("Delete", px + 10, py + ph - 38, pw - 20, 28,
            () -> { if (deleteCallback != null) deleteCallback.run(); });
        deleteBtn.bgR = 0.5f; deleteBtn.bgG = 0.1f; deleteBtn.bgB = 0.1f;
        deleteBtn.textR = 1f; deleteBtn.textG = 1f; deleteBtn.textB = 1f;
    }

    public void setDeleteCallback(Runnable cb) { deleteCallback = cb; }
    public void hide() { visible = false; selectedField = -1; }
    public boolean isVisible() { return visible; }
    public boolean isCloseClicked(float mx, float my) { return visible && closeBtn.isClicked(mx, my); }
    public boolean contains(float mx, float my) {
        return mx >= px && mx <= px + pw && my >= py && my <= py + ph;
    }
    public void setEditCallback(Runnable cb) { editCallback = cb; }
    public void setPreEditCallback(Runnable cb) { preEditCallback = cb; }

    protected abstract boolean hasEntity();

    public void render(Shader uiShader, Shader textShader, Matrix4f ortho,
                       FloatBuffer buf, int vao, int vbo) {
        if (!visible || !hasEntity()) return;

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        uiShader.bind();
        uiShader.setUniformMat4f("projection", ortho);

        float alpha = BlurBackground.panelAlpha();
        float mr = BlurBackground.menuR;
        float mg = BlurBackground.menuG;
        float mb = BlurBackground.menuB;
        buf.clear();
        buf.put(new float[]{
            px,    py,    mr, mg, mb, alpha,
            px+pw, py,    mr, mg, mb, alpha,
            px+pw, py+ph, mr, mg, mb, alpha,
            px,    py,    mr, mg, mb, alpha,
            px+pw, py+ph, mr, mg, mb, alpha,
            px,    py+ph, mr, mg, mb, alpha,
        }).flip();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        renderContent(uiShader, textShader, ortho, buf, vao, vbo);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        uiShader.unbind();

        renderText(textShader);

        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        deleteBtn.textR = tR; deleteBtn.textG = tG; deleteBtn.textB = tB;
        deleteBtn.bgA = BlurBackground.btnAlpha();
        deleteBtn.render(uiShader, textShader, ortho, buf, vao, vbo);
        closeBtn.render(uiShader, textShader, ortho, buf, vao, vbo);
    }

    protected abstract void renderContent(Shader uiShader, Shader textShader, Matrix4f ortho,
                                          FloatBuffer buf, int vao, int vbo);

    protected abstract void renderText(Shader textShader);

    public void setPosition(float x, float y) {
        px = x;
        py = y;
        closeBtn.x = px + pw - 28;
        closeBtn.y = py + 4;
        deleteBtn.x = px + 10;
        deleteBtn.y = py + ph - 38;
    }

    public float getPx() { return px; }
    public float getPy() { return py; }
    public float getPw() { return pw; }
    public float getPh() { return ph; }

    protected void drawHighlightRect(FloatBuffer buf, float x, float y, float w, float h) {
        buf.clear();
        buf.put(new float[]{
            x, y, 0.3f, 0.5f, 0.9f, 0.3f,
            x+w, y, 0.3f, 0.5f, 0.9f, 0.3f,
            x+w, y+h, 0.3f, 0.5f, 0.9f, 0.3f,
            x, y, 0.3f, 0.5f, 0.9f, 0.3f,
            x+w, y+h, 0.3f, 0.5f, 0.9f, 0.3f,
            x, y+h, 0.3f, 0.5f, 0.9f, 0.3f,
        }).flip();
    }

    protected void drawLine(FloatBuffer buf, float x1, float y1, float x2, float y2,
                            float r, float g, float b, float a) {
        buf.clear();
        buf.put(new float[]{ x1, y1, r, g, b, a, x2, y2, r, g, b, a }).flip();
    }
}
