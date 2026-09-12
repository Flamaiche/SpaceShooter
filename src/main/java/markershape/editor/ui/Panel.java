package markershape.editor.ui;

import markershape.editor.ui.menu.BlurBackground;

import java.util.ArrayList;
import java.util.List;

public abstract class Panel extends UIElement {
    protected final List<UIElement> children = new ArrayList<>();

    public Panel(UIResources res) {
        super(res);
    }

    public void addChild(UIElement e) {
        e.parent = this;
        children.add(e);
    }

    public List<UIElement> getChildren() {
        return children;
    }

    @Override
    public boolean click(float mx, float my) {
        if (!visible) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).click(mx, my)) return true;
        }
        if (isClickable() && contains(mx, my)) {
            onClick();
            return true;
        }
        return false;
    }

    @Override
    public final void render() {
        if (!visible) return;
        res.begin2D();
        drawBackground();
        renderContent();
        for (UIElement c : children) c.render();
        renderText();
    }

    protected void drawBackground() {
        float[] c = res.menuColor();
        res.drawQuad(x, y, w, h, c[0], c[1], c[2], BlurBackground.panelAlpha());
    }

    protected void renderContent() {}

    protected void renderText() {}
}
