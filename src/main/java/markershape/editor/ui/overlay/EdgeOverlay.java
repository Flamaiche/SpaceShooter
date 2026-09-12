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

        float modeY = y + 90;
        if (my >= modeY && my <= modeY + 20) {
            if (mx >= x + MODE_X && mx <= x + w - 12) {
                if (preEditCallback != null) preEditCallback.run();
                edge.mode = edge.mode.equals("stun") ? "move" : "stun";
                selectedField = -1;
                if (editCallback != null) editCallback.run();
                return 0;
            }
        }

        float thickY = y + 120;
        if (my >= thickY && my <= thickY + 20) {
            if (mx >= x + MINUS_X && mx <= x + MINUS_X + BTN_W) {
                if (preEditCallback != null) preEditCallback.run();
                edge.thickness = Math.max(0.001f, edge.thickness - 0.02f);
                selectedField = 1;
                if (editCallback != null) editCallback.run();
                return 1;
            }
            if (mx >= x + PLUS_X && mx <= x + PLUS_X + BTN_W) {
                if (preEditCallback != null) preEditCallback.run();
                edge.thickness = Math.min(10f, edge.thickness + 0.02f);
                selectedField = 1;
                if (editCallback != null) editCallback.run();
                return 1;
            }
            if (mx >= x + VAL_X && mx <= x + VAL_X + VAL_W) {
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
            float sy = y + 120;
            res.drawQuad(x + VAL_X, sy, VAL_W, 20, 0.3f, 0.5f, 0.9f, 0.3f);
        }
    }

    @Override
    protected void renderText() {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        float dimR = tR * 0.7f, dimG = tG * 0.7f, dimB = tB * 0.7f;

        res.drawText("Edge #" + edge.id, x + 12, y + 10, 1.5f, tR, tG, tB);
        res.drawText("Vertex A: " + vertexA, x + 12, y + 42, 1.5f, tR, tG, tB);
        res.drawText("Vertex B: " + vertexB, x + 12, y + 66, 1.5f, tR, tG, tB);

        String modeStr = edge.mode.equals("stun") ? "stun" : "move";
        res.drawText("Mode: " + modeStr, x + 12, y + 90, 1.5f, dimR, dimG, dimB);

        float tcR = (selectedField == 1) ? tR : dimR;
        float tcG = (selectedField == 1) ? tG : dimG;
        float tcB = (selectedField == 1) ? tB : dimB;
        res.drawText("Thick:", x + 12, y + 120, 1.5f, tR, tG, tB);
        res.drawText(String.format("%.3f", edge.thickness),
            x + VAL_X, y + 120, 1.5f, tcR, tcG, tcB);
        res.drawText("[-]", x + MINUS_X, y + 121, 1.5f, tR, tG, tB);
        res.drawText("[+]", x + PLUS_X, y + 121, 1.5f, tR, tG, tB);
    }
}