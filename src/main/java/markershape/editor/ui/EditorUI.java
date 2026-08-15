package markershape.editor.ui;

import learngl.Shader;
import markershape.config.ConfigParametres;
import markershape.editor.ui.control.EntityListPanel;
import markershape.editor.ui.control.FilterPanel;
import markershape.editor.ui.framework.UIButton;
import markershape.editor.ui.framework.UIContainer;
import markershape.editor.ui.framework.UIRenderer;
import markershape.editor.ui.framework.UIText;
import markershape.editor.ui.menu.BlurBackground;
import markershape.editor.ui.menu.ConfirmSavePopup;
import markershape.editor.ui.menu.NewMenu;
import markershape.editor.ui.util.TextColor;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class EditorUI {
    private int width, height;
    private Shader shader, textShader;
    private int vao, vbo;

    public static final int BAR_H = 36;
    public static final int BTN_W = 130;

    public final FilterPanel filter;
    public final NewMenu newMenu;
    public final ConfirmSavePopup confirmSave;
    public final EntityListPanel entityList;

    private final UIRenderer uiRenderer = new UIRenderer();
    private final UIContainer root = new UIContainer(0, 0, 1, 1);
    private UIButton saveBtn, quitBtn, filterBtn, newBtn;

    public EditorUI(int w, int h, Runnable onSave, Runnable onQuit, Runnable onNewEdge, Runnable onNewVertex) {
        root.alpha = UIContainer.Alpha.NONE;
        shader = new Shader("shaders/markershape/ui_Vertex.glsl",
                            "shaders/markershape/ui_Fragment.glsl");
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 6 * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 6 * 4, 2 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        filter = new FilterPanel(shader, textShader, vao, vbo);
        newMenu = new NewMenu();
        confirmSave = new ConfirmSavePopup(textShader);
        entityList = new EntityListPanel(shader, textShader, vao, vbo);

        setSize(w, h);
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        uiRenderer.setScreenSize(w, h);
        filter.setSize(w, h);
        confirmSave.setSize(w, h);
        newMenu.setSize(w, h);
        entityList.setSize(w, h);
    }

    public void syncFromConfig() {
    }

    private static float ts(float designScale) { return designScale / 720f; }

    private void buildBar(String currentFile) {
        root.clear();
        boolean opaque = !BlurBackground.transparentUI;
        float fw = width, fh = height;

        UIContainer bar = new UIContainer(0, 0, 1, BAR_H / fh);
        bar.alpha = UIContainer.Alpha.PANEL;
        bar.bgR = BlurBackground.menuR;
        bar.bgG = BlurBackground.menuG;
        bar.bgB = BlurBackground.menuB;
        root.add(bar);

        float btnW = BTN_W / fw;
        saveBtn = makeBarButton((width - BTN_W * 2 - 10) / fw, btnW, "Sauvegarder", opaque);
        quitBtn = makeBarButton((width - BTN_W - 5) / fw, btnW, "Quitter", opaque);
        newBtn = makeBarButton((width - BTN_W * 4 - 25) / fw, btnW, "New", opaque);
        filterBtn = makeBarButton((width - BTN_W * 3 - 20) / fw, btnW, "Filtre", opaque);
        bar.add(saveBtn);
        bar.add(quitBtn);
        bar.add(newBtn);
        bar.add(filterBtn);
        applyNewBtnStyle(opaque);

        String label = currentFile != null ? currentFile.replace(".json", "") : "[no shape]";
        UIText fileLabel = new UIText(10f / fw, 10f / fh, ts(1.5f), "MarkerShape - " + label);
        root.add(fileLabel);
    }

    private UIButton makeBarButton(float x, float w, String label, boolean opaque) {
        UIButton b = new UIButton(x, 0, w, 1f, label, ts(1.5f));
        if (opaque) {
            b.alpha = UIContainer.Alpha.BTN;
            b.bgR = BlurBackground.menuR;
            b.bgG = BlurBackground.menuG;
            b.bgB = BlurBackground.menuB;
        } else {
            b.alpha = UIContainer.Alpha.NONE;
        }
        return b;
    }

    private void applyNewBtnStyle(boolean opaque) {
        int mode = newMenu.getActiveMode();
        if (!opaque) {
            newBtn.useConfigText = false;
            newBtn.tR = 1f; newBtn.tG = 1f; newBtn.tB = 1f;
            if (mode == 0) { newBtn.tG = 0.7f; newBtn.tB = 0.3f; }
            else if (mode == 1) { newBtn.tG = 0.3f; newBtn.tB = 0.3f; }
        } else {
            if (mode == 0) {
                newBtn.bgR = 0.4f; newBtn.bgG = 0.25f; newBtn.bgB = 0.15f;
                float tc = TextColor.contrast(newBtn.bgR, newBtn.bgG, newBtn.bgB);
                newBtn.useConfigText = false;
                newBtn.tR = tc; newBtn.tG = tc; newBtn.tB = tc;
            } else if (mode == 1) {
                newBtn.bgR = 0.4f; newBtn.bgG = 0.15f; newBtn.bgB = 0.15f;
                float tc = TextColor.contrast(newBtn.bgR, newBtn.bgG, newBtn.bgB);
                newBtn.useConfigText = false;
                newBtn.tR = tc; newBtn.tG = tc; newBtn.tB = tc;
            } else {
                newBtn.bgR = BlurBackground.menuR;
                newBtn.bgG = BlurBackground.menuG;
                newBtn.bgB = BlurBackground.menuB;
                newBtn.useConfigText = true;
            }
        }
    }

    public void render(String currentFile) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        buildBar(currentFile);
        root.render(uiRenderer);

        newMenu.setBtnPos(newBtn.absX(uiRenderer), newBtn.absY(uiRenderer));
        newMenu.render();

        filter.render(filterBtn.absX(uiRenderer), BAR_H);

        drawConfirmSave();
    }

    public void renderConfirmOnly() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        drawConfirmSave();
    }

    private void drawConfirmSave() {
        if (!confirmSave.isVisible()) return;
        float cx = (width - ConfirmSavePopup.CONFIRM_W) / 2;
        float cy = (36 + (height - 36) / 2) - ConfirmSavePopup.CONFIRM_H / 2;
        float boxA = BlurBackground.boxAlpha();
        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;

        FloatBuffer b = buf();
        b.put(new float[]{
            cx, cy, mr, mg, mb, boxA,
            cx+ConfirmSavePopup.CONFIRM_W, cy, mr, mg, mb, boxA,
            cx+ConfirmSavePopup.CONFIRM_W, cy+ConfirmSavePopup.CONFIRM_H, mr, mg, mb, boxA,
            cx, cy, mr, mg, mb, boxA,
            cx+ConfirmSavePopup.CONFIRM_W, cy+ConfirmSavePopup.CONFIRM_H, mr, mg, mb, boxA,
            cx, cy+ConfirmSavePopup.CONFIRM_H, mr, mg, mb, boxA,
        }).flip();
        shader.bind();
        shader.setUniformMat4f("projection", ortho());
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, b, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        shader.unbind();

        confirmSave.render();
    }

    public void renderEntityList(int w, int h) {
        entityList.render(w, h);
    }

    public boolean isOverUI(float mx, float my) {
        if (my < BAR_H) return true;
        if (filter.contains(mx, my)) return true;
        if (newMenu.contains(mx, my)) return true;
        if (confirmSave.contains(mx, my)) return true;
        if (entityList.contains(mx, my)) return true;
        return false;
    }

    public boolean isSaveClicked(float mx, float my) {
        return saveBtn != null && saveBtn.contains(mx, my, uiRenderer);
    }

    public boolean isQuitClicked(float mx, float my) {
        return quitBtn != null && quitBtn.contains(mx, my, uiRenderer);
    }

    public int clickNew(float mx, float my) {
        if (newBtn != null && newBtn.contains(mx, my, uiRenderer)) {
            newMenu.toggle();
            filter.setOpen(false);
            return -2;
        }
        return newMenu.click(mx, my);
    }

    public int clickEntityList(float mx, float my) {
        return entityList.click(mx, my);
    }

    public void setActiveMode(int mode) {
        newMenu.setActiveMode(mode);
    }

    public void closeNewMenu() { newMenu.close(); }

    public void showConfirmSave() { confirmSave.show(); }
    public void closeConfirmSave() { confirmSave.close(); }
    public boolean isConfirmSaveVisible() { return confirmSave.isVisible(); }
    public void setConfirmSaveAction(Runnable r) { confirmSave.setConfirmAction(r); }
    public Runnable getConfirmSaveAction() { return confirmSave.getConfirmAction(); }
    public int clickConfirmSave(float mx, float my) { return confirmSave.click(mx, my); }

    public int clickFilter(float mx, float my) {
        if (filterBtn != null && filterBtn.contains(mx, my, uiRenderer)) {
            filter.toggle();
            newMenu.close();
            return -2;
        }
        return filter.clickFilter(mx, my, filterBtn.absX(uiRenderer));
    }

    public boolean isFilterOpen() { return filter.isOpen(); }
    public boolean[] getFilterValues() { return filter.filterValues; }
    public float[] getSliderValues() { return filter.sliderValues; }
    public boolean isSnapEnabled() { return filter.isSnapEnabled(); }
    public float getSnapStep() { return filter.getSnapStep(); }
    public void setSnapEnabled(boolean v) { filter.setSnapEnabled(v); }
    public void setSnapStep(float v) { filter.setSnapStep(v); }

    public void setFilterCallback(Runnable cb) { filter.setFilterCallback(cb); }

    private final org.joml.Matrix4f _ortho = new org.joml.Matrix4f();
    public org.joml.Matrix4f ortho() {
        _ortho.setOrtho2D(0, width, height, 0);
        return _ortho;
    }

    private final java.nio.FloatBuffer _buf = org.lwjgl.BufferUtils.createFloatBuffer(6 * 6);
    public java.nio.FloatBuffer buf() { _buf.clear(); return _buf; }

    public Shader shader() { return shader; }
    public Shader textShader() { return textShader; }
    public int vao() { return vao; }
    public int vbo() { return vbo; }

    public void cleanup() {
        uiRenderer.cleanup();
        newMenu.cleanup();
        shader.cleanup();
        textShader.cleanup();
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}
