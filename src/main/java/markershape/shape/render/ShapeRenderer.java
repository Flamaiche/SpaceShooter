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
import markershape.shape.render.point.GhostPointRenderer;
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
    private final GhostPointRenderer ghostPointRenderer = new GhostPointRenderer();
    private final FrontArrowRenderer frontArrowRenderer = new FrontArrowRenderer();
    private final OrbitPivotRenderer orbitPivotRenderer = new OrbitPivotRenderer();
    public final ShadowRenderer shadow = new ShadowRenderer();
    private final GridRenderer grid = new GridRenderer();

    private int hoveredVertexId = -1;
    private int selectedVertexId = -1;
    private int selectedEdgeId = -1;
    private final Set<Integer> multiVertexHighlight = new TreeSet<>();
    private final Set<Integer> multiEdgeHighlight = new TreeSet<>();
    private boolean showFaces = true, showEdges = true, showPoints = true;
    private float pointSize = 5f, lineWidth = 3f, faceAlpha = 1f;
    private int screenW = 1280, screenH = 720;

    private boolean marqueeVisible;
    private float marqueeX1, marqueeY1, marqueeX2, marqueeY2;
    private boolean rubberVisible;
    private float rubberAx, rubberAy, rubberBx, rubberBy;
    private float[] tracePreview; // alternating sx, sy screen points

    public ShapeRenderer() {
        try {
            shader = new Shader(shaderPath + "default_Vertex.glsl", shaderPath + "default_Fragment.glsl");
        } catch (Exception e) {
            System.err.println("[ShapeRenderer] default shader load error: " + e.getMessage());
        }
    }

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
        ghostPointRenderer.cleanup();
        frontArrowRenderer.cleanup();
        orbitPivotRenderer.cleanup();
    }

    public void setHoveredVertex(int id) {
        hoveredVertexId = id;
        edgeHighlightRenderer.setHoveredVertex(id);
    }
    public void setHoveredEdge(int id) { edgeHighlightRenderer.setHoveredEdge(id); }
    public void setSelectedEdge(int id) {
        selectedEdgeId = id;
        edgeHighlightRenderer.setSelectedEdge(id);
    }
    public void setSelectedVertex(int id) {
        selectedVertexId = id;
        edgeHighlightRenderer.setSelectedVertex(id);
    }
    public void setMultiVertexHighlight(Set<Integer> ids) {
        multiVertexHighlight.clear();
        if (ids != null) multiVertexHighlight.addAll(ids);
    }
    public void setMultiEdgeHighlight(Set<Integer> ids) {
        multiEdgeHighlight.clear();
        if (ids != null) multiEdgeHighlight.addAll(ids);
    }
    public void setHoveredPositionIds(Set<Integer> ids) { edgeHighlightRenderer.setHoveredPositionIds(ids); }

    public void setMarquee(boolean visible, float x1, float y1, float x2, float y2) {
        marqueeVisible = visible;
        marqueeX1 = Math.min(x1, x2);
        marqueeY1 = Math.min(y1, y2);
        marqueeX2 = Math.max(x1, x2);
        marqueeY2 = Math.max(y1, y2);
    }

    public void setRubberBand(float ax, float ay, float bx, float by) {
        rubberVisible = true;
        rubberAx = ax; rubberAy = ay; rubberBx = bx; rubberBy = by;
    }

    public void clearRubberBand() {
        rubberVisible = false;
    }

    /** Sets the 2D polyline drawn during a "Tracé" session (screen-space points). */
    public void setTracePreview(float[] xy) {
        tracePreview = xy;
    }
    public void setCrosshair(boolean visible, org.joml.Vector3f pos) {
        crosshairRenderer.setVisible(visible);
        crosshairRenderer.setPosition(pos);
    }

    public void setPlacementGhost(boolean visible, org.joml.Vector3f pos) {
        ghostPointRenderer.setVisible(visible);
        ghostPointRenderer.setPointSize(pointSize);
        if (pos != null) ghostPointRenderer.setPosition(pos.x, pos.y, pos.z);
    }

    /** Hides the transient placement/arrow overlays (e.g. when the menu is shown). */
    public void hideTransientOverlays() {
        ghostPointRenderer.setVisible(false);
        frontArrowRenderer.setVisible(false);
        crosshairRenderer.setVisible(false);
        orbitPivotRenderer.setVisible(false);
    }

    public void setFrontArrow(boolean visible, org.joml.Vector3f center, org.joml.Vector3f dir, float length) {
        frontArrowRenderer.setVisible(visible);
        if (!visible) return;
        frontArrowRenderer.setArrow(center.x, center.y, center.z, dir, length);
    }

    /**
     * Shows/hides the orbit pivot "+" marker. When visible, draws a fixed
     * billboard cross at the pivot point (kept for the whole orbit gesture).
     */
    public void setOrbitPivotMarker(boolean visible, org.joml.Vector3f pos, float halfLen) {
        orbitPivotRenderer.setVisible(visible);
        if (!visible) return;
        if (pos != null) orbitPivotRenderer.setPosition(pos.x, pos.y, pos.z);
        orbitPivotRenderer.setHalfLength(halfLen);
    }

    /** Updates the pivot marker billboard orientation to the camera axes. */
    public void setOrbitPivotMarkerAxes(float rx, float ry, float rz, float ux, float uy, float uz) {
        orbitPivotRenderer.setAxes(rx, ry, rz, ux, uy, uz);
    }

    public void render(Matrix4f view, Matrix4f projection) {
        if (shader == null) {
            LogFile.log("[ShapeRenderer] render skipped: shader=" + (shader == null));
            return;
        }

        while (glGetError() != GL_NO_ERROR);

        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", identity);

        glEnable(GL_DEPTH_TEST);

        if (grid.anyVisible()) grid.render();

        if (shape != null) {
            if (showFaces) {
                shader.setUniform1f("uAlpha", faceAlpha);
                faceRenderer.render(shader, shapeData);
            }
        }

        if (shapeData != null) {
            if (showEdges && !shapeData.edges.isEmpty()) {
                edgeBatchRenderer.render(shader, shapeData);
            }

            if (showPoints) {
                pointRenderer.render(shader, shapeData);
            }

            // Edge highlights in 2D overlay
            if (showEdges) {
                edgeHighlightRenderer.render2D(shapeData, view, projection, screenW, screenH);
            }

            // Multi-selected edges / vertices in 2D overlay (distinct cyan tint)
            Matrix4f mvp = new Matrix4f(projection);
            mvp.mul(view);
            if (showEdges) {
                for (int id : multiEdgeHighlight) {
                    if (id == selectedEdgeId) continue;
                    Edge me = shapeData.edges.get(id);
                    if (me == null) continue;
                    Vertex mva = shapeData.vertices.get(me.a);
                    Vertex mvb = shapeData.vertices.get(me.b);
                    if (mva == null || mvb == null) continue;
                    Vector4f pa = new Vector4f(mva.x, mva.y, mva.z, 1f).mul(mvp);
                    Vector4f pb = new Vector4f(mvb.x, mvb.y, mvb.z, 1f).mul(mvp);
                    if (pa.w <= 0 || pb.w <= 0) continue;
                    float ax = (pa.x / pa.w * 0.5f + 0.5f) * screenW;
                    float ay = (1f - (pa.y / pa.w * 0.5f + 0.5f)) * screenH;
                    float bx = (pb.x / pb.w * 0.5f + 0.5f) * screenW;
                    float by = (1f - (pb.y / pb.w * 0.5f + 0.5f)) * screenH;
                    shadow.drawEdge(ax, ay, bx, by, 0.55f, 0.9f, 1f, 0.9f, 3f);
                }
            }

            // Hovered vertex glow in 2D overlay
            if (showPoints && hoveredVertexId >= 0) {
                Vertex v = shapeData.vertices.get(hoveredVertexId);
                if (v != null) {
                    Vector4f p = new Vector4f(v.x, v.y, v.z, 1f).mul(mvp);
                    if (p.w > 0) {
                        float sx = (p.x / p.w * 0.5f + 0.5f) * screenW;
                        float sy = (1f - (p.y / p.w * 0.5f + 0.5f)) * screenH;
                        shadow.drawPoint(sx, sy, 1f, 1f, 0.6f, 1f, pointSize);
                    }
                }
            }

            // Multi-selected vertex glows in 2D overlay
            if (showPoints) {
                for (int id : multiVertexHighlight) {
                    Vertex v = shapeData.vertices.get(id);
                    if (v == null) continue;
                    Vector4f p = new Vector4f(v.x, v.y, v.z, 1f).mul(mvp);
                    if (p.w <= 0) continue;
                    float sx = (p.x / p.w * 0.5f + 0.5f) * screenW;
                    float sy = (1f - (p.y / p.w * 0.5f + 0.5f)) * screenH;
                    shadow.drawPoint(sx, sy, 0.55f, 0.9f, 1f, 1f, pointSize);
                }
            }

            // Marquee selection box (2D overlay)
            if (marqueeVisible) {
                float mx1 = marqueeX1, my1 = marqueeY1, mx2 = marqueeX2, my2 = marqueeY2;
                shadow.drawEdge(mx1, my1, mx2, my1, 0.35f, 0.7f, 1f, 0.9f, 2f);
                shadow.drawEdge(mx2, my1, mx2, my2, 0.35f, 0.7f, 1f, 0.9f, 2f);
                shadow.drawEdge(mx2, my2, mx1, my2, 0.35f, 0.7f, 1f, 0.9f, 2f);
                shadow.drawEdge(mx1, my2, mx1, my1, 0.35f, 0.7f, 1f, 0.9f, 2f);
            }

            // Rubber-band edge preview (2D overlay)
            if (rubberVisible) {
                shadow.drawEdge(rubberAx, rubberAy, rubberBx, rubberBy, 0.3f, 0.9f, 1f, 0.9f, 2f);
            }

            // Tracé loop preview (2D overlay)
            if (tracePreview != null && tracePreview.length >= 4) {
                int n = tracePreview.length / 2;
                for (int i = 0; i + 1 < n; i++) {
                    shadow.drawEdge(tracePreview[i * 2], tracePreview[i * 2 + 1],
                        tracePreview[(i + 1) * 2], tracePreview[(i + 1) * 2 + 1],
                        0.9f, 0.55f, 0.2f, 0.9f, 3f);
                }
                if (n >= 3) {
                    shadow.drawEdge(tracePreview[0], tracePreview[1],
                        tracePreview[(n - 1) * 2], tracePreview[(n - 1) * 2 + 1],
                        0.9f, 0.55f, 0.2f, 0.5f, 2f);
                }
            }

            crosshairRenderer.render(shader, shapeData);
            ghostPointRenderer.render(shader, shapeData);
            frontArrowRenderer.render(shader, shapeData);
            orbitPivotRenderer.render(shader, shapeData);
        }

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

    public boolean hasShape() { return shapeData != null; }

    public void cleanup() {
        cleanupResources();
        if (shader != null) { shader.cleanup(); shader = null; }
        grid.cleanup();
        shapeData = null;
    }
}
