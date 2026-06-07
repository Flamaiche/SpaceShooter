package markershape.editor.ui.menu;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.editor.ui.control.Button;
import markershape.shape.ShapeLoader;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class MenuUI {
    private int width, height;
    private Shader shader, textShader;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    private String[] shapes;
    private static final int ITEM_H = 40;
    private static final int START_Y = 120;
    private Button paramBtn, quitBtn;
    private Runnable onQuit;

    public MenuUI(int w, int h, Runnable onQuit) {
        this.onQuit = onQuit;
        shader = new Shader("shaders/markershape/ui_Vertex.glsl",
                            "shaders/markershape/ui_Fragment.glsl");
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 6 * 4, 2 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        refresh();
        setSize(w, h);
    }

    public void refresh() {
        shapes = ShapeLoader.listShapes();
        if (shapes == null) shapes = new String[0];
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

        drawQuad(width / 2f - 200, START_Y - 20, 400, shapes.length * ITEM_H + 40,
                 0.08f, 0.08f, 0.12f, 0.9f);

        for (int i = 0; i < shapes.length; i++) {
            float y = START_Y + i * ITEM_H;
            float shade = 0.15f + (i % 2 == 0 ? 0.05f : 0f);
            drawQuad(width / 2f - 180, y, 360, ITEM_H - 4, shade, shade, shade + 0.05f, 0.8f);
        }

        shader.unbind();

        float bx = width / 2f - 180;
        float by = START_Y + shapes.length * ITEM_H + 12;
        if (paramBtn == null || paramBtn.x != bx || paramBtn.y != by) {
            paramBtn = new Button("Parametres", bx, by, 170, 36, () ->
                System.out.println("[MarkerShape] Parametres (not implemented yet)"));
            paramBtn.textScale = 2.5f;
            paramBtn.bgR = 0.2f; paramBtn.bgG = 0.2f; paramBtn.bgB = 0.3f;
            quitBtn = new Button("Quitter", width / 2f + 10, by, 170, 36, onQuit);
            quitBtn.textScale = 2.5f;
            quitBtn.bgR = 0.3f; quitBtn.bgG = 0.1f; quitBtn.bgB = 0.1f;
        }

        paramBtn.render(shader, textShader, ortho, buf, vao, vbo);
        quitBtn.render(shader, textShader, ortho, buf, vao, vbo);

        Text.drawText(textShader, "MarkerShape", width / 2f - 70, 50, 3.5f, 1f, 1f, 1f);
        for (int i = 0; i < shapes.length; i++) {
            String name = shapes[i].replace(".json", "");
            Text.drawText(textShader, name, width / 2f - 170, START_Y + i * ITEM_H + 10, 2.5f, 0.8f, 0.8f, 1f);
        }
    }

    public String clickShape(float mx, float my) {
        for (int i = 0; i < shapes.length; i++) {
            float y = START_Y + i * ITEM_H;
            if (mx > width / 2f - 180 && mx < width / 2f + 180
                && my > y && my < y + ITEM_H - 4)
                return shapes[i];
        }
        return null;
    }

    public boolean isParametresClicked(float mx, float my) {
        if (paramBtn != null && paramBtn.isClicked(mx, my)) { paramBtn.click(); return true; }
        return false;
    }

    public boolean isQuitterClicked(float mx, float my) {
        if (quitBtn != null && quitBtn.isClicked(mx, my)) { quitBtn.click(); return true; }
        return false;
    }

    public void cleanup() {
        shader.cleanup();
        textShader.cleanup();
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
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
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
}
