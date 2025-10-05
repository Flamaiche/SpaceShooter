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

        this.textManager = new TextManager(initialWindowWidth, initialWindowHeight);

        setText(text);
    }

    private ArrayList<TextHUD> destructeurText(String text) {
        ArrayList<TextHUD> listHUD = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            TextHUD letter = new TextHUD(null, String.valueOf(c), null, null, scale, r, g, b);
            listHUD.add(letter);
        }
        return listHUD;
    }

    public void setText(String text) {
        this.letters = new ArrayList<>(destructeurText(text));
        this.textManager.setTexts(letters);
    }

    public void update(double deltaTime, int currentWindowWidth, int currentWindowHeight) {
        time += deltaTime;

        textManager.update((float) deltaTime, currentWindowWidth, currentWindowHeight);

        float scaleX = (float) currentWindowWidth / textManager.getBaseWidth();
        float scaleY = (float) currentWindowHeight / textManager.getBaseHeight();
        float uniformScale = Math.min(scaleX, scaleY);

        double cx = (double) currentWindowWidth / 2.0;
        double cy = (double) currentWindowHeight / 2.0;

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
