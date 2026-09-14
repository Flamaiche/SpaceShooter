package markershape.shape.render;

import learngl.Shader;
import markershape.shape.ShapeData;
import org.joml.Matrix4f;

public interface Renderer {
    void render(Shader shader, ShapeData data, Matrix4f view, Matrix4f projection, int screenW, int screenH);
    void cleanup();
}