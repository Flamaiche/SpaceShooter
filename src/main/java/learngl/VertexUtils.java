package learngl;

/**
 * Utility class for vertex data manipulation.
 * Provides methods to convert between vertex formats and to transform
 * logical (screen-space) coordinates into normalized device coordinates.
 */
public final class VertexUtils {

    /**
     * The number of float components per vertex: 3 position + 3 color + 2 texture coordinates.
     */
    public static final int FLOATS_PER_VERTEX = 8;

    private VertexUtils() {}

    /**
     * Expands a position-only vertex array (x, y, z) into the full vertex format
     * by adding default white color (1, 1, 1) and leaving texture coordinates as 0.
     *
     * @param vertices array of (x, y, z) triplets
     * @return a new array in the format [x, y, z, 1, 1, 1, 0, 0]
     */
    public static float[] autoAddSlotColor(float[] vertices) {
        int vertexCount = vertices.length / 3;
        float[] full = new float[vertexCount * FLOATS_PER_VERTEX];
        for (int i = 0; i < vertexCount; i++) {
            full[i * FLOATS_PER_VERTEX]     = vertices[i * 3];
            full[i * FLOATS_PER_VERTEX + 1] = vertices[i * 3 + 1];
            full[i * FLOATS_PER_VERTEX + 2] = vertices[i * 3 + 2];
            full[i * FLOATS_PER_VERTEX + 3] = 1.0f;
            full[i * FLOATS_PER_VERTEX + 4] = 1.0f;
            full[i * FLOATS_PER_VERTEX + 5] = 1.0f;
        }
        return full;
    }

    /**
     * Expands a position+color vertex array (x, y, z, r, g, b) into the full
     * vertex format by adding default texture coordinates (0, 0).
     *
     * @param vertices array of (x, y, z, r, g, b) sextuplets
     * @return a new array in the format [x, y, z, r, g, b, 0, 0]
     */
    public static float[] autoAddSlotTexture(float[] vertices) {
        int vertexCount = vertices.length / 6;
        float[] full = new float[vertexCount * FLOATS_PER_VERTEX];
        for (int i = 0; i < vertexCount; i++) {
            full[i * FLOATS_PER_VERTEX]     = vertices[i * 6];
            full[i * FLOATS_PER_VERTEX + 1] = vertices[i * 6 + 1];
            full[i * FLOATS_PER_VERTEX + 2] = vertices[i * 6 + 2];
            full[i * FLOATS_PER_VERTEX + 3] = vertices[i * 6 + 3];
            full[i * FLOATS_PER_VERTEX + 4] = vertices[i * 6 + 4];
            full[i * FLOATS_PER_VERTEX + 5] = vertices[i * 6 + 5];
            full[i * FLOATS_PER_VERTEX + 6] = 0f;
            full[i * FLOATS_PER_VERTEX + 7] = 0f;
        }
        return full;
    }

    /**
     * Converts vertex data from logical (screen-space) pixel coordinates to
     * normalized device coordinates (range [-1, 1]).
     *
     * @param logicalVertices vertex data in logical pixel coordinates
     * @param logicalWidth    the logical reference width
     * @param logicalHeight   the logical reference height
     * @return a new array with normalized coordinates
     */
    public static float[] convertLogicalToNormalized(float[] logicalVertices, int logicalWidth, int logicalHeight) {
        float[] normalized = new float[logicalVertices.length];
        int vertexCount = logicalVertices.length / FLOATS_PER_VERTEX;
        for (int i = 0; i < vertexCount; i++) {
            float x = logicalVertices[i * FLOATS_PER_VERTEX];
            float y = logicalVertices[i * FLOATS_PER_VERTEX + 1];
            normalized[i * FLOATS_PER_VERTEX]     = (x / (logicalWidth  / 2f)) - 1f;
            normalized[i * FLOATS_PER_VERTEX + 1] = (y / (logicalHeight / 2f)) - 1f;
            normalized[i * FLOATS_PER_VERTEX + 2] = logicalVertices[i * FLOATS_PER_VERTEX + 2];
            normalized[i * FLOATS_PER_VERTEX + 3] = logicalVertices[i * FLOATS_PER_VERTEX + 3];
            normalized[i * FLOATS_PER_VERTEX + 4] = logicalVertices[i * FLOATS_PER_VERTEX + 4];
            normalized[i * FLOATS_PER_VERTEX + 5] = logicalVertices[i * FLOATS_PER_VERTEX + 5];
            normalized[i * FLOATS_PER_VERTEX + 6] = logicalVertices[i * FLOATS_PER_VERTEX + 6];
            normalized[i * FLOATS_PER_VERTEX + 7] = logicalVertices[i * FLOATS_PER_VERTEX + 7];
        }
        return normalized;
    }
}
