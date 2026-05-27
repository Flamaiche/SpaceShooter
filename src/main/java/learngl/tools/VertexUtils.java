package learngl.tools;

public final class VertexUtils {

    public static final int FLOATS_PER_VERTEX = 8;

    private VertexUtils() {}

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

    public static float[] autoAddSlotTexture(float[] vertices) {
        int vertexCount = vertices.length / 6;
        float[] full = new float[vertexCount * FLOATS_PER_VERTEX];
        for (int i = 0; i < vertexCount; i++) {
            full[i * 8]     = vertices[i * 6];
            full[i * 8 + 1] = vertices[i * 6 + 1];
            full[i * 8 + 2] = vertices[i * 6 + 2];
            full[i * 8 + 3] = vertices[i * 6 + 3];
            full[i * 8 + 4] = vertices[i * 6 + 4];
            full[i * 8 + 5] = vertices[i * 6 + 5];
            full[i * 8 + 6] = 0f;
            full[i * 8 + 7] = 0f;
        }
        return full;
    }

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
