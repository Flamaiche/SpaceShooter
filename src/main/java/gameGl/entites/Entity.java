package gameGl.entites;

import learnGL.tools.Shape;
import org.joml.Matrix4f;

public abstract class Entity {
    protected Shape corps;
    protected final Matrix4f modelMatrix = new Matrix4f();

    public abstract void update(float deltaTime);
    public abstract void render(Matrix4f view, Matrix4f projection);
    public abstract void cleanup();
    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }
    public Shape getCorps() {
        return corps;
    }
}
