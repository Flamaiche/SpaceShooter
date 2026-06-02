package markershape.model;

import java.util.HashSet;
import java.util.Set;

public class Vertex {
    public int id;
    public float x, y, z;
    public float r, g, b;
    public final Set<Integer> edgeIds = new HashSet<>();

    public Vertex() {}

    public Vertex(int id, float x, float y, float z, float r, float g, float b) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
