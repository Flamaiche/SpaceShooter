package markershape.shape.render;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates triangle geometry (position + color) so that every 3D helper
 * primitive (points, lines, crosses...) is eventually rasterized as real
 * triangles — exactly like the mesh faces.
 */
public final class TriBuilder {
    private final List<Float> verts = new ArrayList<>();

    /** Returns true if no vertices have been accumulated. */
    public boolean isEmpty() { return verts.isEmpty(); }

    /** Returns the number of accumulated vertices (6 floats each). */
    public int vertexCount() { return verts.size() / 6; }

    /** Appends a single triangle defined by three positions and one shared colour. */
    public void tri(float x0, float y0, float z0,
             float x1, float y1, float z1,
             float x2, float y2, float z2,
             float r, float g, float b) {
        verts.add(x0); verts.add(y0); verts.add(z0); verts.add(r); verts.add(g); verts.add(b);
        verts.add(x1); verts.add(y1); verts.add(z1); verts.add(r); verts.add(g); verts.add(b);
        verts.add(x2); verts.add(y2); verts.add(z2); verts.add(r); verts.add(g); verts.add(b);
    }

    /**
     * Two triangles forming a quad between A and B, extruded along the unit
     * thickness direction t by +/- half. Result is 6 vertices.
     */
    public void quad(float ax, float ay, float az,
             float bx, float by, float bz,
             float tx, float ty, float tz,
             float half, float r, float g, float b) {
        float aox = ax - tx * half, aoy = ay - ty * half, aoz = az - tz * half;
        float a1x = ax + tx * half, a1y = ay + ty * half, a1z = az + tz * half;
        float box = bx - tx * half, boy = by - ty * half, boz = bz - tz * half;
        float b1x = bx + tx * half, b1y = by + ty * half, b1z = bz + tz * half;
        tri(aox, aoy, aoz, box, boy, boz, b1x, b1y, b1z, r, g, b);
        tri(aox, aoy, aoz, b1x, b1y, b1z, a1x, a1y, a1z, r, g, b);
    }

    /**
     * Camera-facing billboard quad centered on (cx,cy,cz), sized with the
     * camera right (rx,ry,rz) and up (ux,uy,uz) axes (unit vectors), using
     * half-sized extents. Result is 6 vertices.
     */
    public void pquad(float cx, float cy, float cz,
               float rx, float ry, float rz,
               float ux, float uy, float uz,
               float half, float r, float g, float b) {
        float p0x = cx - rx * half - ux * half, p0y = cy - ry * half - uy * half, p0z = cz - rz * half - uz * half;
        float p1x = cx + rx * half - ux * half, p1y = cy + ry * half - uy * half, p1z = cz + rz * half - uz * half;
        float p2x = cx + rx * half + ux * half, p2y = cy + ry * half + uy * half, p2z = cz + rz * half + uz * half;
        float p3x = cx - rx * half + ux * half, p3y = cy - ry * half + uy * half, p3z = cz - rz * half + uz * half;
        tri(p0x, p0y, p0z, p1x, p1y, p1z, p2x, p2y, p2z, r, g, b);
        tri(p0x, p0y, p0z, p2x, p2y, p2z, p3x, p3y, p3z, r, g, b);
    }

    /** [x,y,z,r,g,b] array ready for {@code VertexUtils.autoAddSlotTexture}. */
    public float[] toFloats() {
        float[] out = new float[verts.size()];
        for (int i = 0; i < verts.size(); i++) out[i] = verts.get(i);
        return out;
    }
}