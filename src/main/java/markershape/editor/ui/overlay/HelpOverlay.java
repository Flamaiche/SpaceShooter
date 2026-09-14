package markershape.editor.ui.overlay;

import markershape.config.ConfigParametres;
import markershape.editor.ui.UIElement;
import markershape.editor.ui.UIResources;

/** Non-modal help panel listing the editor key bindings (toggle with H). */
public class HelpOverlay extends UIElement {
    private static final float PW = 660;
    private static final float PAD = 10;
    private static final float LINE_H = 21;
    private static final String[] LINES = {
        "Raccourcis MarkerShape",
        "Gauche      : selectionner (Ctrl = multi, vide = deselec)",
        "Vide+glisse : marquee (selection en boite)",
        "Shift+glisse sommet : rubber-band (creation d'arete)",
        "Droit       : orbite | Tap droit sur un sommet : pipette couleur",
        "Drag sommet : deplacement (X / Y / Z / G / Echap = contraintes)",
        "Molette : zoom | Milieu / Shift+Droit : panoramique",
        "Ctrl+Z / Ctrl+Shift+Z : annuler / refaire | Ctrl+S : sauver",
        "Suppr/Backspace : supprimer | Ctrl+A : tout selectionner",
        "G           : grab - la selection suit le curseur (LMB=valider)",
        "S : subdiviser | E : extruder | M : fusionner les sommets",
        "F           : remplir la boucle d'aretes (sinon capture front)",
        "T           : mode Trace (face) - 1er point / Entree ferme",
        "Ctrl+D      : dupliquer | Ctrl+C : copier | Ctrl+V : coller",
        "Ctrl+F      : capturer le vecteur avant | K : nettoyage",
        "R           : vue de face | H : cette aide",
        "Mode Vertex : MAJ+clic sur un sommet = frere co-localise",
        "Echap       : sortir du mode / fermer les fenetres",
    };

    public HelpOverlay(UIResources res) {
        super(res);
        visible = false;
        w = PW;
        h = LINES.length * LINE_H + PAD * 2;
        x = PAD;
        y = 40;
    }

    public void toggle() { visible = !visible; }
    public void hide() { visible = false; }
    public boolean isVisible() { return visible; }

    @Override
    public void render() {
        if (!visible) return;

        float[] c = res.menuColor();
        res.drawQuad(x, y, w, h, c[0], c[1], c[2], 0.92f);

        ConfigParametres cfg = ConfigParametres.get();
        float tR = cfg.getFloat("textR") / 255f, tG = cfg.getFloat("textG") / 255f, tB = cfg.getFloat("textB") / 255f;

        for (int i = 0; i < LINES.length; i++) {
            float ly = y + PAD + i * LINE_H;
            float scale = (i == 0) ? 1.8f : 1.25f;
            float col = (i == 0) ? 1f : 0.9f;
            res.drawText(LINES[i], x + PAD, ly, scale, tR * col, tG * col, tB * col);
        }
    }
}