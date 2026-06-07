package markershape.editor;

import java.util.ArrayList;
import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import markershape.shape.render.ShapeRenderer;
import markershape.editor.ui.overlay.EdgeOverlay;
import markershape.editor.ui.overlay.SiblingPicker;
import markershape.editor.ui.overlay.VertexOverlay;

public class SelectionManager {
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

    public SelectionManager(VertexOverlay vo, EdgeOverlay eo, SiblingPicker sp) {
        this.vertexOverlay = vo;
        this.edgeOverlay = eo;
        this.siblingPicker = sp;
    }

    public void setRenderer(ShapeRenderer r) { this.renderer = r; }

    public void selectVertex(int id) {
        ShapeData data = renderer.getShapeData();
        if (data == null) return;
        Vertex v = data.vertices.get(id);
        if (v == null) return;
        selectedVertex = id;
        selectedEdge = -1;
        renderer.setSelectedVertex(id);
        renderer.setSelectedEdge(-1);
        crosshairPos.set(v.x, v.y, v.z);
        crosshairValid = true;
        int edgeCount = v.edgeIds.size();
        int[] siblings = findSiblings(data, v);
        vertexOverlay.show(v, edgeCount, siblings);
        vertexOverlay.setPosition(10, 50);
        edgeOverlay.hide();
    }

    public void selectEdge(int id) {
        ShapeData data = renderer.getShapeData();
        if (data == null) return;
        Edge e = data.edges.get(id);
        if (e == null) return;
        selectedEdge = id;
        selectedVertex = -1;
        renderer.setSelectedEdge(id);
        renderer.setSelectedVertex(-1);
        crosshairValid = false;
        edgeOverlay.show(e, e.a, e.b);
        edgeOverlay.setPosition(10, 50);
        vertexOverlay.hide();
    }

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

    public boolean isOverOverlay(float mx, float my) {
        if (vertexOverlay.isVisible() && vertexOverlay.contains(mx, my)) return true;
        if (edgeOverlay.isVisible() && edgeOverlay.contains(mx, my)) return true;
        return false;
    }

    public void hideOverlays() {
        vertexOverlay.hide();
        edgeOverlay.hide();
    }

    public void reset() {
        selectedVertex = -1;
        selectedEdge = -1;
        hoveredVertex = -1;
        hoveredEdge = -1;
        crosshairValid = false;
        hideOverlays();
        if (renderer != null) {
            renderer.setSelectedVertex(-1);
            renderer.setSelectedEdge(-1);
        }
    }
}
