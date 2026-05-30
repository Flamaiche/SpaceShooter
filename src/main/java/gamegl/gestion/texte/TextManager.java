package gamegl.gestion.texte;

import gamegl.gestion.donnees.GameData;
import learngl.Shader;
import java.util.ArrayList;

/**
 * Gestionnaire de texte HUD avec support d'alignement et de redimensionnement.
 */
public class TextManager {

    private final GameData data;
    private ArrayList<TextHUD> texts = new ArrayList<>();
    private boolean debugMode = false;
    private final ArrayList<TextHUD> topTexts = new ArrayList<>();
    private final ArrayList<TextHUD> bottomTexts = new ArrayList<>();
    private final ArrayList<TextHUD> centerTexts = new ArrayList<>();

    private final int baseWidth = 800;
    private final int baseHeight = 600;

    private int windowWidth;
    private int windowHeight;

    /** Marge par défaut pour l'alignement du texte. */
    public static final float margin = 20f;
    /** Échelle uniforme par défaut du texte. */
    public static final float uniformTextScale = 1.5f;

    /**
     * Constructeur du gestionnaire de texte.
     *
     * @param data           données du jeu
     * @param initialWidth   largeur initiale de la fenêtre
     * @param initialHeight  hauteur initiale de la fenêtre
     */
    public TextManager(GameData data, int initialWidth, int initialHeight) {
        this.data = data;
        this.windowWidth = initialWidth;
        this.windowHeight = initialHeight;
    }

    /**
     * Met à jour la taille de la fenêtre.
     *
     * @param width  nouvelle largeur
     * @param height nouvelle hauteur
     */
    public void setWindowSize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
    }

    /**
     * Active ou désactive le mode debug.
     *
     * @param debug true pour activer le debug
     */
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
        for (TextHUD t : texts) {
            if (t.getDebugActive()) t.setActive(debug);
        }
    }

    /**
     * Définit la liste des textes HUD.
     *
     * @param texts liste des textes HUD
     */
    public void setTexts(ArrayList<TextHUD> texts) {
        this.texts = texts;
        setDebugMode(debugMode);
    }

    /**
     * Retourne la liste des textes HUD.
     *
     * @return liste des textes HUD
     */
    public ArrayList<TextHUD> getTexts() { return texts; }

    /**
     * Met à jour le gestionnaire avec la taille actuelle de la fenêtre.
     *
     * @param currentWindowWidth    largeur actuelle de la fenêtre
     * @param currentWindowHeight   hauteur actuelle de la fenêtre
     */
    public void update(int currentWindowWidth, int currentWindowHeight) {
        setWindowSize(currentWindowWidth, currentWindowHeight);
    }

    /**
     * Affiche tous les textes HUD triés par alignement.
     *
     * @param shader le shader à utiliser
     */
    public void render(Shader shader) {
        float scaleX = (float) windowWidth / baseWidth;
        float scaleY = (float) windowHeight / baseHeight;
        float uniformScale = Math.min(scaleX, scaleY);

        topTexts.clear();
        bottomTexts.clear();
        centerTexts.clear();

        for (TextHUD t : texts) {
            if (!t.isActive()) continue;

            if (t.getVAlign() == null) {
                centerTexts.add(t);
            } else {
                switch (t.getVAlign()) {
                    case TOP -> topTexts.add(t);
                    case BOTTOM -> bottomTexts.add(t);
                    case CENTER -> centerTexts.add(t);
                }
            }
        }

        renderAlignedTexts(topTexts, shader, uniformScale, true);
        renderAlignedTexts(bottomTexts, shader, uniformScale, false);
        renderCenterTexts(centerTexts, shader, uniformScale);
    }

    private void renderAlignedTexts(ArrayList<TextHUD> texts, Shader shader, float uniformScale, boolean fromTop) {
        float[] yOffsets = { margin * uniformScale, margin * uniformScale, margin * uniformScale };
        for (TextHUD t : texts) {
            String content = t.getText(data);
            float[] extent = Text.getTextExtent(content, t.getScale() * uniformScale);
            float rx = calcRenderX(t, extent[0], uniformScale);
            int idx = alignmentIndex(t.getHAlign());
            float ry = calcRenderYAligned(extent[1], yOffsets, fromTop, idx);
            drawText(t, shader, content, extent, rx, ry, uniformScale);
            if (t.getHAlign() != null && t.getVAlign() != null) {
                yOffsets[alignmentIndex(t.getHAlign())] += extent[1] + margin * uniformScale;
            }
        }
    }

    private void renderCenterTexts(ArrayList<TextHUD> texts, Shader shader, float uniformScale) {
        float totalHeight = -margin * uniformScale;
        for (TextHUD t : texts) {
            totalHeight += Text.getTextExtent(t.getText(data), t.getScale() * uniformScale)[1]
                    + margin * uniformScale;
        }
        float startY = (windowHeight - totalHeight) / 2f;
        float centerOffset = 0f;

        for (TextHUD t : texts) {
            String content = t.getText(data);
            float[] extent = Text.getTextExtent(content, t.getScale() * uniformScale);
            float rx = calcRenderX(t, extent[0], uniformScale);
            float ry = (t.getVAlign() == null) ? t.getY() : startY + centerOffset;
            drawText(t, shader, content, extent, rx, ry, uniformScale);
            if (t.getVAlign() != null) centerOffset += extent[1] + margin * uniformScale;
        }
    }

    private void drawText(TextHUD t, Shader shader, String content, float[] extent, float renderX, float renderY, float uniformScale) {
        t.setScreenPosition(renderX, renderY);
        t.setSize(extent[0], extent[1]);
        Text.drawText(shader, content, renderX, renderY, t.getScale() * uniformScale,
                t.getR(), t.getG(), t.getB());
    }

    private float calcRenderX(TextHUD t, float textWidth, float uniformScale) {
        if (t.getHAlign() == null) return t.getX();
        return switch (t.getHAlign()) {
            case LEFT -> margin * uniformScale;
            case RIGHT -> windowWidth - margin * uniformScale - textWidth;
            case CENTER -> (windowWidth - textWidth) / 2f;
        };
    }

    private float calcRenderYAligned(float textHeight, float[] yOffsets, boolean fromTop, int idx) {
        return fromTop ? yOffsets[idx] : windowHeight - yOffsets[idx] - textHeight;
    }

    private int alignmentIndex(TextHUD.HorizontalAlignment hAlign) {
        if (hAlign == null) return 2;
        return switch (hAlign) {
            case LEFT -> 0;
            case RIGHT -> 1;
            case CENTER -> 2;
        };
    }

    /**
     * Retourne la largeur de base pour le calcul d'échelle.
     *
     * @return largeur de base
     */
    public int getBaseWidth() {
        return baseWidth;
    }

    /**
     * Retourne la hauteur de base pour le calcul d'échelle.
     *
     * @return hauteur de base
     */
    public int getBaseHeight() {
        return baseHeight;
    }

    /**
     * Retourne l'état du mode debug.
     *
     * @return true si le debug est activé
     */
    public boolean getDebugMode() {
        return debugMode;
    }
}
