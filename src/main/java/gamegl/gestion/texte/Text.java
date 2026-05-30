package gamegl.gestion.texte;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import learngl.Shader;

/**
 * Gestion de l'affichage de texte avec OpenGL et STB Easy Font.
 */
public class Text {
    private static int vao, vbo;
    private static boolean initialized = false;
    private static ByteBuffer textBuffer;
    private static int lastVpW = -1, lastVpH = -1;
    private static final Matrix4f orthoMatrix = new Matrix4f();
    private static final int[] vp = new int[4];

    private static void init() {
        if (initialized) return;
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        initialized = true;
    }

    private static ByteBuffer ensureBuffer(int textLength) {
        int needed = textLength * 300;
        if (textBuffer == null || textBuffer.capacity() < needed) {
            textBuffer = BufferUtils.createByteBuffer(needed);
        }
        textBuffer.clear();
        return textBuffer;
    }

    /**
     * Dessine un texte à l'écran.
     *
     * @param shader le shader à utiliser
     * @param text   le texte à afficher
     * @param x      position X
     * @param y      position Y
     * @param scale  échelle du texte
     * @param r      composante rouge de la couleur
     * @param g      composante verte de la couleur
     * @param b      composante bleue de la couleur
     */
    public static void drawText(Shader shader, String text,
                                float x, float y, float scale,
                                float r, float g, float b) {
        if (text == null || text.isEmpty()) return;
        init();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        ByteBuffer buffer = ensureBuffer(text.length());
        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, buffer);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0);

        shader.bind();

        glGetIntegerv(GL_VIEWPORT, vp);
        int winW = vp[2], winH = vp[3];
        if (winW != lastVpW || winH != lastVpH) {
            orthoMatrix.identity().ortho2D(0f, winW, winH, 0f);
            lastVpW = winW;
            lastVpH = winH;
        }
        shader.setUniformMat4f("projection", orthoMatrix);

        shader.setUniform2f("offset", x, y);
        shader.setUniform1f("scale", scale);
        shader.setUniform3f("textColor", r, g, b);

        glDrawArrays(GL_QUADS, 0, quads * 4);

        shader.unbind();
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    /**
     * Calcule les dimensions du texte après rendu.
     *
     * @param text  le texte à mesurer
     * @param scale échelle du texte
     * @return tableau {largeur, hauteur} du texte après mise à l'échelle
     */
    public static float[] getTextExtent(String text, float scale) {
        if (text == null || text.isEmpty()) return new float[]{0f, 0f};

        ByteBuffer buffer = ensureBuffer(text.length());
        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, buffer);

        float maxX = 0f;
        float maxY = 0f;
        float minY = Float.MAX_VALUE;
        for (int i = 0; i < quads * 4; i++) {
            int pos = i * 16;
            float x = buffer.getFloat(pos);
            float y = buffer.getFloat(pos + 4);
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (y < minY) minY = y;
        }
        return new float[]{maxX * scale, (maxY - minY) * scale};
    }

    /**
     * Libère les ressources OpenGL allouées pour le texte.
     */
    public static void cleanup() {
        if (!initialized) return;
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        initialized = false;
        lastVpW = -1;
        lastVpH = -1;
        textBuffer = null;
    }
}
