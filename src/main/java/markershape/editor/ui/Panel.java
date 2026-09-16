package markershape.editor.ui;

import markershape.editor.ui.menu.BlurBackground;

import java.util.ArrayList;
import java.util.List;

/** A {@link UIElement} that owns a list of child elements, renders a background, and forwards clicks to its children. */
public abstract class Panel extends UIElement {
    protected final List<UIElement> children = new ArrayList<>();

    /** @param res the shared UI resources used by this panel and its children */
    public Panel(UIResources res) {
        super(res);
    }

    /** Adds a child element and sets its parent to this panel. */
    public void addChild(UIElement e) {
        e.parent = this;
        children.add(e);
    }

    /** Returns the list of child elements in top-to-bottom render order. */
    public List<UIElement> getChildren() {
        return children;
    }

    /** Forwards the click to children (front first), then to this panel itself. */
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

    /** Renders the background, content, children, and text in order when visible. */
    @Override
    public final void render() {
        if (!visible) return;
        res.begin2D();
        drawBackground();
        renderContent();
        for (UIElement c : children) c.render();
        renderText();
    }

    /** Draws a semi-transparent menu-colored quad filling the panel rectangle. */
    protected void drawBackground() {
        float[] c = res.menuColor();
        res.drawQuad(x, y, w, h, c[0], c[1], c[2], BlurBackground.panelAlpha());
    }

    /** Override to render content between the background and child elements. */
    protected void renderContent() {}

    /** Override to render text on top of children. */
    protected void renderText() {}
}
