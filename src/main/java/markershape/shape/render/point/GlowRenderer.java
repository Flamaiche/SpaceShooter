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

public class GlowRenderer implements Renderer {
    private int vao = -1, vbo = -1;
    private int hoveredVertexId = -1;
    private int prevHovered = -1;

    public void setHoveredVertex(int id) { hoveredVertexId = id; }

    @Override
    public void render(Shader shader, ShapeData data) {
        if (hoveredVertexId < 0 || data == null || !data.vertices.containsKey(hoveredVertexId)) return;
        if (vao < 0) build();

        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindVertexArray(vao);
        shader.setUniform1f("uAlpha", 1f);

        glPointSize(11f);
        updateVbo(data, 1f, 1f, 1f);
        glDrawArrays(GL_POINTS, 0, 1);

        glPointSize(7f);
        updateVbo(data, 0f, 0f, 0f);
        glDrawArrays(GL_POINTS, 0, 1);

        glPointSize(1f);
        glDisable(GL_BLEND);
        glDepthMask(true);
        glBindVertexArray(0);
    }

    private void updateVbo(ShapeData data, float r, float g, float b) {
        Vertex v = data.vertices.get(hoveredVertexId);
        if (v == null) return;
        FloatBuffer buf = BufferUtils.createFloatBuffer(6);
        buf.put(v.x); buf.put(v.y); buf.put(v.z);
        buf.put(r); buf.put(g); buf.put(b);
        buf.flip();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void build() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, 6 * 4, GL_DYNAMIC_DRAW);
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
