package markershape.editor.ui.control;

import markershape.editor.ui.UIElement;
import markershape.editor.ui.UIResources;

public class Button extends UIElement {
    public String text;
    public boolean showBackground = true;
    public float bgR = 0.3f, bgG = 0.3f, bgB = 0.3f, bgA = 0.8f;
    public float textR = 1f, textG = 1f, textB = 1f;
    public float textScale = 1.5f;
    public Runnable action;

    public Button(UIResources res, String text, float x, float y,
                  float w, float h, Runnable action) {
        super(res);
        this.text = text;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.action = action;
        this.clickable = true;
    }

    @Override
    public void render() {
        if (!visible) return;
        res.begin2D();
        if (showBackground) {
            res.drawQuad(x, y, w, h, bgR, bgG, bgB, bgA);
        }
        res.drawTextCentered(text, x, y, w, h, textScale, textR, textG, textB);
    }

    @Override
    protected void onClick() {
        if (action != null) action.run();
    }
}
