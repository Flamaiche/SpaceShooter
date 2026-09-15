package markershape.editor.ui.overlay;

import markershape.config.ConfigParametres;
import markershape.editor.action.AngleUtils;
import markershape.editor.ui.UIResources;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;

import java.util.List;
import java.util.function.Consumer;

public class VertexOverlay extends Overlay {
    private Vertex vertex;
    private int edgeCount;
    private int[] siblingIds;
    private float[][] siblingBadgePos;
    private ShapeData data;

    private static final int FIELD_X = 0, FIELD_Y = 1, FIELD_Z = 2;
    private static final String[] fieldLabels = {"X", "Y", "Z"};
    private static final float[] fieldYOff = {34, 60, 86};
    private static final float FIELD_H = 20;

    private static final float VAL_X = 34;
    private static final float VAL_W = 90;
    private static final float BTN_W = 18;
    private static final float MINUS_X = 144;
    private static final float PLUS_X = 166;

    private static final float SEP_Y = 122;
    private static final float EDGES_Y = 132;
    private static final float ANGLE_HEAD_Y = 154;
    private static final float ANGLE_ROW_START = 176;
    private static final float ANGLE_ROW_H = 22;
    private static final int MAX_ANGLE_ROWS = 6;
    private static final float ANGLE_VAL_X = 58;
    private static final float ANGLE_VAL_W = 74;
    private static final float ANGLE_MINUS_X = 146;
    private static final float ANGLE_PLUS_X = 170;
    private static final float SIB_BASE_Y = 168;
    private static final float SIB_PAD = 10;
    private static final float SIB_ROW_H = 20;
    private static final float BOTTOM_PAD = 46;

    private Consumer<Integer> switchCallback;

    private boolean typing;
    private String typedOld;
    private final StringBuilder typedBuf = new StringBuilder();
    private long editStart;
    private int selectedAngle = -1;

    private List<AngleUtils.AdjacentAngle> angleRows;
    private int angleRowCount;

    public VertexOverlay(UIResources res) {
        super(res, 280, 320);
    }

    public void setSwitchCallback(Consumer<Integer> cb) { switchCallback = cb; }

    public void show(Vertex v, int edges) { show(v, edges, new int[0], null); }

    public void show(Vertex v, int edges, int[] siblingIds) { show(v, edges, siblingIds, null); }

    public void show(Vertex v, int edges, int[] siblingIds, ShapeData data) {
        vertex = v;
        edgeCount = edges;
        this.siblingIds = siblingIds != null ? siblingIds : new int[0];
        this.data = data;
        visible = true;
        selectedField = -1;
        selectedAngle = -1;
        typing = false;
        angleRows = null;
        angleRowCount = 0;
        recomputeAngles();
        updateHeight();
    }

    @Override public void hide() {
        super.hide();
        vertex = null;
        siblingIds = null;
        siblingBadgePos = null;
        data = null;
        angleRows = null;
        angleRowCount = 0;
        typing = false;
        selectedAngle = -1;
    }

    public Vertex getVertex() { return vertex; }
    @Override protected boolean hasEntity() { return vertex != null; }
    public boolean isTyping() { return visible && typing; }

    private void recomputeAngles() {
        angleRows = null;
        angleRowCount = 0;
        if (data == null || vertex == null) return;
        List<AngleUtils.AdjacentAngle> fan = AngleUtils.fan(data, vertex.id);
        if (fan.isEmpty()) return;
        angleRows = fan;
        angleRowCount = fan.size();
    }

    private int angleRowsShown() {
        return Math.min(Math.max(0, angleRowCount), MAX_ANGLE_ROWS);
    }

    private int angleBlockRows() {
        int shown = angleRowsShown();
        return angleRowCount > shown ? shown + 1 : shown;
    }

    private float sibTop() {
        return (angleRowCount > 0)
            ? ANGLE_ROW_START + angleBlockRows() * ANGLE_ROW_H + SIB_PAD
            : SIB_BASE_Y + SIB_PAD;
    }

    private int layoutSiblingLines() {
        if (siblingIds == null || siblingIds.length == 0) return 0;
        String also = "Also:";
        float labelExt = res.getTextExtent(also, 1.1f)[0];
        float bx = x + 12 + labelExt + 4;
        float maxX = x + w - 12;
        int row = 0;
        for (int i = 0; i < siblingIds.length; i++) {
            String label = "[#" + siblingIds[i] + "]";
            float ext = res.getTextExtent(label, 1.1f)[0];
            if (bx + ext > maxX) {
                bx = x + 12;
                row++;
            }
            bx += ext + 4;
        }
        return row + 1;
    }

    private void updateHeight() {
        float contentEnd = (angleRowCount > 0)
            ? ANGLE_ROW_START + angleBlockRows() * ANGLE_ROW_H
            : SIB_BASE_Y;
        int sibLines = layoutSiblingLines();
        float sibH = sibLines > 0 ? SIB_PAD + sibLines * SIB_ROW_H : 0;
        h = contentEnd + sibH + BOTTOM_PAD;
    }

    protected void beginTyping(int field) {
        selectedField = field;
        selectedAngle = -1;
        typing = true;
        typedOld = String.format("%.3f", getFieldValue(field));
        typedBuf.setLength(0);
        editStart = System.currentTimeMillis();
    }

    private void beginAngleTyping(int row) {
        if (angleRows == null || row >= angleRows.size()) return;
        selectedField = -1;
        selectedAngle = row;
        typing = true;
        typedOld = String.format("%.1f", angleRows.get(row).degrees());
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
        if (key == 257) { confirmTyping(); return true; }          // ENTER
        if (key == 256) { cancelTyping(); return true; }            // ESCAPE
        if (key == 259 && typedBuf.length() > 0) {                  // BACKSPACE
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
                if (selectedField >= 0) {
                    if (preEditCallback != null) preEditCallback.run();
                    setFieldValue(selectedField, v);
                    if (editCallback != null) editCallback.run();
                } else if (selectedAngle >= 0 && angleRows != null
                           && selectedAngle < angleRows.size() && vertex != null && data != null) {
                    AngleUtils.AdjacentAngle row = angleRows.get(selectedAngle);
                    if (preEditCallback != null) preEditCallback.run();
                    AngleUtils.setAngle(data, vertex.id, row.otherA(), row.otherB(), v);
                    if (editCallback != null) editCallback.run();
                    recomputeAngles();
                }
            } catch (NumberFormatException ignored) { }
        }
        typing = false;
        selectedField = -1;
        selectedAngle = -1;
    }

    private void cancelTyping() {
        typing = false;
        selectedField = -1;
        selectedAngle = -1;
    }

    private void stepAngle(int row, int dir) {
        if (angleRows == null || row >= angleRows.size() || vertex == null || data == null) return;
        AngleUtils.AdjacentAngle a = angleRows.get(row);
        float target = Math.max(1f, Math.min(179f, a.degrees() + dir * 1f));
        if (preEditCallback != null) preEditCallback.run();
        AngleUtils.setAngle(data, vertex.id, a.otherA(), a.otherB(), target);
        selectedAngle = row;
        if (editCallback != null) editCallback.run();
        recomputeAngles();
    }

    public int clickField(float mx, float my) {
        if (!visible || vertex == null) return -1;

        if (isCloseClicked(mx, my)) { hide(); return -1; }

        if (deleteBtn.contains(mx, my)) { deleteBtn.click(mx, my); return 20; }

        if (siblingIds != null && siblingBadgePos != null && siblingIds.length > 0) {
            for (int i = 0; i < siblingIds.length; i++) {
                float sx = siblingBadgePos[i][0], sy = siblingBadgePos[i][1];
                String label = "[#" + siblingIds[i] + "]";
                float[] ext = res.getTextExtent(label, 1.1f);
                if (mx >= sx && mx <= sx + ext[0] && my >= sy && my <= sy + 20) {
                    if (switchCallback != null) switchCallback.accept(siblingIds[i]);
                    return 10;
                }
            }
        }

        recomputeAngles();

        for (int i = 0; i < fieldYOff.length; i++) {
            float by = y + fieldYOff[i];
            if (my < by || my > by + FIELD_H) continue;

            if (mx >= x + MINUS_X && mx <= x + MINUS_X + BTN_W) {
                confirmTyping();
                if (preEditCallback != null) preEditCallback.run();
                setFieldValue(i, getFieldValue(i) - 0.1f);
                selectedField = i;
                if (editCallback != null) editCallback.run();
                return i;
            }
            if (mx >= x + PLUS_X && mx <= x + PLUS_X + BTN_W) {
                confirmTyping();
                if (preEditCallback != null) preEditCallback.run();
                setFieldValue(i, getFieldValue(i) + 0.1f);
                selectedField = i;
                if (editCallback != null) editCallback.run();
                return i;
            }
            if (mx >= x + VAL_X && mx <= x + VAL_X + VAL_W) {
                if (selectedField == i) { confirmTyping(); }
                else { beginTyping(i); }
                return i;
            }
        }

        if (angleRows != null) {
            int shown = angleRowsShown();
            for (int i = 0; i < shown; i++) {
                float ay = y + ANGLE_ROW_START + i * ANGLE_ROW_H;
                if (my < ay || my > ay + 20) continue;
                if (mx >= x + ANGLE_MINUS_X && mx <= x + ANGLE_MINUS_X + BTN_W) {
                    stepAngle(i, -1);
                    return 30 + i;
                }
                if (mx >= x + ANGLE_PLUS_X && mx <= x + ANGLE_PLUS_X + BTN_W) {
                    stepAngle(i, 1);
                    return 30 + i;
                }
                if (mx >= x + ANGLE_VAL_X && mx <= x + ANGLE_VAL_X + ANGLE_VAL_W) {
                    if (selectedAngle == i) { confirmTyping(); }
                    else { beginAngleTyping(i); }
                    return 30 + i;
                }
            }
        }

        selectedField = -1;
        selectedAngle = -1;
        return -1;
    }

    private float getFieldValue(int i) {
        return switch (i) {
            case 0 -> vertex.x; case 1 -> vertex.y; case 2 -> vertex.z;
            default -> 0;
        };
    }

    private void setFieldValue(int i, float v) {
        switch (i) {
            case 0 -> vertex.x = clamp(v, -100f, 100f);
            case 1 -> vertex.y = clamp(v, -100f, 100f);
            case 2 -> vertex.z = clamp(v, -100f, 100f);
        }
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    protected void renderContent() {
        recomputeAngles();
        if (selectedField >= 0) {
            float sy = y + fieldYOff[selectedField];
            res.drawQuad(x + VAL_X, sy, VAL_W, 20, 0.3f, 0.5f, 0.9f, 0.3f);
        }
        if (selectedAngle >= 0 && angleRows != null) {
            float ay = y + ANGLE_ROW_START + selectedAngle * ANGLE_ROW_H;
            res.drawQuad(x + ANGLE_VAL_X, ay, ANGLE_VAL_W, 20, 0.3f, 0.5f, 0.9f, 0.3f);
        }

        float sepY = y + SEP_Y;
        float[] c = res.menuColor();
        res.drawLine(x + 10, sepY, x + w - 10, sepY, c[0] + 0.15f, c[1] + 0.15f, c[2] + 0.2f, 0.9f);
    }

    @Override
    protected void renderText() {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        float dimR = tR * 0.7f, dimG = tG * 0.7f, dimB = tB * 0.7f;
        float dim2R = tR * 0.5f, dim2G = tG * 0.5f, dim2B = tB * 0.5f;

        res.drawText("Vertex #" + vertex.id, x + 12, y + 8, 1.2f, tR, tG, tB);

        for (int i = 0; i < fieldYOff.length; i++) {
            float fy = y + fieldYOff[i];
            boolean sel = (i == selectedField);

            res.drawText(fieldLabels[i], x + 12, fy, 1.2f, tR, tG, tB);

            if (typing && sel) {
                String display = typedBuf.toString();
                long elapsed = System.currentTimeMillis() - editStart;
                if ((elapsed / 500) % 2 == 0) display += "|";
                res.drawText(display, x + VAL_X, fy, 1.2f, tR, tG, tB);
            } else {
                float val = getFieldValue(i);
                res.drawText(String.format("%.3f", val), x + VAL_X, fy, 1.2f, sel ? tR : dimR, sel ? tG : dimG, sel ? tB : dimB);
            }
            res.drawText("[-]", x + MINUS_X, fy + 1, 1.2f, tR, tG, tB);
            res.drawText("[+]", x + PLUS_X, fy + 1, 1.2f, tR, tG, tB);
        }

        res.drawText("Edges: " + edgeCount, x + 12, y + EDGES_Y, 1.1f, dimR, dimG, dimB);

        if (angleRowCount > 0) {
            res.drawText("Angles (" + angleRowCount + ")", x + 12, y + ANGLE_HEAD_Y, 1.1f, dimR, dimG, dimB);
            int shown = angleRowsShown();
            for (int i = 0; i < shown; i++) {
                AngleUtils.AdjacentAngle a = angleRows.get(i);
                float ay = y + ANGLE_ROW_START + i * ANGLE_ROW_H;
                boolean sel = (i == selectedAngle);
                res.drawText("E" + a.edgeA() + "/E" + a.edgeB(), x + 12, ay, 1.2f, tR * 0.9f, tG * 0.9f, tB * 0.9f);
                if (typing && sel) {
                    String display = typedBuf.toString();
                    long elapsed = System.currentTimeMillis() - editStart;
                    if ((elapsed / 500) % 2 == 0) display += "|";
                    res.drawText(display, x + ANGLE_VAL_X, ay, 1.2f, tR, tG, tB);
                } else {
                    res.drawText(String.format("%.1f deg", a.degrees()), x + ANGLE_VAL_X, ay, 1.2f,
                        sel ? tR : dimR, sel ? tG : dimG, sel ? tB : dimB);
                }
                res.drawText("[-]", x + ANGLE_MINUS_X, ay + 1, 1.2f, tR, tG, tB);
                res.drawText("[+]", x + ANGLE_PLUS_X, ay + 1, 1.2f, tR, tG, tB);
            }
            if (angleRowCount > shown) {
                res.drawText("+" + (angleRowCount - shown) + " autres",
                    x + 12, y + ANGLE_ROW_START + shown * ANGLE_ROW_H, 1.1f, dim2R, dim2G, dim2B);
            }
        }

        if (siblingIds != null && siblingIds.length > 0) {
            float[] labelExt = res.getTextExtent("Also:", 1.1f);
            float baseY = y + sibTop();
            res.drawText("Also:", x + 12, baseY, 1.1f, dimR, dimG, dimB);
            float bx = x + 12 + labelExt[0] + 4;
            float by = baseY;
            float maxX = x + w - 12;
            int row = 0;
            siblingBadgePos = new float[siblingIds.length][2];
            for (int i = 0; i < siblingIds.length; i++) {
                String label = "[#" + siblingIds[i] + "]";
                float[] ext = res.getTextExtent(label, 1.1f);
                if (bx + ext[0] > maxX) {
                    bx = x + 12;
                    by = baseY + (++row) * SIB_ROW_H;
                }
                siblingBadgePos[i][0] = bx;
                siblingBadgePos[i][1] = by;
                res.drawText(label, bx, by, 1.1f, dim2R, dim2G, dim2B);
                bx += ext[0] + 4;
            }
        }
    }
}