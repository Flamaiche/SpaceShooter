package learngl.tools.shape;

import org.joml.Vector3f;

public class ShapeMerge {

    public static float[] merge(Part... parts) {
        int total = 0;
        for (Part p : parts) total += p.vertices.length;

        float[] result = new float[total];
        int idx = 0;
        Vector3f v = new Vector3f();

        for (Part p : parts) {
            for (int i = 0; i < p.vertices.length; i += 3) {
                v.set(p.vertices[i], p.vertices[i + 1], p.vertices[i + 2]);
                v.mul(p.sx, p.sy, p.sz);
                if (p.rx != 0) v.rotateX((float) Math.toRadians(p.rx));
                if (p.ry != 0) v.rotateY((float) Math.toRadians(p.ry));
                if (p.rz != 0) v.rotateZ((float) Math.toRadians(p.rz));
                v.add(p.tx, p.ty, p.tz);
                result[idx++] = v.x;
                result[idx++] = v.y;
                result[idx++] = v.z;
            }
        }
        return result;
    }

    public static Part part(float[] vertices) {
        return new Part(vertices);
    }

    public static class Part {
        private float[] vertices;
        private float tx, ty, tz;
        private float rx, ry, rz;
        private float sx = 1, sy = 1, sz = 1;

        Part(float[] vertices) { this.vertices = vertices; }

        public Part translate(float x, float y, float z) {
            tx = x; ty = y; tz = z; return this;
        }

        public Part rotateX(float deg) { rx = deg; return this; }
        public Part rotateY(float deg) { ry = deg; return this; }
        public Part rotateZ(float deg) { rz = deg; return this; }

        public Part scale(float x, float y, float z) {
            sx = x; sy = y; sz = z; return this;
        }
    }
}
