package markershape.editor.ui.overlay;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.shape.Edge;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class EdgeOverlay extends Overlay {
    private Edge edge;
    private int vertexA, vertexB;

    private static final float VAL_X = 90;
    private static final float VAL_W = 70;
    private static final float MINUS_X = 170;
    private static final float PLUS_X = 192;
    private static final float BTN_W = 18;
    private static final float MODE_X = 68;

    public EdgeOverlay() {
        super(280, 240);
    }

    public void show(Edge e, int va, int vb) {
        edge = e;
        vertexA = va;
        vertexB = vb;
        visible = true;
        selectedField = -1;
    }

    @Override public void hide() { super.hide(); edge = null; }
    public Edge getEdge() { return edge; }
    @Override protected boolean hasEntity() { return edge != null; }

    public int clickField(float mx, float my) {
        if (!visible || edge == null) return -1;

        if (isCloseClicked(mx, my)) { hide(); return -1; }

        if (deleteBtn.isClicked(mx, my)) { deleteBtn.click(); return 10; }

        float modeY = py + 90;
        if (my >= modeY && my <= modeY + 20) {
            if (mx >= px + MODE_X && mx <= px + pw - 12) {
                if (preEditCallback != null) preEditCallback.run();
                edge.mode = edge.mode.equals("stun") ? "move" : "stun";
                selectedField = -1;
                if (editCallback != null) editCallback.run();
                return 0;
            }
        }

        float thickY = py + 120;
        if (my >= thickY && my <= thickY + 20) {
            if (mx >= px + MINUS_X && mx <= px + MINUS_X + BTN_W) {
                if (preEditCallback != null) preEditCallback.run();
                edge.thickness = Math.max(0.001f, edge.thickness - 0.02f);
                selectedField = 1;
                if (editCallback != null) editCallback.run();
                return 1;
            }
            if (mx >= px + PLUS_X && mx <= px + PLUS_X + BTN_W) {
                if (preEditCallback != null) preEditCallback.run();
                edge.thickness = Math.min(10f, edge.thickness + 0.02f);
                selectedField = 1;
                if (editCallback != null) editCallback.run();
                return 1;
            }
            if (mx >= px + VAL_X && mx <= px + VAL_X + VAL_W) {
                selectedField = (selectedField == 1) ? -1 : 1;
                return 1;
            }
        }

        selectedField = -1;
        return -1;
    }

    @Override
    protected void renderContent(Shader uiShader, Shader textShader, Matrix4f ortho,
                                 FloatBuffer buf, int vao, int vbo) {
        if (selectedField == 1) {
            float sy = py + 120;
            drawHighlightRect(buf, px + VAL_X, sy, VAL_W, 20);
            glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }
    }

    @Override
    protected void renderText(Shader textShader) {
        Text.drawText(textShader, "Edge #" + edge.id, px + 12, py + 10, 1.5f, 1f, 1f, 1f);
        Text.drawText(textShader, "Vertex A: " + vertexA, px + 12, py + 42, 1.5f, 0.8f, 0.8f, 1f);
        Text.drawText(textShader, "Vertex B: " + vertexB, px + 12, py + 66, 1.5f, 0.8f, 0.8f, 1f);

        String modeStr = edge.mode.equals("stun") ? "stun" : "move";
        Text.drawText(textShader, "Mode: " + modeStr, px + 12, py + 90, 1.5f, 0.8f, 0.8f, 0.8f);

        float tc = (selectedField == 1) ? 1f : 0.8f;
        Text.drawText(textShader, "Thick:", px + 12, py + 120, 1.5f, 0.8f, 0.8f, 1f);
        Text.drawText(textShader, String.format("%.3f", edge.thickness),
            px + VAL_X, py + 120, 1.5f, tc, tc, tc);
        Text.drawText(textShader, "[-]", px + MINUS_X, py + 121, 1.5f, 0.8f, 0.8f, 1f);
        Text.drawText(textShader, "[+]", px + PLUS_X, py + 121, 1.5f, 0.8f, 0.8f, 1f);
    }
}
