package markershape.editor.ui.menu;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import markershape.config.ConfigParametres;
import markershape.editor.ui.control.Button;
import markershape.editor.ui.util.TextColor;
import markershape.shape.ShapeLoader;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class MenuUI {
    private int width, height;
    private Shader shader, textShader;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    private String[] shapes;
    private static final int PANEL_W = 480;
    private static final int ITEM_H = 40;
    private static final int ITEM_GAP = 4;
    private static final int PANEL_Y = 140;
    private Button paramBtn, quitBtn;
    private Runnable onQuit, onParams;
    private float lastBgR = -1f, lastBgG = -1f, lastBgB = -1f;

    public MenuUI(int w, int h, Runnable onQuit, Runnable onParams) {
        this.onQuit = onQuit;
        this.onParams = onParams;
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
        refresh();
        setSize(w, h);
    }

    public void refresh() {
        shapes = ShapeLoader.listShapes();
        if (shapes == null) shapes = new String[0];
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        ortho.setOrtho2D(0, width, height, 0);
    }

    public void render() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        ConfigParametres cfg = ConfigParametres.get();
        float bgR = BlurBackground.menuR;
        float bgG = BlurBackground.menuG;
        float bgB = BlurBackground.menuB;

        float cx = width / 2f;
        float px = cx - PANEL_W / 2f;

        float listH = shapes.length * (ITEM_H + ITEM_GAP);
        float panelH = listH + 40;

        float panelAlpha = BlurBackground.panelAlpha();
        float rowAlpha = BlurBackground.rowAlpha();
        shader.bind();
        shader.setUniformMat4f("projection", ortho);
        drawQuad(px, PANEL_Y, PANEL_W, panelH, bgR, bgG, bgB, panelAlpha);
        for (int i = 0; i < shapes.length; i++) {
            float y = PANEL_Y + 20 + i * (ITEM_H + ITEM_GAP);
            float aOff = (i % 2 == 0 ? 0.03f : 0f);
            drawQuad(px + 10, y, PANEL_W - 20, ITEM_H, bgR + aOff, bgG + aOff, bgB + aOff, rowAlpha);
        }
        shader.unbind();

        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        Text.drawText(textShader, "MarkerShape",
            cx - Text.getTextExtent("MarkerShape", 4f)[0] / 2f, 40, 4f, tR, tG, tB);
        Text.drawText(textShader, "Editeur de modeles 3D",
            cx - Text.getTextExtent("Editeur de modeles 3D", 1.8f)[0] / 2f, 85, 1.8f, tR, tG, tB);

        for (int i = 0; i < shapes.length; i++) {
            String name = shapes[i].replace(".json", "");
            float y = PANEL_Y + 20 + i * (ITEM_H + ITEM_GAP);
            Text.drawText(textShader, name,
                cx - Text.getTextExtent(name, 2.2f)[0] / 2f, y + 8, 2.2f, tR, tG, tB);
        }

        float by = PANEL_Y + panelH + 16;
        float btnW = 180;
        float btnH = 38;
        float gap = 20;
        float totalW = btnW * 2 + gap;
        float bx = cx - totalW / 2f;

        if (paramBtn == null || paramBtn.x != bx || paramBtn.y != by) {
            paramBtn = new Button("Parametres", bx, by, btnW, btnH, onParams);
            paramBtn.textScale = 2.2f;
            paramBtn.bgR = bgR + 0.05f; paramBtn.bgG = bgG + 0.05f; paramBtn.bgB = bgB + 0.1f;

            quitBtn = new Button("Quitter", bx + btnW + gap, by, btnW, btnH, onQuit);
            quitBtn.textScale = 2.2f;
            quitBtn.bgR = bgR + 0.1f; quitBtn.bgG = bgG + 0.02f; quitBtn.bgB = bgB + 0.02f;
        }

        float btnAlpha = BlurBackground.btnAlpha();
        paramBtn.bgA = btnAlpha;
        quitBtn.bgA = btnAlpha;
        paramBtn.textR = tR; paramBtn.textG = tG; paramBtn.textB = tB;
        quitBtn.textR = tR; quitBtn.textG = tG; quitBtn.textB = tB;

        paramBtn.render(shader, textShader, ortho, buf, vao, vbo);
        quitBtn.render(shader, textShader, ortho, buf, vao, vbo);
    }

    public String clickShape(float mx, float my) {
        float cx = width / 2f;
        float px = cx - PANEL_W / 2f;
        float listH = shapes.length * (ITEM_H + ITEM_GAP);
        float panelH = listH + 40;
        if (mx < px + 10 || mx > px + PANEL_W - 10) return null;
        if (my < PANEL_Y + 20 || my > PANEL_Y + panelH - 20) return null;
        for (int i = 0; i < shapes.length; i++) {
            float y = PANEL_Y + 20 + i * (ITEM_H + ITEM_GAP);
            if (my >= y && my <= y + ITEM_H) return shapes[i];
        }
        return null;
    }

    public boolean isParametresClicked(float mx, float my) {
        if (paramBtn != null && paramBtn.isClicked(mx, my)) { paramBtn.click(); return true; }
        return false;
    }

    public boolean isQuitterClicked(float mx, float my) {
        if (quitBtn != null && quitBtn.isClicked(mx, my)) { quitBtn.click(); return true; }
        return false;
    }

    public void cleanup() {
        shader.cleanup();
        textShader.cleanup();
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }

    private void drawQuad(float x, float y, float w, float h,
                          float r, float g, float b, float a) {
        float[] verts = {
            x,   y,   r, g, b, a,
            x+w, y,   r, g, b, a,
            x+w, y+h, r, g, b, a,
            x,   y,   r, g, b, a,
            x+w, y+h, r, g, b, a,
            x,   y+h, r, g, b, a
        };
        buf.clear();
        buf.put(verts).flip();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
}
