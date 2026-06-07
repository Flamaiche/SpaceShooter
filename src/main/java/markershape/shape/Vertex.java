package markershape.shape;

import java.util.HashSet;
import java.util.Set;

/**
 * A 3D vertex with position, colour, and a set of incident edge IDs.
 * When a vertex is removed, all edges referencing it are cascaded.
 */
public class Vertex {
    public int id;
    public float x, y, z;
    public float r, g, b;
    public final Set<Integer> edgeIds = new HashSet<>();

    public Vertex() {}

    /**
     * Constructs a vertex with the given position and colour.
     *
     * @param id vertex identifier
     * @param x  X position
     * @param y  Y position
     * @param z  Z position
     * @param r  red component [0..1]
     * @param g  green component [0..1]
     * @param b  blue component [0..1]
     */
    public Vertex(int id, float x, float y, float z, float r, float g, float b) {
        this.id = id;
        this.x = x;  this.y = y;  this.z = z;
        this.r = r;  this.g = g;  this.b = b;
    }

    public Vertex copy() {
        Vertex c = new Vertex(id, x, y, z, r, g, b);
        c.edgeIds.addAll(edgeIds);
        return c;
    }
}
