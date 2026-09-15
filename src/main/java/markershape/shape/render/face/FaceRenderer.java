package markershape.shape.render.face;

import learngl.Shader;
import learngl.shape.Shape;
import learngl.VertexUtils;
import markershape.shape.Face;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.shape.render.Renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FaceRenderer implements Renderer {
    private Shape shape;
    private Shader currentShader;

    public void build(ShapeData data, Shader shader) {
        build(data.vertices, data.faces, shader);
    }

    /** Builds a reduced face set (used by LOD). Vertices come from the shape data. */
    public void build(HashMap<Integer, Vertex> vertices, List<Face> faces, Shader shader) {
        cleanup();
        this.currentShader = shader;
        if (vertices.isEmpty()) return;

        List<Float> verts = new ArrayList<>();
        for (Face tri : faces) {
            Vertex va = vertices.get(tri.a);
            Vertex vb = vertices.get(tri.b);
            Vertex vc = vertices.get(tri.c);
            if (va == null || vb == null || vc == null) continue;
            verts.add(va.x); verts.add(va.y); verts.add(va.z);
            verts.add(tri.r); verts.add(tri.g); verts.add(tri.bl);
            verts.add(vb.x); verts.add(vb.y); verts.add(vb.z);
            verts.add(tri.r); verts.add(tri.g); verts.add(tri.bl);
            verts.add(vc.x); verts.add(vc.y); verts.add(vc.z);
            verts.add(tri.r); verts.add(tri.g); verts.add(tri.bl);
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
    public void render(Shader shader, ShapeData data, org.joml.Matrix4f view, org.joml.Matrix4f projection, int screenW, int screenH) {
        if (shape != null && currentShader != null) {
            shape.render();
        }
    }

    @Override
    public void cleanup() {
        if (shape != null) { shape.cleanup(); shape = null; }
    }
}
