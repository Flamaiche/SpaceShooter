package learngl.shape;

import learngl.Shader;
import learngl.Texture;
import learngl.VertexUtils;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Represents a renderable 3D shape with vertex data, managed through OpenGL buffers.
 * Supports optional shader and texture binding, visibility frustum checks, collision
 * detection via MeshCollider, cloning, color modification, and scaling.
 */
public class Shape {
    private final float[] vertices;
    private final int vaoId;
    private final int vboId;
    private final int vertexCount;

    /**
     * The OpenGL draw mode used when rendering this shape.
     */
    public static int drawMode = GL_TRIANGLES;

    private Shader shader = null;
    private Texture texture = null;

    /**
     * Creates a shape from an array of normalized vertex data.
     * Each vertex consists of FLOATS_PER_VERTEX floats (position, color, texture coordinates).
     *
     * @param vertices the vertex data array
     */
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

    /**
     * Sets the shader program used to render this shape.
     *
     * @param shader the shader to use, or null to unset
     */
    public void setShader(Shader shader) { this.shader = shader; }

    /**
     * Returns the currently assigned shader.
     *
     * @return the shader, or null if none is set
     */
    public Shader getShader() { return shader; }

    /**
     * Sets the texture to apply to this shape.
     *
     * @param texture the texture to use, or null to unset
     */
    public void setTexture(Texture texture) { this.texture = texture; }

    /**
     * Renders the shape by binding its VAO, enabling vertex attribute arrays,
     * issuing a draw call, and then cleaning up state. Binds the texture if present.
     */
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

    /**
     * Tests whether this shape is visible within the view frustum defined by the
     * combined projection-view-model matrix. Checks each vertex in normalized
     * device coordinate space.
     *
     * @param projection the projection matrix
     * @param view       the view matrix
     * @param modelMatrix the model matrix
     * @return true if at least one vertex lies within the clip space [-1, 1]
     */
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

    /**
     * Releases all OpenGL resources associated with this shape (VAO, VBO).
     */
    public void cleanup() {
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    /**
     * Re-uploads the vertex data to the GPU and re-specifies the vertex attribute
     * pointers for position (3 floats), color (3 floats), and texture coordinates (2 floats).
     */
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

    /**
     * Creates a deep copy of this shape, including its vertex data, shader, and texture.
     *
     * @return a new Shape instance with cloned data
     */
    public Shape clone() {
        Shape clone = new Shape(vertices.clone());
        clone.setShader(this.shader);
        clone.setTexture(this.texture);
        return clone;
    }

    /**
     * Sets the color of every vertex to the given RGB values.
     *
     * @param r the red component (0.0 - 1.0)
     * @param g the green component (0.0 - 1.0)
     * @param b the blue component (0.0 - 1.0)
     */
    public void setColor(float r, float g, float b) {
        for (int i = 0; i < vertexCount; i++) {
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 3] = r;
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 4] = g;
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 5] = b;
        }
        updateBuffers();
    }

    /**
     * Updates only the position components (x, y, z) of each vertex while keeping
     * color and texture data unchanged.
     *
     * @param newVertices an array containing the new position data; must match the current vertex count
     * @throws IllegalArgumentException if the array length differs from the current vertex data length
     */
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

    /**
     * Uniformly scales the position (x, y, z) of every vertex by the given factor.
     *
     * @param scale the scale factor
     */
    public void setScale(float scale) {
        for (int i = 0; i < vertexCount; i++) {
            vertices[i * VertexUtils.FLOATS_PER_VERTEX]     *= scale;
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 1] *= scale;
            vertices[i * VertexUtils.FLOATS_PER_VERTEX + 2] *= scale;
        }
        updateBuffers();
    }

    /**
     * Tests whether this shape's mesh intersects another shape's mesh using
     * an optimized AABB + triangle-triangle intersection test.
     *
     * @param other the other shape to test against
     * @param modelA the model matrix for this shape
     * @param modelB the model matrix for the other shape
     * @return true if the meshes intersect
     */
    public boolean intersectsOptimized(Shape other, Matrix4f modelA, Matrix4f modelB) {
        return MeshCollider.intersectsOptimized(this.vertices, other.vertices, modelA, modelB);
    }

    /**
     * Casts a ray against this shape's mesh and returns the distance to the nearest
     * intersection, or -1 if no intersection occurs.
     *
     * @param origin the ray origin in world space
     * @param dir    the ray direction in world space
     * @param model  the model matrix for this shape
     * @return the distance to the nearest intersection, or -1 if none
     */
    public float intersectRayDistance(Vector3f origin, Vector3f dir, Matrix4f model) {
        return MeshCollider.intersectRayDistance(vertices, origin, dir, model);
    }
}
