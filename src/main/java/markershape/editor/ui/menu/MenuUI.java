package markershape.editor.ui.menu;

import markershape.config.ConfigParametres;
import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.control.Button;
import markershape.shape.ShapeLoader;

/**
 * Main menu screen listing the available shapes, plus the "Parametres" and
 * "Quitter" buttons.
 */
public class MenuUI extends Panel {
    private int width;
    private String[] shapes;
    private static final int PANEL_W = 480;
    private static final int ITEM_H = 32;
    private static final int ITEM_GAP = 4;
    private static final int PANEL_Y = 140;
    private static final float BTN_W = 180;
    private static final float BTN_H = 38;
    private static final float BTN_GAP = 20;
    private Button paramBtn, quitBtn;
    private Runnable onQuit, onParams;

    /** Builds the menu with the shape list and the two action buttons. */
    public MenuUI(UIResources res, int w, int h, Runnable onQuit, Runnable onParams) {
        super(res);
        this.onQuit = onQuit;
        this.onParams = onParams;
        refresh();
        setSize(w, h);

        paramBtn = new Button(res, "Parametres", 0, 0, BTN_W, BTN_H, onParams);
        paramBtn.textScale = 1.8f;
        quitBtn = new Button(res, "Quitter", 0, 0, BTN_W, BTN_H, onQuit);
        quitBtn.textScale = 1.8f;
        addChild(paramBtn);
        addChild(quitBtn);
    }

    /** Reloads the list of available shape files. */
    public void refresh() {
        shapes = ShapeLoader.listShapes();
        if (shapes == null) shapes = new String[0];
    }

    /** Sets the window size used for the menu layout. */
    public void setSize(int w, int h) {
        width = w;
        res.setSize(w, h);
    }

    /** @return the total height of the shape list. */
    private float listH() {
        return shapes.length * (ITEM_H + ITEM_GAP);
    }

    /** @return the height of the shape-list panel (list plus padding). */
    private float panelH() {
        return listH() + 40;
    }

    /** Positions the panel and draws the background quads behind the list. */
    @Override
    protected void drawBackground() {
        x = width / 2f - PANEL_W / 2f;
        y = PANEL_Y;
        w = PANEL_W;
        h = panelH();

        float[] c = res.menuColor();
        float panelAlpha = BlurBackground.panelAlpha();
        float rowAlpha = BlurBackground.rowAlpha();
        res.drawQuad(x, y, PANEL_W, h, c[0], c[1], c[2], panelAlpha);
        for (int i = 0; i < shapes.length; i++) {
            float yy = y + 20 + i * (ITEM_H + ITEM_GAP);
            float aOff = (i % 2 == 0 ? 0.03f : 0f);
            res.drawQuad(x + 10, yy, PANEL_W - 20, ITEM_H, c[0] + aOff, c[1] + aOff, c[2] + aOff, rowAlpha);
        }
    }

    /** Draws the title, subtitle and shape names, then positions the buttons. */
    @Override
    protected void renderContent() {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        float cx = width / 2f;
        res.drawTextCenteredX("MarkerShape", cx, 40, 3.2f, tR, tG, tB);
        res.drawTextCenteredX("Editeur de modeles 3D", cx, 80, 1.5f, tR, tG, tB);

        for (int i = 0; i < shapes.length; i++) {
            String name = shapes[i].replace(".json", "");
            float yy = y + 20 + i * (ITEM_H + ITEM_GAP);
            res.drawTextCenteredX(name, cx, yy + 8, 1.7f, tR, tG, tB);
        }

        positionButtons(tR, tG, tB);
    }

    /** Positions and styles the parametres/quitter buttons. */
    private void positionButtons(float tR, float tG, float tB) {
        float by = y + panelH() + 16;
        float totalW = BTN_W * 2 + BTN_GAP;
        float bx = width / 2f - totalW / 2f;
        paramBtn.x = bx;
        paramBtn.y = by;
        quitBtn.x = bx + BTN_W + BTN_GAP;
        quitBtn.y = by;

        float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
        paramBtn.bgR = mr + 0.05f; paramBtn.bgG = mg + 0.05f; paramBtn.bgB = mb + 0.1f;
        quitBtn.bgR = mr + 0.1f; quitBtn.bgG = mg + 0.02f; quitBtn.bgB = mb + 0.02f;
        float btnAlpha = BlurBackground.btnAlpha();
        paramBtn.bgA = btnAlpha;
        quitBtn.bgA = btnAlpha;
        paramBtn.textR = tR; paramBtn.textG = tG; paramBtn.textB = tB;
        quitBtn.textR = tR; quitBtn.textG = tG; quitBtn.textB = tB;
    }

    /** @return the clicked shape file name, or null if no item was clicked. */
    public String clickShape(float mx, float my) {
        float px = width / 2f - PANEL_W / 2f;
        if (mx < px + 10 || mx > px + PANEL_W - 10) return null;
        if (my < PANEL_Y + 20 || my > PANEL_Y + panelH() - 20) return null;
        for (int i = 0; i < shapes.length; i++) {
            float yy = PANEL_Y + 20 + i * (ITEM_H + ITEM_GAP);
            if (my >= yy && my <= yy + ITEM_H) return shapes[i];
        }
        return null;
    }

    /** @return true if the parametres button was clicked. */
    public boolean isParametresClicked(float mx, float my) {
        if (paramBtn.contains(mx, my)) { paramBtn.click(mx, my); return true; }
        return false;
    }

    /** @return true if the quitter button was clicked. */
    public boolean isQuitterClicked(float mx, float my) {
        if (quitBtn.contains(mx, my)) { quitBtn.click(mx, my); return true; }
        return false;
    }
}