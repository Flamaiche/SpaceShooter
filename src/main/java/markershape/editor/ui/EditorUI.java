package markershape.editor.ui;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.editor.ui.control.Button;
import markershape.editor.ui.control.EntityListPanel;
import markershape.editor.ui.control.FilterPanel;
import markershape.editor.ui.menu.BlurBackground;
import markershape.editor.ui.menu.ConfirmSavePopup;
import markershape.editor.ui.menu.NewMenu;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class EditorUI {
    private int width, height;
    private Shader shader, textShader, blurShader;
    private int vao, vbo;

    public static final int BAR_H = 36;
    public static final int BTN_W = 130;
    private Button saveBtn, quitBtn, filterBtn, newBtn;
    private Runnable onSave, onQuit, onNewEdge, onNewVertex;

    public boolean transparentBar = true;

    public final FilterPanel filter;
    public final NewMenu newMenu;
    public final ConfirmSavePopup confirmSave;
    public final BlurBackground blur;
    public final EntityListPanel entityList;

    public EditorUI(int w, int h, Runnable onSave, Runnable onQuit, Runnable onNewEdge, Runnable onNewVertex) {
        this.onSave = onSave;
        this.onQuit = onQuit;
        this.onNewEdge = onNewEdge;
        this.onNewVertex = onNewVertex;

        shader = new Shader("shaders/markershape/ui_Vertex.glsl",
                            "shaders/markershape/ui_Fragment.glsl");
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
        blurShader = new Shader("shaders/markershape/blur_Vertex.glsl",
                                "shaders/markershape/blur_Fragment.glsl");

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

        blur = new BlurBackground(blurShader, vao, vbo);
        filter = new FilterPanel(shader, textShader, vao, vbo, blur);
        newMenu = new NewMenu(shader, textShader, vao, vbo, blur);
        confirmSave = new ConfirmSavePopup(blur, textShader);
        entityList = new EntityListPanel(shader, textShader, vao, vbo, blur);

        setSize(w, h);
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        blur.setSize(w, h);
        filter.setSize(w, h);
        confirmSave.setSize(w, h);
        newMenu.setSize(w, h);
        entityList.setSize(w, h);

        saveBtn = new Button("Sauvegarder", width - BTN_W * 2 - 10, 0, BTN_W, BAR_H, onSave);
        saveBtn.textScale = 1.5f;
        saveBtn.showBackground = !transparentBar;

        quitBtn = new Button("Quitter", width - BTN_W - 5, 0, BTN_W, BAR_H, onQuit);
        quitBtn.textScale = 1.5f;
        quitBtn.showBackground = !transparentBar;

        newBtn = new Button("New", width - BTN_W * 4 - 25, 0, BTN_W, BAR_H, () -> {
            newMenu.toggle();
            filter.setOpen(false);
        });
        newBtn.textScale = 1.5f;
        newBtn.showBackground = !transparentBar;

        filterBtn = new Button("Filtre", width - BTN_W * 3 - 20, 0, BTN_W, BAR_H, () -> {
            filter.toggle();
            newMenu.close();
        });
        filterBtn.textScale = 1.5f;
        filterBtn.showBackground = !transparentBar;
        syncFromConfig();
    }

    public void syncFromConfig() {
        transparentBar = BlurBackground.transparentUI;
        boolean opaque = !BlurBackground.transparentUI;
        saveBtn.showBackground = opaque;
        quitBtn.showBackground = opaque;
        newBtn.showBackground = opaque;
        filterBtn.showBackground = opaque;

        if (opaque) {
            saveBtn.bgR = BlurBackground.menuR; saveBtn.bgG = BlurBackground.menuG; saveBtn.bgB = BlurBackground.menuB;
            quitBtn.bgR = BlurBackground.menuR; quitBtn.bgG = BlurBackground.menuG; quitBtn.bgB = BlurBackground.menuB;
            newBtn.bgR = BlurBackground.menuR; newBtn.bgG = BlurBackground.menuG; newBtn.bgB = BlurBackground.menuB;
            filterBtn.bgR = BlurBackground.menuR; filterBtn.bgG = BlurBackground.menuG; filterBtn.bgB = BlurBackground.menuB;
        }
    }

    public void render(String currentFile) {
        blur.captureScreen();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if (BlurBackground.transparentUI) {
            blur.drawBlurredBg(0, 0, width, BAR_H, 0.75f, BlurBackground.menuR, BlurBackground.menuG, BlurBackground.menuB);
        } else {
            FloatBuffer b = buf();
            float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
            b.put(new float[]{
                0f, 0f, mr, mg, mb, 1f,
                (float)width, 0f, mr, mg, mb, 1f,
                (float)width, (float)BAR_H, mr, mg, mb, 1f,
                0f, 0f, mr, mg, mb, 1f,
                (float)width, (float)BAR_H, mr, mg, mb, 1f,
                0f, (float)BAR_H, mr, mg, mb, 1f,
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
        }
        saveBtn.render(shader, textShader, ortho(), buf(), vao, vbo);
        quitBtn.render(shader, textShader, ortho(), buf(), vao, vbo);
        newBtn.render(shader, textShader, ortho(), buf(), vao, vbo);
        filterBtn.render(shader, textShader, ortho(), buf(), vao, vbo);

        newMenu.setBtnPos(newBtn.x, newBtn.y);
        newMenu.render();

        filter.render(filterBtn.x, BAR_H);

        String label = currentFile != null ? currentFile.replace(".json", "") : "[no shape]";
        float tc = BlurBackground.textColor();
        Text.drawText(textShader, "MarkerShape - " + label, 10, 10, 1.5f, tc, tc, tc);

        if (!BlurBackground.transparentUI && confirmSave.isVisible()) {
            float cx = (width - ConfirmSavePopup.CONFIRM_W) / 2;
            float cy = (36 + (height - 36) / 2) - ConfirmSavePopup.CONFIRM_H / 2;
            FloatBuffer b = buf();
            float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
            b.put(new float[]{
                cx, cy, mr, mg, mb, 0.92f,
                cx+ConfirmSavePopup.CONFIRM_W, cy, mr, mg, mb, 0.92f,
                cx+ConfirmSavePopup.CONFIRM_W, cy+ConfirmSavePopup.CONFIRM_H, mr, mg, mb, 0.92f,
                cx, cy, mr, mg, mb, 0.92f,
                cx+ConfirmSavePopup.CONFIRM_W, cy+ConfirmSavePopup.CONFIRM_H, mr, mg, mb, 0.92f,
                cx, cy+ConfirmSavePopup.CONFIRM_H, mr, mg, mb, 0.92f,
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
        }

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
        return saveBtn.isClicked(mx, my);
    }

    public boolean isQuitClicked(float mx, float my) {
        return quitBtn.isClicked(mx, my);
    }

    public int clickNew(float mx, float my) {
        if (newBtn.isClicked(mx, my)) {
            newBtn.click();
            return -2;
        }
        return newMenu.click(mx, my);
    }

    public int clickEntityList(float mx, float my) {
        return entityList.click(mx, my);
    }

    public void setActiveMode(int mode) {
        newMenu.setActiveMode(mode);
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
        }
    }

    public void closeNewMenu() { newMenu.close(); }

    public void showConfirmSave() { confirmSave.show(); }
    public void closeConfirmSave() { confirmSave.close(); }
    public boolean isConfirmSaveVisible() { return confirmSave.isVisible(); }
    public void setConfirmSaveAction(Runnable r) { confirmSave.setConfirmAction(r); }
    public Runnable getConfirmSaveAction() { return confirmSave.getConfirmAction(); }
    public int clickConfirmSave(float mx, float my) { return confirmSave.click(mx, my); }

    public int clickFilter(float mx, float my) {
        if (filterBtn.isClicked(mx, my)) { filterBtn.click(); newMenu.close(); return -2; }
        return filter.clickFilter(mx, my, filterBtn.x);
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
        shader.cleanup();
        textShader.cleanup();
        blurShader.cleanup();
        blur.cleanup();
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}
