package markershape.editor.ui.overlay;

import markershape.editor.ui.UIResources;
import markershape.editor.ui.widgets.AutoWindow;

/** Non-modal help panel listing the editor key bindings (toggle with H). */
public class HelpOverlay extends AutoWindow {
    private static final String[] LINES = {
        "Gauche      : selectionner (Ctrl = multi, vide = deselec)",
        "Vide+glisse : marquee (selection en boite)",
        "Shift+glisse sommet : rubber-band (creation d'arete)",
        "Droit       : orbite | Tap droit sur une arete : pipette couleur",
        "Drag sommet : deplacement (panneau Axe flottant pour contraintes)",
        "Molette : zoom | Milieu / Shift+Droit : panoramique",
        "Ctrl+Z / Ctrl+Shift+Z : annuler / refaire | Ctrl+S : sauver",
        "Suppr/Backspace : supprimer (confirme) | Ctrl+A : tout selectionner",
        "Tab pendant drag : lock le grab (relache le clic, suit le curseur)",
        "S : subdiviser | E : extruder | M : fusionner les sommets",
        "F           : remplir la boucle d'aretes",
        "N           : creer une face par selection (Entree valide, Echap annule)",
        "T           : mode Trace (face) - 1er point / Entree ferme",
        "Ctrl+D      : dupliquer | Ctrl+C : copier | Ctrl+V : coller",
        "K           : nettoyage",
        "R           : vue de face | H : cette aide",
        "Mode Vertex : MAJ+clic sur un sommet = frere co-localise",
        "Echap       : sortir du mode / fermer les fenetres",
    };

    /** Creates the help panel with the hard-coded shortcut lines. */
    public HelpOverlay(UIResources res) {
        super(res);
        visible = false;
        setTitle("Raccourcis MarkerShape");
        setMaxWidth(660);
        for (String line : LINES) addText(line, 1.1f);
        autoSize();
        x = 10;
        y = 40;
    }

    /** Toggles the visibility of the help panel. */
    public void toggle() { visible = !visible; }
    /** Hides the help panel. */
    public void hide() { visible = false; }
    /** @return true if the help panel is visible. */
    public boolean isVisible() { return visible; }
}