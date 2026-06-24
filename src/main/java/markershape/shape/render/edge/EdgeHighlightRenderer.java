package markershape.shape.render.edge;

import learngl.Shader;
import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.shape.render.Renderer;
import markershape.shape.render.ShadowRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Set;

import static org.lwjgl.opengl.GL11.*;

public class EdgeHighlightRenderer implements Renderer {
    private final ShadowRenderer shadow = new ShadowRenderer();
    private int hoveredEdgeId = -1;
    private int selectedEdgeId = -1;
    private int hoveredVertexId = -1;
    private int selectedVertexId = -1;
    private Set<Integer> hoveredPositionIds;

    public void setHoveredEdge(int id) { hoveredEdgeId = id; }
    public void setSelectedEdge(int id) { selectedEdgeId = id; }
    public void setHoveredVertex(int id) { hoveredVertexId = id; }
    public void setSelectedVertex(int id) { selectedVertexId = id; }
    public void setHoveredPositionIds(Set<Integer> ids) { hoveredPositionIds = ids; }

    public void setScreenSize(int w, int h) { shadow.setScreenSize(w, h); }

    @Override
    public void render(Shader shader, ShapeData data) {
        // 3D shader path — used as stub; actual work done by render2D
    }

    public void render2D(ShapeData data, Matrix4f view, Matrix4f projection, int w, int h) {
        shadow.setScreenSize(w, h);
        if (data == null) return;

        Matrix4f mvp = new Matrix4f(projection);
        mvp.mul(view);

        boolean showConnected = (hoveredVertexId >= 0 || selectedVertexId >= 0) && !data.edges.isEmpty();
        if (showConnected) {
            ArrayList<Edge> hoveredOnly = new ArrayList<>();
            ArrayList<Edge> selectedOnly = new ArrayList<>();
            ArrayList<Edge> common = new ArrayList<>();
            for (Edge e : data.edges.values()) {
                boolean onHovered = hoveredVertexId >= 0 && hoveredPositionIds != null
                    && (hoveredPositionIds.contains(e.a) || hoveredPositionIds.contains(e.b));
                boolean onSelected = selectedVertexId >= 0
                    && (e.a == selectedVertexId || e.b == selectedVertexId);
                if (onHovered && onSelected) common.add(e);
                else if (onSelected) selectedOnly.add(e);
                else if (onHovered) hoveredOnly.add(e);
            }
            drawBatch2D(data, hoveredOnly, mvp, w, h, 0.85f, 0.9f, 1f, 1f);
            drawBatch2D(data, selectedOnly, mvp, w, h, 1f, 0.95f, 0.6f, 1f);
            drawBatch2D(data, common, mvp, w, h, 1f, 0.2f, 0.2f, 1f);
        }

        if (selectedEdgeId >= 0 && data.edges.containsKey(selectedEdgeId)) {
            if (selectedEdgeId == hoveredEdgeId) {
                drawSingle2D(data, selectedEdgeId, mvp, w, h, 1f, 0.2f, 0.2f, 1f);
            } else {
                drawSingle2D(data, selectedEdgeId, mvp, w, h, 1f, 0.95f, 0.6f, 1f);
                if (hoveredEdgeId >= 0 && data.edges.containsKey(hoveredEdgeId)) {
                    drawSingle2D(data, hoveredEdgeId, mvp, w, h, 1f, 1f, 1f, 1f);
                }
            }
        } else if (hoveredEdgeId >= 0 && data.edges.containsKey(hoveredEdgeId)) {
            drawSingle2D(data, hoveredEdgeId, mvp, w, h, 1f, 1f, 1f, 1f);
        }
    }

    private void drawSingle2D(ShapeData data, int edgeId, Matrix4f mvp, int w, int h,
                               float r, float g, float b, float a) {
        Edge e = data.edges.get(edgeId);
        if (e == null) return;
        Vertex va = data.vertices.get(e.a);
        Vertex vb = data.vertices.get(e.b);
        if (va == null || vb == null) return;

        Vector4f p = new Vector4f();
        p.set(va.x, va.y, va.z, 1f).mul(mvp);
        if (p.w <= 0) return;
        float ax = (p.x / p.w * 0.5f + 0.5f) * w;
        float ay = (1f - (p.y / p.w * 0.5f + 0.5f)) * h;

        p.set(vb.x, vb.y, vb.z, 1f).mul(mvp);
        if (p.w <= 0) return;
        float bx = (p.x / p.w * 0.5f + 0.5f) * w;
        float by = (1f - (p.y / p.w * 0.5f + 0.5f)) * h;

        shadow.drawEdge(ax, ay, bx, by, r, g, b, a, 3f);
    }

    private void drawBatch2D(ShapeData data, ArrayList<Edge> edges, Matrix4f mvp, int w, int h,
                              float r, float g, float b, float a) {
        if (edges.isEmpty()) return;
        FloatBuffer buf = BufferUtils.createFloatBuffer(edges.size() * 2 * 6);
        Vector4f p = new Vector4f();
        for (Edge e : edges) {
            Vertex eva = data.vertices.get(e.a);
            Vertex evb = data.vertices.get(e.b);
            if (eva == null || evb == null) continue;

            p.set(eva.x, eva.y, eva.z, 1f).mul(mvp);
            if (p.w <= 0) continue;
            buf.put((p.x / p.w * 0.5f + 0.5f) * w);
            buf.put((1f - (p.y / p.w * 0.5f + 0.5f)) * h);
            buf.put(r).put(g).put(b).put(a);

            p.set(evb.x, evb.y, evb.z, 1f).mul(mvp);
            if (p.w <= 0) continue;
            buf.put((p.x / p.w * 0.5f + 0.5f) * w);
            buf.put((1f - (p.y / p.w * 0.5f + 0.5f)) * h);
            buf.put(r).put(g).put(b).put(a);
        }
        buf.flip();
        int vertCount = buf.limit() / 6;
        if (vertCount > 0) shadow.drawEdgeBatch(buf, vertCount, r, g, b, a, 1.5f);
    }

    @Override
    public void cleanup() {
        shadow.cleanup();
    }
}
