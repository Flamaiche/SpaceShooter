package gamegl.entites;

import learngl.shape.Shape;
import org.joml.Matrix4f;

/**
 * Abstract base class for all game entities.
 * Provides a common shape, model matrix, and lifecycle methods (update, render, cleanup).
 */
public abstract class Entity {
    protected Shape corps;
    protected final Matrix4f modelMatrix = new Matrix4f();

    /**
     * Updates the entity's state.
     *
     * @param deltaTime time elapsed since the last update
     */
    public abstract void update(float deltaTime);

    /**
     * Renders the entity using the given view and projection matrices.
     *
     * @param view       the view matrix
     * @param projection the projection matrix
     */
    public abstract void render(Matrix4f view, Matrix4f projection);

    /** Releases resources held by this entity. */
    public abstract void cleanup();

    /**
     * Returns the model matrix of this entity.
     *
     * @return the model matrix
     */
    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    /**
     * Returns the body shape of this entity.
     *
     * @return the shape representing this entity's body
     */
    public Shape getBody() {
        return corps;
    }
}
