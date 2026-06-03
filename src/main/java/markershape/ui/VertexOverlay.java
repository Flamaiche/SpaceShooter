package markershape.ui;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.model.Vertex;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class VertexOverlay {
    private boolean visible;
    private Vertex vertex;
    private int edgeCount;
    private float px, py;
    private static final float PW = 280, PH = 200;
    private final Button closeBtn;

    public VertexOverlay() {
        px = 100; py = 100;
        closeBtn = new Button("X", px + PW - 28, py + 4, 24, 24, null);
        closeBtn.showBackground = false;
        closeBtn.textScale = 1.5f;
        closeBtn.textR = 1f; closeBtn.textG = 0.3f; closeBtn.textB = 0.3f;
    }

    public void show(Vertex v, int edges) {
        vertex = v;
        edgeCount = edges;
        visible = true;
    }

    public void hide() {
        visible = false;
        vertex = null;
    }

    public boolean isVisible() { return visible; }

    public boolean isCloseClicked(float mx, float my) {
        return visible && closeBtn.isClicked(mx, my);
    }

    public Vertex getVertex() { return vertex; }

    public void render(Shader uiShader, Shader textShader, Matrix4f ortho,
                       FloatBuffer buf, int vao, int vbo) {
        if (!visible || vertex == null) return;

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        uiShader.bind();
        uiShader.setUniformMat4f("projection", ortho);

        buf.clear();
        buf.put(new float[]{
            px,    py,    0.15f, 0.15f, 0.22f, 0.95f,
            px+PW, py,    0.15f, 0.15f, 0.22f, 0.95f,
            px+PW, py+PH, 0.15f, 0.15f, 0.22f, 0.95f,
            px,    py,    0.15f, 0.15f, 0.22f, 0.95f,
            px+PW, py+PH, 0.15f, 0.15f, 0.22f, 0.95f,
            px,    py+PH, 0.15f, 0.15f, 0.22f, 0.95f,
        }).flip();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        uiShader.unbind();

        Text.drawText(textShader, "Vertex #" + vertex.id, px + 12, py + 10, 1.5f, 1f, 1f, 1f);

        String xs = String.format("%.3f", vertex.x);
        String ys = String.format("%.3f", vertex.y);
        String zs = String.format("%.3f", vertex.z);
        Text.drawText(textShader, "X: " + xs, px + 12, py + 42, 1.3f, 0.8f, 0.8f, 1f);
        Text.drawText(textShader, "Y: " + ys, px + 12, py + 66, 1.3f, 0.8f, 0.8f, 1f);
        Text.drawText(textShader, "Z: " + zs, px + 12, py + 90, 1.3f, 0.8f, 0.8f, 1f);

        String cs = String.format("%.2f %.2f %.2f", vertex.r, vertex.g, vertex.b);
        Text.drawText(textShader, "Color: " + cs, px + 12, py + 120, 1.3f, 0.8f, 0.8f, 0.8f);
        Text.drawText(textShader, "Edges: " + edgeCount, px + 12, py + 144, 1.3f, 0.8f, 0.8f, 0.8f);

        closeBtn.render(uiShader, textShader, ortho, buf, vao, vbo);
    }

    public void setPosition(float x, float y) {
        px = x;
        py = y;
        closeBtn.x = px + PW - 28;
        closeBtn.y = py + 4;
    }
}
