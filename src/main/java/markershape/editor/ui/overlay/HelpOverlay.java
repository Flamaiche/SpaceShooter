package markershape.editor.ui.overlay;

import markershape.config.ConfigParametres;
import markershape.editor.ui.UIElement;
import markershape.editor.ui.UIResources;

/** Non-modal help panel listing the editor key bindings (toggle with H). */
public class HelpOverlay extends UIElement {
    private static final float PW = 560;
    private static final float PAD = 10;
    private static final float LINE_H = 22;
    private static final String[] LINES = {
        "Raccourcis MarkerShape",
        "Gauche          : selectionner (Ctrl = multi, vide = deselec)",
        "Vide + glisser  : marquee (selection en boite) | Shift + glisser un sommet : rubber-band",
        "Droit (tap)     : pipette couleur sur un sommet | Droit long : orbite",
        "Glisser gauche  : deplacer le(s) sommet(s) | X / Y / Z / G / Echap = contraintes",
        "Milieu / Shift+Droit : panoramique | Molette : zoom",
        "Ctrl+Z / Ctrl+W / Ctrl+Shift+Z : annuler / refaire | Ctrl+S : sauver",
        "Suppr / Backspace : supprimer la selection | Ctrl+A : tout selectionner",
        "G               : grab - la selection suit le curseur (LMB=valider, Echap=cancel)",
        "S : subdiviser l'arete | E : extruder l'arete | M : fusionner les sommets",
        "F               : remplir la boucle d'aretes selectionnee (else capture avant)",
        "T               : mode Tracé (face) - cliquer le 1er point ou Entree pour fermer",
        "Ctrl+...        : D dupliquer | C copier | V coller | F capturer le vecteur avant",
        "K               : nettoyage (sommets orphelins, aretes isolees, faces invalides)",
        "R               : vue de face | H : cette aide",
        "Mode Vertex     : MAJ+clic sur un sommet = sommet frere co-localise",
        "Echap           : sortir du mode / fermer les fenetres",
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
            float scale = (i == 0) ? 1.8f : 1.35f;
            float col = (i == 0) ? 1f : 0.9f;
            res.drawText(LINES[i], x + PAD, ly, scale, tR * col, tG * col, tB * col);
        }
    }
}