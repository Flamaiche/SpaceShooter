package markershape.editor.ui;

/**
 * Base class for all UI widgets: handles the bounding rectangle,
 * visibility, clickability, and hit-testing shared by the menu system.
 */
public abstract class UIElement {
    public UIResources res;
    public float x, y, w, h;
    public boolean visible = true;
    public boolean clickable = false;
    public UIElement parent;

    /**
     * @param res the shared UI resources used to render this element
     */
    public UIElement(UIResources res) {
        this.res = res;
    }

    /** Returns whether the given screen point lies inside this element's rectangle. */
    public boolean contains(float mx, float my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    /** Returns whether this element (or one of its parents) is clickable. */
    public boolean isClickable() {
        return clickable || (parent != null && parent.isClickable());
    }

    /** Runs {@link #onClick()} when the element is visible, clickable and contains the point. */
    public boolean click(float mx, float my) {
        if (!visible) return false;
        if (isClickable() && contains(mx, my)) {
            onClick();
            return true;
        }
        return false;
    }

    /** Called when the element is clicked; subclasses should override as needed. */
    protected void onClick() {}

    /** Makes the element visible. */
    public void show() { visible = true; }
    /** Hides the element. */
    public void hide() { visible = false; }
    /** Returns whether the element is visible. */
    public boolean isVisible() { return visible; }

    /** Renders the element; subclasses implement the actual drawing. */
    public abstract void render();
}
