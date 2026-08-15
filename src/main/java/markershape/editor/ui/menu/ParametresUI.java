package markershape.editor.ui.menu;

import markershape.config.ConfigParametres;
import markershape.editor.ui.framework.UIContainer;
import markershape.editor.ui.framework.UIButton;
import markershape.editor.ui.framework.UIEditableField;
import markershape.editor.ui.framework.UIRenderer;
import markershape.editor.ui.framework.UIText;
import markershape.editor.ui.widgets.EditableTextField;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ParametresUI {
    private UIRenderer renderer;
    public boolean visible;
    private int currentMenu = -1;
    private final Runnable onBack;
    private Runnable onApply;
    private final UIEditableField hexField;
    private final UIEditableField textHexField;
    private final UIEditableField floatField;
    private String editingFloatKey;
    private JsonObject baselineValeurs;

    private boolean confirmVisible;
    private Runnable confirmOuiAction;
    private Runnable confirmNonAction;

    private final UIContainer root = new UIContainer(0, 0, 1, 1);
    private final UIContainer confirmRoot = new UIContainer(0, 0, 1, 1);

    private float panelX, panelY, panelW, panelH;

    private static final float MENU_X = 440f / 1280f;
    private static final float MENU_W = 400f / 1280f;
    private static final float CAT_H = 42f / 720f;
    private static final float CAT_GAP = 6f / 720f;
    private static final float ROW_H = 32f / 720f;
    private static final float ROW_GAP = 4f / 720f;
    private static final float TITLE_Y = 50f / 720f;
    private static final float SY_CATS = 130f / 720f;
    private static final float SY_SUB = 110f / 720f;
    private static final float SWATCH_H = 70f / 720f;

    private static final float CONFIRM_W = 220f / 1280f;
    private static final float CONFIRM_H = 100f / 720f;
    private static final float CONFIRM_BTN_W = 70f / 1280f;
    private static final float CONFIRM_BTN_H = 28f / 720f;

    private static final float ROW_W = MENU_W * 1280f;
    private static final float ROW_H_PX = ROW_H * 720f;
    private static final float VX_FRAC = (MENU_W / 2f + 20f / 1280f) / MENU_W;
    private static final float LABEL_X_FRAC = 8f / ROW_W;
    private static final float LABEL_Y_FRAC = 6f / ROW_H_PX;
    private static final float VALUE_OFF_FRAC = 2f / ROW_W;
    private static final float MINUS_OFF_FRAC = 26f / ROW_W;
    private static final float PLUS_OFF_FRAC = 8f / ROW_W;
    private static final float MINUS_HIT_LO = 30f / ROW_W;
    private static final float MINUS_HIT_HI = 6f / ROW_W;
    private static final float PLUS_HIT_LO = 6f / ROW_W;
    private static final float PLUS_HIT_HI = 30f / ROW_W;
    private static final float VALUE_HIT_TAIL = 4f / ROW_W;

    private static float ts(float designScale) { return designScale / 720f; }

    public ParametresUI(Runnable onBack) {
        this.onBack = onBack;
        this.hexField = new UIEditableField("#000000", EditableTextField.ValueType.HEX_COLOR);
        this.textHexField = new UIEditableField("#33210F", EditableTextField.ValueType.HEX_COLOR);
        this.floatField = new UIEditableField("0", EditableTextField.ValueType.FLOAT);
        renderer = new UIRenderer();
        root.alpha = UIContainer.Alpha.NONE;
        buildConfirmTree();
    }

    public void setSize(int w, int h) { renderer.setScreenSize(w, h); }
    public void loadFromConfig() {
        ConfigParametres.recharger();
        ConfigParametres cfg = ConfigParametres.get();
        baselineValeurs = cfg.valeurs != null ? cfg.valeurs.deepCopy() : null;
        currentMenu = -1;
    }
    public void setOnApply(Runnable r) { onApply = r; }

    private void revertToBaseline() {
        ConfigParametres cfg = ConfigParametres.get();
        if (baselineValeurs != null) {
            cfg.valeurs = baselineValeurs.deepCopy();
        }
        cfg.resetDirty();
        cancelFields();
        if (onApply != null) onApply.run();
    }

    public void render() {
        if (!visible) return;
        root.clear();
        if (confirmVisible) {
            confirmRoot.render(renderer);
            return;
        }
        if (currentMenu < 0) buildCategories();
        else buildSubMenu();
        root.render(renderer);
    }

    // ---------- Categories view ----------

    private List<Integer> getVisibleCategoryIndices() {
        ConfigParametres cfg = ConfigParametres.get();
        List<Integer> idxs = new ArrayList<>();
        if (cfg.categories == null) return idxs;
        for (int i = 0; i < cfg.categories.size(); i++) {
            if (cfg.categories.get(i).isVisible(cfg)) idxs.add(i);
        }
        return idxs;
    }

    private void buildCategories() {
        ConfigParametres cfg = ConfigParametres.get();
        List<Integer> visible = getVisibleCategoryIndices();
        List<ConfigParametres.Categorie> cats = cfg.categories;
        if (cats == null) return;

        UIText title = new UIText(0, TITLE_Y, ts(3f), "Parametres");
        title.w = 1f;
        title.centered = true;
        root.add(title);

        float sy = SY_CATS;
        float contentH = visible.size() * (CAT_H + CAT_GAP);
        float btnY = sy + contentH + 22f / 720f;
        panelX = MENU_X - 15f / 1280f;
        panelY = sy - 10f / 720f;
        panelW = MENU_W + 30f / 1280f;
        panelH = contentH + 80f / 720f;

        UIContainer panel = new UIContainer(panelX, panelY, panelW, panelH);
        root.add(panel);

        for (int vi = 0; vi < visible.size(); vi++) {
            int idx = visible.get(vi);
            ConfigParametres.Categorie cat = cats.get(idx);
            float y = sy + vi * (CAT_H + CAT_GAP);
            float rx = relX(MENU_X);
            float ry = relY(y);
            float rw = MENU_W / panelW;
            float rh = CAT_H / panelH;

            UIContainer row = new UIContainer(rx, ry, rw, rh);
            row.alpha = UIContainer.Alpha.ROW;
            row.onClickAction = () -> { currentMenu = idx; cancelFields(); };
            panel.add(row);

            UIText label = new UIText(16f / (MENU_W * 1280f), 10f / (CAT_H * 720f), ts(2f), cat.label);
            label.w = 1f;
            label.h = 1f;
            row.add(label);
        }

        if (cfg.hasChanges()) {
            UIButton save = makeButton("Sauvegarder", 0.5f - 210f / 1280f, btnY, btnAlphaBg(1.1f),
                () -> showConfirmPopup(
                    () -> { ConfigParametres.sauvegarder(); if (onApply != null) onApply.run(); this.visible = false; if (onBack != null) onBack.run(); },
                    () -> revertToBaseline()));
            panel.add(save);
        }
        UIButton back = makeButton("Retour", 0.5f + 10f / 1280f, btnY, null,
            () -> {
                if (cfg.hasChanges()) {
                    showConfirmPopup(
                        () -> { ConfigParametres.sauvegarder(); if (onApply != null) onApply.run(); this.visible = false; if (onBack != null) onBack.run(); },
                        () -> { revertToBaseline(); this.visible = false; if (onBack != null) onBack.run(); });
                } else {
                    if (onApply != null) onApply.run();
                    this.visible = false;
                    if (onBack != null) onBack.run();
                }
            });
        panel.add(back);
    }

    private UIButton makeButton(String label, float winX, float winY, float[] tint, Runnable action) {
        UIButton b = new UIButton(relX(winX), relY(winY), 200f / 1280f / panelW, 38f / 720f / panelH, label, ts(2f));
        b.alpha = UIContainer.Alpha.BTN;
        if (tint != null) {
            b.bgR = BlurBackground.menuR * tint[0];
            b.bgG = BlurBackground.menuG * tint[1];
            b.bgB = BlurBackground.menuB * tint[2];
        }
        b.onClickAction = action;
        return b;
    }

    private float[] btnAlphaBg(float gMul) {
        return new float[]{0.9f, gMul, 0.9f};
    }

    private float relX(float winX) { return (winX - panelX) / panelW; }
    private float relY(float winY) { return (winY - panelY) / panelH; }

    // ---------- Submenu view ----------

    private boolean hasColorPicker(String catId) {
        return "arriereplan".equals(catId) || "menu".equals(catId);
    }

    private boolean isArriere() {
        return "arriereplan".equals(catId());
    }

    private String catId() {
        ConfigParametres cfg = ConfigParametres.get();
        if (cfg.categories == null || currentMenu < 0 || currentMenu >= cfg.categories.size()) return null;
        return cfg.categories.get(currentMenu).id;
    }

    private void buildSubMenu() {
        ConfigParametres cfg = ConfigParametres.get();
        List<ConfigParametres.Categorie> cats = cfg.categories;
        if (cats == null || currentMenu < 0 || currentMenu >= cats.size()) return;
        ConfigParametres.Categorie cat = cats.get(currentMenu);
        if (cat.params == null) return;

        UIText title = new UIText(0, TITLE_Y, ts(3f), cat.label);
        title.w = 1f;
        title.centered = true;
        root.add(title);

        float sy = SY_SUB;
        boolean arriere = isArriere();
        int preambleRows = hasColorPicker(cat.id) ? (arriere ? 0 : 2) : 0;
        int visibleCount = 0;
        for (ConfigParametres.Param p : cat.params) {
            if (p.isVisible(cfg)) visibleCount++;
        }
        float contentH = arriere ? (SWATCH_H + 3 * (ROW_H + ROW_GAP) + 8f / 720f + SWATCH_H + 3 * (ROW_H + ROW_GAP))
                                 : (preambleRows * (ROW_H + ROW_GAP) + visibleCount * (ROW_H + ROW_GAP));
        float btnY = sy + contentH + 22f / 720f;
        panelX = MENU_X - 15f / 1280f;
        panelY = sy - 10f / 720f;
        panelW = MENU_W + 30f / 1280f;
        panelH = contentH + 80f / 720f;

        UIContainer panel = new UIContainer(panelX, panelY, panelW, panelH);
        root.add(panel);

        int rendered = 0;

        if (hasColorPicker(cat.id)) {
            if (arriere) {
                float curY = sy;
                addSwatch(panel, curY, "bg", hexField);
                curY += SWATCH_H;
                for (int idx : new int[]{0, 1, 2}) {
                    addFloatRow(panel, curY, cat.params.get(idx));
                    curY += ROW_H + ROW_GAP;
                    rendered++;
                }
                curY += 8f / 720f;
                addSwatch(panel, curY, "text", textHexField);
                curY += SWATCH_H;
                for (int idx : new int[]{3, 4, 5}) {
                    addFloatRow(panel, curY, cat.params.get(idx));
                    curY += ROW_H + ROW_GAP;
                    rendered++;
                }
            } else {
                float curY = sy;
                addSwatch(panel, curY, "menu", hexField);
            }
        }

        for (ConfigParametres.Param p : cat.params) {
            if (!p.isVisible(cfg)) continue;
            if (arriere && rendered >= 6) break;
            if (hasColorPicker(cat.id) && !arriere && rendered >= visibleCount) break;
            float y = sy + preambleRows * (ROW_H + ROW_GAP) + rendered * (ROW_H + ROW_GAP);
            if ("bool".equals(p.type)) {
                addBoolRow(panel, y, p);
            } else {
                addFloatRow(panel, y, p);
            }
            rendered++;
        }

        UIButton apply = makeButton("Appliquer", 0.5f - 210f / 1280f, btnY, btnAlphaBg(1.1f),
            () -> { if (onApply != null) onApply.run(); });
        panel.add(apply);
        UIButton back = makeButton("Retour", 0.5f + 10f / 1280f, btnY, null,
            () -> { currentMenu = -1; cancelFields(); });
        panel.add(back);
    }

    private void addSwatch(UIContainer panel, float winY, String prefix, UIEditableField field) {
        ConfigParametres cfg = ConfigParametres.get();
        float vr = cfg.getFloat(prefix + "R"), vg = cfg.getFloat(prefix + "G"), vb = cfg.getFloat(prefix + "B");

        UIContainer swatch = new UIContainer(relX(MENU_X + 30f / 1280f), relY(winY),
            (MENU_W - 60f / 1280f) / panelW, SWATCH_H / panelH);
        swatch.alpha = UIContainer.Alpha.OPAQUE;
        swatch.bgR = vr / 255f;
        swatch.bgG = vg / 255f;
        swatch.bgB = vb / 255f;
        panel.add(swatch);

        String hex = String.format("#%02X%02X%02X", (int) vr, (int) vg, (int) vb);
        float[] tc = swatchTextColor(vr, vg, vb);

        field.setText(hex);
        field.centered = true;
        field.relScale = ts(2f);
        field.x = 0f;
        field.y = 0f;
        field.w = 1f;
        field.h = 1f;
        field.setColor(tc[0], tc[1], tc[2]);
        field.setOnConfirm(newHex -> {
            int nr = Integer.parseInt(newHex.substring(1, 3), 16);
            int ng = Integer.parseInt(newHex.substring(3, 5), 16);
            int nb = Integer.parseInt(newHex.substring(5, 7), 16);
            cfg.setFloat(prefix + "R", nr); cfg.setFloat(prefix + "G", ng); cfg.setFloat(prefix + "B", nb);
        });
        swatch.add(field);
    }

    private void addBoolRow(UIContainer panel, float winY, ConfigParametres.Param p) {
        BoolRow row = new BoolRow(p);
        row.x = relX(MENU_X);
        row.y = relY(winY);
        row.w = MENU_W / panelW;
        row.h = ROW_H / panelH;
        row.alpha = UIContainer.Alpha.ROW;
        row.onClickAction = () -> {
            ConfigParametres cfg = ConfigParametres.get();
            cfg.setBool(p.key, !cfg.getBool(p.key));
            editingFloatKey = null;
        };
        panel.add(row);
    }

    private void addFloatRow(UIContainer panel, float winY, ConfigParametres.Param p) {
        FloatRow row = new FloatRow(p);
        row.x = relX(MENU_X);
        row.y = relY(winY);
        row.w = MENU_W / panelW;
        row.h = ROW_H / panelH;
        row.alpha = UIContainer.Alpha.ROW;
        row.onMinus = () -> {
            ConfigParametres cfg = ConfigParametres.get();
            float val = cfg.getFloat(p.key);
            if (val > p.min) { cfg.setFloat(p.key, val - p.step); editingFloatKey = null; }
        };
        row.onPlus = () -> {
            ConfigParametres cfg = ConfigParametres.get();
            float val = cfg.getFloat(p.key);
            if (val < p.max) { cfg.setFloat(p.key, val + p.step); editingFloatKey = null; }
        };
        row.onValue = () -> {
            ConfigParametres cfg = ConfigParametres.get();
            float val = cfg.getFloat(p.key);
            editingFloatKey = p.key;
            floatField.setText(fmtNum(val));
            floatField.setBounds(p.min, p.max);
            floatField.setOnConfirm(newVal -> {
                try { cfg.setFloat(p.key, Float.parseFloat(newVal)); }
                catch (NumberFormatException ignored) {}
                editingFloatKey = null;
            });
            floatField.activate();
        };
        panel.add(row);
        if (editingFloatKey != null && editingFloatKey.equals(p.key)) {
            ConfigParametres cfg = ConfigParametres.get();
            floatField.centered = false;
            floatField.relScale = ts(1.7f);
            floatField.x = VX_FRAC + VALUE_OFF_FRAC;
            floatField.y = LABEL_Y_FRAC;
            floatField.w = 1f;
            floatField.h = 1f;
            floatField.setColor(cfg.getFloat("textR") / 255f, cfg.getFloat("textG") / 255f, cfg.getFloat("textB") / 255f);
            row.add(floatField);
        }
    }

    // ---------- Float / Bool rows ----------

    private static class FloatRow extends UIContainer {
        final ConfigParametres.Param p;
        Runnable onMinus, onPlus, onValue;

        FloatRow(ConfigParametres.Param p) { this.p = p; }

        @Override
        protected void renderSelf(UIRenderer r) {
            super.renderSelf(r);
            ConfigParametres cfg = ConfigParametres.get();
            float tr = cfg.getFloat("textR") / 255f, tg = cfg.getFloat("textG") / 255f, tb = cfg.getFloat("textB") / 255f;
            float x0 = absX(r), y0 = absY(r), rw = absW(r), rh = absH(r);
            float labelScale = ts(1.6f) * r.getHeight();
            r.text(p.label + ":", x0 + LABEL_X_FRAC * rw, y0 + LABEL_Y_FRAC * rh, labelScale, tr, tg, tb);
            drawControls(r, cfg, tr, tg, tb, x0, y0, rw, rh);
        }

        private void drawControls(UIRenderer r, ConfigParametres cfg, float tr, float tg, float tb,
                                  float x0, float y0, float rw, float rh) {
            float val = cfg.getFloat(p.key);
            float vx = x0 + VX_FRAC * rw;
            String vs = fmtNum(val);
            float cs = ts(1.7f) * r.getHeight();
            float ty = y0 + LABEL_Y_FRAC * rh;
            r.text("[-]", x0 + (VX_FRAC - MINUS_OFF_FRAC) * rw, ty, cs,
                val > p.min ? tr : tr * 0.3f, val > p.min ? tg : tg * 0.3f, val > p.min ? tb : tb * 0.3f);
            r.text(vs, x0 + (VX_FRAC + VALUE_OFF_FRAC) * rw, ty, cs, tr, tg, tb);
            float[] ext = r.textExtent(vs, cs);
            r.text("[+]", vx + ext[0] + PLUS_OFF_FRAC * rw, ty, cs,
                val < p.max ? tr : tr * 0.3f, val < p.max ? tg : tg * 0.3f, val < p.max ? tb : tb * 0.3f);
        }

        @Override
        protected boolean onClickSelf(float px, float py, UIRenderer r) {
            ConfigParametres cfg = ConfigParametres.get();
            float val = cfg.getFloat(p.key);
            float x0 = absX(r), y0 = absY(r), rw = absW(r), rh = absH(r);
            if (py < y0 || py > y0 + rh) return false;
            float vx = x0 + VX_FRAC * rw;
            float[] ext = r.textExtent(fmtNum(val), ts(1.7f) * r.getHeight());
            if (px >= vx - MINUS_HIT_LO * rw && px <= vx - MINUS_HIT_HI * rw) { if (onMinus != null) onMinus.run(); return true; }
            if (px >= vx + ext[0] + PLUS_HIT_LO * rw && px <= vx + ext[0] + PLUS_HIT_HI * rw) { if (onPlus != null) onPlus.run(); return true; }
            if (px >= vx && px <= vx + ext[0] + VALUE_HIT_TAIL * rw) { if (onValue != null) onValue.run(); return true; }
            return false;
        }
    }

    private static class BoolRow extends UIContainer {
        final ConfigParametres.Param p;

        BoolRow(ConfigParametres.Param p) { this.p = p; }

        @Override
        protected void renderSelf(UIRenderer r) {
            super.renderSelf(r);
            ConfigParametres cfg = ConfigParametres.get();
            boolean val = cfg.getBool(p.key);
            float tr = cfg.getFloat("textR") / 255f, tg = cfg.getFloat("textG") / 255f, tb = cfg.getFloat("textB") / 255f;
            String prefix = val ? "[x] " : "[ ] ";
            float brightness = val ? 1f : 0.5f;
            r.text(prefix + p.label, absX(r) + LABEL_X_FRAC * absW(r), absY(r) + LABEL_Y_FRAC * absH(r),
                ts(1.8f) * r.getHeight(), tr * brightness, tg * brightness, tb * brightness);
        }
    }

    // ---------- Click handling ----------

    public void click(float mx, float my) {
        if (!visible) return;
        if (confirmVisible) {
            confirmRoot.onClick(mx, my, renderer);
            return;
        }
        if (currentMenu < 0) {
            root.onClick(mx, my, renderer);
            return;
        }
        String id = catId();
        if (hasColorPicker(id != null ? id : "")) {
            if (hexField.click(mx, my, renderer)) return;
            if (isArriere() && textHexField.click(mx, my, renderer)) return;
        }
        root.onClick(mx, my, renderer);
    }

    public void handleKey(int key, int action) {
        if (confirmVisible) return;
        if (hexField.isEditing()) {
            hexField.keyAction(key, action);
        } else if (textHexField.isEditing()) {
            textHexField.keyAction(key, action);
        } else if (floatField.isEditing()) {
            floatField.keyAction(key, action);
            if (!floatField.isEditing()) editingFloatKey = null;
        }
    }

    public void handleChar(int codepoint) {
        if (confirmVisible) return;
        if (hexField.isEditing()) {
            hexField.keyChar(codepoint);
        } else if (textHexField.isEditing()) {
            textHexField.keyChar(codepoint);
        } else if (floatField.isEditing()) {
            floatField.keyChar(codepoint);
        }
    }

    private void cancelFields() {
        hexField.cancelEditing();
        textHexField.cancelEditing();
        floatField.cancelEditing();
        editingFloatKey = null;
    }

    // ---------- Confirm popup ----------

    private void buildConfirmTree() {
        confirmRoot.alpha = UIContainer.Alpha.DIM;

        float cx = (1f - CONFIRM_W) / 2f;
        float cy = (36f / 720f + (1f - 36f / 720f) / 2f) - CONFIRM_H / 2f;

        UIContainer box = new UIContainer(cx, cy, CONFIRM_W, CONFIRM_H);
        box.alpha = UIContainer.Alpha.BOX;
        confirmRoot.add(box);

        UIText q = new UIText(0f, (18f / 720f) / CONFIRM_H, ts(1.5f), "Sauvegarder ?");
        q.w = 1f;
        q.centered = true;
        box.add(q);

        float by = (CONFIRM_H - CONFIRM_BTN_H - 12f / 720f) / CONFIRM_H;
        UIButton oui = new UIButton((20f / 1280f) / CONFIRM_W, by, CONFIRM_BTN_W / CONFIRM_W, CONFIRM_BTN_H / CONFIRM_H, "Oui", ts(1.5f));
        oui.alpha = UIContainer.Alpha.BTN;
        oui.onClickAction = () -> {
            confirmVisible = false;
            if (confirmOuiAction != null) confirmOuiAction.run();
        };
        box.add(oui);

        UIButton non = new UIButton((CONFIRM_W - 20f / 1280f - CONFIRM_BTN_W) / CONFIRM_W, by, CONFIRM_BTN_W / CONFIRM_W, CONFIRM_BTN_H / CONFIRM_H, "Non", ts(1.5f));
        non.alpha = UIContainer.Alpha.BTN;
        non.onClickAction = () -> {
            confirmVisible = false;
            if (confirmNonAction != null) confirmNonAction.run();
        };
        box.add(non);
    }

    private void showConfirmPopup(Runnable oui, Runnable non) {
        confirmVisible = true;
        confirmOuiAction = oui;
        confirmNonAction = non;
    }

    // ---------- Util ----------

    private static String fmtNum(float v) {
        if (v == Math.floor(v) && !Float.isInfinite(v)) return String.valueOf((int) v);
        return String.format("%.2f", v).replace(',', '.');
    }

    private static float[] swatchTextColor(float r, float g, float b) {
        float avg = (r + g + b) / 3f;
        if (avg >= 127.5f) {
            return new float[]{r * 0.4f / 255f, g * 0.4f / 255f, b * 0.4f / 255f};
        }
        return new float[]{(r + (255f - r) * 0.65f) / 255f, (g + (255f - g) * 0.65f) / 255f, (b + (255f - b) * 0.65f) / 255f};
    }

    public void cleanup() {
        renderer.cleanup();
    }
}
