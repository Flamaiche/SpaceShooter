package markershape.ui;

import learngl.Shader;
import markershape.io.ShapeLoader;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

/**
 * Main menu UI for the MarkerShape application.
 * Lists available shape files and provides Parametres and Quitter buttons.
 */
public class MenuUI {
    private int width, height;
    private Shader shader, textShader;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    private String[] shapes;
    private static final int ITEM_H = 40;
    private static final int START_Y = 120;

    /**
     * Constructs the main menu UI.
     *
     * @param w initial viewport width
     * @param h initial viewport height
     */
    public MenuUI(int w, int h) {
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
        refresh();
        setSize(w, h);
    }

    /** Refreshes the list of shapes from disk. */
    public void refresh() {
        shapes = ShapeLoader.listShapes();
        if (shapes == null) shapes = new String[0];
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

    /** Renders the main menu (shape list and buttons). */
    public void render() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        shader.bind();
        shader.setUniformMat4f("projection", ortho);

        drawQuad(width / 2f - 200, START_Y - 20, 400, shapes.length * ITEM_H + 40,
                 0.08f, 0.08f, 0.12f, 0.9f);

        for (int i = 0; i < shapes.length; i++) {
            float y = START_Y + i * ITEM_H;
            float shade = 0.15f + (i % 2 == 0 ? 0.05f : 0f);
            drawQuad(width / 2f - 180, y, 360, ITEM_H - 4, shade, shade, shade + 0.05f, 0.8f);
        }

        drawQuad(width / 2f - 180, START_Y + shapes.length * ITEM_H + 12, 170, 36,
                 0.2f, 0.2f, 0.3f, 0.8f);
        drawQuad(width / 2f + 10, START_Y + shapes.length * ITEM_H + 12, 170, 36,
                 0.3f, 0.1f, 0.1f, 0.8f);

        shader.unbind();

        TextRenderer.drawText(textShader, "MarkerShape", width / 2f - 70, 50, 1.5f, 1f, 1f, 1f);
        for (int i = 0; i < shapes.length; i++) {
            String name = shapes[i].replace(".json", "");
            TextRenderer.drawText(textShader, name, width / 2f - 170, START_Y + i * ITEM_H + 10, 1f, 0.8f, 0.8f, 1f);
        }
        TextRenderer.drawText(textShader, "Parametres", width / 2f - 140, START_Y + shapes.length * ITEM_H + 20, 1f, 1f, 1f, 1f);
        TextRenderer.drawText(textShader, "Quitter", width / 2f + 40, START_Y + shapes.length * ITEM_H + 20, 1f, 1f, 1f, 1f);
    }

    /**
     * Returns the name of the shape clicked at the given position, or null.
     *
     * @param mx mouse X coordinate
     * @param my mouse Y coordinate
     * @return the clicked shape file name, or null
     */
    public String clickShape(float mx, float my) {
        for (int i = 0; i < shapes.length; i++) {
            float y = START_Y + i * ITEM_H;
            if (mx > width / 2f - 180 && mx < width / 2f + 180
                && my > y && my < y + ITEM_H - 4)
                return shapes[i];
        }
        return null;
    }

    /**
     * Checks whether the given coordinates hit the Parametres button.
     *
     * @param mx mouse X coordinate
     * @param my mouse Y coordinate
     * @return true if the Parametres button was clicked
     */
    public boolean isParametresClicked(float mx, float my) {
        float bx = width / 2f - 180;
        float by = START_Y + shapes.length * ITEM_H + 12;
        return mx > bx && mx < bx + 170 && my > by && my < by + 36;
    }

    /**
     * Checks whether the given coordinates hit the Quitter button.
     *
     * @param mx mouse X coordinate
     * @param my mouse Y coordinate
     * @return true if the Quitter button was clicked
     */
    public boolean isQuitterClicked(float mx, float my) {
        float bx = width / 2f + 10;
        float by = START_Y + shapes.length * ITEM_H + 12;
        return mx > bx && mx < bx + 170 && my > by && my < by + 36;
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
