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

/**
 * 2D overlay UI for the shape editor.
 * Renders a top bar with the shape name, a save button, and a quit button.
 */
public class EditorUI {
    private int width, height;
    private Shader shader, textShader;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    private static final int BAR_H = 36;
    private static final int BTN_W = 130;

    /**
     * Constructs the editor UI overlay.
     *
     * @param w initial viewport width
     * @param h initial viewport height
     */
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

    /**
     * Updates the UI dimensions and orthographic projection.
     *
     * @param w the new viewport width
     * @param h the new viewport height
     */
    public void setSize(int w, int h) {
        width = w;
        height = h;
        ortho.setOrtho2D(0, width, height, 0);
    }

    /**
     * Renders the editor UI overlay (top bar with buttons).
     *
     * @param currentFile the currently open shape file name
     */
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

    /**
     * Checks whether the given coordinates hit the save button.
     *
     * @param mx mouse X coordinate
     * @param my mouse Y coordinate
     * @return true if the save button was clicked
     */
    public boolean isSaveClicked(float mx, float my) {
        return my < BAR_H && mx > width - BTN_W * 2 - 10 && mx < width - BTN_W - 10;
    }

    /**
     * Checks whether the given coordinates hit the quit button.
     *
     * @param mx mouse X coordinate
     * @param my mouse Y coordinate
     * @return true if the quit button was clicked
     */
    public boolean isQuitClicked(float mx, float my) {
        return my < BAR_H && mx > width - BTN_W - 5 && mx < width;
    }

    /** Frees the shader programs and deletes the OpenGL buffers. */
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
