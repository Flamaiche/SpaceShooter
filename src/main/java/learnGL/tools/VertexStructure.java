package learnGL.tools;

import java.util.*;
import org.joml.Vector3f;

public class VertexStructure {

    private List<Vector3f> vertices = new ArrayList<>();
    private List<int[]> triangles = new ArrayList<>();
    private Map<Integer, Set<Integer>> voisins = new HashMap<>();
    private List<float[]> colors = new ArrayList<>();

    private static final float EPSILON = 1e-6f;

    public VertexStructure(float[] flatVertices) {
        buildFromFlatArray(flatVertices);
    }

    public VertexStructure() {}

    private void buildFromFlatArray(float[] verts) {
        int vertexCount = verts.length / Shape.FLOATS_PER_VERTEX;

        for (int i = 0; i < vertexCount; i++) {
            Vector3f v = new Vector3f(
                    verts[i * Shape.FLOATS_PER_VERTEX],
                    verts[i * Shape.FLOATS_PER_VERTEX + 1],
                    verts[i * Shape.FLOATS_PER_VERTEX + 2]
            );
            addVertex(v, new float[]{verts[i * Shape.FLOATS_PER_VERTEX + 3],
                    verts[i * Shape.FLOATS_PER_VERTEX + 4],
                    verts[i * Shape.FLOATS_PER_VERTEX + 5]});
        }

        for (int i = 0; i < vertexCount; i += 3) {
            if (i + 2 < vertices.size()) {
                addTriangle(i, i + 1, i + 2);
            }
        }
    }

    public void addVertex(Vector3f v, float[] color) {
        int index = vertices.size();
        vertices.add(v);
        colors.add(color.clone());
        voisins.put(index, new HashSet<>());
    }

    private void addTriangle(int a, int b, int c) {
        triangles.add(new int[]{a, b, c});

        voisins.get(a).add(b); voisins.get(a).add(c);
        voisins.get(b).add(a); voisins.get(b).add(c);
        voisins.get(c).add(a); voisins.get(c).add(b);
    }

    public void addVertex(Vector3f v, List<Vector3f> connectTo) {
        int idx = vertices.size();
        vertices.add(v);
        colors.add(new float[]{1f, 1f, 1f});
        voisins.put(idx, new HashSet<>());

        if (connectTo != null && connectTo.size() >= 2) {
            for (int i = 0; i < connectTo.size() - 1; i++) {
                int a = vertices.indexOf(connectTo.get(i));
                int b = vertices.indexOf(connectTo.get(i + 1));
                if (a != -1 && b != -1) addTriangle(idx, a, b);
            }
            int last = vertices.indexOf(connectTo.get(connectTo.size() - 1));
            int first = vertices.indexOf(connectTo.get(0));
            if (last != -1 && first != -1) addTriangle(idx, last, first);
        }
    }

    public void rebuildVoisins() {
        voisins.clear();
        for (int i = 0; i < vertices.size(); i++) voisins.put(i, new HashSet<>());
        for (int[] t : triangles) {
            voisins.get(t[0]).add(t[1]); voisins.get(t[0]).add(t[2]);
            voisins.get(t[1]).add(t[0]); voisins.get(t[1]).add(t[2]);
            voisins.get(t[2]).add(t[0]); voisins.get(t[2]).add(t[1]);
        }
    }

    public float[] toFlatVertexArray() {
        float[] array = new float[triangles.size() * 3 * Shape.FLOATS_PER_VERTEX];
        int k = 0;
        for (int[] t : triangles) {
            for (int i : t) {
                Vector3f v = vertices.get(i);
                float[] c = colors.get(i);
                array[k++] = v.x;
                array[k++] = v.y;
                array[k++] = v.z;
                array[k++] = c[0]; // r
                array[k++] = c[1]; // g
                array[k++] = c[2]; // b
                array[k++] = 0f;   // u
                array[k++] = 0f;   // v
            }
        }
        return array;
    }

    public void setVertexColor(int vertexIndex, float r, float g, float b) {
        if (vertexIndex < 0 || vertexIndex >= colors.size()) return;
        colors.set(vertexIndex, new float[]{r, g, b});
    }

    public void setAllColors(float r, float g, float b) {
        for (int i = 0; i < colors.size(); i++) {
            colors.set(i, new float[]{r, g, b});
        }
    }

    public int vertexCount() { return vertices.size(); }

    public int triangleCount() { return triangles.size(); }

    public Vector3f getVertex(int index) { return vertices.get(index); }

    public List<Vector3f> getVerticesList() {
        return new ArrayList<>(vertices);
    }

    public Set<Integer> getVoisins(int index) { return voisins.get(index); }

    public float[] getColor(int index) { return colors.get(index); }
}
