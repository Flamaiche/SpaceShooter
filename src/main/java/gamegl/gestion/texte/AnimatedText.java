package gamegl.gestion.texte;

import gamegl.gestion.donnees.GameData;
import learngl.tools.Shader;
import java.util.ArrayList;

/**
 * Texte animé dont chaque lettre ou groupe de lettres suit une transformation dynamique.
 */
public class AnimatedText {

    private ArrayList<TextHUD> letters = new ArrayList<>();
    private final TextTransformFunction transformFunction;
    private final TextManager textManager;

    private double time = 0;
    private double radius;
    private double centerX, centerY;
    private double toursPerSecond;

    private float scale;
    private float r;
    private float g;
    private float b;

    private TextHUD.HorizontalAlignment ha = null;
    private TextHUD.VerticalAlignment va = null;

    private int lettrePack = 1;

    /**
     * Constructeur d'un texte animé avec alignement horizontal et vertical.
     *
     * @param text                  texte à animer
     * @param scale                 échelle du texte
     * @param r                     composante rouge
     * @param g                     composante verte
     * @param b                     composante bleue
     * @param radius                rayon de l'animation
     * @param centerX               centre X
     * @param centerY               centre Y
     * @param toursPerSecond        vitesse de rotation
     * @param transformFunction     fonction de transformation positionnelle
     * @param initialWindowWidth    largeur initiale de la fenêtre
     * @param initialWindowHeight   hauteur initiale de la fenêtre
     * @param lettrePack            nombre de lettres par groupe
     * @param ha                    alignement horizontal
     * @param va                    alignement vertical
     * @param data                  données du jeu
     */
    public AnimatedText(String text, float scale, float r, float g, float b,
                        double radius, double centerX, double centerY, double toursPerSecond,
                        TextTransformFunction transformFunction,
                        int initialWindowWidth, int initialWindowHeight, int lettrePack, TextHUD.HorizontalAlignment ha, TextHUD.VerticalAlignment va,
                        GameData data) {
        this(text, scale, r, g, b, radius, centerX, centerY, toursPerSecond, transformFunction, lettrePack, initialWindowWidth, initialWindowHeight, data);
        this.ha = ha;
        this.va = va;
        setText(text);
    }


    /**
     * Constructeur d'un texte animé sans alignement explicite.
     *
     * @param text                  texte à animer
     * @param scale                 échelle du texte
     * @param r                     composante rouge
     * @param g                     composante verte
     * @param b                     composante bleue
     * @param radius                rayon de l'animation
     * @param centerX               centre X
     * @param centerY               centre Y
     * @param toursPerSecond        vitesse de rotation
     * @param transformFunction     fonction de transformation positionnelle
     * @param initialWindowWidth    largeur initiale de la fenêtre
     * @param initialWindowHeight   hauteur initiale de la fenêtre
     * @param lettrePack            nombre de lettres par groupe
     * @param data                  données du jeu
     */
    public AnimatedText(String text, float scale, float r, float g, float b,
                        double radius, double centerX, double centerY, double toursPerSecond,
                        TextTransformFunction transformFunction,
                        int initialWindowWidth, int initialWindowHeight, int lettrePack,
                        GameData data) {

        this.scale = scale;
        this.r = r;
        this.g = g;
        this.b = b;

        this.radius = radius;
        this.centerX = centerX;
        this.centerY = centerY;
        this.toursPerSecond = toursPerSecond;
        this.transformFunction = transformFunction;

        this.textManager = new TextManager(data, initialWindowWidth, initialWindowHeight);

        setText(text);
    }

    private ArrayList<TextHUD> destructeurText(String text) {
        ArrayList<TextHUD> listHUD = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i));

            if (sb.length() == lettrePack || i == text.length() - 1) {
                TextHUD letter = new TextHUD(null, sb.toString(), ha, va, scale, r, g, b);
                listHUD.add(letter);
                sb.setLength(0);
            }
        }

        return listHUD;
    }

    /**
     * Définit le texte à animer, en reconstruisant les groupes de lettres.
     *
     * @param text le texte à animer
     */
    public void setText(String text) {
        this.letters = new ArrayList<>(destructeurText(text));
        this.textManager.setTexts(letters);
    }

    /**
     * Met à jour la position de chaque lettre selon la fonction de transformation.
     *
     * @param deltaTime             temps écoulé depuis la dernière frame
     * @param currentWindowWidth    largeur actuelle de la fenêtre
     * @param currentWindowHeight   hauteur actuelle de la fenêtre
     */
    public void update(double deltaTime, int currentWindowWidth, int currentWindowHeight) {
        time += deltaTime;

        textManager.update((float) deltaTime, currentWindowWidth, currentWindowHeight);

        float scaleX = (float) currentWindowWidth / textManager.getBaseWidth();
        float scaleY = (float) currentWindowHeight / textManager.getBaseHeight();
        float uniformScale = Math.min(scaleX, scaleY);

        double cx = this.centerX;
        double cy = this.centerY;

        for (int i = 0; i < letters.size(); i++) {
            TextHUD letter = letters.get(i);

            double[] pos = transformFunction.apply(time, radius * uniformScale, cx, cy, toursPerSecond, i);
            letter.setX((float) pos[0]);
            letter.setY((float) pos[1]);

            letter.setScale(scale);
        }

        textManager.setTexts(letters);

        textManager.update(0f, currentWindowWidth, currentWindowHeight);
    }

    /**
     * Affiche le texte animé via le gestionnaire de texte.
     *
     * @param shader le shader à utiliser
     */
    public void render(Shader shader) {
        textManager.render(shader);
    }

    /**
     * Retourne la liste des TextHUD représentant les lettres.
     *
     * @return liste des lettres
     */
    public ArrayList<TextHUD> getLetters() {
        return letters;
    }

    /**
     * Retourne le gestionnaire de texte interne.
     *
     * @return le TextManager
     */
    public TextManager getTextManager() {
        return textManager;
    }

    /**
     * Définit le nombre de lettres par groupe.
     *
     * @param lettrePack nombre de lettres par groupe
     */
    public void setLettrePack(int lettrePack) {
        this.lettrePack = lettrePack;
    }
}
