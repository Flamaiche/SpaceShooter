package markershape.shape.render;

import learngl.Shader;
import markershape.shape.ShapeData;

public interface Renderer {
    void render(Shader shader, ShapeData data);
    void cleanup();
}
