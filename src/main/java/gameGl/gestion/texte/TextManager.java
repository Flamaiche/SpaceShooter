package gameGl.gestion.texte;

import gameGl.gestion.GameData;
import learnGL.tools.Shader;
import java.util.ArrayList;

public class TextManager {

    private final ArrayList<TextHUD> texts = new ArrayList<>();
    private boolean debugMode = false;

    private final int baseWidth = 800;
    private final int baseHeight = 600;

    private int windowWidth;
    private int windowHeight;

    private final float margin = 20f;
    private final float lineHeight = 20f;
    private final float uniformTextScale = 1.5f;

    public TextManager(int initialWidth, int initialHeight) {
        this.windowWidth = initialWidth;
        this.windowHeight = initialHeight;
        initTexts();
    }

    public void setWindowSize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
    }

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
        for (TextHUD t : texts) {
            switch (t.getType()) {
                case FPS, POSITION, ORIENTATION, ACTIVE_BALLS, ACTIVE_ENEMIES, DISTANCE_TARGET -> t.setActive(debug);
                default -> {
                }
            }
        }
    }

    private void initTexts() {
        // HUD joueur (left/top)
        texts.add(new TextHUD(TextHUD.TextType.SCORE, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.LIVES, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f)); // <-- nouveau
        texts.add(new TextHUD(TextHUD.TextType.TIME, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.BALLS, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.ENEMIES, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));

        // Debug (right/top)
        texts.add(new TextHUD(TextHUD.TextType.FPS, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f));
        texts.add(new TextHUD(TextHUD.TextType.POSITION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f));
        texts.add(new TextHUD(TextHUD.TextType.ORIENTATION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f));
        texts.add(new TextHUD(TextHUD.TextType.ACTIVE_BALLS, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f));
        texts.add(new TextHUD(TextHUD.TextType.ACTIVE_ENEMIES, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f));
        texts.add(new TextHUD(TextHUD.TextType.DISTANCE_TARGET, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f));

        setDebugMode(false);
    }

    public void update(float deltaTime, int currentWindowWidth, int currentWindowHeight) {
        setWindowSize(currentWindowWidth, currentWindowHeight);
        // Les valeurs des textes sont lues dynamiquement depuis GameData dans getText()
    }

    public void render(Shader shader) {
        GameData data = GameData.getInstance();

        float scaleX = (float) windowWidth / baseWidth;
        float scaleY = (float) windowHeight / baseHeight;
        float uniformScale = Math.min(scaleX, scaleY);

        // Séparer les textes par vAlign pour gérer le centrage correctement
        ArrayList<TextHUD> topTexts = new ArrayList<>();
        ArrayList<TextHUD> bottomTexts = new ArrayList<>();
        ArrayList<TextHUD> centerTexts = new ArrayList<>();

        for (TextHUD t : texts) {
            if (!t.isActive()) continue;
            switch (t.getVAlign()) {
                case TOP -> topTexts.add(t);
                case BOTTOM -> bottomTexts.add(t);
                case CENTER -> centerTexts.add(t);
            }
        }

        // Offsets pour top et bottom
        float yOffsetTopLeft = margin * uniformScale;
        float yOffsetTopRight = margin * uniformScale;
        float yOffsetBottomLeft = margin * uniformScale;
        float yOffsetBottomRight = margin * uniformScale;

        // --- TOP TEXTS ---
        for (TextHUD t : topTexts) {
            String content = t.getText(data);
            float textWidth = Text.getTextWidth(content, t.getScale() * uniformScale);

            float renderX = (t.getHAlign() == TextHUD.HorizontalAlignment.LEFT) ? margin * uniformScale :
                    (t.getHAlign() == TextHUD.HorizontalAlignment.RIGHT) ? windowWidth - margin * uniformScale - textWidth :
                            (windowWidth - textWidth) / 2f;

            float renderY;
            if (t.getHAlign() == TextHUD.HorizontalAlignment.LEFT)
                renderY = yOffsetTopLeft;
            else if (t.getHAlign() == TextHUD.HorizontalAlignment.RIGHT)
                renderY = yOffsetTopRight;
            else
                renderY = yOffsetTopLeft; // CENTER horizontal utilise top offset

            Text.drawText(shader, content, renderX, renderY, t.getScale() * uniformScale, t.getR(), t.getG(), t.getB());

            // incrément offset
            if (t.getHAlign() == TextHUD.HorizontalAlignment.LEFT)
                yOffsetTopLeft += lineHeight * uniformScale;
            else if (t.getHAlign() == TextHUD.HorizontalAlignment.RIGHT)
                yOffsetTopRight += lineHeight * uniformScale;
            else
                yOffsetTopLeft += lineHeight * uniformScale;
        }

        // --- BOTTOM TEXTS ---
        for (TextHUD t : bottomTexts) {
            String content = t.getText(data);
            float textWidth = Text.getTextWidth(content, t.getScale() * uniformScale);

            float renderX = (t.getHAlign() == TextHUD.HorizontalAlignment.LEFT) ? margin * uniformScale :
                    (t.getHAlign() == TextHUD.HorizontalAlignment.RIGHT) ? windowWidth - margin * uniformScale - textWidth :
                            (windowWidth - textWidth) / 2f;

            float renderY;
            if (t.getHAlign() == TextHUD.HorizontalAlignment.LEFT)
                renderY = windowHeight - yOffsetBottomLeft - lineHeight * uniformScale;
            else if (t.getHAlign() == TextHUD.HorizontalAlignment.RIGHT)
                renderY = windowHeight - yOffsetBottomRight - lineHeight * uniformScale;
            else
                renderY = windowHeight - yOffsetBottomLeft - lineHeight * uniformScale; // CENTER horizontal

            Text.drawText(shader, content, renderX, renderY, t.getScale() * uniformScale, t.getR(), t.getG(), t.getB());

            // incrément offset
            if (t.getHAlign() == TextHUD.HorizontalAlignment.LEFT)
                yOffsetBottomLeft += lineHeight * uniformScale;
            else if (t.getHAlign() == TextHUD.HorizontalAlignment.RIGHT)
                yOffsetBottomRight += lineHeight * uniformScale;
            else
                yOffsetBottomLeft += lineHeight * uniformScale;
        }

        // --- CENTER TEXTS ---
        // Calcul hauteur totale du bloc
        float totalHeight = centerTexts.size() * lineHeight * uniformScale;
        float startY = (windowHeight - totalHeight) / 2f;
        float centerOffset = 0f;

        for (TextHUD t : centerTexts) {
            String content = t.getText(data);
            float textWidth = Text.getTextWidth(content, t.getScale() * uniformScale);

            float renderX = (t.getHAlign() == TextHUD.HorizontalAlignment.LEFT) ? margin * uniformScale :
                    (t.getHAlign() == TextHUD.HorizontalAlignment.RIGHT) ? windowWidth - margin * uniformScale - textWidth :
                            (windowWidth - textWidth) / 2f;

            float renderY = startY + centerOffset;

            Text.drawText(shader, content, renderX, renderY, t.getScale() * uniformScale, t.getR(), t.getG(), t.getB());

            centerOffset += lineHeight * uniformScale;
        }
    }

}