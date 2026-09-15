package markershape.editor.ui.overlay;

import markershape.config.ConfigParametres;
import markershape.editor.ui.UIResources;
import markershape.shape.Edge;

public class EdgeOverlay extends Overlay {
    private Edge edge;
    private int vertexA, vertexB;

    private static final float VAL_X = 90;
    private static final float VAL_W = 64;
    private static final float MINUS_X = 164;
    private static final float PLUS_X = 186;
    private static final float BTN_W = 18;
    private static final float SWATCH_X = 212;
    private static final float SWATCH_W = 52;
    private static final float MODE_X = 68;

    private static final int MODE_ROWS = 4;
    private static final float[] COLOR_Y = {150, 176, 202};

    public EdgeOverlay(UIResources res) {
        super(res, 280, 270);
    }

    private boolean typing;
    private String typedOld;
    private final StringBuilder typedBuf = new StringBuilder();
    private long editStart;

    public void show(Edge e, int va, int vb) {
        edge = e;
        vertexA = va;
        vertexB = vb;
        visible = true;
        selectedField = -1;
        typing = false;
    }

    @Override public void hide() { super.hide(); edge = null; typing = false; }
    public Edge getEdge() { return edge; }
    @Override protected boolean hasEntity() { return edge != null; }
    public boolean isTyping() { return visible && typing; }

    private void beginTyping(int field) {
        selectedField = field;
        typing = true;
        typedOld = String.format(field == 1 ? "%.3f" : "%.2f", getFieldValue(field));
        typedBuf.setLength(0);
        editStart = System.currentTimeMillis();
    }

    public void charTyped(int codepoint) {
        if (!isTyping()) return;
        char c = (char) codepoint;
        if (Character.isDigit(c) || c == '-' || c == '.') {
            typedBuf.append(c);
            editStart = System.currentTimeMillis();
        }
    }

    public boolean keyTyped(int key, int action) {
        if (!isTyping() || action != 1) return false;
        if (key == 257) { confirmTyping(); return true; }
        if (key == 256) { cancelTyping(); return true; }
        if (key == 259 && typedBuf.length() > 0) {
            typedBuf.deleteCharAt(typedBuf.length() - 1);
            editStart = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private void confirmTyping() {
        if (!typing) return;
        String s = typedBuf.toString();
        if (!s.isEmpty()) {
            try {
                float v = Float.parseFloat(s);
                if (preEditCallback != null) preEditCallback.run();
                setFieldValue(selectedField, v);
                if (editCallback != null) editCallback.run();
            } catch (NumberFormatException ignored) { }
        }
        typing = false;
        selectedField = -1;
    }

    private void cancelTyping() {
        typing = false;
        selectedField = -1;
    }

    private float getFieldValue(int field) {
        if (edge == null) return 0;
        return switch (field) {
            case 1 -> edge.thickness;
            case 2 -> edge.r;
            case 3 -> edge.g;
            case 4 -> edge.bl;
            default -> 0;
        };
    }

    private void setFieldValue(int field, float v) {
        if (edge == null) return;
        switch (field) {
            case 1 -> edge.thickness = Math.max(0.001f, Math.min(10f, v));
            case 2 -> edge.r = Math.max(0f, Math.min(1f, v));
            case 3 -> edge.g = Math.max(0f, Math.min(1f, v));
            case 4 -> edge.bl = Math.max(0f, Math.min(1f, v));
        }
    }

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
                confirmTyping();
                if (preEditCallback != null) preEditCallback.run();
                setFieldValue(1, edge.thickness - 0.02f);
                selectedField = 1;
                if (editCallback != null) editCallback.run();
                return 1;
            }
            if (mx >= x + PLUS_X && mx <= x + PLUS_X + BTN_W) {
                confirmTyping();
                if (preEditCallback != null) preEditCallback.run();
                setFieldValue(1, edge.thickness + 0.02f);
                selectedField = 1;
                if (editCallback != null) editCallback.run();
                return 1;
            }
            if (mx >= x + VAL_X && mx <= x + VAL_X + VAL_W) {
                if (selectedField == 1) confirmTyping();
                else beginTyping(1);
                return 1;
            }
        }

        for (int i = 0; i < COLOR_Y.length; i++) {
            int field = i + 2;
            float cy = y + COLOR_Y[i];
            if (my < cy || my > cy + 20) continue;
            if (mx >= x + MINUS_X && mx <= x + MINUS_X + BTN_W) {
                confirmTyping();
                if (preEditCallback != null) preEditCallback.run();
                setFieldValue(field, getFieldValue(field) - 0.05f);
                selectedField = field;
                if (editCallback != null) editCallback.run();
                return field;
            }
            if (mx >= x + PLUS_X && mx <= x + PLUS_X + BTN_W) {
                confirmTyping();
                if (preEditCallback != null) preEditCallback.run();
                setFieldValue(field, getFieldValue(field) + 0.05f);
                selectedField = field;
                if (editCallback != null) editCallback.run();
                return field;
            }
            if (mx >= x + VAL_X && mx <= x + VAL_X + VAL_W) {
                if (selectedField == field) confirmTyping();
                else beginTyping(field);
                return field;
            }
        }

        selectedField = -1;
        return -1;
    }

    @Override
    protected void renderContent() {
        if (edge == null) return;
        if (selectedField == 1) {
            float sy = y + 120;
            res.drawQuad(x + VAL_X, sy, VAL_W, 20, 0.3f, 0.5f, 0.9f, 0.3f);
        }
        for (int i = 0; i < COLOR_Y.length; i++) {
            int field = i + 2;
            if (selectedField == field) {
                res.drawQuad(x + VAL_X, y + COLOR_Y[i], VAL_W, 20, 0.3f, 0.5f, 0.9f, 0.3f);
            }
        }
        float[] c = res.menuColor();
        res.drawLine(x + 10, y + 142, x + w - 10, y + 142, c[0] + 0.15f, c[1] + 0.15f, c[2] + 0.2f, 0.9f);
        res.drawQuad(x + SWATCH_X, y + 146, SWATCH_W, 16, edge.r, edge.g, edge.bl, 1f);
    }

    @Override
    protected void renderText() {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        float dimR = tR * 0.7f, dimG = tG * 0.7f, dimB = tB * 0.7f;

        res.drawText("Edge #" + edge.id, x + 12, y + 10, 1.2f, tR, tG, tB);
        res.drawText("Vertex A: " + vertexA, x + 12, y + 42, 1.2f, tR, tG, tB);
        res.drawText("Vertex B: " + vertexB, x + 12, y + 66, 1.2f, tR, tG, tB);

        String modeStr = edge.mode.equals("stun") ? "stun" : "move";
        res.drawText("Mode: " + modeStr, x + 12, y + 90, 1.2f, dimR, dimG, dimB);

        float tcR = (selectedField == 1) ? tR : dimR;
        float tcG = (selectedField == 1) ? tG : dimG;
        float tcB = (selectedField == 1) ? tB : dimB;
        res.drawText("Thick:", x + 12, y + 120, 1.2f, tR, tG, tB);
        if (typing && selectedField == 1) {
            String display = typedBuf.toString();
            long elapsed = System.currentTimeMillis() - editStart;
            if ((elapsed / 500) % 2 == 0) display += "|";
            res.drawText(display, x + VAL_X, y + 120, 1.2f, tR, tG, tB);
        } else {
            res.drawText(String.format("%.3f", edge.thickness),
                x + VAL_X, y + 120, 1.2f, tcR, tcG, tcB);
        }
        res.drawText("[-]", x + MINUS_X, y + 121, 1.2f, tR, tG, tB);
        res.drawText("[+]", x + PLUS_X, y + 121, 1.2f, tR, tG, tB);

        res.drawText("Color:", x + 12, y + 148, 1.1f, dimR, dimG, dimB);
        char[] labels = {'R', 'G', 'B'};
        for (int i = 0; i < COLOR_Y.length; i++) {
            int field = i + 2;
            float cy = y + COLOR_Y[i];
            float cR = (selectedField == field) ? tR : dimR;
            float cG = (selectedField == field) ? tG : dimG;
            float cB = (selectedField == field) ? tB : dimB;
            res.drawText(String.valueOf(labels[i]), x + 12, cy, 1.2f, tR, tG, tB);
            if (typing && selectedField == field) {
                String display = typedBuf.toString();
                long elapsed = System.currentTimeMillis() - editStart;
                if ((elapsed / 500) % 2 == 0) display += "|";
                res.drawText(display, x + VAL_X, cy, 1.2f, tR, tG, tB);
            } else {
                res.drawText(String.format("%.2f", getFieldValue(field)),
                    x + VAL_X, cy, 1.2f, cR, cG, cB);
            }
            res.drawText("[-]", x + MINUS_X, cy + 1, 1.2f, tR, tG, tB);
            res.drawText("[+]", x + PLUS_X, cy + 1, 1.2f, tR, tG, tB);
        }
    }
}