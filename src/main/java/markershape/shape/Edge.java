package markershape.shape;

/**
 * An edge between two vertices identified by their IDs.
 * Supports a mode string (e.g. "stun", "move") and a render thickness.
 */
public class Edge {
    public int id;
    public int a, b;
    public String mode;
    public float thickness;
    public float r, g, bl;

    /** Default constructor (leaves fields at their default values). */
    public Edge() {}

    /**
     * Constructs an edge between two vertices.
     *
     * @param id        edge identifier
     * @param a         source vertex ID
     * @param b         destination vertex ID
     * @param mode      behaviour mode ("stun" or "move")
     * @param thickness render thickness in world units
     */
    public Edge(int id, int a, int b, String mode, float thickness) {
        this.id = id;
        this.a = a;  this.b = b;
        this.mode = mode;
        this.thickness = thickness;
        this.r = 1f; this.g = 1f; this.bl = 1f;
    }

    /** Constructs an edge between two vertices with an explicit colour. */
    public Edge(int id, int a, int b, String mode, float thickness, float r, float g, float bl) {
        this(id, a, b, mode, thickness);
        this.r = r; this.g = g; this.bl = bl;
    }

    /** Returns a deep copy of this edge. */
    public Edge copy() {
        return new Edge(id, a, b, mode, thickness, r, g, bl);
    }
}
