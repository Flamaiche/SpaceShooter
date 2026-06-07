package markershape.shape.render.face;

import learngl.Shader;
import learngl.shape.Shape;
import learngl.VertexUtils;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.shape.render.Renderer;

import java.util.ArrayList;
import java.util.List;

public class FaceRenderer implements Renderer {
    private Shape shape;
    private Shader currentShader;

    public void build(ShapeData data, Shader shader) {
        cleanup();
        this.currentShader = shader;
        if (data.vertices.isEmpty()) return;

        List<Float> verts = new ArrayList<>();
        for (int[] tri : data.faces) {
            for (int idx : tri) {
                Vertex v = data.vertices.get(idx);
                if (v == null) continue;
                verts.add(v.x); verts.add(v.y); verts.add(v.z);
                verts.add(v.r); verts.add(v.g); verts.add(v.b);
            }
        }
        if (verts.isEmpty()) return;

        float[] raw = new float[verts.size()];
        for (int i = 0; i < verts.size(); i++) raw[i] = verts.get(i);
        float[] full = VertexUtils.autoAddSlotTexture(raw);
        shape = new Shape(full);
        shape.setShader(shader);
    }

    public void setShader(Shader shader) {
        currentShader = shader;
        if (shape != null) shape.setShader(shader);
    }

    @Override
    public void render(Shader shader, ShapeData data) {
        if (shape != null && currentShader != null) {
            shape.render();
        }
    }

    @Override
    public void cleanup() {
        if (shape != null) { shape.cleanup(); shape = null; }
    }
}
