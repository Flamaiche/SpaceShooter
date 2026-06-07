package markershape.shape;

import java.util.*;

/**
 * Converts a float[] array of triangle data (3 vertices × 6 floats each)
 * into ShapeData (vertices, faces, edges) and back.
 *
 * float[] layout (per triangle):
 *   3 vertices × 6 floats each = 18 floats per triangle
 *   Per vertex: [x, y, z, r, g, b]
 *
 * Edges are auto-generated from faces (GL_TRIANGLES).
 */
public class Converter {

    private static final String DEFAULT_EDGE_MODE = "stun";
    private static final float DEFAULT_EDGE_THICKNESS = 0.02f;

    /**
     * Converts a float[] triangle array into a ShapeData.
     *
     * @param shipData the float[] from generatePlayerShip(size)
     * @param shipName the name for the shape
     * @return populated ShapeData with vertices, faces, and edges
     */
    public static ShapeData convert(float[] shipData, String shipName) {
        ShapeData data = new ShapeData();
        data.name = shipName;
        data.shader = "default";

        final int FLOATS_PER_VERTEX = 6;
        final int VERTICES_PER_TRI = 3;
        final int FLOATS_PER_TRI = VERTICES_PER_TRI * FLOATS_PER_VERTEX; // 18

        int vertexId = 0;
        List<int[]> triangleList = new ArrayList<>();
        List<int[]> edgeListRaw = new ArrayList<>();

        for (int i = 0; i < shipData.length; i += FLOATS_PER_TRI) {
            int[] triIndices = new int[VERTICES_PER_TRI];

            for (int v = 0; v < VERTICES_PER_TRI; v++) {
                int base = i + v * FLOATS_PER_VERTEX;
                float x = shipData[base + 0];
                float y = shipData[base + 1];
                float z = shipData[base + 2];
                float r = shipData[base + 3];
                float g = shipData[base + 4];
                float b = shipData[base + 5];

                Vertex vert = new Vertex();
                vert.id = vertexId;
                vert.x = x;
                vert.y = y;
                vert.z = z;
                vert.r = r;
                vert.g = g;
                vert.b = b;
                data.addVertex(vert);
                triIndices[v] = vertexId;
                vertexId++;
            }

            triangleList.add(triIndices);

            int a = triIndices[0];
            int b = triIndices[1];
            int c = triIndices[2];

            edgeListRaw.add(new int[]{a, b});
            edgeListRaw.add(new int[]{b, c});
            edgeListRaw.add(new int[]{c, a});
        }

        for (int[] tri : triangleList) {
            data.faces.add(tri);
        }

        Set<String> seenEdges = new HashSet<>();
        int edgeId = 0;

        for (int[] e : edgeListRaw) {
            int aEdge = e[0];
            int bEdge = e[1];
            int min = Math.min(aEdge, bEdge);
            int max = Math.max(aEdge, bEdge);
            String key = min + "-" + max;

            if (seenEdges.contains(key)) {
                continue;
            }
            seenEdges.add(key);

            Edge edge = new Edge();
            edge.id = edgeId++;
            edge.a = aEdge;
            edge.b = bEdge;
            edge.mode = DEFAULT_EDGE_MODE;
            edge.thickness = DEFAULT_EDGE_THICKNESS;
            data.addEdge(edge);
        }

        return data;
    }

    /**
     * Converts ShapeData back to float[] in the same format.
     *
     * @param data the ShapeData (with vertices and faces)
     * @return float[] array suitable for OpenGL vertex buffers
     */
    public static float[] convertToFloatArray(ShapeData data) {
        final int FLOATS_PER_VERTEX = 6;
        final int VERTICES_PER_TRI = 3;
        final int FLOATS_PER_TRI = VERTICES_PER_TRI * FLOATS_PER_VERTEX; // 18

        int totalFloats = data.faces.size() * FLOATS_PER_TRI;
        float[] result = new float[totalFloats];
        int idx = 0;

        Map<Integer, Vertex> vertexMap = data.vertices;

        for (int[] tri : data.faces) {
            for (int v = 0; v < VERTICES_PER_TRI; v++) {
                int vertId = tri[v];
                Vertex vert = vertexMap.get(vertId);

                if (vert == null) {
                    throw new IllegalStateException(
                            "Vertex id " + vertId + " not found in ShapeData for face " + tri[v]
                    );
                }

                result[idx++] = vert.x;
                result[idx++] = vert.y;
                result[idx++] = vert.z;
                result[idx++] = vert.r;
                result[idx++] = vert.g;
                result[idx++] = vert.b;
            }
        }

        return result;
    }

    /**
     * Convenience: convert and save in one step.
     *
     * @param shipData the float[] from generatePlayerShip(size)
     * @param shipName the shape name (without .json)
     * @param filename the JSON filename (must end with .json)
     * @return true if saved successfully
     */
    public static boolean convertAndSave(float[] shipData, String shipName, String filename) {
        ShapeData data = convert(shipData, shipName);
        return ShapeLoader.save(data, filename);
    }
}
