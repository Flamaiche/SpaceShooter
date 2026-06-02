package markershape.render;

import learngl.Shader;
import learngl.shape.Shape;
import learngl.VertexUtils;
import markershape.io.ShapeIO;
import markershape.model.*;

import java.util.*;

/**
 * Converts ShapeData into renderable OpenGL geometry and draws it.
 * Manages a single shape and its associated shader at a time.
 */
public class ShapeRenderer {
    private Shape shape;
    private Shader shader;
    private final String shaderPath = "shaders/markershape/";
    private final org.joml.Matrix4f identity = new org.joml.Matrix4f();

    /**
     * Loads a shape from a JSON file and builds the render geometry.
     *
     * @param filename the shape file to load
     * @return true if the shape was loaded successfully
     */
    public boolean loadShape(String filename) {
        ShapeData data = ShapeIO.load(filename);
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
    }

    /**
     * Renders the current shape with the given view and projection matrices.
     *
     * @param view       the view matrix
     * @param projection the projection matrix
     */
    public void render(org.joml.Matrix4f view, org.joml.Matrix4f projection) {
        if (shape == null || shader == null) return;

        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", identity);
        shape.render();
        shader.unbind();
    }

    /** Returns whether a shape is currently loaded. */
    public boolean hasShape() { return shape != null; }

    /** Frees the shape geometry and shader. */
    public void cleanup() {
        if (shape != null) { shape.cleanup(); shape = null; }
        if (shader != null) { shader.cleanup(); shader = null; }
    }
}
