package markershape.editor.ui.menu;

import learngl.Shader;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class BlurBackground {
    private int width, height;
    private Shader blurShader;
    private int screenTex = -1;
    private int vao, vbo;
    private final Matrix4f ortho = new Matrix4f();
    private final FloatBuffer buf = BufferUtils.createFloatBuffer(6 * 6);

    public BlurBackground(Shader blurShader, int vao, int vbo) {
        this.blurShader = blurShader;
        this.vao = vao;
        this.vbo = vbo;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        ortho.setOrtho2D(0, width, h, 0);
        if (screenTex >= 0) { glDeleteTextures(screenTex); screenTex = -1; }
    }

    public void captureScreen() {
        if (screenTex < 0) {
            screenTex = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, screenTex);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        glBindTexture(GL_TEXTURE_2D, screenTex);
        glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void drawBlurredBg(float x, float y, float w, float h, float alpha, float tr, float tg, float tb) {
        glEnable(GL_BLEND);
        glDisable(GL_DEPTH_TEST);
        blurShader.bind();
        blurShader.setUniformMat4f("projection", ortho);
        int texLoc = glGetUniformLocation(blurShader.getProgramId(), "uScreenTex");
        if (texLoc != -1) glUniform1i(texLoc, 0);
        blurShader.setUniform2f("uScreenSize", width, height);
        blurShader.setUniform1f("uAlpha", alpha);
        blurShader.setUniform3f("uTint", tr, tg, tb);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, screenTex);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        buf.clear();
        buf.put(new float[]{
            x, y, 0f, 0f, 0f, 0f,
            x+w, y, 0f, 0f, 0f, 0f,
            x+w, y+h, 0f, 0f, 0f, 0f,
            x, y, 0f, 0f, 0f, 0f,
            x+w, y+h, 0f, 0f, 0f, 0f,
            x, y+h, 0f, 0f, 0f, 0f,
        }).flip();
        glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, 0);
        blurShader.unbind();
    }

    public void cleanup() {
        if (screenTex >= 0) { glDeleteTextures(screenTex); screenTex = -1; }
    }
}
