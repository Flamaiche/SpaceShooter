package markershape.editor.ui.menu;

import markershape.config.ConfigParametres;
import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.control.Button;
import markershape.editor.ui.widgets.EditableTextField;

import java.util.ArrayList;
import java.util.List;

public class ParametresUI extends Panel {
    private int width, height;
    private static final int MENU_X = 440;
    private static final int MENU_W = 400;
    private static final int CAT_H = 42;
    private static final int CAT_GAP = 6;
    private static final int ROW_H = 32;
    private static final int ROW_GAP = 4;

    private int currentMenu = -1;
    private final Runnable onBack;
    private Runnable onApply;
    private final EditableTextField hexField;
    private final EditableTextField textHexField;
    private final EditableTextField floatField;
    private String editingFloatKey;

    private boolean confirmVisible;
    private Runnable confirmOuiAction;
    private Runnable confirmNonAction;
    private static final float CONFIRM_W = 220;
    private static final float CONFIRM_H = 100;
    private static final float CONFIRM_BTN_W = 70;
    private static final float CONFIRM_BTN_H = 28;

    private Button saveBtn, backBtn;

    public ParametresUI(UIResources res, Runnable onBack) {
        super(res);
        this.onBack = onBack;
        visible = false;
        this.hexField = new EditableTextField("#000000", EditableTextField.ValueType.HEX_COLOR, 0, 0);
        this.textHexField = new EditableTextField("#33210F", EditableTextField.ValueType.HEX_COLOR, 0, 0);
        this.floatField = new EditableTextField("0", EditableTextField.ValueType.FLOAT, 0, 0);

        saveBtn = new Button(res, "", 0, 0, 200, 38, null);
        saveBtn.textScale = 2f;
        backBtn = new Button(res, "", 0, 0, 200, 38, null);
        backBtn.textScale = 2f;
        addChild(saveBtn);
        addChild(backBtn);
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        res.setSize(w, h);
    }

    public void loadFromConfig() {
        ConfigParametres.recharger();
        currentMenu = -1;
    }

    public void setOnApply(Runnable r) { onApply = r; }

    @Override
    protected void renderContent() {
        if (confirmVisible) {
            saveBtn.hide();
            backBtn.hide();
            renderConfirmPopup();
            return;
        }
        saveBtn.show();
        backBtn.show();
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

        float[] c = res.menuColor();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        res.drawText("Parametres",
            width / 2f - res.getTextExtent("Parametres", 3f)[0] / 2f, 50, 3f, tR, tG, tB);

        float sy = 130;
        float contentH = visible.size() * (CAT_H + CAT_GAP);
        float btnY = sy + contentH + 22;
        float panelAlpha = BlurBackground.panelAlpha();
        float rowAlpha = BlurBackground.rowAlpha();
        res.drawQuad(MENU_X - 15, sy - 10, MENU_W + 30, contentH + 80, c[0], c[1], c[2], panelAlpha);
        for (int vi = 0; vi < visible.size(); vi++) {
            ConfigParametres.Categorie cat = cats.get(visible.get(vi));
            float y = sy + vi * (CAT_H + CAT_GAP);
            res.drawQuad(MENU_X, y, MENU_W, CAT_H, c[0], c[1], c[2], rowAlpha);
            res.drawText(cat.label + "  >", MENU_X + 16, y + 10, 2f, tR, tG, tB);
        }

        setupButtons("Sauvegarder", "Retour", btnY, cfg.hasChanges(), tR, tG, tB);
    }

    private void renderSubMenu() {
        ConfigParametres cfg = ConfigParametres.get();
        List<ConfigParametres.Categorie> cats = cfg.categories;
        if (cats == null || currentMenu < 0 || currentMenu >= cats.size()) return;
        ConfigParametres.Categorie cat = cats.get(currentMenu);
        if (cat.params == null) return;

        float[] c = res.menuColor();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        res.drawText(cat.label,
            width / 2f - res.getTextExtent(cat.label, 3f)[0] / 2f, 50, 3f, tR, tG, tB);

        float sy = 110;
        boolean isArriere = "arriereplan".equals(cat.id);
        int preambleRows = hasColorPicker(cat.id) ? (isArriere ? 0 : 2) : 0;
        int visibleCount = 0;
        for (ConfigParametres.Param p : cat.params) {
            if (p.isVisible(cfg)) visibleCount++;
        }
        float contentH = isArriere ? (80 + 3 * (ROW_H + ROW_GAP) + 8 + 80 + 3 * (ROW_H + ROW_GAP))
                                  : (preambleRows * (ROW_H + ROW_GAP) + visibleCount * (ROW_H + ROW_GAP));
        float btnY = sy + contentH + 22;
        float panelAlpha = BlurBackground.panelAlpha();
        res.drawQuad(MENU_X - 15, sy - 10, MENU_W + 30, contentH + 80, c[0], c[1], c[2], panelAlpha);

        int rendered = 0;

        if (hasColorPicker(cat.id)) {
            if (isArriere) {
                float curY = sy;
                renderColorEditor(curY, hexField,
                    (int) cfg.getFloat("bgR"), (int) cfg.getFloat("bgG"), (int) cfg.getFloat("bgB"), "bg");

                curY += 80;
                curY = renderFloatGroup(curY, cat.params, new int[]{0, 1, 2}, false);
                rendered += 3;
                curY += 8;

                renderColorEditor(curY, textHexField,
                    (int) cfg.getFloat("textR"), (int) cfg.getFloat("textG"), (int) cfg.getFloat("textB"), "text");

                curY += 80;
                curY = renderFloatGroup(curY, cat.params, new int[]{3, 4, 5}, true);
                rendered += 3;
            } else {
                renderColorEditor(sy, hexField,
                    (int) cfg.getFloat("menuR"), (int) cfg.getFloat("menuG"), (int) cfg.getFloat("menuB"), "menu");
            }
        }

        for (ConfigParametres.Param p : cat.params) {
            if (!p.isVisible(cfg)) continue;
            if (isArriere && rendered >= 6) break;
            if (hasColorPicker(cat.id) && !isArriere && rendered >= visibleCount) break;
            float y = sy + preambleRows * (ROW_H + ROW_GAP) + rendered * (ROW_H + ROW_GAP);

            if ("bool".equals(p.type)) {
                drawBoolRow(y, p, tR, tG, tB);
            } else {
                drawFloatRow(y, p, tR, tG, tB);
                if (p.key.equals(editingFloatKey) && !isArriere) renderFloatField(y, p);
            }
            rendered++;
        }

        setupButtons("Appliquer", "Retour", btnY, false, tR, tG, tB);
    }

    private void renderColorEditor(float y, EditableTextField field,
                                   int r, int g, int b, String cfgPrefix) {
        ConfigParametres cfg = ConfigParametres.get();
        res.drawQuad(MENU_X + 30, y, MENU_W - 60, 70, r / 255f, g / 255f, b / 255f, 1f);
        String hex = String.format("#%02X%02X%02X", r, g, b);
        float hx = width / 2f - res.getTextExtent(hex, 2f)[0] / 2f;
        field.setText(hex);
        field.setPosition(hx, y + 26);
        field.setScale(2f);
        field.setColor(r / 255f, g / 255f, b / 255f);
        field.setOnConfirm(newHex -> {
            int nr = Integer.parseInt(newHex.substring(1, 3), 16);
            int ng = Integer.parseInt(newHex.substring(3, 5), 16);
            int nb = Integer.parseInt(newHex.substring(5, 7), 16);
            cfg.setFloat(cfgPrefix + "R", nr); cfg.setFloat(cfgPrefix + "G", ng); cfg.setFloat(cfgPrefix + "B", nb);
            field.setText(field.getText());
        });
        field.render(res);
    }

    private float renderFloatGroup(float curY, List<ConfigParametres.Param> params,
                                   int[] indices, boolean showEditField) {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        for (int idx : indices) {
            ConfigParametres.Param p = params.get(idx);
            drawFloatRow(curY, p, tR, tG, tB);
            if (showEditField && p.key.equals(editingFloatKey)) renderFloatField(curY, p);
            curY += ROW_H + ROW_GAP;
        }
        return curY;
    }

    private boolean clickFloatRow(float mx, float my, float y, ConfigParametres.Param p) {
        ConfigParametres cfg = ConfigParametres.get();
        float val = cfg.getFloat(p.key);
        float vx = MENU_X + MENU_W / 2f + 20;
        float[] ext = res.getTextExtent(fmtNum(val), 1.7f);
        if (mx >= vx - 30 && mx <= vx - 6 && my >= y && my <= y + ROW_H) {
            if (val > p.min) { cfg.setFloat(p.key, val - p.step); editingFloatKey = null; }
            return true;
        }
        if (mx >= vx + ext[0] + 6 && mx <= vx + ext[0] + 30 && my >= y && my <= y + ROW_H) {
            if (val < p.max) { cfg.setFloat(p.key, val + p.step); editingFloatKey = null; }
            return true;
        }
        if (!p.key.equals(editingFloatKey) || !floatField.isEditing()) {
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
                return true;
            }
        }
        return false;
    }

    private void setupButtons(String primaryLabel, String backLabel, float btnY,
                              boolean hasChanges, float tR, float tG, float tB) {
        saveBtn.text = primaryLabel;
        saveBtn.x = width / 2f - 210;
        saveBtn.y = btnY;
        backBtn.text = backLabel;
        backBtn.x = width / 2f + 10;
        backBtn.y = btnY;

        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        saveBtn.bgR = mr * 0.9f; saveBtn.bgG = mg * 1.1f; saveBtn.bgB = mb * 0.9f;
        backBtn.bgR = mr; backBtn.bgG = mg; backBtn.bgB = mb;
        float a = BlurBackground.btnAlpha();
        saveBtn.bgA = a; backBtn.bgA = a;
        saveBtn.textR = tR; saveBtn.textG = tG; saveBtn.textB = tB;
        backBtn.textR = tR; backBtn.textG = tG; backBtn.textB = tB;

        if (currentMenu < 0) {
            if (hasChanges) {
                saveBtn.show();
                saveBtn.action = () -> showConfirmPopup(
                    () -> {
                        ConfigParametres.sauvegarder();
                        if (onApply != null) onApply.run();
                        this.visible = false;
                        if (onBack != null) onBack.run();
                    },
                    () -> {});
            } else {
                saveBtn.hide();
            }
            backBtn.action = () -> {
                if (hasChanges) {
                    showConfirmPopup(
                        () -> {
                            ConfigParametres.sauvegarder();
                            if (onApply != null) onApply.run();
                            this.visible = false;
                            if (onBack != null) onBack.run();
                        },
                        () -> {
                            this.visible = false;
                            if (onBack != null) onBack.run();
                        });
                } else {
                    if (onApply != null) onApply.run();
                    this.visible = false;
                    if (onBack != null) onBack.run();
                }
            };
        } else {
            saveBtn.action = () -> {
                if (onApply != null) onApply.run();
            };
            backBtn.action = () -> { currentMenu = -1; };
        }
    }

    private void renderFloatField(float y, ConfigParametres.Param p) {
        ConfigParametres cfg = ConfigParametres.get();
        float val = cfg.getFloat(p.key);
        float tr = cfg.getFloat("textR") / 255f, tg = cfg.getFloat("textG") / 255f, tb = cfg.getFloat("textB") / 255f;
        float vx = MENU_X + MENU_W / 2f + 20;
        String display = fmtNum(val);
        floatField.setText(display);
        floatField.setPosition(vx + 2, y + 6);
        floatField.setScale(1.7f);
        floatField.setBounds(p.min, p.max);
        floatField.setColor(tr, tg, tb);
        floatField.setOnConfirm(newVal -> {
            try { cfg.setFloat(p.key, Float.parseFloat(newVal)); }
            catch (NumberFormatException ignored) {}
            editingFloatKey = null;
        });
        floatField.render(res);
    }

    @Override
    public boolean click(float mx, float my) {
        if (!visible) return false;
        if (confirmVisible) {
            handleConfirmClick(mx, my);
            return true;
        }
        if (currentMenu < 0) clickCategories(mx, my);
        else clickSubMenu(mx, my);
        return true;
    }

    private void clickCategories(float mx, float my) {
        hexField.cancelEditing();
        textHexField.cancelEditing();
        floatField.cancelEditing();
        editingFloatKey = null;
        List<Integer> visibleIdxs = getVisibleCategoryIndices();
        ConfigParametres cfg = ConfigParametres.get();
        if (cfg.categories == null) return;

        float sy = 130;
        float contentH = visibleIdxs.size() * (CAT_H + CAT_GAP);
        for (int vi = 0; vi < visibleIdxs.size(); vi++) {
            int idx = visibleIdxs.get(vi);
            float y = sy + vi * (CAT_H + CAT_GAP);
            if (mx >= MENU_X && mx <= MENU_X + MENU_W && my >= y && my <= y + CAT_H) {
                currentMenu = idx;
                return;
            }
        }
        float by = sy + contentH + 22;
        if (my >= by && my <= by + 38) {
            if (saveBtn.isVisible() && saveBtn.contains(mx, my)) {
                saveBtn.click(mx, my);
                return;
            }
            if (backBtn.contains(mx, my)) {
                backBtn.click(mx, my);
                return;
            }
        }
    }

    private void clickSubMenu(float mx, float my) {
        ConfigParametres cfg = ConfigParametres.get();
        List<ConfigParametres.Categorie> cats = cfg.categories;
        if (cats == null || currentMenu < 0 || currentMenu >= cats.size()) return;
        ConfigParametres.Categorie cat = cats.get(currentMenu);
        if (cat.params == null) return;

        if (hasColorPicker(cat.id)) {
            if (hexField.click(res, mx, my)) return;
            if ("arriereplan".equals(cat.id) && textHexField.click(res, mx, my)) return;
        }

        float sy = 110;
        boolean isArriere = "arriereplan".equals(cat.id);
        int preambleRows = hasColorPicker(cat.id) ? (isArriere ? 0 : 2) : 0;
        int rendered = 0;

        if (isArriere) {
            float curY = sy + 80;
            for (int idx : new int[]{0, 1, 2}) {
                ConfigParametres.Param p = cat.params.get(idx);
                if ("float".equals(p.type) && clickFloatRow(mx, my, curY, p)) return;
                curY += ROW_H + ROW_GAP;
                rendered++;
            }
            curY = sy + 80 + 3 * (ROW_H + ROW_GAP) + 8 + 80;
            for (int idx : new int[]{3, 4, 5}) {
                ConfigParametres.Param p = cat.params.get(idx);
                if ("float".equals(p.type) && clickFloatRow(mx, my, curY, p)) return;
                curY += ROW_H + ROW_GAP;
                rendered++;
            }
        } else {
            for (ConfigParametres.Param p : cat.params) {
                if (!p.isVisible(cfg)) continue;
                float y = sy + preambleRows * (ROW_H + ROW_GAP) + rendered * (ROW_H + ROW_GAP);

                if ("bool".equals(p.type)) {
                    if (mx >= MENU_X && mx <= MENU_X + MENU_W && my >= y && my <= y + ROW_H) {
                        cfg.setBool(p.key, !cfg.getBool(p.key));
                        return;
                    }
                } else {
                    if (clickFloatRow(mx, my, y, p)) return;
                }
                rendered++;
            }
        }

        float contentH = isArriere ? (80 + 3 * (ROW_H + ROW_GAP) + 8 + 80 + 3 * (ROW_H + ROW_GAP))
                                  : (preambleRows * (ROW_H + ROW_GAP) + rendered * (ROW_H + ROW_GAP));
        float by = sy + contentH + 22;
        if (my >= by && my <= by + 38) {
            hexField.cancelEditing();
            textHexField.cancelEditing();
            floatField.cancelEditing();
            editingFloatKey = null;
            if (saveBtn.contains(mx, my)) {
                saveBtn.click(mx, my);
                return;
            }
            if (backBtn.contains(mx, my)) {
                backBtn.click(mx, my);
                return;
            }
        }
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

    private boolean hasColorPicker(String catId) {
        return "arriereplan".equals(catId) || "menu".equals(catId);
    }

    private void showConfirmPopup(Runnable oui, Runnable non) {
        confirmVisible = true;
        confirmOuiAction = oui;
        confirmNonAction = non;
    }

    private void renderConfirmPopup() {
        float cx = (width - CONFIRM_W) / 2;
        float cy = (36 + (height - 36) / 2) - CONFIRM_H / 2;
        ConfigParametres cfg = ConfigParametres.get();
        float[] c = res.menuColor();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        res.drawQuad(0, 0, width, height, c[0], c[1], c[2], BlurBackground.dimAlpha());
        res.drawQuad(cx, cy, CONFIRM_W, CONFIRM_H, c[0], c[1], c[2], BlurBackground.boxAlpha());
        res.drawText("Sauvegarder ?",
            cx + CONFIRM_W / 2 - res.getTextExtent("Sauvegarder ?", 1.5f)[0] / 2f, cy + 18, 1.5f, tR, tG, tB);
        float btnY = cy + CONFIRM_H - CONFIRM_BTN_H - 12;
        res.drawText("Oui", cx + 30, btnY + 2, 1.5f, tR, tG, tB);
        res.drawText("Non", cx + CONFIRM_W - 60, btnY + 2, 1.5f, tR, tG, tB);
    }

    private void handleConfirmClick(float mx, float my) {
        float cx = (width - CONFIRM_W) / 2;
        float cy = (36 + (height - 36) / 2) - CONFIRM_H / 2;
        float btnY = cy + CONFIRM_H - CONFIRM_BTN_H - 12;
        float ouiX = cx + 20;
        float nonX = cx + CONFIRM_W - 20 - CONFIRM_BTN_W;
        if (my >= btnY && my <= btnY + CONFIRM_BTN_H) {
            if (mx >= ouiX && mx <= ouiX + CONFIRM_BTN_W) {
                confirmVisible = false;
                if (confirmOuiAction != null) confirmOuiAction.run();
                return;
            }
            if (mx >= nonX && mx <= nonX + CONFIRM_BTN_W) {
                confirmVisible = false;
                if (confirmNonAction != null) confirmNonAction.run();
                return;
            }
        }
    }

    private void drawBoolRow(float y, ConfigParametres.Param p, float tr, float tg, float tb) {
        ConfigParametres cfg = ConfigParametres.get();
        boolean val = cfg.getBool(p.key);
        String prefix = val ? "[x] " : "[ ] ";
        float brightness = val ? 1f : 0.5f;
        res.drawText(prefix + p.label, MENU_X + 8, y + 6, 1.8f,
            tr * brightness, tg * brightness, tb * brightness);
    }

    private void drawFloatRow(float y, ConfigParametres.Param p, float tr, float tg, float tb) {
        ConfigParametres cfg = ConfigParametres.get();
        float val = cfg.getFloat(p.key);

        res.drawText(p.label + ":", MENU_X + 8, y + 6, 1.6f, tr, tg, tb);

        float vx = MENU_X + MENU_W / 2f + 20;
        res.drawText("[-]", vx - 26, y + 6, 1.7f,
            val > p.min ? tr : tr * 0.3f, val > p.min ? tg : tg * 0.3f, val > p.min ? tb : tb * 0.3f);
        res.drawText(fmtNum(val), vx + 2, y + 6, 1.7f, tr, tg, tb);
        float[] ext = res.getTextExtent(fmtNum(val), 1.7f);
        res.drawText("[+]", vx + ext[0] + 8, y + 6, 1.7f,
            val < p.max ? tr : tr * 0.3f, val < p.max ? tg : tg * 0.3f, val < p.max ? tb : tb * 0.3f);
    }

    private String fmtNum(float v) {
        if (v == Math.floor(v) && !Float.isInfinite(v)) return String.valueOf((int) v);
        return String.format("%.2f", v).replace(',', '.');
    }
}
