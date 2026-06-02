package markershape.ui;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import learngl.Shader;

public class TextRenderer {
    private static int vao, vbo;
    private static boolean initialized = false;
    private static ByteBuffer quadBuffer;
    private static final Matrix4f ortho = new Matrix4f();
    private static int lastVpW = -1, lastVpH = -1;
    private static final int[] vp = new int[4];

    private static void init() {
        if (initialized) return;
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        initialized = true;
    }

    private static ByteBuffer ensureBuffer(int textLength) {
        int needed = textLength * 300;
        if (quadBuffer == null || quadBuffer.capacity() < needed) {
            quadBuffer = BufferUtils.createByteBuffer(needed);
        }
        quadBuffer.clear();
        return quadBuffer;
    }

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

        buffer.flip();
        int floatsPerQuad = 4 * 4;
        FloatBuffer src = buffer.asFloatBuffer();
        FloatBuffer tri = BufferUtils.createFloatBuffer(quads * 6 * 2);

        for (int q = 0; q < quads; q++) {
            int base = q * 4;
            float x0 = src.get(base * 4);
            float y0 = src.get(base * 4 + 1);
            float x1 = src.get((base + 1) * 4);
            float y1 = src.get((base + 1) * 4 + 1);
            float x2 = src.get((base + 2) * 4);
            float y2 = src.get((base + 2) * 4 + 1);
            float x3 = src.get((base + 3) * 4);
            float y3 = src.get((base + 3) * 4 + 1);

            tri.put(x0).put(y0);
            tri.put(x1).put(y1);
            tri.put(x2).put(y2);
            tri.put(x0).put(y0);
            tri.put(x2).put(y2);
            tri.put(x3).put(y3);
        }
        tri.flip();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, tri, GL_DYNAMIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8, 0);

        shader.bind();

        glGetIntegerv(GL_VIEWPORT, vp);
        int winW = vp[2], winH = vp[3];
        if (winW != lastVpW || winH != lastVpH) {
            ortho.identity().setOrtho2D(0f, winW, winH, 0f);
            lastVpW = winW;
            lastVpH = winH;
        }
        shader.setUniformMat4f("projection", ortho);
        shader.setUniform2f("offset", x, y);
        shader.setUniform1f("scale", scale);
        shader.setUniform3f("textColor", r, g, b);

        glDrawArrays(GL_TRIANGLES, 0, quads * 6);

        shader.unbind();
        glDisableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public static void cleanup() {
        if (!initialized) return;
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        initialized = false;
        lastVpW = -1;
        lastVpH = -1;
        quadBuffer = null;
    }
}
