package markershape.shape.render.edge;

import learngl.Shader;
import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.shape.render.Renderer;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class EdgeBatchRenderer implements Renderer {
    private int vao = -1, vbo = -1;
    private int count;
    private float lineWidth = 3f;

    public void setLineWidth(float w) { lineWidth = w; }

    @Override
    public void render(Shader shader, ShapeData data) {
        if (data == null || data.edges.isEmpty()) return;
        if (vao < 0) build(data);

        count = data.edges.size();
        FloatBuffer buf = BufferUtils.createFloatBuffer(count * 2 * 6);
        for (Edge e : data.edges.values()) {
            Vertex va = data.vertices.get(e.a);
            Vertex vb = data.vertices.get(e.b);
            if (va == null || vb == null) continue;
            float r = (va.r + vb.r) * 0.5f, g = (va.g + vb.g) * 0.5f, b = (va.b + vb.b) * 0.5f;
            buf.put(va.x); buf.put(va.y); buf.put(va.z);
            buf.put(r); buf.put(g); buf.put(b);
            buf.put(vb.x); buf.put(vb.y); buf.put(vb.z);
            buf.put(r); buf.put(g); buf.put(b);
        }
        buf.flip();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDepthMask(false);
        glLineWidth(lineWidth);
        glDrawArrays(GL_LINES, 0, count * 2);
        glLineWidth(1f);
        glDepthMask(true);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void build(ShapeData data) {
        if (data.edges.isEmpty()) return;
        count = data.edges.size();
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) count * 2 * 6 * 4, GL_DYNAMIC_DRAW);
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
