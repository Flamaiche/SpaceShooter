package markershape.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShapeData {
    public String name;
    public String shader;
    public HashMap<Integer, Vertex> vertices;
    public HashMap<Integer, Edge> edges;
    public List<int[]> faces;

    public ShapeData() {
        vertices = new HashMap<>();
        edges = new HashMap<>();
        faces = new ArrayList<>();
    }

    public ShapeData(String name, String shader) {
        this();
        this.name = name;
        this.shader = shader;
    }

    public void addVertex(Vertex v) {
        vertices.put(v.id, v);
    }

    public void addEdge(Edge e) {
        edges.put(e.id, e);
        Vertex va = vertices.get(e.a);
        Vertex vb = vertices.get(e.b);
        if (va != null) va.edgeIds.add(e.id);
        if (vb != null) vb.edgeIds.add(e.id);
    }

    public void removeVertex(int id) {
        Vertex v = vertices.get(id);
        if (v == null) return;
        for (int eid : new ArrayList<>(v.edgeIds)) {
            removeEdge(eid);
        }
        vertices.remove(id);
    }

    public void removeEdge(int id) {
        Edge e = edges.remove(id);
        if (e == null) return;
        Vertex va = vertices.get(e.a);
        Vertex vb = vertices.get(e.b);
        if (va != null) va.edgeIds.remove(id);
        if (vb != null) vb.edgeIds.remove(id);
    }
}
