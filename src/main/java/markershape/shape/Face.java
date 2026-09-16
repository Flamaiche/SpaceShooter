package markershape.shape;

/**
 * A triangular face referencing three vertex IDs, carrying its own render
 * colour (chosen by the user).
 */
public class Face {
    public int a, b, c;
    public float r, g, bl;

    /** Default constructor (leaves fields at their default values). */
    public Face() {}

    /** Constructs an opaque white face from three vertex IDs. */
    public Face(int a, int b, int c) {
        this.a = a;  this.b = b;  this.c = c;
        this.r = 1f; this.g = 1f; this.bl = 1f;
    }

    /** Constructs a face from three vertex IDs and an explicit colour. */
    public Face(int a, int b, int c, float r, float g, float bl) {
        this.a = a;  this.b = b;  this.c = c;
        this.r = r;  this.g = g;  this.bl = bl;
    }

    /** Returns true if this face references the given vertex ID. */
    public boolean contains(int id) {
        return a == id || b == id || c == id;
    }

    /** Returns a deep copy of this face. */
    public Face copy() {
        return new Face(a, b, c, r, g, bl);
    }

    /** Compares two faces by vertex IDs and colour. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Face f)) return false;
        return a == f.a && b == f.b && c == f.c
            && r == f.r && g == f.g && bl == f.bl;
    }

    /** Computes a hash code from the vertex IDs and colour. */
    @Override
    public int hashCode() {
        int h = a;
        h = 31 * h + b;
        h = 31 * h + c;
        h = 31 * h + Float.floatToIntBits(r);
        h = 31 * h + Float.floatToIntBits(g);
        h = 31 * h + Float.floatToIntBits(bl);
        return h;
    }
}