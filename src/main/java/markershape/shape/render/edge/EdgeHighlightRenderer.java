package markershape.shape.render.edge;

import learngl.Shader;
import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.shape.render.Renderer;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Set;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class EdgeHighlightRenderer implements Renderer {
    private int connVao = -1, connVbo = -1;
    private int lineVao = -1, lineVbo = -1;
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

    @Override
    public void render(Shader shader, ShapeData data) {
        if (data == null) return;
        boolean showConnected = (hoveredVertexId >= 0 || selectedVertexId >= 0) && !data.edges.isEmpty();
        if (showConnected) {
            if (connVao < 0) buildConn();
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
            drawConnectedBatch(data, hoveredOnly, 0.85f, 0.9f, 1f);
            drawConnectedBatch(data, selectedOnly, 1f, 0.95f, 0.6f);
            drawConnectedBatch(data, common, 1f, 0.2f, 0.2f);
        }

        if (lineVao < 0) buildLine();

        if (selectedEdgeId >= 0 && data.edges.containsKey(selectedEdgeId)) {
            if (selectedEdgeId == hoveredEdgeId) {
                drawSingleEdge(data, selectedEdgeId, 1f, 0.2f, 0.2f);
            } else {
                drawSingleEdge(data, selectedEdgeId, 1f, 0.95f, 0.6f);
                if (hoveredEdgeId >= 0 && data.edges.containsKey(hoveredEdgeId)) {
                    drawSingleEdge(data, hoveredEdgeId, 1f, 1f, 1f);
                }
            }
        } else if (hoveredEdgeId >= 0 && data.edges.containsKey(hoveredEdgeId)) {
            drawSingleEdge(data, hoveredEdgeId, 1f, 1f, 1f);
        }
    }

    private void drawSingleEdge(ShapeData data, int edgeId, float r, float g, float b) {
        Edge e = data.edges.get(edgeId);
        Vertex va = data.vertices.get(e.a);
        Vertex vb = data.vertices.get(e.b);
        if (va == null || vb == null) return;
        FloatBuffer buf = BufferUtils.createFloatBuffer(12);
        buf.put(va.x); buf.put(va.y); buf.put(va.z);
        buf.put(r); buf.put(g); buf.put(b);
        buf.put(vb.x); buf.put(vb.y); buf.put(vb.z);
        buf.put(r); buf.put(g); buf.put(b);
        buf.flip();
        glDepthMask(false);
        glLineWidth(3f);
        glBindVertexArray(lineVao);
        glBindBuffer(GL_ARRAY_BUFFER, lineVbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, buf);
        glDrawArrays(GL_LINES, 0, 2);
        glLineWidth(1f);
        glDepthMask(true);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void drawConnectedBatch(ShapeData data, ArrayList<Edge> edges, float r, float g, float b) {
        if (edges.isEmpty()) return;
        FloatBuffer cb = BufferUtils.createFloatBuffer(edges.size() * 2 * 6);
        for (Edge e : edges) {
            Vertex eva = data.vertices.get(e.a);
            Vertex evb = data.vertices.get(e.b);
            if (eva == null || evb == null) continue;
            cb.put(eva.x); cb.put(eva.y); cb.put(eva.z);
            cb.put(r); cb.put(g); cb.put(b);
            cb.put(evb.x); cb.put(evb.y); cb.put(evb.z);
            cb.put(r); cb.put(g); cb.put(b);
        }
        cb.flip();
        glDepthMask(false);
        glLineWidth(1.5f);
        glBindVertexArray(connVao);
        glBindBuffer(GL_ARRAY_BUFFER, connVbo);
        glBufferData(GL_ARRAY_BUFFER, cb, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINES, 0, edges.size() * 2);
        glLineWidth(1f);
        glDepthMask(true);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void buildConn() {
        connVao = glGenVertexArrays();
        connVbo = glGenBuffers();
        glBindVertexArray(connVao);
        glBindBuffer(GL_ARRAY_BUFFER, connVbo);
        glBufferData(GL_ARRAY_BUFFER, 256 * 6 * 4, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 3 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void buildLine() {
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

    @Override
    public void cleanup() {
        if (connVao >= 0) { glDeleteVertexArrays(connVao); connVao = -1; }
        if (connVbo >= 0) { glDeleteBuffers(connVbo); connVbo = -1; }
        if (lineVao >= 0) { glDeleteVertexArrays(lineVao); lineVao = -1; }
        if (lineVbo >= 0) { glDeleteBuffers(lineVbo); lineVbo = -1; }
    }
}
