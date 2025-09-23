package gameGl.gestion.texte;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import learnGL.tools.Shader;

public class Text {
    private static int vao, vbo;
    private static boolean initialized = false;

    private static void init() {
        if (initialized) return;
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        initialized = true;
    }

    public static void drawText(Shader shader, String text,
                                float x, float y, float scale,
                                float r, float g, float b) {
        if (text == null || text.isEmpty()) return;
        init();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        ByteBuffer buffer = BufferUtils.createByteBuffer(text.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, buffer);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0);

        shader.bind();

        int[] vp = new int[4];
        glGetIntegerv(GL_VIEWPORT, vp);
        int winW = vp[2], winH = vp[3];
        shader.setUniformMat4f("projection", new Matrix4f().ortho2D(0f, winW, winH, 0f));

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

    public static float getTextWidth(String text, float scale) {
        if (text == null || text.isEmpty()) return 0f;

        ByteBuffer buffer = BufferUtils.createByteBuffer(text.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, text, null, buffer);

        float maxX = 0f;
        for (int i = 0; i < quads * 4; i++) {
            int pos = i * 16;
            float x = buffer.getFloat(pos);
            if (x > maxX) maxX = x;
        }
        return maxX * scale;
    }

    public static void cleanup() {
        if (!initialized) return;
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
        initialized = false;
    }
}
