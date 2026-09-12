package markershape.editor.ui;

public abstract class UIElement {
    public UIResources res;
    public float x, y, w, h;
    public boolean visible = true;
    public boolean clickable = false;
    public UIElement parent;

    public UIElement(UIResources res) {
        this.res = res;
    }

    public boolean contains(float mx, float my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public boolean isClickable() {
        return clickable || (parent != null && parent.isClickable());
    }

    public boolean click(float mx, float my) {
        if (!visible) return false;
        if (isClickable() && contains(mx, my)) {
            onClick();
            return true;
        }
        return false;
    }

    protected void onClick() {}

    public void show() { visible = true; }
    public void hide() { visible = false; }
    public boolean isVisible() { return visible; }

    public abstract void render();
}
