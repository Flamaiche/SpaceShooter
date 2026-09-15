package markershape.shape;

/**
 * A triangular face referencing three vertex IDs, carrying its own render
 * colour (chosen by the user).
 */
public class Face {
    public int a, b, c;
    public float r, g, bl;

    public Face() {}

    public Face(int a, int b, int c) {
        this.a = a;  this.b = b;  this.c = c;
        this.r = 1f; this.g = 1f; this.bl = 1f;
    }

    public Face(int a, int b, int c, float r, float g, float bl) {
        this.a = a;  this.b = b;  this.c = c;
        this.r = r;  this.g = g;  this.bl = bl;
    }

    public boolean contains(int id) {
        return a == id || b == id || c == id;
    }

    public Face copy() {
        return new Face(a, b, c, r, g, bl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Face f)) return false;
        return a == f.a && b == f.b && c == f.c;
    }

    @Override
    public int hashCode() {
        int h = a;
        h = 31 * h + b;
        h = 31 * h + c;
        return h;
    }
}