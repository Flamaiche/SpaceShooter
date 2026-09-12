package markershape.editor.ui.menu;

import gamegl.gestion.texte.Text;
import markershape.config.ConfigParametres;
import markershape.editor.ui.Panel;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.control.Button;
import markershape.shape.ShapeLoader;

public class MenuUI extends Panel {
    private int width, height;
    private String[] shapes;
    private static final int PANEL_W = 480;
    private static final int ITEM_H = 40;
    private static final int ITEM_GAP = 4;
    private static final int PANEL_Y = 140;
    private Button paramBtn, quitBtn;
    private Runnable onQuit, onParams;

    public MenuUI(UIResources res, int w, int h, Runnable onQuit, Runnable onParams) {
        super(res);
        this.onQuit = onQuit;
        this.onParams = onParams;
        refresh();
        setSize(w, h);
    }

    public void refresh() {
        shapes = ShapeLoader.listShapes();
        if (shapes == null) shapes = new String[0];
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        res.setSize(w, h);
    }

    @Override
    protected void drawBackground() {
        float cx = width / 2f;
        float px = cx - PANEL_W / 2f;
        float listH = shapes.length * (ITEM_H + ITEM_GAP);
        float panelH = listH + 40;
        x = px;
        y = PANEL_Y;
        w = PANEL_W;
        h = panelH;

        float[] c = res.menuColor();
        float panelAlpha = BlurBackground.panelAlpha();
        float rowAlpha = BlurBackground.rowAlpha();
        res.drawQuad(px, PANEL_Y, PANEL_W, panelH, c[0], c[1], c[2], panelAlpha);
        for (int i = 0; i < shapes.length; i++) {
            float yy = PANEL_Y + 20 + i * (ITEM_H + ITEM_GAP);
            float aOff = (i % 2 == 0 ? 0.03f : 0f);
            res.drawQuad(px + 10, yy, PANEL_W - 20, ITEM_H, c[0] + aOff, c[1] + aOff, c[2] + aOff, rowAlpha);
        }
    }

    @Override
    protected void renderContent() {
        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;
        float cx = width / 2f;
        res.drawText("MarkerShape", cx - Text.getTextExtent("MarkerShape", 4f)[0] / 2f, 40, 4f, tR, tG, tB);
        res.drawText("Editeur de modeles 3D",
            cx - Text.getTextExtent("Editeur de modeles 3D", 1.8f)[0] / 2f, 85, 1.8f, tR, tG, tB);

        for (int i = 0; i < shapes.length; i++) {
            String name = shapes[i].replace(".json", "");
            float yy = PANEL_Y + 20 + i * (ITEM_H + ITEM_GAP);
            res.drawText(name, cx - Text.getTextExtent(name, 2.2f)[0] / 2f, yy + 8, 2.2f, tR, tG, tB);
        }

        float by = PANEL_Y + (shapes.length * (ITEM_H + ITEM_GAP) + 40) + 16;
        float btnW = 180;
        float btnH = 38;
        float gap = 20;
        float totalW = btnW * 2 + gap;
        float bx = cx - totalW / 2f;

        if (paramBtn == null || paramBtn.x != bx || paramBtn.y != by) {
            if (paramBtn != null) {
                children.remove(paramBtn);
                children.remove(quitBtn);
            }
            float mr = BlurBackground.menuR, mg = BlurBackground.menuG, mb = BlurBackground.menuB;
            paramBtn = new Button(res, "Parametres", bx, by, btnW, btnH, onParams);
            paramBtn.textScale = 2.2f;
            paramBtn.bgR = mr + 0.05f; paramBtn.bgG = mg + 0.05f; paramBtn.bgB = mb + 0.1f;
            addChild(paramBtn);

            quitBtn = new Button(res, "Quitter", bx + btnW + gap, by, btnW, btnH, onQuit);
            quitBtn.textScale = 2.2f;
            quitBtn.bgR = mr + 0.1f; quitBtn.bgG = mg + 0.02f; quitBtn.bgB = mb + 0.02f;
            addChild(quitBtn);
        }

        float btnAlpha = BlurBackground.btnAlpha();
        paramBtn.bgA = btnAlpha;
        quitBtn.bgA = btnAlpha;
        paramBtn.textR = tR; paramBtn.textG = tG; paramBtn.textB = tB;
        quitBtn.textR = tR; quitBtn.textG = tG; quitBtn.textB = tB;
    }

    public String clickShape(float mx, float my) {
        float cx = width / 2f;
        float px = cx - PANEL_W / 2f;
        float listH = shapes.length * (ITEM_H + ITEM_GAP);
        float panelH = listH + 40;
        if (mx < px + 10 || mx > px + PANEL_W - 10) return null;
        if (my < PANEL_Y + 20 || my > PANEL_Y + panelH - 20) return null;
        for (int i = 0; i < shapes.length; i++) {
            float y = PANEL_Y + 20 + i * (ITEM_H + ITEM_GAP);
            if (my >= y && my <= y + ITEM_H) return shapes[i];
        }
        return null;
    }

    public boolean isParametresClicked(float mx, float my) {
        if (paramBtn != null && paramBtn.contains(mx, my)) { paramBtn.click(mx, my); return true; }
        return false;
    }

    public boolean isQuitterClicked(float mx, float my) {
        if (quitBtn != null && quitBtn.contains(mx, my)) { quitBtn.click(mx, my); return true; }
        return false;
    }

    public void cleanup() {}
}
