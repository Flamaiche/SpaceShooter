package markershape.editor.ui.overlay;

import markershape.config.ConfigParametres;
import markershape.editor.ui.UIResources;
import markershape.shape.Edge;

public class EdgeOverlay extends Overlay {
    private Edge edge;
    private int vertexA, vertexB;

    private static final float VAL_X = 90;
    private static final float VAL_W = 70;
    private static final float MINUS_X = 170;
    private static final float PLUS_X = 192;
    private static final float BTN_W = 18;
    private static final float MODE_X = 68;

    public EdgeOverlay(UIResources res) {
        super(res, 280, 240);
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

        if (deleteBtn.contains(mx, my)) { deleteBtn.click(mx, my); return 10; }

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
    protected void renderContent() {
        if (selectedField == 1) {
            float sy = py + 120;
            res.drawQuad(px + VAL_X, sy, VAL_W, 20, 0.3f, 0.5f, 0.9f, 0.3f);
        }
    }

    @Override
    protected void renderText() {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        float dimR = tR * 0.7f, dimG = tG * 0.7f, dimB = tB * 0.7f;

        res.drawText("Edge #" + edge.id, px + 12, py + 10, 1.5f, tR, tG, tB);
        res.drawText("Vertex A: " + vertexA, px + 12, py + 42, 1.5f, tR, tG, tB);
        res.drawText("Vertex B: " + vertexB, px + 12, py + 66, 1.5f, tR, tG, tB);

        String modeStr = edge.mode.equals("stun") ? "stun" : "move";
        res.drawText("Mode: " + modeStr, px + 12, py + 90, 1.5f, dimR, dimG, dimB);

        float tcR = (selectedField == 1) ? tR : dimR;
        float tcG = (selectedField == 1) ? tG : dimG;
        float tcB = (selectedField == 1) ? tB : dimB;
        res.drawText("Thick:", px + 12, py + 120, 1.5f, tR, tG, tB);
        res.drawText(String.format("%.3f", edge.thickness),
            px + VAL_X, py + 120, 1.5f, tcR, tcG, tcB);
        res.drawText("[-]", px + MINUS_X, py + 121, 1.5f, tR, tG, tB);
        res.drawText("[+]", px + PLUS_X, py + 121, 1.5f, tR, tG, tB);
    }
}