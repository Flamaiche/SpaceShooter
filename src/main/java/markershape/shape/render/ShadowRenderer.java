package markershape.shape.render;

import learngl.Shader;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class ShadowRenderer {
    private Shader uiShader;
    private int pointVao = -1, pointVbo = -1;
    private int edgeVao = -1, edgeVbo = -1;
    private int batchVao = -1, batchVbo = -1;
    private final Matrix4f ortho = new Matrix4f();

    private void ensureShader() {
        if (uiShader == null) {
            uiShader = new Shader("shaders/markershape/ui_Vertex.glsl",
                                  "shaders/markershape/ui_Fragment.glsl");
        }
    }

    public void setScreenSize(int w, int h) {
        ortho.setOrtho2D(0, w, h, 0);
    }

    public void drawPoint(float sx, float sy, float r, float g, float b, float a, float size) {
        if (pointVao < 0) buildPoint();
        ensureShader();

        FloatBuffer buf = BufferUtils.createFloatBuffer(6);
        buf.put(sx).put(sy).put(r).put(g).put(b).put(a);
        buf.flip();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);

        uiShader.bind();
        uiShader.setUniformMat4f("projection", ortho);
        glBindVertexArray(pointVao);
        glBindBuffer(GL_ARRAY_BUFFER, pointVbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glPointSize(size);
        glDrawArrays(GL_POINTS, 0, 1);
        glPointSize(1f);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        uiShader.unbind();

        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    public void drawEdge(float ax, float ay, float bx, float by,
                         float r, float g, float b, float a, float width) {
        if (edgeVao < 0) buildEdge();
        ensureShader();

        FloatBuffer buf = BufferUtils.createFloatBuffer(12);
        buf.put(ax).put(ay).put(r).put(g).put(b).put(a);
        buf.put(bx).put(by).put(r).put(g).put(b).put(a);
        buf.flip();

        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);

        uiShader.bind();
        uiShader.setUniformMat4f("projection", ortho);
        glBindVertexArray(edgeVao);
        glBindBuffer(GL_ARRAY_BUFFER, edgeVbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glLineWidth(width);
        glDrawArrays(GL_LINES, 0, 2);
        glLineWidth(1f);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        uiShader.unbind();

        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
    }

    public void drawEdgeBatch(FloatBuffer buf, int vertCount, float r, float g, float b, float a, float width) {
        if (vertCount < 2) return;
        if (batchVao < 0) buildBatch();
        ensureShader();

        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);

        uiShader.bind();
        uiShader.setUniformMat4f("projection", ortho);
        glBindVertexArray(batchVao);
        glBindBuffer(GL_ARRAY_BUFFER, batchVbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glLineWidth(width);
        glDrawArrays(GL_LINES, 0, vertCount);
        glLineWidth(1f);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        uiShader.unbind();

        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);
    }

    private void buildPoint() {
        pointVao = glGenVertexArrays();
        pointVbo = glGenBuffers();
        glBindVertexArray(pointVao);
        glBindBuffer(GL_ARRAY_BUFFER, pointVbo);
        glBufferData(GL_ARRAY_BUFFER, 6 * 4, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 6 * 4, 2 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void buildEdge() {
        edgeVao = glGenVertexArrays();
        edgeVbo = glGenBuffers();
        glBindVertexArray(edgeVao);
        glBindBuffer(GL_ARRAY_BUFFER, edgeVbo);
        glBufferData(GL_ARRAY_BUFFER, 12 * 4, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 6 * 4, 2 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void buildBatch() {
        batchVao = glGenVertexArrays();
        batchVbo = glGenBuffers();
        glBindVertexArray(batchVao);
        glBindBuffer(GL_ARRAY_BUFFER, batchVbo);
        glBufferData(GL_ARRAY_BUFFER, 512 * 6 * 4, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 6 * 4, 2 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        if (uiShader != null) { uiShader.cleanup(); uiShader = null; }
        if (pointVao >= 0) { glDeleteVertexArrays(pointVao); pointVao = -1; }
        if (pointVbo >= 0) { glDeleteBuffers(pointVbo); pointVbo = -1; }
        if (edgeVao >= 0) { glDeleteVertexArrays(edgeVao); edgeVao = -1; }
        if (edgeVbo >= 0) { glDeleteBuffers(edgeVbo); edgeVbo = -1; }
        if (batchVao >= 0) { glDeleteVertexArrays(batchVao); batchVao = -1; }
        if (batchVbo >= 0) { glDeleteBuffers(batchVbo); batchVbo = -1; }
    }
}
