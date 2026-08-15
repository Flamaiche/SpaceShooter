package markershape.editor.ui.menu;

import markershape.config.ConfigParametres;
import markershape.editor.ui.framework.UIContainer;
import markershape.editor.ui.framework.UIRenderer;
import markershape.editor.ui.framework.UIText;

public class NewMenu {
    private boolean newMenuOpen;
    private int activeMode = -1; // -1=none, 0=vertex, 1=edge
    public static final float NEW_DROP_W = 130;
    public static final float NEW_ITEM_H = 26;

    private float btnX, btnY;
    private int width, height;
    private UIRenderer renderer;
    private final UIContainer root = new UIContainer(0, 0, 1, 1);

    private static float ts(float designScale) { return designScale / 720f; }

    public NewMenu() {
        renderer = new UIRenderer();
        root.alpha = UIContainer.Alpha.NONE;
    }

    public boolean isOpen() { return newMenuOpen; }
    public void setOpen(boolean v) { newMenuOpen = v; }
    public void toggle() { newMenuOpen = !newMenuOpen; }
    public void close() { newMenuOpen = false; }

    public void setActiveMode(int mode) { activeMode = mode; }
    public int getActiveMode() { return activeMode; }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        renderer.setScreenSize(w, h);
    }

    public void setBtnPos(float x, float y) { btnX = x; btnY = y; }

    public boolean contains(float mx, float my) {
        return newMenuOpen && my >= btnY + 36 && my <= btnY + 36 + 2 * NEW_ITEM_H
            && mx >= btnX && mx <= btnX + NEW_DROP_W;
    }

    public void render() {
        if (!newMenuOpen) return;
        root.clear();
        build();
        root.render(renderer);
    }

    private void build() {
        float dx = btnX, dy = btnY + 36;
        UIContainer box = new UIContainer(dx / width, dy / height,
            NEW_DROP_W / width, 2 * NEW_ITEM_H / height);
        box.alpha = UIContainer.Alpha.PANEL;
        box.bgR = BlurBackground.menuR;
        box.bgG = BlurBackground.menuG;
        box.bgB = BlurBackground.menuB;
        root.add(box);

        String[] items = {"Vertex", "Edge"};
        for (int i = 0; i < 2; i++) {
            boolean active = (i == activeMode);
            UIContainer item = new UIContainer(0, i * 0.5f, 1f, 0.5f);
            if (active) {
                item.alpha = UIContainer.Alpha.HIGHLIGHT;
                item.bgR = BlurBackground.menuR + 0.15f;
                item.bgG = BlurBackground.menuG + 0.1f;
                item.bgB = BlurBackground.menuB;
            } else {
                item.alpha = UIContainer.Alpha.NONE;
            }
            box.add(item);

            UIText t = new UIText(8f / NEW_DROP_W, 4f / NEW_ITEM_H, ts(1.5f),
                (active ? "> " : "  ") + items[i]);
            if (active) {
                ConfigParametres cfg = ConfigParametres.get();
                t.useConfigText = false;
                t.tR = cfg.getFloat("textR") / 255f * 1.3f;
                t.tG = cfg.getFloat("textG") / 255f * 1.3f;
                t.tB = cfg.getFloat("textB") / 255f * 1.3f;
            }
            item.add(t);
        }
    }

    /** Returns 0=Vertex, 1=Edge, -1=nothing. */
    public int click(float mx, float my) {
        if (!newMenuOpen) return -1;
        float dx = btnX;
        float dy = btnY + 36;
        if (mx < dx || mx > dx + NEW_DROP_W || my < dy || my > dy + 2 * NEW_ITEM_H) return -1;
        int idx = (int) ((my - dy) / NEW_ITEM_H);
        if (idx < 0 || idx > 1) return -1;
        newMenuOpen = false;
        return idx;
    }

    public void cleanup() {
        renderer.cleanup();
    }
}
