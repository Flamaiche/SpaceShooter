package markershape.shape.render.point;

import learngl.Shader;
import markershape.shape.ShapeData;
import markershape.shape.render.Renderer;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Semi-transparent placement ghost rendered as a point at real point size,
 * shown before the user validates a vertex placement click.
 */
public class GhostPointRenderer implements Renderer {
    private int vao = -1, vbo = -1;
    private boolean visible;
    private float px, py, pz;
    private float pointSize = 5f;

    public void setVisible(boolean v) { visible = v; }
    public void setPosition(float x, float y, float z) { px = x; py = y; pz = z; }
    public void setPointSize(float s) { pointSize = s; }

    @Override
    public void render(Shader shader, ShapeData data) {
        if (!visible) return;
        if (vao < 0) ensureBuilt();
        shader.bind();

        FloatBuffer buf = BufferUtils.createFloatBuffer(6);
        buf.put(px).put(py).put(pz);
        buf.put(1f).put(0.85f).put(0.3f);
        buf.flip();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);
        shader.setUniform1f("uAlpha", 0.55f);
        glPointSize(pointSize);
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_POINTS, 0, 1);
        glPointSize(1f);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void ensureBuilt() {
        if (vao >= 0) return;
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, 6L * 4, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 3 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    @Override
    public void cleanup() {
        if (vao >= 0) { glDeleteVertexArrays(vao); vao = -1; }
        if (vbo >= 0) { glDeleteBuffers(vbo); vbo = -1; }
    }
}