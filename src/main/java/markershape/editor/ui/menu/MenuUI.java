package markershape.editor.ui.menu;

import markershape.editor.ui.framework.UIButton;
import markershape.editor.ui.framework.UIContainer;
import markershape.editor.ui.framework.UIRenderer;
import markershape.editor.ui.framework.UIText;
import markershape.shape.ShapeLoader;

public class MenuUI {
    private UIRenderer renderer;
    private String[] shapes;
    private Runnable onQuit, onParams;
    private String clickedShape;
    private UIButton paramBtn, quitBtn;

    private final UIContainer root = new UIContainer(0, 0, 1, 1);
    private float panelX, panelY, panelH;

    private static final float PANEL_W = 480f / 1280f;
    private static final float PANEL_Y = 140f / 720f;
    private static final float ITEM_H = 40f / 720f;
    private static final float ITEM_GAP = 4f / 720f;

    private static float ts(float designScale) { return designScale / 720f; }

    public MenuUI(int w, int h, Runnable onQuit, Runnable onParams) {
        this.onQuit = onQuit;
        this.onParams = onParams;
        root.alpha = UIContainer.Alpha.NONE;
        renderer = new UIRenderer();
        refresh();
        renderer.setScreenSize(w, h);
    }

    public void refresh() {
        shapes = ShapeLoader.listShapes();
        if (shapes == null) shapes = new String[0];
    }

    public void setSize(int w, int h) {
        renderer.setScreenSize(w, h);
    }

    public void render() {
        root.clear();
        build();
        root.render(renderer);
    }

    private void build() {
        float listH = shapes.length * (ITEM_H + ITEM_GAP);
        panelH = listH + 40f / 720f;
        panelX = 0.5f - PANEL_W / 2f;
        panelY = PANEL_Y;

        UIText title = new UIText(0f, 40f / 720f, ts(4f), "MarkerShape");
        title.w = 1f;
        title.centered = true;
        root.add(title);

        UIText subtitle = new UIText(0f, 85f / 720f, ts(1.8f), "Editeur de modeles 3D");
        subtitle.w = 1f;
        subtitle.centered = true;
        root.add(subtitle);

        UIContainer panel = new UIContainer(panelX, panelY, PANEL_W, panelH);
        panel.alpha = UIContainer.Alpha.PANEL;
        panel.bgR = BlurBackground.menuR;
        panel.bgG = BlurBackground.menuG;
        panel.bgB = BlurBackground.menuB;
        root.add(panel);

        float rowHRel = ITEM_H / panelH;
        for (int i = 0; i < shapes.length; i++) {
            final int idx = i;
            String name = shapes[i].replace(".json", "");
            float y = panelY + 20f / 720f + i * (ITEM_H + ITEM_GAP);
            float ry = (y - panelY) / panelH;
            float aOff = (i % 2 == 0 ? 0.03f : 0f);

            UIContainer row = new UIContainer(10f / (PANEL_W * 1280f), ry,
                (PANEL_W - 20f / 1280f) / PANEL_W, rowHRel);
            row.alpha = UIContainer.Alpha.ROW;
            row.bgR = BlurBackground.menuR + aOff;
            row.bgG = BlurBackground.menuG + aOff;
            row.bgB = BlurBackground.menuB + aOff;
            row.onClickAction = () -> clickedShape = shapes[idx];
            panel.add(row);

            UIText label = new UIText(0f, 8f / (ITEM_H * 720f), ts(2.2f), name);
            label.w = 1f;
            label.h = 1f;
            label.centered = true;
            row.add(label);
        }

        float by = panelY + panelH + 16f / 720f;
        float btnW = 180f / 1280f;
        float btnH = 38f / 720f;
        float bx = 0.5f - (btnW * 2f + 20f / 1280f) / 2f;

        paramBtn = makeButton(bx, by, btnW, btnH, "Parametres",
            BlurBackground.menuR + 0.05f, BlurBackground.menuG + 0.05f, BlurBackground.menuB + 0.1f,
            () -> onParams.run());
        quitBtn = makeButton(bx + btnW + 20f / 1280f, by, btnW, btnH, "Quitter",
            BlurBackground.menuR + 0.1f, BlurBackground.menuG + 0.02f, BlurBackground.menuB + 0.02f,
            () -> onQuit.run());
        root.add(paramBtn);
        root.add(quitBtn);
    }

    private UIButton makeButton(float x, float y, float w, float h, String label,
                                float r, float g, float b, Runnable action) {
        UIButton btn = new UIButton(x, y, w, h, label, ts(2.2f));
        btn.alpha = UIContainer.Alpha.BTN;
        btn.bgR = r;
        btn.bgG = g;
        btn.bgB = b;
        btn.onClickAction = action;
        return btn;
    }

    public String clickShape(float mx, float my) {
        clickedShape = null;
        root.onClick(mx, my, renderer);
        return clickedShape;
    }

    public boolean isParametresClicked(float mx, float my) {
        return paramBtn != null && paramBtn.contains(mx, my, renderer);
    }

    public boolean isQuitterClicked(float mx, float my) {
        return quitBtn != null && quitBtn.contains(mx, my, renderer);
    }

    public void cleanup() {
        renderer.cleanup();
    }
}
