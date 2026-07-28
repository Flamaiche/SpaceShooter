package markershape.editor.ui.menu;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.config.ConfigParametres;
import markershape.editor.ui.widgets.EditableTextField;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class ParametresUI {
    private int width, height;
    private Shader shader, textShader;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    private static final int MENU_X = 440;
    private static final int MENU_W = 400;
    private static final int CAT_H = 42;
    private static final int CAT_GAP = 6;
    private static final int ROW_H = 32;
    private static final int ROW_GAP = 4;

    private int currentMenu = -1;
    private final Runnable onBack;
    private Runnable onApply;
    public boolean visible;
    private final EditableTextField hexField;
    private final EditableTextField floatField;
    private String editingFloatKey;

    public ParametresUI(Runnable onBack) {
        this.onBack = onBack;
        this.hexField = new EditableTextField("#000000", EditableTextField.ValueType.HEX_COLOR, 0, 0);
        this.floatField = new EditableTextField("0", EditableTextField.ValueType.FLOAT, 0, 0);
        shader = new Shader("shaders/markershape/ui_Vertex.glsl",
                             "shaders/markershape/ui_Fragment.glsl");
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 6 * 4, 2 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void setSize(int w, int h) { width = w; height = h; ortho.setOrtho2D(0, width, height, 0); }
    public void loadFromConfig() { ConfigParametres.recharger(); currentMenu = -1; }
    public void setOnApply(Runnable r) { onApply = r; }

    public void render() {
        if (!visible) return;
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        drawQuad(0, 0, width, height, 0.08f, 0.08f, 0.1f, 0.85f);

        if (currentMenu < 0) renderCategories();
        else renderSubMenu();
    }

    private List<Integer> getVisibleCategoryIndices() {
        ConfigParametres cfg = ConfigParametres.get();
        List<Integer> idxs = new ArrayList<>();
        if (cfg.categories == null) return idxs;
        for (int i = 0; i < cfg.categories.size(); i++) {
            if (cfg.categories.get(i).isVisible(cfg)) idxs.add(i);
        }
        return idxs;
    }

    private void renderCategories() {
        ConfigParametres cfg = ConfigParametres.get();
        List<Integer> visible = getVisibleCategoryIndices();
        List<ConfigParametres.Categorie> cats = cfg.categories;
        if (cats == null) return;

        Text.drawText(textShader, "Parametres",
            width / 2f - Text.getTextExtent("Parametres", 3f)[0] / 2f, 50, 3f, 1f, 1f, 1f);

        float sy = 130;
        for (int vi = 0; vi < visible.size(); vi++) {
            ConfigParametres.Categorie cat = cats.get(visible.get(vi));
            float y = sy + vi * (CAT_H + CAT_GAP);
            drawQuad(MENU_X, y, MENU_W, CAT_H, 0.15f, 0.15f, 0.2f, 0.7f);
            Text.drawText(textShader, cat.label + "  >", MENU_X + 16, y + 10, 2f, 0.8f, 0.85f, 1f);
        }

        float by = sy + visible.size() * (CAT_H + CAT_GAP) + 20;
        drawButton(width / 2f - 210, by, 200, 38, "Sauvegarder", 0.2f, 0.3f, 0.2f);
        drawButton(width / 2f + 10, by, 200, 38, "Retour", 0.25f, 0.25f, 0.3f);
    }

    private void renderSubMenu() {
        ConfigParametres cfg = ConfigParametres.get();
        List<ConfigParametres.Categorie> cats = cfg.categories;
        if (cats == null || currentMenu < 0 || currentMenu >= cats.size()) return;
        ConfigParametres.Categorie cat = cats.get(currentMenu);
        if (cat.params == null) return;

        Text.drawText(textShader, cat.label,
            width / 2f - Text.getTextExtent(cat.label, 3f)[0] / 2f, 50, 3f, 1f, 1f, 1f);

        float sy = 110;
        int preambleRows = 0;

        if (hasColorPicker(cat.id)) {
            String prefix = "arriereplan".equals(cat.id) ? "bg" : "menu";
            float vr = cfg.getFloat(prefix + "R"), vg = cfg.getFloat(prefix + "G"), vb = cfg.getFloat(prefix + "B");
            drawQuad(MENU_X + 30, sy, MENU_W - 60, 70, vr, vg, vb, 1f);

            String hex = String.format("#%02X%02X%02X", (int)(vr*255), (int)(vg*255), (int)(vb*255));
            float hx = width / 2f - Text.getTextExtent(hex, 2f)[0] / 2f;
            hexField.setText(hex);
            hexField.setPosition(hx, sy + 26);
            hexField.setScale(2f);
            hexField.setColor(1f - vr, 1f - vg, 1f - vb);
            hexField.setOnConfirm(newHex -> {
                int nr = Integer.parseInt(newHex.substring(1, 3), 16);
                int ng = Integer.parseInt(newHex.substring(3, 5), 16);
                int nb = Integer.parseInt(newHex.substring(5, 7), 16);
                float fr = nr / 255f, fg = ng / 255f, fb = nb / 255f;
                cfg.setFloat(prefix + "R", fr);
                cfg.setFloat(prefix + "G", fg);
                cfg.setFloat(prefix + "B", fb);
                if ("arriereplan".equals(cat.id)) {
                    cfg.setFloat("menuR", fr);
                    cfg.setFloat("menuG", fg);
                    cfg.setFloat("menuB", fb);
                }
                hexField.setText(hexField.getText());
            });
            hexField.render(textShader);
            preambleRows = 2;
        }

        int rendered = 0;
        for (ConfigParametres.Param p : cat.params) {
            if (!p.isVisible(cfg)) continue;
            float y = sy + preambleRows * (ROW_H + ROW_GAP) + rendered * (ROW_H + ROW_GAP);

            if ("bool".equals(p.type)) {
                drawBoolRow(y, p);
            } else {
                drawFloatRow(y, p);
                if (p.key.equals(editingFloatKey)) {
                    float vx = MENU_X + MENU_W / 2f + 20;
                    float val = cfg.getFloat(p.key);
                    String display = fmtNum(val);
                    floatField.setText(display);
                    floatField.setPosition(vx + 2, y + 6);
                    floatField.setScale(1.7f);
                    floatField.setBounds(p.min, p.max);
                    floatField.setColor(1f, 1f, 0f);
                    floatField.setOnConfirm(newVal -> {
                        try { cfg.setFloat(p.key, Float.parseFloat(newVal)); }
                        catch (NumberFormatException ignored) {}
                        editingFloatKey = null;
                    });
                    floatField.render(textShader);
                }
            }
            rendered++;
        }

        float by = sy + preambleRows * (ROW_H + ROW_GAP) + rendered * (ROW_H + ROW_GAP) + 20;
        drawButton(width / 2f - 100, by, 200, 38, "Retour", 0.25f, 0.25f, 0.3f);
    }

    public void click(float mx, float my) {
        if (!visible) return;
        if (currentMenu < 0) clickCategories(mx, my);
        else clickSubMenu(mx, my);
    }

    private void clickCategories(float mx, float my) {
        hexField.cancelEditing();
        floatField.cancelEditing();
        editingFloatKey = null;
        List<Integer> visibleIdxs = getVisibleCategoryIndices();
        ConfigParametres cfg = ConfigParametres.get();
        if (cfg.categories == null) return;

        float sy = 130;
        for (int vi = 0; vi < visibleIdxs.size(); vi++) {
            int idx = visibleIdxs.get(vi);
            float y = sy + vi * (CAT_H + CAT_GAP);
            if (mx >= MENU_X && mx <= MENU_X + MENU_W && my >= y && my <= y + CAT_H) {
                currentMenu = idx;
                return;
            }
        }
        float by = sy + visibleIdxs.size() * (CAT_H + CAT_GAP) + 20;
        if (my >= by && my <= by + 38) {
            if (mx >= width / 2f - 210 && mx <= width / 2f - 10) {
                ConfigParametres.sauvegarder();
                if (onApply != null) onApply.run();
                this.visible = false;
                if (onBack != null) onBack.run();
                return;
            }
            if (mx >= width / 2f + 10 && mx <= width / 2f + 210) {
                if (onApply != null) onApply.run();
                this.visible = false;
                if (onBack != null) onBack.run();
            }
        }
    }

    private void clickSubMenu(float mx, float my) {
        ConfigParametres cfg = ConfigParametres.get();
        List<ConfigParametres.Categorie> cats = cfg.categories;
        if (cats == null || currentMenu < 0 || currentMenu >= cats.size()) return;
        ConfigParametres.Categorie cat = cats.get(currentMenu);
        if (cat.params == null) return;

        if (hasColorPicker(cat.id) && hexField.click(mx, my)) return;

        float sy = 110;
        int preambleRows = hasColorPicker(cat.id) ? 2 : 0;
        int rendered = 0;

        for (ConfigParametres.Param p : cat.params) {
            if (!p.isVisible(cfg)) continue;
            float y = sy + preambleRows * (ROW_H + ROW_GAP) + rendered * (ROW_H + ROW_GAP);

            if ("bool".equals(p.type)) {
                if (mx >= MENU_X && mx <= MENU_X + MENU_W && my >= y && my <= y + ROW_H) {
                    cfg.setBool(p.key, !cfg.getBool(p.key));
                    return;
                }
            } else {
                float val = cfg.getFloat(p.key);
                float vx = MENU_X + MENU_W / 2f + 20;

                boolean onMinus = mx >= vx - 30 && mx <= vx - 6 && my >= y && my <= y + ROW_H;
                boolean onPlus  = mx >= vx + Text.getTextExtent(fmtNum(val), 1.7f)[0] + 6
                               && mx <= vx + Text.getTextExtent(fmtNum(val), 1.7f)[0] + 30
                               && my >= y && my <= y + ROW_H;

                if (onMinus && val > p.min) {
                    cfg.setFloat(p.key, val - p.step);
                    editingFloatKey = null;
                    return;
                }
                if (onPlus && val < p.max) {
                    cfg.setFloat(p.key, val + p.step);
                    editingFloatKey = null;
                    return;
                }

                if (!p.key.equals(editingFloatKey) || !floatField.isEditing()) {
                    float[] ext = Text.getTextExtent(fmtNum(val), 1.7f);
                    if (mx >= vx && mx <= vx + ext[0] + 4 && my >= y && my <= y + ROW_H) {
                        editingFloatKey = p.key;
                        floatField.setText(fmtNum(val));
                        floatField.setPosition(vx + 2, y + 6);
                        floatField.setScale(1.7f);
                        floatField.setBounds(p.min, p.max);
                        floatField.setOnConfirm(newVal -> {
                            try { cfg.setFloat(p.key, Float.parseFloat(newVal)); }
                            catch (NumberFormatException ignored) {}
                            editingFloatKey = null;
                        });
                        floatField.activate();
                        return;
                    }
                }
            }
            rendered++;
        }

        float by = sy + preambleRows * (ROW_H + ROW_GAP) + rendered * (ROW_H + ROW_GAP) + 20;
        if (my >= by && my <= by + 38 && mx >= width / 2f - 100 && mx <= width / 2f + 100) {
            hexField.cancelEditing();
            floatField.cancelEditing();
            editingFloatKey = null;
            currentMenu = -1;
        }
    }

    public void handleKey(int key, int action) {
        if (hexField.isEditing()) {
            hexField.keyAction(key, action);
        } else if (floatField.isEditing()) {
            floatField.keyAction(key, action);
            if (!floatField.isEditing()) editingFloatKey = null;
        }
    }

    public void handleChar(int codepoint) {
        if (hexField.isEditing()) {
            hexField.keyChar(codepoint);
        } else if (floatField.isEditing()) {
            floatField.keyChar(codepoint);
        }
    }

    private boolean hasColorPicker(String catId) {
        return "arriereplan".equals(catId) || "menu".equals(catId);
    }

    private void drawButton(float x, float y, float w, float h, String label, float r, float g, float b) {
        drawQuad(x, y, w, h, r, g, b, 0.9f);
        Text.drawText(textShader, label, x + (w - Text.getTextExtent(label, 2f)[0]) / 2f, y + 8, 2f, 0.9f, 0.9f, 1f);
    }

    private void drawBoolRow(float y, ConfigParametres.Param p) {
        ConfigParametres cfg = ConfigParametres.get();
        boolean val = cfg.getBool(p.key);
        String prefix = val ? "[x] " : "[ ] ";
        Text.drawText(textShader, prefix + p.label, MENU_X + 8, y + 6, 1.8f,
            val ? 0.6f : 0.4f, val ? 1f : 0.4f, val ? 0.6f : 0.4f);
    }

    private void drawFloatRow(float y, ConfigParametres.Param p) {
        ConfigParametres cfg = ConfigParametres.get();
        float val = cfg.getFloat(p.key);
        Text.drawText(textShader, p.label + ":", MENU_X + 8, y + 6, 1.6f, 0.8f, 0.8f, 0.9f);

        float vx = MENU_X + MENU_W / 2f + 20;
        Text.drawText(textShader, "[-]", vx - 26, y + 6, 1.7f,
            val > p.min ? 0.9f : 0.3f, val > p.min ? 0.4f : 0.3f, val > p.min ? 0.4f : 0.3f);
        Text.drawText(textShader, fmtNum(val), vx + 2, y + 6, 1.7f, 1f, 1f, 1f);
        float[] ext = Text.getTextExtent(fmtNum(val), 1.7f);
        Text.drawText(textShader, "[+]", vx + ext[0] + 8, y + 6, 1.7f,
            val < p.max ? 0.4f : 0.3f, val < p.max ? 0.9f : 0.3f, val < p.max ? 0.4f : 0.3f);
    }

    private void drawQuad(float x, float y, float w, float h, float r, float g, float b, float a) {
        shader.bind();
        shader.setUniformMat4f("projection", ortho);
        buf.clear();
        buf.put(new float[]{
            x, y, r, g, b, a, x+w, y, r, g, b, a, x+w, y+h, r, g, b, a,
            x, y, r, g, b, a, x+w, y+h, r, g, b, a, x, y+h, r, g, b, a,
        }).flip();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        shader.unbind();
    }

    private String fmtNum(float v) {
        if (v == Math.floor(v) && !Float.isInfinite(v)) return String.valueOf((int) v);
        return String.format("%.2f", v).replace(',', '.');
    }

    public void cleanup() {
        shader.cleanup();
        textShader.cleanup();
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}
