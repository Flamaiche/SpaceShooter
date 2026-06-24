package markershape.shape.render.point;

import learngl.Shader;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.shape.render.Renderer;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class PointRenderer implements Renderer {
    private int vao = -1, vbo = -1;
    private int count;
    private float pointSize = 5f;

    public void setPointSize(float s) { pointSize = s; }
    public float getPointSize() { return pointSize; }

    @Override
    public void render(Shader shader, ShapeData data) {
        if (data == null || data.vertices.isEmpty()) return;
        if (vao < 0) build(data);

        count = data.vertices.size();
        FloatBuffer buf = BufferUtils.createFloatBuffer(count * 6);
        for (Vertex v : data.vertices.values()) {
            buf.put(v.x); buf.put(v.y); buf.put(v.z);
            buf.put(v.r); buf.put(v.g); buf.put(v.b);
        }
        buf.flip();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);
        shader.setUniform1f("uAlpha", 1f);
        glPointSize(pointSize);
        glDrawArrays(GL_POINTS, 0, count);
        glPointSize(1f);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void build(ShapeData data) {
        if (data.vertices.isEmpty()) return;
        count = data.vertices.size();
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) count * 6 * 4, GL_DYNAMIC_DRAW);
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
        count = 0;
    }
}
