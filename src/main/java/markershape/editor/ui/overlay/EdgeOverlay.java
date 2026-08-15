package markershape.editor.ui.overlay;

import markershape.config.ConfigParametres;
import markershape.editor.ui.framework.UIContainer;
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

        if (isDeleteClicked(mx, my)) {
            if (deleteCallback != null) deleteCallback.run();
            return 10;
        }

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
    protected void buildContent(UIContainer panel) {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        float dimR = tR * 0.7f, dimG = tG * 0.7f, dimB = tB * 0.7f;

        panel.add(label(12, 10, "Edge #" + edge.id, tR, tG, tB));
        panel.add(label(12, 42, "Vertex A: " + vertexA, tR, tG, tB));
        panel.add(label(12, 66, "Vertex B: " + vertexB, tR, tG, tB));

        String modeStr = edge.mode.equals("stun") ? "stun" : "move";
        panel.add(label(12, 90, "Mode: " + modeStr, dimR, dimG, dimB));

        float tcR = (selectedField == 1) ? tR : dimR;
        float tcG = (selectedField == 1) ? tG : dimG;
        float tcB = (selectedField == 1) ? tB : dimB;
        panel.add(label(12, 120, "Thick:", tR, tG, tB));
        if (selectedField == 1) panel.add(box(VAL_X, 120, VAL_W, 20, 0.3f, 0.5f, 0.9f, 0.3f));
        panel.add(label(VAL_X, 120, String.format("%.3f", edge.thickness), tcR, tcG, tcB));
        panel.add(label(MINUS_X, 121, "[-]", tR, tG, tB));
        panel.add(label(PLUS_X, 121, "[+]", tR, tG, tB));
    }
}
