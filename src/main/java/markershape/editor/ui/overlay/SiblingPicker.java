package markershape.editor.ui.overlay;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.shape.*;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class SiblingPicker {
    private boolean visible;
    private int[] ids;
    private Vertex[] vertices;
    private float px, py, ph;
    private static final float PW = 220;
    private static final float ROW_H = 26;
    private Consumer<Integer> callback;

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

    public void render(Shader uiShader, Shader textShader, Matrix4f ortho,
                       FloatBuffer buf, int vao, int vbo) {
        if (!visible || vertices == null) return;

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        uiShader.bind();
        uiShader.setUniformMat4f("projection", ortho);

        if (!markershape.editor.ui.menu.BlurBackground.transparentUI) {
            buf.clear();
            buf.put(new float[]{
                px, py, 0.1f, 0.1f, 0.15f, 0.95f,
                px+PW, py, 0.1f, 0.1f, 0.15f, 0.95f,
                px+PW, py+ph, 0.1f, 0.1f, 0.15f, 0.95f,
                px, py, 0.1f, 0.1f, 0.15f, 0.95f,
                px+PW, py+ph, 0.1f, 0.1f, 0.15f, 0.95f,
                px, py+ph, 0.1f, 0.1f, 0.15f, 0.95f,
            }).flip();
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }

        // row backgrounds
        if (!markershape.editor.ui.menu.BlurBackground.transparentUI) {
            for (int i = 0; i < ids.length; i++) {
                float ry = py + 30 + i * ROW_H;
                float accent = (i % 2 == 0) ? 0.18f : 0.14f;
                buf.clear();
                buf.put(new float[]{
                    px+2, ry, accent, accent, accent+0.03f, 1f,
                    px+PW-2, ry, accent, accent, accent+0.03f, 1f,
                    px+PW-2, ry+ROW_H, accent, accent, accent+0.03f, 1f,
                    px+2, ry, accent, accent, accent+0.03f, 1f,
                    px+PW-2, ry+ROW_H, accent, accent, accent+0.03f, 1f,
                    px+2, ry+ROW_H, accent, accent, accent+0.03f, 1f,
                }).flip();
                glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
                glDrawArrays(GL_TRIANGLES, 0, 6);
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        uiShader.unbind();

        Text.drawText(textShader, "Select vertex:", px + 8, py + 8, 1.5f, 0.8f, 0.8f, 1f);

        for (int i = 0; i < vertices.length; i++) {
            Vertex v = vertices[i];
            if (v == null) continue;
            float ry = py + 30 + i * ROW_H + 4;
            float sw = 16;
            // color swatch using quad
            uiShader.bind();
            uiShader.setUniformMat4f("projection", ortho);
            buf.clear();
            buf.put(new float[]{
                px+8, ry, v.r, v.g, v.b, 1f,
                px+8+sw, ry, v.r, v.g, v.b, 1f,
                px+8+sw, ry+sw, v.r, v.g, v.b, 1f,
                px+8, ry, v.r, v.g, v.b, 1f,
                px+8+sw, ry+sw, v.r, v.g, v.b, 1f,
                px+8, ry+sw, v.r, v.g, v.b, 1f,
            }).flip();
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindVertexArray(0);
            uiShader.unbind();

            Text.drawText(textShader, "#" + v.id + " (" + String.format("%.2f,%.2f,%.2f", v.r, v.g, v.b) + ")",
                px + 30, ry, 1.5f, 0.8f, 0.8f, 0.8f);
        }
    }
}
