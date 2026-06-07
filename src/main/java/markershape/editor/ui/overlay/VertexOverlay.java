package markershape.editor.ui.overlay;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.shape.Vertex;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class VertexOverlay extends Overlay {
    private Vertex vertex;
    private int edgeCount;
    private int[] siblingIds;
    private float[] siblingBadgeX;

    private static final int FIELD_X = 0, FIELD_Y = 1, FIELD_Z = 2;
    private static final int FIELD_R = 3, FIELD_G = 4, FIELD_B = 5;
    private static final String[] fieldLabels = {"X", "Y", "Z", "R", "G", "B"};
    private static final float[] fieldYOff = {34, 58, 82, 110, 134, 158};

    private static final float VAL_X = 34;
    private static final float VAL_W = 90;
    private static final float BTN_W = 18;
    private static final float MINUS_X = 144;
    private static final float PLUS_X = 166;

    private Consumer<Integer> switchCallback;

    public VertexOverlay() {
        super(280, 290);
    }

    public void setSwitchCallback(Consumer<Integer> cb) { switchCallback = cb; }

    public void show(Vertex v, int edges) { show(v, edges, new int[0]); }

    public void show(Vertex v, int edges, int[] siblingIds) {
        vertex = v;
        edgeCount = edges;
        this.siblingIds = siblingIds != null ? siblingIds : new int[0];
        visible = true;
        selectedField = -1;
    }

    @Override public void hide() { super.hide(); vertex = null; siblingIds = null; siblingBadgeX = null; }
    public Vertex getVertex() { return vertex; }
    @Override protected boolean hasEntity() { return vertex != null; }

    public int clickField(float mx, float my) {
        if (!visible || vertex == null) return -1;

        if (deleteBtn.isClicked(mx, my)) { deleteBtn.click(); return 20; }

        if (siblingIds != null && siblingBadgeX != null && siblingIds.length > 0) {
            float sy = py + 210;
            if (my >= sy && my <= sy + 22) {
                for (int i = 0; i < siblingIds.length; i++) {
                    String label = "[#" + siblingIds[i] + "]";
                    float[] ext = Text.getTextExtent(label, 1.3f);
                    if (mx >= siblingBadgeX[i] && mx <= siblingBadgeX[i] + ext[0]) {
                        if (switchCallback != null) switchCallback.accept(siblingIds[i]);
                        return 10;
                    }
                }
            }
        }

        for (int i = 0; i < fieldYOff.length; i++) {
            float by = py + fieldYOff[i];
            if (my < by || my > by + 20) continue;

            if (mx >= px + MINUS_X && mx <= px + MINUS_X + BTN_W) {
                if (preEditCallback != null) preEditCallback.run();
                float step = i <= 2 ? 0.1f : 0.05f;
                setFieldValue(i, getFieldValue(i) - step);
                selectedField = i;
                if (editCallback != null) editCallback.run();
                return i;
            }
            if (mx >= px + PLUS_X && mx <= px + PLUS_X + BTN_W) {
                if (preEditCallback != null) preEditCallback.run();
                float step = i <= 2 ? 0.1f : 0.05f;
                setFieldValue(i, getFieldValue(i) + step);
                selectedField = i;
                if (editCallback != null) editCallback.run();
                return i;
            }
            if (mx >= px + VAL_X && mx <= px + VAL_X + VAL_W) {
                selectedField = (selectedField == i) ? -1 : i;
                return i;
            }
        }
        selectedField = -1;
        return -1;
    }

    private float getFieldValue(int i) {
        return switch (i) {
            case 0 -> vertex.x; case 1 -> vertex.y; case 2 -> vertex.z;
            case 3 -> vertex.r; case 4 -> vertex.g; case 5 -> vertex.b;
            default -> 0;
        };
    }

    private void setFieldValue(int i, float v) {
        switch (i) {
            case 0 -> vertex.x = clamp(v, -100f, 100f);
            case 1 -> vertex.y = clamp(v, -100f, 100f);
            case 2 -> vertex.z = clamp(v, -100f, 100f);
            case 3 -> vertex.r = clamp(v, 0f, 1f);
            case 4 -> vertex.g = clamp(v, 0f, 1f);
            case 5 -> vertex.b = clamp(v, 0f, 1f);
        }
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    protected void renderContent(Shader uiShader, Shader textShader, Matrix4f ortho,
                                 FloatBuffer buf, int vao, int vbo) {
        if (selectedField >= 0) {
            float sy = py + fieldYOff[selectedField];
            drawHighlightRect(buf, px + VAL_X, sy, VAL_W, 20);
            glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
            glDrawArrays(GL_TRIANGLES, 0, 6);
        }

        float sepY = py + fieldYOff[0] - 4;
        drawLine(buf, px + 10, sepY, px + pw - 10, sepY, 0.3f, 0.3f, 0.4f, 1f);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINES, 0, 2);
    }

    @Override
    protected void renderText(Shader textShader) {
        Text.drawText(textShader, "Vertex #" + vertex.id, px + 12, py + 10, 1.5f, 1f, 1f, 1f);

        for (int i = 0; i < fieldYOff.length; i++) {
            float fy = py + fieldYOff[i];
            float val = getFieldValue(i);
            String fmt = i <= 2 ? "%.3f" : "%.2f";
            boolean sel = (i == selectedField);
            float tc = sel ? 1f : 0.8f;
            float ta = sel ? 1f : 0.8f;

            Text.drawText(textShader, fieldLabels[i], px + 12, fy, 1.3f, 0.8f, 0.8f, 1f);
            Text.drawText(textShader, String.format(fmt, val), px + VAL_X, fy, 1.3f, tc, tc, ta);
            Text.drawText(textShader, "[-]", px + MINUS_X, fy + 1, 1.2f, 0.8f, 0.8f, 1f);
            Text.drawText(textShader, "[+]", px + PLUS_X, fy + 1, 1.2f, 0.8f, 0.8f, 1f);
        }

        Text.drawText(textShader, "Edges: " + edgeCount, px + 12, py + 186, 1.3f, 0.8f, 0.8f, 0.8f);

        if (siblingIds != null && siblingIds.length > 0) {
            float[] labelExt = Text.getTextExtent("Also:", 1.3f);
            Text.drawText(textShader, "Also:", px + 12, py + 210, 1.3f, 0.8f, 0.8f, 0.8f);
            float bx = px + 12 + labelExt[0] + 4;
            siblingBadgeX = new float[siblingIds.length];
            for (int i = 0; i < siblingIds.length; i++) {
                String label = "[#" + siblingIds[i] + "]";
                siblingBadgeX[i] = bx;
                Text.drawText(textShader, label, bx, py + 210, 1.3f, 0.7f, 0.7f, 0.7f);
                float[] ext = Text.getTextExtent(label, 1.3f);
                bx += ext[0] + 4;
            }
        }
    }
}
