package markershape.model;

public class Edge {
    public int id;
    public int a, b;
    public String mode;
    public float thickness;

    public Edge() {}

    public Edge(int id, int a, int b, String mode, float thickness) {
        this.id = id;
        this.a = a;
        this.b = b;
        this.mode = mode;
        this.thickness = thickness;
    }
}
