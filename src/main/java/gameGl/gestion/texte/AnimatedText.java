package gameGl.gestion.texte;

import learnGL.tools.Shader;
import java.util.ArrayList;

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

    /**
     * @param initialWindowWidth  largeur de la fenêtre au moment de la création (TextManager construit avec ça)
     * @param initialWindowHeight hauteur initiale
     */
    public AnimatedText(String text, float scale, float r, float g, float b,
                        double radius, double centerX, double centerY, double toursPerSecond,
                        TextTransformFunction transformFunction,
                        int initialWindowWidth, int initialWindowHeight) {

        this.scale = scale;
        this.r = r;
        this.g = g;
        this.b = b;

        this.radius = radius;
        this.centerX = centerX;
        this.centerY = centerY;
        this.toursPerSecond = toursPerSecond;
        this.transformFunction = transformFunction;

        // TextManager dédié pour ce AnimatedText (garde le même comportement d'auto-scale)
        this.textManager = new TextManager(initialWindowWidth, initialWindowHeight);

        // initialise les lettres
        setText(text);
    }

    private ArrayList<TextHUD> destructeurText(String text) {
        ArrayList<TextHUD> listHUD = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // hAlign/vAlign = null -> on gère la position via setX/setY (TextManager traitera les échelles)
            TextHUD letter = new TextHUD(null, String.valueOf(c), null, null, scale, r, g, b);
            listHUD.add(letter);
        }
        return listHUD;
    }

    public void setText(String text) {
        this.letters = new ArrayList<>(destructeurText(text));
        this.textManager.setTexts(letters);
    }

    /**
     * @param deltaTime seconds elapsed
     * @param currentWindowWidth current window width (utile pour calculer uniformScale et centre)
     * @param currentWindowHeight current window height
     */
    public void update(double deltaTime, int currentWindowWidth, int currentWindowHeight) {
        time += deltaTime;

        // informer le TextManager de la taille de fenêtre actuelle (il mettra à jour son uniformScale interne lors du render)
        textManager.update((float) deltaTime, currentWindowWidth, currentWindowHeight);

        // calculer uniformScale localement (même formule que dans TextManager)
        float scaleX = (float) currentWindowWidth / textManager.getBaseWidth();
        float scaleY = (float) currentWindowHeight / textManager.getBaseHeight();
        float uniformScale = Math.min(scaleX, scaleY);

        // centre : on aligne au centre de la fenêtre (on recalcule dynamiquement)
        double cx = (double) currentWindowWidth / 2.0;
        double cy = (double) currentWindowHeight / 2.0;

        // mettre à jour la position de chaque lettre via la transformFunction
        for (int i = 0; i < letters.size(); i++) {
            TextHUD letter = letters.get(i);

            // on passe le rayon scalé et le centre scalé (comme dans ton code original)
            double[] pos = transformFunction.apply(time, radius * uniformScale, cx, cy, toursPerSecond, i);
            letter.setX((float) pos[0]);
            letter.setY((float) pos[1]);

            // garder la "base" scale ; le TextManager appliquera scale * uniformScale lors du rendu
            letter.setScale(scale);
        }

        // synchroniser la liste de texts dans le TextManager
        textManager.setTexts(letters);

        // appeler une update "zéro" si besoin pour recalculer width/height internes (optionnel)
        textManager.update(0f, currentWindowWidth, currentWindowHeight);
    }

    public void render(Shader shader) {
        textManager.render(shader);
    }

    public ArrayList<TextHUD> getLetters() {
        return letters;
    }

    public TextManager getTextManager() {
        return textManager;
    }
}
