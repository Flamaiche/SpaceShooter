package markershape.shape.render;

import learngl.Shader;
import markershape.shape.ShapeData;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Up-cross ("+") marker showing the orbit pivot: a fixed world point around
 * which the camera orbits while the right mouse button drags. Only visible
 * during an orbit gesture; hidden when the gesture ends.
 */
public class OrbitPivotRenderer implements Renderer {
    private int vao = -1, vbo = -1;
    private boolean visible;
    private float px, py, pz;
    private float halfLen = 0.5f;
    private final float[] right = {1, 0, 0};
    private final float[] up = {0, 1, 0};

    public void setVisible(boolean v) { visible = v; }
    public boolean isVisible() { return visible; }
    public void setPosition(float x, float y, float z) { px = x; py = y; pz = z; }
    public void setHalfLength(float h) { halfLen = Math.max(0.1f, h); }

    /** Billboard orientation: the "+" is drawn in the camera's right/up plane. */
    public void setAxes(float rx, float ry, float rz, float ux, float uy, float uz) {
        right[0] = rx; right[1] = ry; right[2] = rz;
        up[0] = ux; up[1] = uy; up[2] = uz;
    }

    @Override
    public void render(Shader shader, ShapeData data) {
        if (!visible) return;
        if (vao < 0) ensureBuilt();
        shader.bind();

        // Two crossing segments centered on the pivot ("+"), billboard-aligned.
        FloatBuffer buf = BufferUtils.createFloatBuffer(4 * 6);
        buf.put(px - right[0] * halfLen).put(py - right[1] * halfLen).put(pz - right[2] * halfLen)
            .put(1f).put(0.9f).put(0.3f);
        buf.put(px + right[0] * halfLen).put(py + right[1] * halfLen).put(pz + right[2] * halfLen)
            .put(1f).put(0.9f).put(0.3f);
        buf.put(px - up[0] * halfLen).put(py - up[1] * halfLen).put(pz - up[2] * halfLen)
            .put(1f).put(0.9f).put(0.3f);
        buf.put(px + up[0] * halfLen).put(py + up[1] * halfLen).put(pz + up[2] * halfLen)
            .put(1f).put(0.9f).put(0.3f);
        buf.flip();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);
        shader.setUniform1f("uAlpha", 1f);
        glLineWidth(3f);
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINES, 0, 4);
        glLineWidth(1f);
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
        glBufferData(GL_ARRAY_BUFFER, 4L * 2 * 6 * 4, GL_DYNAMIC_DRAW);
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