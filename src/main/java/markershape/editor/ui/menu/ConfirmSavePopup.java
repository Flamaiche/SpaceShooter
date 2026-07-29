package markershape.editor.ui.menu;

import gamegl.gestion.texte.Text;
import learngl.Shader;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class ConfirmSavePopup {
    private int width, height;
    private boolean visible;
    private Runnable confirmAction;
    private BlurBackground blur;
    private Shader textShader;

    public static final float CONFIRM_W = 220;
    public static final float CONFIRM_H = 100;
    public static final float CONFIRM_BTN_W = 70;
    public static final float CONFIRM_BTN_H = 28;

    public ConfirmSavePopup(BlurBackground blur, Shader textShader) {
        this.blur = blur;
        this.textShader = textShader;
    }

    public void setSize(int w, int h) { width = w; height = h; }
    public boolean isVisible() { return visible; }
    public void show() { visible = true; }
    public void close() { visible = false; }
    public void setConfirmAction(Runnable r) { confirmAction = r; }
    public Runnable getConfirmAction() { return confirmAction; }

    public boolean contains(float mx, float my) {
        if (!visible) return false;
        float cx = (width - CONFIRM_W) / 2;
        float cy = (36 + (height - 36) / 2) - CONFIRM_H / 2;
        return mx >= cx && mx <= cx + CONFIRM_W && my >= cy && my <= cy + CONFIRM_H;
    }

    /** Returns 1=Oui, 2=Non, 0=click on popup (no btn), -1=not on popup. */
    public int click(float mx, float my) {
        if (!visible) return -1;
        float cx = (width - CONFIRM_W) / 2;
        float cy = (36 + (height - 36) / 2) - CONFIRM_H / 2;
        float btnY = cy + CONFIRM_H - CONFIRM_BTN_H - 12;
        float ouiX = cx + 20;
        float nonX = cx + CONFIRM_W - 20 - CONFIRM_BTN_W;
        if (my >= btnY && my <= btnY + CONFIRM_BTN_H) {
            if (mx >= ouiX && mx <= ouiX + CONFIRM_BTN_W) return 1;
            if (mx >= nonX && mx <= nonX + CONFIRM_BTN_W) return 2;
        }
        return 0;
    }

    public void render() {
        if (!visible) return;
        float cx = (width - CONFIRM_W) / 2;
        float cy = (36 + (height - 36) / 2) - CONFIRM_H / 2;

        blur.drawBlurredBg(0, 0, width, height, 0.55f, BlurBackground.menuR, BlurBackground.menuG, BlurBackground.menuB);
        blur.drawBlurredBg(cx, cy, CONFIRM_W, CONFIRM_H, 0.88f, BlurBackground.menuR, BlurBackground.menuG, BlurBackground.menuB);

        float tc = BlurBackground.textColor();
        Text.drawText(textShader, "Sauvegarder ?",
            cx + CONFIRM_W / 2 - 50, cy + 18, 1.5f, tc, tc, tc);
        Text.drawText(textShader, "[Oui]",
            cx + 30, cy + CONFIRM_H - CONFIRM_BTN_H - 10, 1.5f, tc, tc, tc);
        Text.drawText(textShader, "[Non]",
            cx + CONFIRM_W - 70, cy + CONFIRM_H - CONFIRM_BTN_H - 10, 1.5f, tc, tc, tc);
    }
}
