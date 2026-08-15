package markershape.editor.ui.framework;

import java.util.ArrayList;
import java.util.List;

public abstract class UIElement {
    public UIElement parent;
    public float x, y, w, h;
    public boolean visible = true;
    protected final List<UIElement> children = new ArrayList<>();

    public void add(UIElement child) {
        child.parent = this;
        children.add(child);
    }

    public void clear() {
        for (UIElement c : children) c.parent = null;
        children.clear();
    }

    public float absX(UIRenderer r) {
        if (parent == null) return x * r.getWidth();
        return parent.absX(r) + x * parent.absW(r);
    }

    public float absY(UIRenderer r) {
        if (parent == null) return y * r.getHeight();
        return parent.absY(r) + y * parent.absH(r);
    }

    public float absW(UIRenderer r) {
        if (parent == null) return w * r.getWidth();
        return w * parent.absW(r);
    }

    public float absH(UIRenderer r) {
        if (parent == null) return h * r.getHeight();
        return h * parent.absH(r);
    }

    public boolean contains(float px, float py, UIRenderer r) {
        return px >= absX(r) && px <= absX(r) + absW(r)
            && py >= absY(r) && py <= absY(r) + absH(r);
    }

    public void render(UIRenderer r) {
        if (!visible) return;
        renderSelf(r);
        for (UIElement c : children) c.render(r);
    }

    protected abstract void renderSelf(UIRenderer r);

    /** Click propagation: topmost child first, then self. Returns true if consumed. */
    public boolean onClick(float px, float py, UIRenderer r) {
        if (!visible) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).onClick(px, py, r)) return true;
        }
        return contains(px, py, r) && onClickSelf(px, py, r);
    }

    protected boolean onClickSelf(float px, float py, UIRenderer r) {
        return false;
    }
}
