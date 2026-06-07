package markershape.shape.render.point;

import learngl.Shader;
import markershape.shape.ShapeData;
import markershape.shape.render.Renderer;
import org.lwjgl.BufferUtils;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class CrosshairRenderer implements Renderer {
    private int vao = -1, vbo = -1;
    private boolean visible;
    private Vector3f pos;
    private boolean showAxisX = true, showAxisY = true, showAxisZ = true;

    public void setVisible(boolean v) { visible = v; }
    public void setPosition(Vector3f p) { pos = p; }
    public void setShowAxisX(boolean v) { showAxisX = v; }
    public void setShowAxisY(boolean v) { showAxisY = v; }
    public void setShowAxisZ(boolean v) { showAxisZ = v; }

    @Override
    public void render(Shader shader, ShapeData data) {
        if (!visible || pos == null || (!showAxisX && !showAxisY && !showAxisZ)) return;
        if (vao < 0) build();

        float x = pos.x, y = pos.y, z = pos.z;
        float len = 100f;
        int segs = 0;
        if (showAxisX) segs += 2;
        if (showAxisY) segs += 2;
        if (showAxisZ) segs += 2;
        FloatBuffer buf = BufferUtils.createFloatBuffer(segs * 6);
        if (showAxisX) {
            buf.put(x - len); buf.put(y); buf.put(z); buf.put(1f); buf.put(0f); buf.put(0f);
            buf.put(x + len); buf.put(y); buf.put(z); buf.put(1f); buf.put(0f); buf.put(0f);
        }
        if (showAxisY) {
            buf.put(x); buf.put(y - len); buf.put(z); buf.put(0f); buf.put(1f); buf.put(0f);
            buf.put(x); buf.put(y + len); buf.put(z); buf.put(0f); buf.put(1f); buf.put(0f);
        }
        if (showAxisZ) {
            buf.put(x); buf.put(y); buf.put(z - len); buf.put(0f); buf.put(0f); buf.put(1f);
            buf.put(x); buf.put(y); buf.put(z + len); buf.put(0f); buf.put(0f); buf.put(1f);
        }
        buf.flip();

        glDepthMask(false);
        glLineWidth(2f);
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINES, 0, segs);
        glLineWidth(1f);
        glDepthMask(true);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void build() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, 6L * 2 * 6 * 4, GL_DYNAMIC_DRAW);
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
