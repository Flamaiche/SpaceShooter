package learngl.tools.shape;

import org.joml.Vector3f;

public class PreVerticesTable {

    public static float[] generateCubeSimple(float n) {
        float h = n / 2f;

        return new float[]{
                -h, -h,  h,   h, -h,  h,   h,  h,  h,
                h,  h,  h,  -h,  h,  h,  -h, -h,  h,
                -h, -h, -h,  -h,  h, -h,   h,  h, -h,
                h,  h, -h,   h, -h, -h,  -h, -h, -h,
                -h, -h, -h,  -h, -h,  h,  -h,  h,  h,
                -h,  h,  h,  -h,  h, -h,  -h, -h, -h,
                h, -h, -h,   h,  h, -h,   h,  h,  h,
                h,  h,  h,   h, -h,  h,   h, -h, -h,
                -h,  h, -h,  -h,  h,  h,   h,  h,  h,
                h,  h,  h,   h,  h, -h,  -h,  h, -h,
                -h, -h, -h,   h, -h, -h,   h, -h,  h,
                h, -h,  h,  -h, -h,  h,  -h, -h, -h
        };
    }

    public static float[] generatePyramid(float baseSize, float height) {
        float h = baseSize / 2f;

        return new float[]{
                -h, 0, -h,   h, 0, -h,   h, 0,  h,
                h, 0,  h,  -h, 0,  h,  -h, 0, -h,

                -h, 0,  h,    h, 0,  h,    0, height, 0,
                -h, 0, -h,    0, height, 0,    h, 0, -h,
                -h, 0, -h,   -h, 0,  h,    0, height, 0,
                h, 0, -h,    h, 0,  h,    0, height, 0
        };
    }

    public static float[] generatePyramid(float sideLength) {
        float hTriangle = (float) (Math.sqrt(3) / 2 * sideLength);
        float height = (float) (Math.sqrt(2.0 / 3.0) * sideLength);

        Vector3f v0 = new Vector3f(-sideLength / 2, 0, -hTriangle / 3);
        Vector3f v1 = new Vector3f(sideLength / 2, 0, -hTriangle / 3);
        Vector3f v2 = new Vector3f(0, 0, 2 * hTriangle / 3);
        Vector3f apex = new Vector3f(0, height, 0);

        return new float[]{
                v0.x, v0.y, v0.z,
                v1.x, v1.y, v1.z,
                v2.x, v2.y, v2.z,

                v0.x, v0.y, v0.z,
                v1.x, v1.y, v1.z,
                apex.x, apex.y, apex.z,

                v1.x, v1.y, v1.z,
                v2.x, v2.y, v2.z,
                apex.x, apex.y, apex.z,

                v2.x, v2.y, v2.z,
                v0.x, v0.y, v0.z,
                apex.x, apex.y, apex.z
        };
    }

    public static float[] generatePlayerShip(float size) {
        float s = size;

        Vector3f[] v = {
            new Vector3f(0, 0, -s),
            new Vector3f(0, s*0.2f, -s*0.3f),
            new Vector3f(0, s*0.2f, s*0.2f),
            new Vector3f(-s*0.2f, 0, -s*0.2f),
            new Vector3f(s*0.2f, 0, -s*0.2f),
            new Vector3f(0, -s*0.12f, -s*0.2f),
            new Vector3f(-s*0.25f, 0, s*0.2f),
            new Vector3f(s*0.25f, 0, s*0.2f),
            new Vector3f(0, -s*0.12f, s*0.2f),
            new Vector3f(-s*0.7f, -s*0.02f, 0),
            new Vector3f(s*0.7f, -s*0.02f, 0),
            new Vector3f(-s*0.7f, -s*0.08f, 0),
            new Vector3f(s*0.7f, -s*0.08f, 0),
        };

        int[][] tris = {
            {0, 1, 3},  {0, 4, 1},  {0, 5, 4},  {0, 3, 5},
            {1, 6, 3},  {1, 2, 6},  {4, 2, 1},  {4, 7, 2},
            {3, 6, 5},  {5, 6, 8},  {5, 8, 7},  {5, 7, 4},
            {2, 7, 6},  {6, 7, 8},
            {3, 9, 1},  {3, 6, 9},  {3, 11, 5}, {6, 11, 8},
            {9, 11, 3}, {9, 6, 11},
            {4, 1, 10}, {4, 10, 7}, {4, 12, 5}, {8, 12, 7},
            {10, 4, 12},{10, 12, 7},
        };

        float[] result = new float[tris.length * 9];
        int idx = 0;
        for (int[] tri : tris) {
            for (int vi : tri) {
                result[idx++] = v[vi].x;
                result[idx++] = v[vi].y;
                result[idx++] = v[vi].z;
            }
        }
        return result;
    }
}
