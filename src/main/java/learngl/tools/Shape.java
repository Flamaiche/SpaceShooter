package learngl.tools;

import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Shape {
    private float[] vertices;
    private final int vaoId;
    private final int vboId;
    private int vertexCount;
    public static int drawMode = GL_TRIANGLES;

    private Shader shader = null;
    private Texture texture = null;

    public Shape(float[] vertices) {
        this.vertexCount = vertices.length / VertexUtils.FLOATS_PER_VERTEX;
        this.vertices = vertices;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        updateBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public Shape(float[] logicalVertices, int logicalWidth, int logicalHeight) {
        this(VertexUtils.convertLogicalToNormalized(logicalVertices, logicalWidth, logicalHeight));
    }

    public void setShader(Shader shader) { this.shader = shader; }
    public Shader getShader() { return shader; }

    public void setTexture(Texture texture) { this.texture = texture; }
    public Texture getTexture() { return texture; }

    public float[] getVertices() { return vertices; }

    public void render() {
        if (texture != null) texture.bind();

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glDrawArrays(drawMode, 0, vertexCount);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);

        if (texture != null) texture.unbind();
    }

    public boolean isVisible(Matrix4f projection, Matrix4f view, Matrix4f modelMatrix) {
        Matrix4f vpMatrix = new Matrix4f(projection).mul(view);

        for (int i = 0; i < vertexCount; i++) {
            Vector4f pos = new Vector4f(
                    vertices[i * VertexUtils.FLOATS_PER_VERTEX],
                    vertices[i * VertexUtils.FLOATS_PER_VERTEX + 1],
                    vertices[i * VertexUtils.FLOATS_PER_VERTEX + 2],
                    1.0f
            );
            pos.mul(modelMatrix).mul(vpMatrix);

            if (pos.w != 0.0f) {
                pos.x /= pos.w;
                pos.y /= pos.w;
                pos.z /= pos.w;
            }

            if (pos.x >= -1f && pos.x <= 1f &&
                pos.y >= -1f && pos.y <= 1f &&
                pos.z >= -1f && pos.z <= 1f) return true;
        }
        return false;
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void updateBuffers() {
        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int stride = VertexUtils.FLOATS_PER_VERTEX * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public float[] center() {
        float[] center = new float[3];
        for (int i = 0; i < vertexCount; i++) {
            center[0] += vertices[i * VertexUtils.FLOATS_PER_VERTEX];
            center[1] += vertices[i * VertexUtils.FLOATS_PER_VERTEX + 1];
            center[2] += vertices[i * VertexUtils.FLOATS_PER_VERTEX + 2];
        }
        center[0] /= vertexCount;
        center[1] /= vertexCount;
        center[2] /= vertexCount;
        return center;
    }

    public Shape clone() {
        Shape clone = new Shape(vertices.clone());
        clone.setShader(this.shader);
        clone.setTexture(this.texture);
        return clone;
    }

    public void setColor(float r, float g, float b) {
        for (int i = 0; i < vertexCount; i++) {
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 3] = r;
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 4] = g;
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 5] = b;
        }
        updateBuffers();
    }

    public void updatePositions(float[] newVertices) {
        if (newVertices.length != vertices.length)
            throw new IllegalArgumentException("Le tableau de vertices doit avoir la même taille !");
        for (int i = 0; i < vertexCount; i++) {
            vertices[i * VertexUtils.FLOATS_PER_VERTEX]     = newVertices[i * VertexUtils.FLOATS_PER_VERTEX];
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 1] = newVertices[i * VertexUtils.FLOATS_PER_VERTEX + 1];
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 2] = newVertices[i * VertexUtils.FLOATS_PER_VERTEX + 2];
        }
        updateBuffers();
    }

    public void setScale(float scale) {
        for (int i = 0; i < vertexCount; i++) {
            vertices[i * VertexUtils.FLOATS_PER_VERTEX]     *= scale;
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 1] *= scale;
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 2] *= scale;
        }
        updateBuffers();
    }

    // Delegated collision methods
    public boolean intersectsOptimized(Shape other, Matrix4f modelA, Matrix4f modelB) {
        return MeshCollider.intersectsOptimized(this.vertices, other.vertices, modelA, modelB);
    }

    public float intersectRayDistance(Vector3f origin, Vector3f dir, Matrix4f model) {
        return MeshCollider.intersectRayDistance(vertices, origin, dir, model);
    }
}
