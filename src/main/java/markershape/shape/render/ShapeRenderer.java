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
import org.joml.Vector3f;
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
    private boolean lodEnabled = true;
    private int lodLevel = 0;
    private float lodDistance = 0f;
    private int renderedFaceCount = 0;
    private Face[] originalFaces;
    private final Vector3f meshCenter = new Vector3f();
    private boolean meshCenterValid = false;

    private boolean marqueeVisible;
    private float marqueeX1, marqueeY1, marqueeX2, marqueeY2;
    private boolean rubberVisible;
    private float rubberAx, rubberAy, rubberBx, rubberBy;
    private float[] tracePreview; // alternating sx, sy screen points
    private java.util.List<Integer> faceSelectPreview; // ordered contour vertex ids (pending face creation)
    private boolean pivotMarkerVisible;
    private final org.joml.Vector3f pivotMarkerPos = new org.joml.Vector3f();

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

        storeOriginalFaces(data);
        rebuildLodGeometry();
        grid.rebuild();

        LogFile.logf("[ShapeRenderer] built: vertices=%d faces=%d triangles=%d",
            data.vertices.size(), data.faces.size(), data.vertices.size() > 0 ? data.faces.size() * 3 : 0);
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
            storeOriginalFaces(data);
            rebuildLodGeometry();
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
    }

    private void storeOriginalFaces(ShapeData data) {
        originalFaces = data.faces.toArray(new Face[0]);
        meshCenterValid = false;
        if (data.vertices == null || data.vertices.isEmpty()) return;
        float cx = 0, cy = 0, cz = 0;
        int n = 0;
        for (Vertex v : data.vertices.values()) {
            cx += v.x; cy += v.y; cz += v.z; n++;
        }
        if (n > 0) {
            meshCenter.set(cx / n, cy / n, cz / n);
            meshCenterValid = true;
        }
    }

    /** (Re)builds the face geometry with the faces reduced to the current LOD level. */
    private void rebuildLodGeometry() {
        if (shader == null || originalFaces == null) return;
        List<Face> src = (lodEnabled && lodLevel > 0)
            ? LOD.reduce(originalFaces, lodLevel)
            : Arrays.asList(originalFaces);
        renderedFaceCount = src.size();
        if (src.isEmpty()) return;

        List<Float> verts = new ArrayList<>();
        for (Face tri : src) {
            Vertex va = shapeData.vertices.get(tri.a);
            Vertex vb = shapeData.vertices.get(tri.b);
            Vertex vc = shapeData.vertices.get(tri.c);
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
        faceRenderer.build(shapeData.vertices, src, shader);
    }

    /** Computes the camera distance and swaps the LOD level if it changed. */
    private void updateLod(Matrix4f view) {
        if (!lodEnabled || !meshCenterValid || originalFaces == null) return;
        Matrix4f inv = new Matrix4f(view).invertAffine();
        float dx = inv.m30() - meshCenter.x;
        float dy = inv.m31() - meshCenter.y;
        float dz = inv.m32() - meshCenter.z;
        lodDistance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        int level = LOD.level(lodDistance);
        if (level != lodLevel) {
            lodLevel = level;
            rebuildLodGeometry();
        }
    }

    public void setLodEnabled(boolean enabled) {
        if (lodEnabled == enabled) return;
        lodEnabled = enabled;
        lodLevel = 0;
        if (shapeData != null) rebuildLodGeometry();
    }
    public boolean isLodEnabled() { return lodEnabled; }
    public int getLodLevel() { return lodEnabled ? lodLevel : 0; }
    public float getLodDistance() { return lodDistance; }
    public int getTotalFaceCount() { return originalFaces != null ? originalFaces.length : 0; }
    public int getRenderedFaceCount() { return renderedFaceCount; }

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

    /** Sets the ordered contour preview for a pending face-by-selection creation. */
    public void setFaceSelectPreview(List<Integer> vertexIds) {
        faceSelectPreview = vertexIds == null ? null : new ArrayList<>(vertexIds);
    }

    public void clearFaceSelectPreview() {
        faceSelectPreview = null;
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
        crosshairRenderer.setVisible(false);
        pivotMarkerVisible = false;
    }

    /**
     * Shows/hides the orbit pivot "+" marker. Rendered as a small fixed-size
     * 2D crosshair (like a game crosshair) at the pivot's screen position,
     * kept for the whole orbit gesture.
     */
    public void setOrbitPivotMarker(boolean visible, org.joml.Vector3f pos, float halfLen) {
        pivotMarkerVisible = visible;
        if (pos != null) pivotMarkerPos.set(pos);
    }

    /** Kept for API compatibility; the 2D crosshair needs no billboard axes. */
    public void setOrbitPivotMarkerAxes(float rx, float ry, float rz, float ux, float uy, float uz) {
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

        if (grid.anyVisible()) grid.render(view, projection, screenW, screenH);

        if (shape != null) {
            updateLod(view);
            if (showFaces) {
                shader.setUniform1f("uAlpha", faceAlpha);
                faceRenderer.render(shader, shapeData, view, projection, screenW, screenH);
            }
        }

        if (shapeData != null) {
            if (showEdges && !shapeData.edges.isEmpty()) {
                edgeBatchRenderer.render(shader, shapeData, view, projection, screenW, screenH);
            }

            if (showPoints) {
                pointRenderer.render(shader, shapeData, view, projection, screenW, screenH);
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

            // Face-by-selection contour preview (2D overlay)
            if (faceSelectPreview != null && faceSelectPreview.size() >= 3 && shapeData != null) {
                Matrix4f mvp2 = new Matrix4f(projection);
                mvp2.mul(view);
                int n = faceSelectPreview.size();
                float[][] sxy = new float[n][2];
                for (int i = 0; i < n; i++) {
                    Vertex v = shapeData.vertices.get(faceSelectPreview.get(i));
                    if (v == null) { sxy[i][0] = Float.NaN; sxy[i][1] = Float.NaN; continue; }
                    Vector4f p = new Vector4f(v.x, v.y, v.z, 1f).mul(mvp2);
                    if (p.w <= 0) { sxy[i][0] = Float.NaN; sxy[i][1] = Float.NaN; continue; }
                    sxy[i][0] = (p.x / p.w * 0.5f + 0.5f) * screenW;
                    sxy[i][1] = (1f - (p.y / p.w * 0.5f + 0.5f)) * screenH;
                }
                for (int i = 0; i < n; i++) {
                    int j = (i + 1) % n;
                    if (Float.isNaN(sxy[i][0]) || Float.isNaN(sxy[j][0])) continue;
                    shadow.drawEdge(sxy[i][0], sxy[i][1], sxy[j][0], sxy[j][1],
                        0.3f, 0.9f, 0.55f, 0.9f, 3f);
                }
                for (int i = 2; i < n; i++) {
                    if (Float.isNaN(sxy[0][0]) || Float.isNaN(sxy[i - 1][0]) || Float.isNaN(sxy[i][0])) continue;
                    shadow.drawEdge(sxy[0][0], sxy[0][1], sxy[i][0], sxy[i][1], 0.3f, 0.9f, 0.55f, 0.35f, 1.5f);
                }
            }

            crosshairRenderer.render(shader, shapeData, view, projection, screenW, screenH);
            ghostPointRenderer.render(shader, shapeData, view, projection, screenW, screenH);

            // Orbit pivot marker: small fixed-size 2D crosshair (like a game crosshair)
            if (pivotMarkerVisible) {
                Vector4f pt = new Vector4f(pivotMarkerPos.x, pivotMarkerPos.y, pivotMarkerPos.z, 1f).mul(mvp);
                if (pt.w > 0) {
                    float sx = (pt.x / pt.w * 0.5f + 0.5f) * screenW;
                    float sy = (1f - (pt.y / pt.w * 0.5f + 0.5f)) * screenH;
                    float arm = 5f;
                    shadow.drawEdge(sx - arm, sy, sx + arm, sy, 1f, 0.9f, 0.3f, 1f, 1.5f);
                    shadow.drawEdge(sx, sy - arm, sx, sy + arm, 1f, 0.9f, 0.3f, 1f, 1.5f);
                    shadow.drawPoint(sx, sy, 1f, 0.9f, 0.3f, 1f, 2.5f);
                }
            }
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

        storeOriginalFaces(data);
        rebuildLodGeometry();
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
