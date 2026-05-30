package gamegl.gestion.texte;

import gamegl.gestion.donnees.GameData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Élément de texte HUD avec formatage automatique selon le type et support d'alignement.
 */
public class TextHUD {

    /** Alignement horizontal possible. */
    public enum HorizontalAlignment { LEFT, CENTER, RIGHT }
    /** Alignement vertical possible. */
    public enum VerticalAlignment { TOP, CENTER, BOTTOM }

    /** Types de texte HUD prédéfinis pour l'affichage de données du jeu. */
    public enum TextType {
        SPEED, VERSION, TOTALSCORE, BESTSCORE, SCORE, LIVES, TIME, BALLS, ENEMIES,
        FPS, POSITION, ORIENTATION, ACTIVE_BALLS, ACTIVE_ENEMIES, DISTANCE_TARGET
    }

    private static final Map<TextType, Function<GameData, String>> FORMATTERS = new LinkedHashMap<>();

    static {
        FORMATTERS.put(TextType.SPEED, data -> String.format("Vitesse: %.1f", data.getSpeed()));
        FORMATTERS.put(TextType.VERSION, data -> "Version: " + data.getVersion());
        FORMATTERS.put(TextType.TOTALSCORE, data -> "Total Score: " + (int) data.getTotalScore());
        FORMATTERS.put(TextType.BESTSCORE, data -> "Best Score: " + (int) data.getBestScore());
        FORMATTERS.put(TextType.SCORE, data -> "Score: " + (int) data.getScore());
        FORMATTERS.put(TextType.LIVES, data -> "Vies: " + (int) data.getLives());
        FORMATTERS.put(TextType.TIME, data -> {
            int minutes = (int) (data.getElapsedTime() / 60);
            int seconds = (int) (data.getElapsedTime() % 60);
            return String.format("Temps: %02d:%02d", minutes, seconds);
        });
        FORMATTERS.put(TextType.BALLS, data -> "Balles: " + (int) data.getBallsFired());
        FORMATTERS.put(TextType.ENEMIES, data -> "Ennemis: " + (int) data.getEnemiesKilled());
        FORMATTERS.put(TextType.FPS, data -> "FPS: " + (int) data.getFPS());
        FORMATTERS.put(TextType.POSITION, data -> {
            float[] pos = data.getPlayerPosition();
            return String.format("Position: %.1f / %.1f / %.1f", pos[0], pos[1], pos[2]);
        });
        FORMATTERS.put(TextType.ORIENTATION, data -> {
            float[] ori = data.getPlayerOrientation();
            return String.format("Orientation: %.1f / %.1f / %.1f", ori[0], ori[1], ori[2]);
        });
        FORMATTERS.put(TextType.ACTIVE_BALLS, data -> {
            float[] b = data.getActiveBalls();
            return "Balles actives: " + (int) b[0] + "/" + (int) b[1];
        });
        FORMATTERS.put(TextType.ACTIVE_ENEMIES, data -> {
            float[] e = data.getActiveEnemies();
            return "Ennemis actifs: " + (int) e[0] + "/" + (int) e[1];
        });
        FORMATTERS.put(TextType.DISTANCE_TARGET, data ->
                String.format("Distance cible: %.1f", data.getDistanceTarget()));
    }

    private final TextType type;
    private final HorizontalAlignment hAlign;
    private final VerticalAlignment vAlign;
    private float scale;
    private float r, g, b;
    private boolean active = true;
    private final boolean debugActive;
    private String text = null;

    private float x = 0f;
    private float y = 0f;

    private float screenX;
    private float screenY;
    private float width;
    private float height;

    /**
     * Constructeur d'un texte HUD formaté à partir d'un type prédéfini.
     *
     * @param type        type de texte à afficher
     * @param hAlign      alignement horizontal
     * @param vAlign      alignement vertical
     * @param scale       échelle du texte
     * @param r           composante rouge de la couleur
     * @param g           composante verte de la couleur
     * @param b           composante bleue de la couleur
     * @param debugActive true si le texte n'est visible qu'en mode debug
     */
    public TextHUD(TextType type, HorizontalAlignment hAlign, VerticalAlignment vAlign,
                   float scale, float r, float g, float b, boolean debugActive) {
        this.type = type;
        this.hAlign = hAlign;
        this.vAlign = vAlign;
        this.scale = scale;
        this.r = r;
        this.g = g;
        this.b = b;
        this.debugActive = debugActive;
    }

    /**
     * Constructeur d'un texte HUD formaté (non debug).
     *
     * @param type   type de texte à afficher
     * @param hAlign alignement horizontal
     * @param vAlign alignement vertical
     * @param scale  échelle du texte
     * @param r      composante rouge
     * @param g      composante verte
     * @param b      composante bleue
     */
    public TextHUD(TextType type, HorizontalAlignment hAlign, VerticalAlignment vAlign,
                   float scale, float r, float g, float b) {
        this(type, hAlign, vAlign, scale, r, g, b, false);
    }

    /**
     * Constructeur d'un texte HUD avec texte statique personnalisé.
     *
     * @param type        type de texte
     * @param text        texte statique à afficher
     * @param hAlign      alignement horizontal
     * @param vAlign      alignement vertical
     * @param scale       échelle du texte
     * @param r           composante rouge
     * @param g           composante verte
     * @param b           composante bleue
     * @param debugActive true si le texte n'est visible qu'en mode debug
     */
    public TextHUD(TextType type, String text, HorizontalAlignment hAlign, VerticalAlignment vAlign,
                   float scale, float r, float g, float b, boolean debugActive) {
        this(type, hAlign, vAlign, scale, r, g, b, debugActive);
        this.text = text;
    }

    /**
     * Constructeur d'un texte HUD avec texte statique (non debug).
     *
     * @param type   type de texte
     * @param text   texte statique à afficher
     * @param hAlign alignement horizontal
     * @param vAlign alignement vertical
     * @param scale  échelle du texte
     * @param r      composante rouge
     * @param g      composante verte
     * @param b      composante bleue
     */
    public TextHUD(TextType type, String text, HorizontalAlignment hAlign, VerticalAlignment vAlign,
                   float scale, float r, float g, float b) {
        this(type, text, hAlign, vAlign, scale, r, g, b, false);
    }

    /**
     * Retourne la position X du texte.
     *
     * @return position X
     */
    public float getX() { return x; }

    /**
     * Définit la position X du texte.
     *
     * @param x position X
     */
    public void setX(float x) { this.x = x; }

    /**
     * Retourne la position Y du texte.
     *
     * @return position Y
     */
    public float getY() { return y; }

    /**
     * Définit la position Y du texte.
     *
     * @param y position Y
     */
    public void setY(float y) { this.y = y; }

    /**
     * Retourne l'échelle du texte.
     *
     * @return échelle
     */
    public float getScale() { return scale; }

    /**
     * Définit l'échelle du texte.
     *
     * @param scale échelle
     */
    public void setScale(float scale) { this.scale = scale; }

    /**
     * Retourne la composante rouge de la couleur.
     *
     * @return rouge
     */
    public float getR() { return r; }

    /**
     * Retourne la composante verte de la couleur.
     *
     * @return vert
     */
    public float getG() { return g; }

    /**
     * Retourne la composante bleue de la couleur.
     *
     * @return bleu
     */
    public float getB() { return b; }

    /**
     * Définit la couleur RGB du texte.
     *
     * @param r rouge
     * @param g vert
     * @param b bleu
     */
    public void setRGB(float r, float g, float b) { this.r = r; this.g = g; this.b = b; }

    /**
     * Retourne si le texte est actif en mode debug.
     *
     * @return true si actif en debug
     */
    public boolean getDebugActive() { return debugActive; }

    /**
     * Retourne si le texte est actif.
     *
     * @return true si actif
     */
    public boolean isActive() { return active; }

    /**
     * Active ou désactive l'affichage du texte.
     *
     * @param active état d'activation
     */
    public void setActive(boolean active) { this.active = active; }

    /**
     * Retourne l'alignement horizontal.
     *
     * @return alignement horizontal
     */
    public HorizontalAlignment getHAlign() { return hAlign; }

    /**
     * Retourne l'alignement vertical.
     *
     * @return alignement vertical
     */
    public VerticalAlignment getVAlign() { return vAlign; }

    /**
     * Retourne le texte formaté à partir des données du jeu ou le texte statique.
     *
     * @param data données du jeu
     * @return texte formaté
     */
    public String getText(GameData data) {
        if (type == null) return getStaticText();
        Function<GameData, String> formatter = FORMATTERS.get(type);
        if (formatter != null) return formatter.apply(data);
        return getStaticText();
    }

    private String getStaticText() {
        return text != null ? text : "Aucun texte : " + type;
    }

    /**
     * Définit le texte statique.
     *
     * @param text texte statique
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Définit la position du texte à l'écran.
     *
     * @param x position X
     * @param y position Y
     */
    public void setScreenPosition(float x, float y) { this.screenX = x; this.screenY = y; }

    /**
     * Retourne la position X à l'écran.
     *
     * @return position X
     */
    public float getScreenX() { return screenX; }

    /**
     * Retourne la position Y à l'écran.
     *
     * @return position Y
     */
    public float getScreenY() { return screenY; }

    /**
     * Définit la taille du texte à l'écran.
     *
     * @param width  largeur
     * @param height hauteur
     */
    public void setSize(float width, float height) { this.width = width; this.height = height; }

    /**
     * Retourne la largeur du texte.
     *
     * @return largeur
     */
    public float getWidth() { return width; }

    /**
     * Retourne la hauteur du texte.
     *
     * @return hauteur
     */
    public float getHeight() { return height; }

    /**
     * Vérifie si les coordonnées de la souris sont dans la zone du texte.
     *
     * @param t      le texte HUD
     * @param mouseX position X de la souris
     * @param mouseY position Y de la souris
     * @return true si la souris survole le texte
     */
    public static boolean coodsMouseOn(TextHUD t, float mouseX, float mouseY) {
        float x = t.getScreenX();
        float y = t.getScreenY();
        float w = t.getWidth();
        float h = t.getHeight();

        return mouseX >= x && mouseX <= x + w &&
                mouseY >= y && mouseY <= y + h;
    }

}
