package markershape.editor.ui.menu;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class NewMenu {
    private boolean newMenuOpen;
    private int activeMode = -1; // -1=none, 0=vertex, 1=edge
    public static final float NEW_DROP_W = 130;
    public static final float NEW_ITEM_H = 26;

    private float btnX, btnY;
    private Shader shader;
    private Shader textShader;
    private BlurBackground blur;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    public NewMenu(Shader shader, Shader textShader, int vao, int vbo, BlurBackground blur) {
        this.shader = shader;
        this.textShader = textShader;
        this.vao = vao;
        this.vbo = vbo;
        this.blur = blur;
    }

    public boolean isOpen() { return newMenuOpen; }
    public void setOpen(boolean v) { newMenuOpen = v; }
    public void toggle() { newMenuOpen = !newMenuOpen; }
    public void close() { newMenuOpen = false; }

    public void setActiveMode(int mode) { activeMode = mode; }
    public int getActiveMode() { return activeMode; }

    public void setSize(int w, int h) { ortho.setOrtho2D(0, w, h, 0); }

    public void setBtnPos(float x, float y) { btnX = x; btnY = y; }

    public boolean contains(float mx, float my) {
        return newMenuOpen && my >= btnY + 36 && my <= btnY + 36 + 2 * NEW_ITEM_H
            && mx >= btnX && mx <= btnX + NEW_DROP_W;
    }

    public void render() {
        if (!newMenuOpen) return;

        float dx = btnX;
        float dy = btnY + 36;
        float dh = 2 * NEW_ITEM_H;
        float border = 1f;

        blur.drawBlurredBg(dx - border, dy - border, NEW_DROP_W + 2 * border, dh + 2 * border,
            0.82f, 0.5f, 0.5f, 0.55f);

        shader.bind();
        shader.setUniformMat4f("projection", ortho);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        buf.clear();
        buf.put(new float[]{
            dx - border, dy - border, 0.5f, 0.5f, 0.7f, 0.9f,
            dx + NEW_DROP_W + border, dy - border, 0.5f, 0.5f, 0.7f, 0.9f,
            dx + NEW_DROP_W + border, dy + dh + border, 0.5f, 0.5f, 0.7f, 0.9f,
            dx - border, dy - border, 0.5f, 0.5f, 0.7f, 0.9f,
            dx + NEW_DROP_W + border, dy + dh + border, 0.5f, 0.5f, 0.7f, 0.9f,
            dx - border, dy + dh + border, 0.5f, 0.5f, 0.7f, 0.9f,
        }).flip();
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        for (int i = 0; i < 2; i++) {
            float iy = dy + i * NEW_ITEM_H;
            if (i == activeMode) {
                buf.clear();
                buf.put(new float[]{
                    dx, iy, 0.3f, 0.25f, 0.15f, 0.85f,
                    dx + NEW_DROP_W, iy, 0.3f, 0.25f, 0.15f, 0.85f,
                    dx + NEW_DROP_W, iy + NEW_ITEM_H, 0.3f, 0.25f, 0.15f, 0.85f,
                    dx, iy, 0.3f, 0.25f, 0.15f, 0.85f,
                    dx + NEW_DROP_W, iy + NEW_ITEM_H, 0.3f, 0.25f, 0.15f, 0.85f,
                    dx, iy + NEW_ITEM_H, 0.3f, 0.25f, 0.15f, 0.85f,
                }).flip();
                glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
                glDrawArrays(GL_TRIANGLES, 0, 6);
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        shader.unbind();

        String[] items = {"Vertex", "Edge"};
        for (int i = 0; i < 2; i++) {
            float iy = dy + i * NEW_ITEM_H;
            String prefix = (i == activeMode) ? "> " : "  ";
            Text.drawText(textShader, prefix + items[i],
                dx + 8, iy + 4, 1.3f,
                i == activeMode ? 1f : 0.8f,
                i == activeMode ? 1f : 0.8f,
                i == activeMode ? 0.4f : 0.8f);
        }
    }

    /** Returns 0=Vertex, 1=Edge, -1=nothing. */
    public int click(float mx, float my) {
        if (!newMenuOpen) return -1;
        float dx = btnX;
        float dy = btnY + 36;
        if (mx < dx || mx > dx + NEW_DROP_W || my < dy || my > dy + 2 * NEW_ITEM_H) return -1;
        int idx = (int) ((my - dy) / NEW_ITEM_H);
        if (idx < 0 || idx > 1) return -1;
        newMenuOpen = false;
        return idx;
    }
}
