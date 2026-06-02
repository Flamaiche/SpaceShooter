package markershape;

import learngl.Shader;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

public class EditorUI {
    private int width, height;
    private Shader shader;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(4 * 6);

    public EditorUI(int w, int h) {
        width = w;
        height = h;
        shader = new Shader("shaders/markershape/ui_Vertex.glsl",
                            "shaders/markershape/ui_Fragment.glsl");
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

    public void render() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        shader.setUniformMat4f("projection", ortho);

        drawTopBar();

        shader.unbind();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void drawTopBar() {
        drawQuad(0, 0, width, 36, 0.1f, 0.1f, 0.15f, 0.8f);
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

    public void cleanup() {
        shader.cleanup();
        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vao);
    }

    public boolean isSaveClicked(float mx, float my) {
        return my < 36 && mx > 150 && mx < 200;
    }

    public boolean isAddClicked(float mx, float my) {
        return my < 36 && mx > 210 && mx < 240;
    }
}
