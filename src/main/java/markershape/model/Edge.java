package markershape.model;

/**
 * An edge between two vertices identified by their IDs.
 * Supports a mode string (e.g. "stun", "move") and a render thickness.
 */
public class Edge {
    public int id;
    public int a, b;
    public String mode;
    public float thickness;

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
        this.a = a;
        this.b = b;
        this.mode = mode;
        this.thickness = thickness;
    }
}
