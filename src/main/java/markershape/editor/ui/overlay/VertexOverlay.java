package markershape.editor.ui.overlay;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.config.ConfigParametres;
import markershape.editor.ui.menu.BlurBackground;
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
    private float[][] siblingBadgePos;

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
        super(280, 320);
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

    @Override public void hide() { super.hide(); vertex = null; siblingIds = null; siblingBadgePos = null; }
    public Vertex getVertex() { return vertex; }
    @Override protected boolean hasEntity() { return vertex != null; }

    public int clickField(float mx, float my) {
        if (!visible || vertex == null) return -1;

        if (isCloseClicked(mx, my)) { hide(); return -1; }

        if (deleteBtn.isClicked(mx, my)) { deleteBtn.click(); return 20; }

        if (siblingIds != null && siblingBadgePos != null && siblingIds.length > 0) {
            for (int i = 0; i < siblingIds.length; i++) {
                float sx = siblingBadgePos[i][0], sy = siblingBadgePos[i][1];
                String label = "[#" + siblingIds[i] + "]";
                float[] ext = Text.getTextExtent(label, 1.5f);
                if (mx >= sx && mx <= sx + ext[0] && my >= sy && my <= sy + 22) {
                    if (switchCallback != null) switchCallback.accept(siblingIds[i]);
                    return 10;
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
        float mr = BlurBackground.menuR;
        float mg = BlurBackground.menuG;
        float mb = BlurBackground.menuB;
        drawLine(buf, px + 10, sepY, px + pw - 10, sepY, mr + 0.15f, mg + 0.15f, mb + 0.2f, 0.9f);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_LINES, 0, 2);
    }

    @Override
    protected void renderText(Shader textShader) {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        float dimR = tR * 0.7f, dimG = tG * 0.7f, dimB = tB * 0.7f;
        float dim2R = tR * 0.5f, dim2G = tG * 0.5f, dim2B = tB * 0.5f;

        Text.drawText(textShader, "Vertex #" + vertex.id, px + 12, py + 10, 1.5f, tR, tG, tB);

        for (int i = 0; i < fieldYOff.length; i++) {
            float fy = py + fieldYOff[i];
            float val = getFieldValue(i);
            String fmt = i <= 2 ? "%.3f" : "%.2f";
            boolean sel = (i == selectedField);

            Text.drawText(textShader, fieldLabels[i], px + 12, fy, 1.5f, tR, tG, tB);
            Text.drawText(textShader, String.format(fmt, val), px + VAL_X, fy, 1.5f, sel ? tR : dimR, sel ? tG : dimG, sel ? tB : dimB);
            Text.drawText(textShader, "[-]", px + MINUS_X, fy + 1, 1.5f, tR, tG, tB);
            Text.drawText(textShader, "[+]", px + PLUS_X, fy + 1, 1.5f, tR, tG, tB);
        }

        Text.drawText(textShader, "Edges: " + edgeCount, px + 12, py + 186, 1.5f, dimR, dimG, dimB);

        if (siblingIds != null && siblingIds.length > 0) {
            float[] labelExt = Text.getTextExtent("Also:", 1.5f);
            float baseY = py + 210;
            Text.drawText(textShader, "Also:", px + 12, baseY, 1.5f, dimR, dimG, dimB);
            float bx = px + 12 + labelExt[0] + 4;
            float by = baseY;
            float maxX = px + pw - 12;
            int row = 0;
            siblingBadgePos = new float[siblingIds.length][2];
            for (int i = 0; i < siblingIds.length; i++) {
                String label = "[#" + siblingIds[i] + "]";
                float[] ext = Text.getTextExtent(label, 1.5f);
                if (bx + ext[0] > maxX) {
                    bx = px + 12;
                    by = baseY + (++row) * 22;
                }
                siblingBadgePos[i][0] = bx;
                siblingBadgePos[i][1] = by;
                Text.drawText(textShader, label, bx, by, 1.5f, dim2R, dim2G, dim2B);
                bx += ext[0] + 4;
            }
        }
    }
}
