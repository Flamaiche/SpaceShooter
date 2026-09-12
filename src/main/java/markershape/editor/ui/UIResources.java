package markershape.editor.ui;

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

public class UIResources {
    private final Shader uiShader;
    private final Shader textShader;
    private final int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    public UIResources() {
        uiShader = new Shader("shaders/markershape/ui_Vertex.glsl",
                              "shaders/markershape/ui_Fragment.glsl");
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 6 * 4, 2 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void setSize(int w, int h) { ortho.setOrtho2D(0, w, h, 0); }

    public void begin2D() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void drawQuad(float x, float y, float w, float h,
                         float r, float g, float b, float a) {
        begin2D();
        uiShader.bind();
        uiShader.setUniformMat4f("projection", ortho);
        buf.clear();
        buf.put(new float[]{
            x, y, r, g, b, a, x+w, y, r, g, b, a, x+w, y+h, r, g, b, a,
            x, y, r, g, b, a, x+w, y+h, r, g, b, a, x, y+h, r, g, b, a,
        }).flip();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        uiShader.unbind();
    }

    public void drawLine(float x1, float y1, float x2, float y2,
                         float r, float g, float b, float a) {
        begin2D();
        uiShader.bind();
        uiShader.setUniformMat4f("projection", ortho);
        buf.clear();
        buf.put(new float[]{ x1, y1, r, g, b, a, x2, y2, r, g, b, a }).flip();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINES, 0, 2);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        uiShader.unbind();
    }

    public void drawText(String text, float x, float y, float scale,
                         float r, float g, float b) {
        Text.drawText(textShader, text, x, y, scale, r, g, b);
    }

    public void drawTextCentered(String text, float x, float y, float w, float h,
                                 float scale, float r, float g, float b) {
        float[] ext = Text.getTextExtent(text, scale);
        drawText(text, x + (w - ext[0]) / 2f, y + (h - ext[1]) / 2f, scale, r, g, b);
    }

    public float[] menuColor() {
        return new float[]{ BlurBackground.menuR, BlurBackground.menuG, BlurBackground.menuB };
    }

    public float[] textColor() {
        ConfigParametres cfg = ConfigParametres.get();
        return new float[]{ cfg.getFloat("textR") / 255f, cfg.getFloat("textG") / 255f, cfg.getFloat("textB") / 255f };
    }

    public Shader uiShader() { return uiShader; }
    public Shader textShader() { return textShader; }
    public Matrix4f ortho() { return ortho; }
    public FloatBuffer buf() { return buf; }
    public int vao() { return vao; }
    public int vbo() { return vbo; }

    public void cleanup() {
        uiShader.cleanup();
        textShader.cleanup();
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}
