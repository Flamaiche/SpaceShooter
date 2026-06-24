package markershape.shape.render;

import learngl.LogFile;
import learngl.Shader;
import learngl.shape.Shape;
import learngl.VertexUtils;
import markershape.shape.ShapeLoader;
import markershape.shape.*;
import markershape.shape.render.GridRenderer;
import markershape.shape.render.edge.EdgeBatchRenderer;
import markershape.shape.render.edge.EdgeHighlightRenderer;
import markershape.shape.render.face.FaceRenderer;
import markershape.shape.render.point.CrosshairRenderer;
import markershape.shape.render.point.PointRenderer;

import java.util.*;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;

public class ShapeRenderer {
    private Shape shape;
    private Shader shader;
    private ShapeData shapeData;
    private final String shaderPath = "shaders/markershape/";
    private final Matrix4f identity = new Matrix4f();

    private final FaceRenderer faceRenderer = new FaceRenderer();
    private final PointRenderer pointRenderer = new PointRenderer();
    private final EdgeBatchRenderer edgeBatchRenderer = new EdgeBatchRenderer();
    private final EdgeHighlightRenderer edgeHighlightRenderer = new EdgeHighlightRenderer();
    private final CrosshairRenderer crosshairRenderer = new CrosshairRenderer();
    public final ShadowRenderer shadow = new ShadowRenderer();
    private final GridRenderer grid = new GridRenderer();

    private int hoveredVertexId = -1;
    private boolean showFaces = true, showEdges = true, showPoints = true;
    private float pointSize = 5f, lineWidth = 3f, faceAlpha = 1f;
    private int screenW = 1280, screenH = 720;

    public void setScreenSize(int w, int h) {
        screenW = w;
        screenH = h;
        shadow.setScreenSize(w, h);
        edgeHighlightRenderer.setScreenSize(w, h);
    }

    public boolean loadShape(String filename) {
        ShapeData data = ShapeLoader.load(filename);
        if (data == null) {
            System.err.println("[ShapeRenderer] failed to load: " + filename);
            return false;
        }
        buildFromData(data);
        return true;
    }

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
        faceRenderer.build(data, shader);

        LogFile.logf("[ShapeRenderer] built: vertices=%d faces=%d triangles=%d",
            data.vertices.size(), data.faces.size(), raw.length / 6);
    }

    public ShapeData getShapeData() { return shapeData; }
    public void setShapeData(ShapeData data) {
        shapeData = data;
        Shader oldShader = shader;
        boolean hadShape = shape != null;
        cleanupResources();
        if (data != null && !data.vertices.isEmpty()) {
            shader = oldShader;
            if (shader == null) return;
            List<Float> verts = new ArrayList<>();
            for (int[] tri : data.faces) {
                for (int idx : tri) {
                    Vertex v = data.vertices.get(idx);
                    if (v == null) continue;
                    verts.add(v.x); verts.add(v.y); verts.add(v.z);
                    verts.add(v.r); verts.add(v.g); verts.add(v.b);
                }
            }
            if (!verts.isEmpty()) {
                float[] raw = new float[verts.size()];
                for (int i = 0; i < verts.size(); i++) raw[i] = verts.get(i);
                float[] full = VertexUtils.autoAddSlotTexture(raw);
                shape = new Shape(full);
                shape.setShader(shader);
                faceRenderer.build(data, shader);
            }
        }
    }

    private void cleanupResources() {
        if (shape != null) { shape.cleanup(); shape = null; }
        faceRenderer.cleanup();
        pointRenderer.cleanup();
        edgeBatchRenderer.cleanup();
        edgeHighlightRenderer.cleanup();
        shadow.cleanup();
        crosshairRenderer.cleanup();
    }

    public void setHoveredVertex(int id) {
        hoveredVertexId = id;
        edgeHighlightRenderer.setHoveredVertex(id);
    }
    public void setHoveredEdge(int id) { edgeHighlightRenderer.setHoveredEdge(id); }
    public void setSelectedEdge(int id) { edgeHighlightRenderer.setSelectedEdge(id); }
    public void setSelectedVertex(int id) { edgeHighlightRenderer.setSelectedVertex(id); }
    public void setHoveredPositionIds(Set<Integer> ids) { edgeHighlightRenderer.setHoveredPositionIds(ids); }
    public void setCrosshair(boolean visible, org.joml.Vector3f pos) {
        crosshairRenderer.setVisible(visible);
        crosshairRenderer.setPosition(pos);
    }

    public void render(Matrix4f view, Matrix4f projection) {
        if (shape == null || shader == null) {
            LogFile.log("[ShapeRenderer] render skipped: shape=" + (shape == null) + " shader=" + (shader == null));
            return;
        }

        while (glGetError() != GL_NO_ERROR);

        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", identity);

        glEnable(GL_DEPTH_TEST);

        if (grid.anyVisible()) grid.render();

        if (showFaces) {
            shader.setUniform1f("uAlpha", faceAlpha);
            faceRenderer.render(shader, shapeData);
        }

        if (showEdges && shapeData != null && !shapeData.edges.isEmpty()) {
            edgeBatchRenderer.render(shader, shapeData);
        }

        if (showPoints && shapeData != null) {
            pointRenderer.render(shader, shapeData);
        }

        // Edge highlights in 2D overlay
        if (showEdges && shapeData != null) {
            edgeHighlightRenderer.render2D(shapeData, view, projection, screenW, screenH);
        }

        // Hovered vertex glow in 2D overlay
        if (showPoints && hoveredVertexId >= 0 && shapeData != null
            && shapeData.vertices.containsKey(hoveredVertexId)) {
            Vertex v = shapeData.vertices.get(hoveredVertexId);
            Matrix4f mvp = new Matrix4f(projection);
            mvp.mul(view);
            Vector4f p = new Vector4f(v.x, v.y, v.z, 1f).mul(mvp);
            if (p.w > 0) {
                float sx = (p.x / p.w * 0.5f + 0.5f) * screenW;
                float sy = (1f - (p.y / p.w * 0.5f + 0.5f)) * screenH;
                shadow.drawPoint(sx, sy, 1f, 1f, 0.6f, 1f, pointSize);
            }
        }

        crosshairRenderer.render(shader, shapeData);

        shader.unbind();
    }

    public void setGridStep(float step) { grid.setGridStep(step); }
    public void setGridVisible(boolean v) { grid.setGridVisible(v); }
    public void setShowAxisX(boolean v) { grid.setShowAxisX(v); crosshairRenderer.setShowAxisX(v); }
    public void setShowAxisY(boolean v) { grid.setShowAxisY(v); crosshairRenderer.setShowAxisY(v); }
    public void setShowAxisZ(boolean v) { grid.setShowAxisZ(v); crosshairRenderer.setShowAxisZ(v); }

    public void setShowFaces(boolean v) { showFaces = v; }
    public void setShowEdges(boolean v) { showEdges = v; }
    public void setShowPoints(boolean v) { showPoints = v; }
    public void setPointSize(float v) { pointSize = v; pointRenderer.setPointSize(v); }
    public float getPointSize() { return pointSize; }
    public void setLineWidth(float v) { lineWidth = v; edgeBatchRenderer.setLineWidth(v); }
    public float getLineWidth() { return lineWidth; }
    public void setFaceAlpha(float v) { faceAlpha = v; }
    public float getFaceAlpha() { return faceAlpha; }

    public void rebuild() {
        if (shapeData == null) return;
        Shader savedShader = shader;
        shader = null;
        ShapeData data = shapeData;
        cleanupResources();
        this.shapeData = data;
        shader = savedShader;
        if (shader == null) return;
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
        faceRenderer.build(data, shader);
        grid.rebuild();
    }

    public boolean hasShape() { return shape != null; }

    public void cleanup() {
        cleanupResources();
        if (shader != null) { shader.cleanup(); shader = null; }
        grid.cleanup();
        shapeData = null;
    }
}
