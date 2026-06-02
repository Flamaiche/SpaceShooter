package markershape.ui;

import learngl.Shader;
import learngl.text.Text;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

public class EditorUI {
    private int width, height;
    private Shader shader, textShader;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    private static final int BAR_H = 36;
    private static final int BTN_W = 130;

    public EditorUI(int w, int h) {
        width = w;
        height = h;
        shader = new Shader("shaders/markershape/ui_Vertex.glsl",
                            "shaders/markershape/ui_Fragment.glsl");
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 6 * 4, 0);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 6 * 4, 2 * 4);
        GL20.glEnableVertexAttribArray(1);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        setSize(w, h);
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        ortho.setOrtho2D(0, width, height, 0);
    }

    public void render(String currentFile) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        shader.setUniformMat4f("projection", ortho);

        drawQuad(0, 0, width, BAR_H, 0.1f, 0.1f, 0.15f, 0.8f);
        drawQuad(width - BTN_W * 2 - 10, 0, BTN_W, BAR_H, 0.2f, 0.2f, 0.3f, 0.8f);
        drawQuad(width - BTN_W - 5, 0, BTN_W, BAR_H, 0.3f, 0.1f, 0.1f, 0.8f);

        shader.unbind();

        textShader.bind();
        textShader.setUniformMat4f("projection", ortho);

        String label = currentFile != null ? currentFile.replace(".json", "") : "[no shape]";
        Text.drawText(textShader, "MarkerShape - " + label, 10, 10, 1f, 1f, 1f, 1f);
        Text.drawText(textShader, "Sauvegarder", width - BTN_W * 2 + 5, 10, 1f, 1f, 1f, 1f);
        Text.drawText(textShader, "Quitter", width - BTN_W + 20, 10, 1f, 1f, 1f, 1f);

        textShader.unbind();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public boolean isSaveClicked(float mx, float my) {
        return my < BAR_H && mx > width - BTN_W * 2 - 10 && mx < width - BTN_W - 10;
    }

    public boolean isQuitClicked(float mx, float my) {
        return my < BAR_H && mx > width - BTN_W - 5 && mx < width;
    }

    public void cleanup() {
        shader.cleanup();
        textShader.cleanup();
        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vao);
    }

    private void drawQuad(float x, float y, float w, float h,
                          float r, float g, float b, float a) {
        float[] verts = {
            x,   y,   r, g, b, a,
            x+w, y,   r, g, b, a,
            x+w, y+h, r, g, b, a,
            x,   y,   r, g, b, a,
            x+w, y+h, r, g, b, a,
            x,   y+h, r, g, b, a
        };
        buf.clear();
        buf.put(verts).flip();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_DYNAMIC_DRAW);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }
}
