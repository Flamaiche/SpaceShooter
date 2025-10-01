package gameGl.gestion.texte;

import gameGl.gestion.GameData;
import learnGL.tools.Shader;
import java.util.ArrayList;

public class TextManager {

    private ArrayList<TextHUD> texts = new ArrayList<>();
    private boolean debugMode = false;

    private final int baseWidth = 800;
    private final int baseHeight = 600;

    private int windowWidth;
    private int windowHeight;

    public static final float margin = 20f;
    public static final float uniformTextScale = 1.5f;

    public TextManager(int initialWidth, int initialHeight) {
        this.windowWidth = initialWidth;
        this.windowHeight = initialHeight;
    }

    public void setWindowSize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
    }

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
        for (TextHUD t : texts) {
            if (t.getDebugActive()) t.setActive(debug);
        }
    }

    public void setTexts(ArrayList<TextHUD> texts) {
        this.texts = texts;
    }

    public void update(float deltaTime, int currentWindowWidth, int currentWindowHeight) {
        setWindowSize(currentWindowWidth, currentWindowHeight);
    }

    public void render(Shader shader) {
        GameData data = GameData.getInstance();

        float scaleX = (float) windowWidth / baseWidth;
        float scaleY = (float) windowHeight / baseHeight;
        float uniformScale = Math.min(scaleX, scaleY);

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

        float yOffsetTopLeft = margin * uniformScale;
        float yOffsetTopRight = margin * uniformScale;
        float yOffsetBottomLeft = margin * uniformScale;
        float yOffsetBottomRight = margin * uniformScale;

        // --- TOP TEXTS ---
        for (TextHUD t : topTexts) {
            String content = t.getText(data);
            float textWidth = Text.getTextWidth(content, t.getScale() * uniformScale);
            float textHeight = Text.getTextHeight(content, t.getScale() * uniformScale);

            float renderX = switch (t.getHAlign()) {
                case LEFT -> margin * uniformScale;
                case RIGHT -> windowWidth - margin * uniformScale - textWidth;
                default -> (windowWidth - textWidth) / 2f;
            };

            float renderY = switch (t.getHAlign()) {
                case LEFT -> yOffsetTopLeft;
                case RIGHT -> yOffsetTopRight;
                default -> yOffsetTopLeft;
            };

            Text.drawText(shader, content, renderX, renderY, t.getScale() * uniformScale, t.getR(), t.getG(), t.getB());

            // incrément offset selon hauteur réelle + margin
            switch (t.getHAlign()) {
                case LEFT -> yOffsetTopLeft += textHeight + margin * uniformScale;
                case RIGHT -> yOffsetTopRight += textHeight + margin * uniformScale;
                default -> yOffsetTopLeft += textHeight + margin * uniformScale;
            }
        }

        // --- BOTTOM TEXTS ---
        for (TextHUD t : bottomTexts) {
            String content = t.getText(data);
            float textWidth = Text.getTextWidth(content, t.getScale() * uniformScale);
            float textHeight = Text.getTextHeight(content, t.getScale() * uniformScale);

            float renderX = switch (t.getHAlign()) {
                case LEFT -> margin * uniformScale;
                case RIGHT -> windowWidth - margin * uniformScale - textWidth;
                default -> (windowWidth - textWidth) / 2f;
            };

            float renderY = switch (t.getHAlign()) {
                case LEFT -> windowHeight - yOffsetBottomLeft - textHeight;
                case RIGHT -> windowHeight - yOffsetBottomRight - textHeight;
                default -> windowHeight - yOffsetBottomLeft - textHeight;
            };

            Text.drawText(shader, content, renderX, renderY, t.getScale() * uniformScale, t.getR(), t.getG(), t.getB());

            switch (t.getHAlign()) {
                case LEFT -> yOffsetBottomLeft += textHeight + margin * uniformScale;
                case RIGHT -> yOffsetBottomRight += textHeight + margin * uniformScale;
                default -> yOffsetBottomLeft += textHeight + margin * uniformScale;
            }
        }

        // --- CENTER TEXTS ---
        float totalHeight = 0f;
        for (TextHUD t : centerTexts) {
            totalHeight += Text.getTextHeight(t.getText(data), t.getScale() * uniformScale) + margin * uniformScale;
        }
        float startY = (windowHeight - totalHeight) / 2f;
        float centerOffset = 0f;

        for (TextHUD t : centerTexts) {
            String content = t.getText(data);
            float textWidth = Text.getTextWidth(content, t.getScale() * uniformScale);
            float textHeight = Text.getTextHeight(content, t.getScale() * uniformScale);

            float renderX = switch (t.getHAlign()) {
                case LEFT -> margin * uniformScale;
                case RIGHT -> windowWidth - margin * uniformScale - textWidth;
                default -> (windowWidth - textWidth) / 2f;
            };

            float renderY = startY + centerOffset;

            Text.drawText(shader, content, renderX, renderY, t.getScale() * uniformScale, t.getR(), t.getG(), t.getB());

            centerOffset += textHeight + margin * uniformScale;
        }
    }
}
