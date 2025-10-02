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

        renderAlignedTexts(topTexts, shader, uniformScale, true);
        renderAlignedTexts(bottomTexts, shader, uniformScale, false);
        renderCenterTexts(centerTexts, shader, uniformScale);
    }

    private void renderAlignedTexts(ArrayList<TextHUD> texts, Shader shader, float uniformScale, boolean fromTop) {
        // index 0 = LEFT, 1 = RIGHT, 2 = CENTER (default)
        float[] yOffsets = { margin * uniformScale, margin * uniformScale, margin * uniformScale };
        GameData data = GameData.getInstance();

        for (TextHUD t : texts) {
            String content = t.getText(data);
            float textWidth = Text.getTextWidth(content, t.getScale() * uniformScale);
            float textHeight = Text.getTextHeight(content, t.getScale() * uniformScale);

            int idx = switch (t.getHAlign()) {
                case LEFT -> 0;
                case RIGHT -> 1;
                default -> 2;
            };

            float renderX = switch (t.getHAlign()) {
                case LEFT -> margin * uniformScale;
                case RIGHT -> windowWidth - margin * uniformScale - textWidth;
                default -> (windowWidth - textWidth) / 2f;
            };

            float renderY = fromTop ? yOffsets[idx] : windowHeight - yOffsets[idx] - textHeight;

            Text.drawText(shader, content, renderX, renderY, t.getScale() * uniformScale, t.getR(), t.getG(), t.getB());

            // incrément offset
            yOffsets[idx] += textHeight + margin * uniformScale;
        }
    }

    private void renderCenterTexts(ArrayList<TextHUD> texts, Shader shader, float uniformScale) {
        GameData data = GameData.getInstance();
        float totalHeight = -margin * uniformScale; // correction dernier margin
        for (TextHUD t : texts) {
            totalHeight += Text.getTextHeight(t.getText(data), t.getScale() * uniformScale) + margin * uniformScale;
        }
        float startY = (windowHeight - totalHeight) / 2f;
        float centerOffset = 0f;

        for (TextHUD t : texts) {
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
