package markershape.ui;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class EditorUI {
    private int width;
    private Shader shader, textShader;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    private static final int BAR_H = 36;
    private static final int BTN_W = 130;
    private Button saveBtn, quitBtn;
    private Runnable onSave, onQuit;

    public EditorUI(int w, int h, Runnable onSave, Runnable onQuit) {
        this.onSave = onSave;
        this.onQuit = onQuit;
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
        setSize(w, h);
    }

    public void setSize(int w, int h) {
        width = w;
        ortho.setOrtho2D(0, width, h, 0);
        saveBtn = new Button("Sauvegarder", width - BTN_W * 2 - 10, 0, BTN_W, BAR_H, onSave);
        saveBtn.textScale = 1.5f;
        saveBtn.bgR = 0.2f; saveBtn.bgG = 0.2f; saveBtn.bgB = 0.3f;
        quitBtn = new Button("Quitter", width - BTN_W - 5, 0, BTN_W, BAR_H, onQuit);
        quitBtn.textScale = 1.5f;
        quitBtn.bgR = 0.3f; quitBtn.bgG = 0.1f; quitBtn.bgB = 0.1f;
    }

    public void render(String currentFile) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        saveBtn.render(shader, textShader, ortho, buf, vao, vbo);
        quitBtn.render(shader, textShader, ortho, buf, vao, vbo);

        String label = currentFile != null ? currentFile.replace(".json", "") : "[no shape]";
        Text.drawText(textShader, "MarkerShape - " + label, 10, 10, 1.5f, 1f, 1f, 1f);
    }

    public boolean isOverUI(float mx, float my) {
        return my < BAR_H;
    }

    public boolean isSaveClicked(float mx, float my) {
        if (saveBtn.isClicked(mx, my)) { saveBtn.click(); return true; }
        return false;
    }

    public boolean isQuitClicked(float mx, float my) {
        if (quitBtn.isClicked(mx, my)) { quitBtn.click(); return true; }
        return false;
    }

    public Shader shader() { return shader; }
    public Shader textShader() { return textShader; }
    public Matrix4f ortho() { return ortho; }
    public FloatBuffer buf() { return buf; }
    public int vao() { return vao; }
    public int vbo() { return vbo; }

    public void cleanup() {
        shader.cleanup();
        textShader.cleanup();
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}
