package markershape.shape.render;

import learngl.Shader;
import markershape.shape.ShapeData;
import org.lwjgl.BufferUtils;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Renders a 3D arrow showing the shape's fixed "front" direction.
 * The arrow points from the shape center along that direction.
 */
public class FrontArrowRenderer implements Renderer {
    private int vao = -1, vbo = -1;
    private boolean visible;
    private float cx, cy, cz;
    private Vector3f dir = new Vector3f(0, 0, -1);
    private float length = 1f;

    public void setVisible(boolean v) { visible = v; }
    public boolean isVisible() { return visible; }

    public void setArrow(float cx, float cy, float cz, Vector3f direction, float arrowLength) {
        this.cx = cx; this.cy = cy; this.cz = cz;
        this.dir = new Vector3f(direction);
        if (dir.lengthSquared() > 1e-6f) dir.normalize();
        this.length = arrowLength > 0 ? arrowLength : 1f;
    }

    @Override
    public void render(Shader shader, ShapeData data) {
        if (!visible) return;
        if (vao < 0) build();
        shader.bind();

        float ex = cx + dir.x * length;
        float ey = cy + dir.y * length;
        float ez = cz + dir.z * length;

        Vector3f perp = new Vector3f(dir).cross(new Vector3f(0, 1, 0));
        if (perp.lengthSquared() < 1e-6f) perp.set(dir).cross(new Vector3f(1, 0, 0));
        perp.normalize().mul(length * 0.18f);
        Vector3f up = new Vector3f(perp).cross(dir).normalize().mul(length * 0.18f);

        // 10 segments: shaft (2 verts) + arrowhead (8 verts)
        FloatBuffer buf = BufferUtils.createFloatBuffer(10 * 6);
        buf.put(cx).put(cy).put(cz).put(1f).put(0.8f).put(0.2f);
        buf.put(ex).put(ey).put(ez).put(1f).put(0.8f).put(0.2f);

        float px1 = ex + perp.x, py1 = ey + perp.y, pz1 = ez + perp.z;
        float px2 = ex - perp.x, py2 = ey - perp.y, pz2 = ez - perp.z;
        float ux1 = ex + up.x, uy1 = ey + up.y, uz1 = ez + up.z;
        float ux2 = ex - up.x, uy2 = ey - up.y, uz2 = ez - up.z;

        buf.put(px1).put(py1).put(pz1).put(1f).put(0.8f).put(0.2f);
        buf.put(ex).put(ey).put(ez).put(1f).put(0.8f).put(0.2f);
        buf.put(px2).put(py2).put(pz2).put(1f).put(0.8f).put(0.2f);
        buf.put(ex).put(ey).put(ez).put(1f).put(0.8f).put(0.2f);
        buf.put(ux1).put(uy1).put(uz1).put(1f).put(0.8f).put(0.2f);
        buf.put(ex).put(ey).put(ez).put(1f).put(0.8f).put(0.2f);
        buf.put(ux2).put(uy2).put(uz2).put(1f).put(0.8f).put(0.2f);
        buf.put(ex).put(ey).put(ez).put(1f).put(0.8f).put(0.2f);
        buf.flip();

        glDepthMask(false);
        glLineWidth(3f);
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINES, 0, 10);
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
        glBufferData(GL_ARRAY_BUFFER, 10L * 2 * 6 * 4, GL_DYNAMIC_DRAW);
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