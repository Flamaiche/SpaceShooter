package markershape.ui;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Button {
    public String text;
    public float x, y, width, height;
    public boolean showBackground = true;
    public float bgR = 0.3f, bgG = 0.3f, bgB = 0.3f, bgA = 0.8f;
    public float textR = 1f, textG = 1f, textB = 1f;
    public float textScale = 1.5f;
    private final Runnable action;

    public Button(String text, float x, float y, float width, float height, Runnable action) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.action = action;
    }

    public void render(Shader uiShader, Shader textShader, Matrix4f ortho,
                       FloatBuffer buf, int vao, int vbo) {
        if (showBackground) {
            uiShader.bind();
            uiShader.setUniformMat4f("projection", ortho);

            buf.clear();
            buf.put(new float[]{
                x,      y,      bgR, bgG, bgB, bgA,
                x+width,y,      bgR, bgG, bgB, bgA,
                x+width,y+height,bgR,bgG,bgB,bgA,
                x,      y,      bgR, bgG, bgB, bgA,
                x+width,y+height,bgR,bgG,bgB,bgA,
                x,      y+height,bgR,bgG,bgB,bgA
            }).flip();

            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, buf, GL_DYNAMIC_DRAW);
            glDrawArrays(GL_TRIANGLES, 0, 6);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

            uiShader.unbind();
        }

        float[] ext = Text.getTextExtent(text, textScale);
        float tx = x + (width - ext[0]) / 2f;
        float ty = y + (height - ext[1]) / 2f;
        Text.drawText(textShader, text, tx, ty, textScale, textR, textG, textB);
    }

    public boolean isClicked(float mx, float my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public void click() {
        if (action != null) action.run();
    }
}
