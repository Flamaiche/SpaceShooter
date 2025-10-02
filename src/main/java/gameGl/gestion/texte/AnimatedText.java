package gameGl.gestion.texte;

import learnGL.tools.Shader;
import java.util.ArrayList;

public class AnimatedText {

    private final ArrayList<TextHUD> letters = new ArrayList<>();
    private final TextTransformFunction transformFunction;

    private double time = 0;
    private double radius;
    private double centerX, centerY;
    private double toursPerSecond;

    public AnimatedText(String text, float scale, float r, float g, float b,
                        double radius, double centerX, double centerY, double toursPerSecond,
                        TextTransformFunction transformFunction) {
        this.radius = radius;
        this.centerX = centerX;
        this.centerY = centerY;
        this.toursPerSecond = toursPerSecond;
        this.transformFunction = transformFunction;

        // Découpe en lettres
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            TextHUD letter = new TextHUD(null, String.valueOf(c),
                    null, null, scale, r, g, b);
            letters.add(letter);
        }
    }

    public void update(double deltaTime) {
        time += deltaTime;

        for (int i = 0; i < letters.size(); i++) {
            TextHUD letter = letters.get(i);
            double[] pos = transformFunction.apply(time, radius, centerX, centerY, toursPerSecond, i);
            letter.setX((float) pos[0]);
            letter.setY((float) pos[1]);
        }
    }

    public void render(Shader shader) {
        for (TextHUD letter : letters) {
            if (letter.isActive()) {
                // On dessine chaque lettre comme un TextHUD classique
                String content = letter.getText(null); // pas besoin de GameData ici
                Text.drawText(shader, content, letter.getX(), letter.getY(),
                        letter.getScale(), letter.getR(), letter.getG(), letter.getB());
            }
        }
    }

    public ArrayList<TextHUD> getLetters() {
        return letters;
    }
}
