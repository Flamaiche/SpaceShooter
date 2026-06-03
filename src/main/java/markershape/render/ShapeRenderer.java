package markershape.render;

import learngl.LogFile;
import learngl.Shader;
import learngl.shape.Shape;
import learngl.VertexUtils;
import markershape.io.ShapeLoader;
import markershape.model.*;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

/**
 * Converts ShapeData into renderable OpenGL geometry and draws it.
 * Manages a single shape and its associated shader at a time.
 */
public class ShapeRenderer {
    private Shape shape;
    private Shader shader;
    private ShapeData shapeData;
    private final String shaderPath = "shaders/markershape/";
    private final org.joml.Matrix4f identity = new org.joml.Matrix4f();

    private int glowVao = -1, glowVbo = -1;
    private int prevHovered = -1;
    private int hoveredVertexId = -1;
    private int lineVao = -1, lineVbo = -1;
    private int hoveredEdgeId = -1;
    private int pendingEdgeA = -1, pendingEdgeB = -1;

    /**
     * Loads a shape from a JSON file and builds the render geometry.
     *
     * @param filename the shape file to load
     * @return true if the shape was loaded successfully
     */
    public boolean loadShape(String filename) {
        ShapeData data = ShapeLoader.load(filename);
        if (data == null) {
            System.err.println("[ShapeRenderer] failed to load: " + filename);
            return false;
        }
        buildFromData(data);
        return true;
    }

    /**
     * Builds render geometry from the given shape data.
     * Cleans up any previously loaded shape first.
     *
     * @param data the shape data to render
     */
    public void buildFromData(ShapeData data) {
        cleanup();
        this.shapeData = data;
        if (data.vertices.isEmpty()) return;

        String shaderName = data.shader != null ? data.shader : "default";
        try {
            shader = new Shader(shaderPath + shaderName + "_Vertex.glsl",
                                shaderPath + shaderName + "_Fragment.glsl");
        } catch (Exception e) {
            System.err.println("[ShapeRenderer] shader load error: " + e.getMessage());
            return;
        }

        List<Float> verts = new ArrayList<>();
        for (int[] tri : data.faces) {
            for (int idx : tri) {
                Vertex v = data.vertices.get(idx);
                if (v == null) continue;
                verts.add(v.x);
                verts.add(v.y);
                verts.add(v.z);
                verts.add(v.r);
                verts.add(v.g);
                verts.add(v.b);
            }
        }

        if (verts.isEmpty()) return;

        float[] raw = new float[verts.size()];
        for (int i = 0; i < verts.size(); i++) raw[i] = verts.get(i);

        float[] full = VertexUtils.autoAddSlotTexture(raw);
        shape = new Shape(full);
        shape.setShader(shader);

        LogFile.logf("[ShapeRenderer] built: vertices=%d faces=%d triangles=%d",
            data.vertices.size(), data.faces.size(), raw.length / 6);

        buildGlowVbo();
        buildLineVao();
    }

    private void buildGlowVbo() {
        glowVao = glGenVertexArrays();
        glowVbo = glGenBuffers();
        glBindVertexArray(glowVao);
        glBindBuffer(GL_ARRAY_BUFFER, glowVbo);
        glBufferData(GL_ARRAY_BUFFER, 6 * 4, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 3 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void buildLineVao() {
        lineVao = glGenVertexArrays();
        lineVbo = glGenBuffers();
        glBindVertexArray(lineVao);
        glBindBuffer(GL_ARRAY_BUFFER, lineVbo);
        glBufferData(GL_ARRAY_BUFFER, 12 * 4, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 3 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void updateLineVbo(int edgeId) {
        if (shapeData == null) return;
        Edge e = shapeData.edges.get(edgeId);
        if (e == null) return;
        if (e.a == pendingEdgeA && e.b == pendingEdgeB) return;
        pendingEdgeA = e.a;
        pendingEdgeB = e.b;
        Vertex va = shapeData.vertices.get(e.a);
        Vertex vb = shapeData.vertices.get(e.b);
        if (va == null || vb == null) return;
        FloatBuffer buf = BufferUtils.createFloatBuffer(12);
        buf.put(va.x); buf.put(va.y); buf.put(va.z);
        buf.put(1f); buf.put(1f); buf.put(1f);
        buf.put(vb.x); buf.put(vb.y); buf.put(vb.z);
        buf.put(1f); buf.put(1f); buf.put(1f);
        buf.flip();
        glBindBuffer(GL_ARRAY_BUFFER, lineVbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, buf);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void updateGlowVbo(float r, float g, float b) {
        Vertex v = shapeData.vertices.get(hoveredVertexId);
        if (v == null) return;
        FloatBuffer buf = BufferUtils.createFloatBuffer(6);
        buf.put(v.x); buf.put(v.y); buf.put(v.z);
        buf.put(r); buf.put(g); buf.put(b);
        buf.flip();
        glBindBuffer(GL_ARRAY_BUFFER, glowVbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void setHoveredVertex(int id) { hoveredVertexId = id; }
    public void setHoveredEdge(int id) { hoveredEdgeId = id; }

    public ShapeData getShapeData() { return shapeData; }

    /**
     * Renders the current shape with the given view and projection matrices.
     *
     * @param view       the view matrix
     * @param projection the projection matrix
     */
    public void render(org.joml.Matrix4f view, org.joml.Matrix4f projection) {
        if (shape == null || shader == null) {
            LogFile.log("[ShapeRenderer] render skipped: shape=" + (shape == null) + " shader=" + (shader == null));
            return;
        }

        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", identity);

        glEnable(GL_DEPTH_TEST);
        shape.render();

        if (hoveredEdgeId >= 0 && shapeData != null && shapeData.edges.containsKey(hoveredEdgeId)) {
            updateLineVbo(hoveredEdgeId);
            glLineWidth(3f);
            glBindVertexArray(lineVao);
            glDrawArrays(GL_LINES, 0, 2);
            glLineWidth(1f);
            glBindVertexArray(0);
        }

        if (hoveredVertexId >= 0 && shapeData != null && shapeData.vertices.containsKey(hoveredVertexId)) {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            if (hoveredVertexId != prevHovered) {
                prevHovered = hoveredVertexId;
            }
            glBindVertexArray(glowVao);
            shader.setUniform1f("uAlpha", 1f);
            glPointSize(11f);
            updateGlowVbo(1f, 1f, 1f);
            glDrawArrays(GL_POINTS, 0, 1);

            glPointSize(7f);
            updateGlowVbo(0f, 0f, 0f);
            glDrawArrays(GL_POINTS, 0, 1);

            glPointSize(1f);
            glDisable(GL_BLEND);
            glBindVertexArray(0);
        }

        shader.unbind();
    }

    /** Returns whether a shape is currently loaded. */
    public boolean hasShape() { return shape != null; }

    /** Frees the shape geometry and shader. */
    public void cleanup() {
        if (shape != null) { shape.cleanup(); shape = null; }
        if (shader != null) { shader.cleanup(); shader = null; }
        if (glowVao >= 0) { glDeleteVertexArrays(glowVao); glowVao = -1; }
        if (glowVbo >= 0) { glDeleteBuffers(glowVbo); glowVbo = -1; }
        if (lineVao >= 0) { glDeleteVertexArrays(lineVao); lineVao = -1; }
        if (lineVbo >= 0) { glDeleteBuffers(lineVbo); lineVbo = -1; }
        pendingEdgeA = -1;
        pendingEdgeB = -1;
        shapeData = null;
        prevHovered = -1;
        hoveredVertexId = -1;
    }
}
