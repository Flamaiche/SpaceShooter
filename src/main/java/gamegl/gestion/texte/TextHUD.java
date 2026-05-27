package gamegl.gestion.texte;

import gamegl.gestion.donnees.GameData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class TextHUD {

    public enum HorizontalAlignment { LEFT, CENTER, RIGHT }
    public enum VerticalAlignment { TOP, CENTER, BOTTOM }

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
    private boolean debugActive;
    private String text = null;

    private float x = 0f;
    private float y = 0f;

    private float screenX;
    private float screenY;
    private float width;
    private float height;

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

    public TextHUD(TextType type, HorizontalAlignment hAlign, VerticalAlignment vAlign,
                   float scale, float r, float g, float b) {
        this(type, hAlign, vAlign, scale, r, g, b, false);
    }

    public TextHUD(TextType type, String text, HorizontalAlignment hAlign, VerticalAlignment vAlign,
                   float scale, float r, float g, float b, boolean debugActive) {
        this(type, hAlign, vAlign, scale, r, g, b, debugActive);
        this.text = text;
    }

    public TextHUD(TextType type, String text, HorizontalAlignment hAlign, VerticalAlignment vAlign,
                   float scale, float r, float g, float b) {
        this(type, text, hAlign, vAlign, scale, r, g, b, false);
    }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; }
    public float getR() { return r; }
    public float getG() { return g; }
    public float getB() { return b; }
    public void setRGB(float r, float g, float b) { this.r = r; this.g = g; this.b = b; }
    public boolean getDebugActive() { return debugActive; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public TextType getType() { return type; }
    public HorizontalAlignment getHAlign() { return hAlign; }
    public VerticalAlignment getVAlign() { return vAlign; }

    public String getText(GameData data) {
        if (type == null) return getStaticText();
        Function<GameData, String> formatter = FORMATTERS.get(type);
        if (formatter != null) return formatter.apply(data);
        return getStaticText();
    }

    private String getStaticText() {
        return text != null ? text : "Aucun texte : " + type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setScreenPosition(float x, float y) { this.screenX = x; this.screenY = y; }
    public float getScreenX() { return screenX; }
    public float getScreenY() { return screenY; }

    public void setSize(float width, float height) { this.width = width; this.height = height; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public static boolean coodsMouseOn(TextHUD t, float mouseX, float mouseY) {
        float x = t.getScreenX();
        float y = t.getScreenY();
        float w = t.getWidth();
        float h = t.getHeight();

        return mouseX >= x && mouseX <= x + w &&
                mouseY >= y && mouseY <= y + h;
    }

    public static class Builder {
        private final TextType type;
        private String text;
        private HorizontalAlignment hAlign;
        private VerticalAlignment vAlign;
        private float scale = 1.0f;
        private float r = 1f, g = 1f, b = 1f;
        private boolean debugActive = false;

        public Builder(TextType type) { this.type = type; }
        public Builder text(String text) { this.text = text; return this; }
        public Builder hAlign(HorizontalAlignment hAlign) { this.hAlign = hAlign; return this; }
        public Builder vAlign(VerticalAlignment vAlign) { this.vAlign = vAlign; return this; }
        public Builder scale(float scale) { this.scale = scale; return this; }
        public Builder color(float r, float g, float b) { this.r = r; this.g = g; this.b = b; return this; }
        public Builder debug(boolean debug) { this.debugActive = debug; return this; }

        public TextHUD build() {
            TextHUD hud = new TextHUD(type, hAlign, vAlign, scale, r, g, b, debugActive);
            hud.text = text;
            return hud;
        }
    }
}
