package markershape.editor.ui;

import markershape.config.ConfigParametres;
import markershape.editor.ui.control.Button;
import markershape.editor.ui.control.EntityListPanel;
import markershape.editor.ui.util.TextColor;
import markershape.editor.ui.control.FilterPanel;
import markershape.editor.ui.control.OriginPanel;
import markershape.editor.ui.menu.BlurBackground;
import markershape.editor.ui.menu.ConfirmDeletePopup;
import markershape.editor.ui.menu.ConfirmSavePopup;
import markershape.editor.ui.menu.NewMenu;
import markershape.editor.ui.menu.ToolPalette;

public class EditorUI extends Panel {
    private int width, height;
    public static final int BAR_H = 36;
    public static final int BTN_W = 130;
    private Button saveBtn, quitBtn, filterBtn, newBtn, outilsBtn, origineBtn;
    private String currentFile;

    public boolean transparentBar = true;

    public final FilterPanel filter;
    public final NewMenu newMenu;
    public final ToolPalette toolsPal;
    public final ConfirmSavePopup confirmSave;
    public final ConfirmDeletePopup confirmDelete;
    public final EntityListPanel entityList;
    public final OriginPanel origin;
    private float lastMenuR = -1f, lastMenuG = -1f, lastMenuB = -1f;
    private boolean lastTransparentUI;
    private int hudLodLevel = 0;
    private int hudTotalFaces = 0;
    private int hudRenderedFaces = 0;
    private float hudLodDistance = 0f;
    private boolean hudHasMesh = false;

    public void setLodStats(int level, float dist, int rendered, int total) {
        hudLodLevel = level;
        hudLodDistance = dist;
        hudRenderedFaces = rendered;
        hudTotalFaces = total;
        hudHasMesh = total > 0;
    }

    public EditorUI(UIResources res, int w, int h,
                    Runnable onSave, Runnable onQuit) {
        super(res);

        filter = new FilterPanel(res);
        newMenu = new NewMenu(res);
        toolsPal = new ToolPalette(res);
        confirmSave = new ConfirmSavePopup(res);
        confirmDelete = new ConfirmDeletePopup(res);
        entityList = new EntityListPanel(res);
        origin = new OriginPanel(res);

        saveBtn = new Button(res, "Sauvegarder", 0, 0, BTN_W, BAR_H, onSave);
        saveBtn.textScale = 1.2f;
        quitBtn = new Button(res, "Quitter", 0, 0, BTN_W, BAR_H, onQuit);
        quitBtn.textScale = 1.2f;
        newBtn = new Button(res, "New", 0, 0, BTN_W, BAR_H, () -> {
            newMenu.toggle();
            toolsPal.close();
            filter.setOpen(false);
            origin.setOpen(false);
        });
        newBtn.textScale = 1.2f;
        filterBtn = new Button(res, "Filtre", 0, 0, BTN_W, BAR_H, () -> {
            filter.toggle();
            newMenu.close();
            toolsPal.close();
            origin.setOpen(false);
        });
        filterBtn.textScale = 1.2f;
        outilsBtn = new Button(res, "Outils", 0, 0, BTN_W, BAR_H, () -> {
            toolsPal.toggle();
            newMenu.close();
            filter.setOpen(false);
            origin.setOpen(false);
        });
        outilsBtn.textScale = 1.2f;
        origineBtn = new Button(res, "Origine", 0, 0, BTN_W, BAR_H, () -> {
            origin.toggle();
            newMenu.close();
            filter.setOpen(false);
            toolsPal.close();
        });
        origineBtn.textScale = 1.2f;

        addChild(saveBtn);
        addChild(quitBtn);
        addChild(newBtn);
        addChild(filterBtn);
        addChild(outilsBtn);
        addChild(origineBtn);
        addChild(toolsPal);
        addChild(newMenu);
        addChild(filter);
        addChild(origin);
        addChild(confirmSave);
        addChild(confirmDelete);

        setSize(w, h);
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        res.setSize(w, h);
        x = 0;
        y = 0;
        this.w = w;
        this.h = BAR_H;
        filter.setSize(w, h);
        confirmSave.setSize(w, h);
        confirmDelete.setSize(w, h);
        newMenu.setSize(w, h);
        toolsPal.setSize(w, h);
        entityList.setSize(w, h);

        saveBtn.x = width - BTN_W * 2 - 10;
        saveBtn.y = 0;
        quitBtn.x = width - BTN_W - 5;
        quitBtn.y = 0;
        newBtn.x = width - BTN_W * 5 - 25;
        newBtn.y = 0;
        filterBtn.x = width - BTN_W * 3 - 15;
        filterBtn.y = 0;
        outilsBtn.x = width - BTN_W * 6 - 30;
        outilsBtn.y = 0;
        origineBtn.x = width - BTN_W * 4 - 20;
        origineBtn.y = 0;
        syncFromConfig();
    }

    public void syncFromConfig() {
        transparentBar = BlurBackground.transparentUI;
        boolean opaque = !BlurBackground.transparentUI;
        saveBtn.showBackground = opaque;
        quitBtn.showBackground = opaque;
        newBtn.showBackground = opaque;
        filterBtn.showBackground = opaque;
        outilsBtn.showBackground = opaque;
        origineBtn.showBackground = opaque;

        if (opaque) {
            saveBtn.bgR = BlurBackground.menuR; saveBtn.bgG = BlurBackground.menuG; saveBtn.bgB = BlurBackground.menuB;
            quitBtn.bgR = BlurBackground.menuR; quitBtn.bgG = BlurBackground.menuG; quitBtn.bgB = BlurBackground.menuB;
            newBtn.bgR = BlurBackground.menuR; newBtn.bgG = BlurBackground.menuG; newBtn.bgB = BlurBackground.menuB;
            filterBtn.bgR = BlurBackground.menuR; filterBtn.bgG = BlurBackground.menuG; filterBtn.bgB = BlurBackground.menuB;
            outilsBtn.bgR = BlurBackground.menuR; outilsBtn.bgG = BlurBackground.menuG; outilsBtn.bgB = BlurBackground.menuB;
            origineBtn.bgR = BlurBackground.menuR; origineBtn.bgG = BlurBackground.menuG; origineBtn.bgB = BlurBackground.menuB;
        }

        ConfigParametres cfg = ConfigParametres.get();

        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        saveBtn.textR = tR; saveBtn.textG = tG; saveBtn.textB = tB;
        quitBtn.textR = tR; quitBtn.textG = tG; quitBtn.textB = tB;
        filterBtn.textR = tR; filterBtn.textG = tG; filterBtn.textB = tB;
        newBtn.textR = tR; newBtn.textG = tG; newBtn.textB = tB;
        outilsBtn.textR = tR; outilsBtn.textG = tG; outilsBtn.textB = tB;
        origineBtn.textR = tR; origineBtn.textG = tG; origineBtn.textB = tB;

        if (opaque) {
            setActiveMode(newMenu.getActiveMode());
        }
    }

    public void render(String currentFile) {
        if (BlurBackground.menuR != lastMenuR || BlurBackground.menuG != lastMenuG || BlurBackground.menuB != lastMenuB
            || BlurBackground.transparentUI != lastTransparentUI) {
            lastMenuR = BlurBackground.menuR; lastMenuG = BlurBackground.menuG; lastMenuB = BlurBackground.menuB;
            lastTransparentUI = BlurBackground.transparentUI;
            syncFromConfig();
        }
        this.currentFile = currentFile;
        render();
    }

    @Override
    protected void drawBackground() {
        float[] c = res.menuColor();
        res.drawQuad(0, 0, width, BAR_H, c[0], c[1], c[2], BlurBackground.panelAlpha());
    }

    @Override
    protected void renderContent() {
        String label = currentFile != null ? currentFile.replace(".json", "") : "[no shape]";
        float[] t = res.textColor();
        res.drawText("MarkerShape - " + label, 10, 11, 1.2f, t[0], t[1], t[2]);

        if (hudHasMesh) {
            float tr, tg, tb;
            switch (hudLodLevel) {
                case 0: tr = 0.5f; tg = 0.9f; tb = 0.5f; break;
                case 1: tr = 1f; tg = 0.85f; tb = 0.3f; break;
                case 2: tr = 1f; tg = 0.6f; tb = 0.2f; break;
                default: tr = 1f; tg = 0.35f; tb = 0.25f; break;
            }
            String s = String.format("LOD %d  %d/%d faces  d=%.1f",
                hudLodLevel, hudRenderedFaces, hudTotalFaces, hudLodDistance);
            float[] ext = res.getTextExtent(s, 1.2f);
            res.drawText(s, width - ext[0] - 12, height - 25, 1.2f, tr, tg, tb);
        }

        newMenu.setBtnPos(newBtn.x, newBtn.y);
        toolsPal.setBtnPos(outilsBtn.x, newBtn.y);
        filter.setPosition(filterBtn.x, BAR_H);
        origin.setPosition(origineBtn.x, BAR_H);
    }

    public void renderEntityList() {
        entityList.render();
    }

    public boolean isOverUI(float mx, float my) {
        if (my < BAR_H) return true;
        if (filter.contains(mx, my)) return true;
        if (origin.contains(mx, my)) return true;
        if (newMenu.contains(mx, my)) return true;
        if (toolsPal.contains(mx, my)) return true;
        if (confirmSave.contains(mx, my)) return true;
        if (confirmDelete.contains(mx, my)) return true;
        if (entityList.contains(mx, my)) return true;
        return false;
    }

    public boolean isSaveClicked(float mx, float my) {
        return saveBtn.contains(mx, my);
    }

    public boolean isQuitClicked(float mx, float my) {
        return quitBtn.contains(mx, my);
    }

    public int clickNew(float mx, float my) {
        if (newBtn.contains(mx, my)) {
            newBtn.click(mx, my);
            return -2;
        }
        return newMenu.clickItem(mx, my);
    }

    public int clickTools(float mx, float my) {
        if (outilsBtn.contains(mx, my)) {
            outilsBtn.click(mx, my);
            return -2;
        }
        return toolsPal.clickItem(mx, my);
    }

    public boolean isToolsOpen() { return toolsPal.isOpen(); }
    public void closeToolsPal() { toolsPal.close(); }

    public int clickEntityList(float mx, float my) {
        return entityList.clickList(mx, my);
    }

    public void setActiveMode(int mode) {
        newMenu.setActiveMode(mode);
        toolsPal.setActiveMode(mode);
        if (BlurBackground.transparentUI) {
            newBtn.textR = 1f; newBtn.textG = 1f; newBtn.textB = 1f;
            if (mode == 0) { newBtn.textR = 1f; newBtn.textG = 0.7f; newBtn.textB = 0.3f; }
            else if (mode == 1) { newBtn.textR = 1f; newBtn.textG = 0.3f; newBtn.textB = 0.3f; }
        } else {
            if (mode == 0) {
                newBtn.bgR = 0.4f; newBtn.bgG = 0.25f; newBtn.bgB = 0.15f;
            } else if (mode == 1) {
                newBtn.bgR = 0.4f; newBtn.bgG = 0.15f; newBtn.bgB = 0.15f;
            } else {
                newBtn.bgR = 0.25f; newBtn.bgG = 0.3f; newBtn.bgB = 0.25f;
            }
            float tc = TextColor.contrast(newBtn.bgR, newBtn.bgG, newBtn.bgB);
            newBtn.textR = tc; newBtn.textG = tc; newBtn.textB = tc;
        }
    }

    public void closeNewMenu() { newMenu.close(); }

    public void showConfirmSave() { confirmSave.show(); }
    public void closeConfirmSave() { confirmSave.close(); }
    public boolean isConfirmSaveVisible() { return confirmSave.isVisible(); }
    public void setConfirmSaveAction(Runnable r) { confirmSave.setConfirmAction(r); }
    public Runnable getConfirmSaveAction() { return confirmSave.getConfirmAction(); }
    public int clickConfirmSave(float mx, float my) { return confirmSave.clickBtn(mx, my); }

    public void showConfirmDelete() { confirmDelete.show(); }
    public void closeConfirmDelete() { confirmDelete.close(); }
    public boolean isConfirmDeleteVisible() { return confirmDelete.isVisible(); }
    public void setConfirmDeleteAction(Runnable r) { confirmDelete.setConfirmAction(r); }
    public Runnable getConfirmDeleteAction() { return confirmDelete.getConfirmAction(); }
    public int clickConfirmDelete(float mx, float my) { return confirmDelete.clickBtn(mx, my); }

    public int clickFilter(float mx, float my) {
        if (filterBtn.contains(mx, my)) { filterBtn.click(mx, my); newMenu.close(); return -2; }
        return filter.clickFilter(mx, my);
    }

    public int clickOrigin(float mx, float my) {
        if (origineBtn.contains(mx, my)) { origineBtn.click(mx, my); newMenu.close(); return 0; }
        return origin.clickOrigin(mx, my);
    }

    public void closeOrigin() { origin.setOpen(false); }
    public boolean isOriginOpen() { return origin.isOpen(); }

    public boolean isFilterOpen() { return filter.isOpen(); }
    public boolean[] getFilterValues() { return filter.filterValues; }
    public float[] getSliderValues() { return filter.sliderValues; }
    public boolean isSnapEnabled() { return filter.isSnapEnabled(); }
    public float getSnapStep() { return filter.getSnapStep(); }
    public void setSnapEnabled(boolean v) { filter.setSnapEnabled(v); }
    public void setSnapStep(float v) { filter.setSnapStep(v); }
    public boolean isMagnetEnabled() { return filter.isMagnetEnabled(); }
    public void setMagnetEnabled(boolean v) { filter.setMagnetEnabled(v); }
    public float getMagnetRadius() { return filter.getMagnetRadius(); }
    public void setMagnetRadius(float v) { filter.setMagnetRadius(v); }

    public void setFilterCallback(Runnable cb) { filter.setFilterCallback(cb); }
}
