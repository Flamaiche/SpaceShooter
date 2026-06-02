package markershape;

import java.io.*;
import java.nio.*;

import org.lwjgl.opengl.GL11;

public class EditorUI {
    private int width, height;
    private int fontTexture;

    public EditorUI(int w, int h) {
        width = w;
        height = h;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
    }

    public void render() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        drawTopBar();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void drawTopBar() {
        GL11.glColor4f(0.1f, 0.1f, 0.15f, 0.8f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(width, 0);
        GL11.glVertex2f(width, 36);
        GL11.glVertex2f(0, 36);
        GL11.glEnd();
    }

    public boolean isSaveClicked(float mx, float my) {
        return my < 36 && mx > 150 && mx < 200;
    }

    public boolean isAddClicked(float mx, float my) {
        return my < 36 && mx > 210 && mx < 240;
    }
}
