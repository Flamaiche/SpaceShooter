package markershape.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.shape.render.ShapeRenderer;
import markershape.editor.ui.overlay.EdgeOverlay;
import markershape.editor.ui.overlay.SiblingPicker;
import markershape.editor.ui.overlay.VertexOverlay;

/** Manages both single (primary) and multi selection of vertices/edges. */
public class SelectionManager {
    public final TreeSet<Integer> multiVertices = new TreeSet<>();
    public final TreeSet<Integer> multiEdges = new TreeSet<>();
    public int selectedVertex = -1;
    public int selectedEdge = -1;
    public int hoveredVertex = -1;
    public int hoveredEdge = -1;

    public final VertexOverlay vertexOverlay;
    public final EdgeOverlay edgeOverlay;
    public final SiblingPicker siblingPicker;
    private ShapeRenderer renderer;
    public org.joml.Vector3f crosshairPos = new org.joml.Vector3f();
    public boolean crosshairValid;

    /** Creates the manager with the overlays used to edit the selected entity. */
    public SelectionManager(VertexOverlay vo, EdgeOverlay eo, SiblingPicker sp) {
        this.vertexOverlay = vo;
        this.edgeOverlay = eo;
        this.siblingPicker = sp;
    }

    /** Sets the renderer used to read/write shape and selection data. */
    public void setRenderer(ShapeRenderer r) { this.renderer = r; }

    /** Clears multi selection and selects a single vertex (shows its overlay). */
    public void selectVertex(int id) {
        ShapeData data = renderer.getShapeData();
        if (data == null) return;
        Vertex v = data.vertices.get(id);
        if (v == null) return;
        selectedVertex = id;
        selectedEdge = -1;
        multiVertices.clear();
        multiVertices.add(id);
        multiEdges.clear();
        refreshSelectionVisual();
    }

    /** Clears multi selection and selects a single edge (shows its overlay). */
    public void selectEdge(int id) {
        ShapeData data = renderer.getShapeData();
        if (data == null) return;
        Edge e = data.edges.get(id);
        if (e == null) return;
        selectedEdge = id;
        selectedVertex = -1;
        multiEdges.clear();
        multiEdges.add(id);
        multiVertices.clear();
        refreshSelectionVisual();
    }

    /** Ctrl+click toggle: adds/removes a vertex to the multi-selection. */
    public void toggleVertexMulti(int id) {
        ShapeData data = renderer.getShapeData();
        if (data == null) return;
        if (data.vertices.get(id) == null) return;
        if (multiVertices.remove(id)) {
            if (selectedVertex == id) {
                if (multiVertices.isEmpty()) selectedVertex = -1;
                else selectedVertex = multiVertices.first();
            }
        } else {
            multiVertices.add(id);
            selectedVertex = id;
            selectedEdge = -1;
            multiEdges.clear();
        }
        refreshSelectionVisual();
    }

    /** Ctrl+click toggle: adds/removes an edge to the multi-selection. */
    public void toggleEdgeMulti(int id) {
        ShapeData data = renderer.getShapeData();
        if (data == null) return;
        if (data.edges.get(id) == null) return;
        if (multiEdges.remove(id)) {
            if (selectedEdge == id) {
                if (multiEdges.isEmpty()) selectedEdge = -1;
                else selectedEdge = multiEdges.first();
            }
        } else {
            multiEdges.add(id);
            selectedEdge = id;
            selectedVertex = -1;
            multiVertices.clear();
        }
        refreshSelectionVisual();
    }

    /** Changes the primary vertex without altering the multi-selection. */
    public void makePrimaryVertex(int id) {
        ShapeData data = renderer.getShapeData();
        if (data == null) return;
        if (data.vertices.get(id) == null) return;
        selectedVertex = id;
        selectedEdge = -1;
        refreshSelectionVisual();
    }

    /** Changes the primary edge without altering the multi-selection. */
    public void makePrimaryEdge(int id) {
        ShapeData data = renderer.getShapeData();
        if (data == null) return;
        if (data.edges.get(id) == null) return;
        selectedEdge = id;
        selectedVertex = -1;
        refreshSelectionVisual();
    }

    /** Selects a freshly created set of vertices as a multi-selection group. */
    public void selectDuplicateVertices(Collection<Integer> ids) {
        multiVertices.clear();
        multiEdges.clear();
        selectedEdge = -1;
        selectedVertex = -1;
        for (int id : ids) {
            if (id >= 0) multiVertices.add(id);
        }
        if (!multiVertices.isEmpty()) selectedVertex = multiVertices.first();
        refreshSelectionVisual();
    }

    /** Pushes the current selection state into the renderer and overlays. */
    public void refreshSelectionVisual() {
        ShapeData data = renderer.getShapeData();
        renderer.setSelectedVertex(selectedVertex);
        renderer.setSelectedEdge(selectedEdge);
        renderer.setMultiVertexHighlight(multiVertices);
        renderer.setMultiEdgeHighlight(multiEdges);
        if (selectedVertex >= 0 && data != null) {
            Vertex v = data.vertices.get(selectedVertex);
            if (v != null) {
                crosshairPos.set(v.x, v.y, v.z);
                crosshairValid = true;
                int edgeCount = v.edgeIds.size();
                int[] siblings = findSiblings(data, v);
                vertexOverlay.show(v, edgeCount, siblings, data);
                vertexOverlay.setPosition(10, 50);
                edgeOverlay.hide();
                return;
            }
        }
        if (selectedEdge >= 0 && data != null) {
            Edge e = data.edges.get(selectedEdge);
            if (e != null) {
                crosshairValid = false;
                edgeOverlay.show(e, e.a, e.b);
                edgeOverlay.setPosition(10, 50);
                vertexOverlay.hide();
                return;
            }
        }
        crosshairValid = false;
        vertexOverlay.hide();
        edgeOverlay.hide();
    }

    /** Returns the ids of all vertices co-located with the given vertex. */
    public int[] findSiblings(ShapeData data, Vertex v) {
        ArrayList<Integer> list = new ArrayList<>();
        for (Vertex other : data.vertices.values()) {
            if (other.id != v.id && other.x == v.x && other.y == v.y && other.z == v.z) {
                list.add(other.id);
            }
        }
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    /** Returns whether the point lies on a currently visible editing overlay. */
    public boolean isOverOverlay(float mx, float my) {
        if (vertexOverlay.isVisible() && vertexOverlay.contains(mx, my)) return true;
        if (edgeOverlay.isVisible() && edgeOverlay.contains(mx, my)) return true;
        return false;
    }

    /** Hides the vertex and edge editing overlays. */
    public void hideOverlays() {
        vertexOverlay.hide();
        edgeOverlay.hide();
    }

    /** Clears all selection state, overlays, and renderer highlights. */
    public void reset() {
        selectedVertex = -1;
        selectedEdge = -1;
        hoveredVertex = -1;
        hoveredEdge = -1;
        multiVertices.clear();
        multiEdges.clear();
        crosshairValid = false;
        hideOverlays();
        if (renderer != null) {
            renderer.setSelectedVertex(-1);
            renderer.setSelectedEdge(-1);
            renderer.setMultiVertexHighlight(multiVertices);
            renderer.setMultiEdgeHighlight(multiEdges);
        }
    }
}