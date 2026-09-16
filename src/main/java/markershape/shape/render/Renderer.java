package markershape.shape.render;

import learngl.Shader;
import markershape.shape.ShapeData;
import org.joml.Matrix4f;

/** Common contract for 3D renderers that draw shape data each frame and release resources on cleanup. */
public interface Renderer {
    /** Draws the given shape data with the supplied shader and view/projection matrices. */
    void render(Shader shader, ShapeData data, Matrix4f view, Matrix4f projection, int screenW, int screenH);
    /** Releases all GPU resources held by this renderer. */
    void cleanup();
}