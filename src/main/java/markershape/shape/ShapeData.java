package markershape.shape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * In-memory representation of a 3D shape.
 * Stores vertices, edges, and faces in hash maps for O(1) lookup.
 * Removal of a vertex cascades to all connected edges.
 */
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

    /**
     * Constructs a named shape with the given shader.
     *
     * @param name   the shape name
     * @param shader the shader identifier
     */
    public ShapeData(String name, String shader) {
        this();
        this.name = name;
        this.shader = shader;
    }

    /**
     * Adds a vertex. Replaces any existing vertex with the same ID.
     *
     * @param v the vertex to add
     */
    public void addVertex(Vertex v) {
        vertices.put(v.id, v);
    }

    /**
     * Adds an edge and registers it on both endpoint vertices.
     *
     * @param e the edge to add
     */
    public void addEdge(Edge e) {
        edges.put(e.id, e);
        Vertex va = vertices.get(e.a);
        Vertex vb = vertices.get(e.b);
        if (va != null) va.edgeIds.add(e.id);
        if (vb != null) vb.edgeIds.add(e.id);
    }

    /**
     * Removes a vertex and all its connected edges.
     *
     * @param id the vertex ID to remove
     */
    public void removeVertex(int id) {
        Vertex v = vertices.get(id);
        if (v == null) return;
        for (int eid : new ArrayList<>(v.edgeIds)) {
            removeEdge(eid);
        }
        vertices.remove(id);
    }

    /**
     * Removes an edge and deregisters it from its endpoint vertices.
     *
     * @param id the edge ID to remove
     */
    public void removeEdge(int id) {
        Edge e = edges.remove(id);
        if (e == null) return;
        Vertex va = vertices.get(e.a);
        Vertex vb = vertices.get(e.b);
        if (va != null) va.edgeIds.remove(id);
        if (vb != null) vb.edgeIds.remove(id);
    }

    /** Deep copy of this shape data. */
    public ShapeData copy() {
        ShapeData c = new ShapeData(name, shader);
        for (Vertex v : vertices.values()) c.vertices.put(v.id, v.copy());
        for (Edge e : edges.values()) c.edges.put(e.id, e.copy());
        for (int[] f : faces) c.faces.add(new int[]{f[0], f[1], f[2]});
        return c;
    }
}
