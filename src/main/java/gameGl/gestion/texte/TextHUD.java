package gameGl.gestion.texte;

import gameGl.gestion.GameData;

public class TextHUD {

    public enum HorizontalAlignment { LEFT, CENTER, RIGHT }
    public enum VerticalAlignment { TOP, CENTER, BOTTOM }

    public enum TextType {
        SCORE, LIVES, TIME, BALLS, ENEMIES,
        FPS, POSITION, ORIENTATION, ACTIVE_BALLS, ACTIVE_ENEMIES, DISTANCE_TARGET
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
        if (type == null) return getText();
        switch (type) {
            case SCORE: return "Score: " + (int)data.getScore();
            case LIVES: return "Vies: " + (int)data.getLives();
            case TIME:  {
                int minutes = (int)(data.getElapsedTime() / 60);
                int seconds = (int)(data.getElapsedTime() % 60);
                return String.format("Temps: %02d:%02d", minutes, seconds);
            }
            case BALLS: return "Balles: " + (int)data.getBallsFired();
            case ENEMIES: return "Ennemis: " + (int)data.getEnemiesKilled();
            case FPS: return "FPS: " + (int)data.getFPS();
            case POSITION: {
                float[] pos = data.getPlayerPosition();
                return String.format("Position: %.1f, %.1f, %.1f", pos[0], pos[1], pos[2]);
            }
            case ORIENTATION: {
                float[] ori = data.getPlayerOrientation();
                return String.format("Orientation: %.1f, %.1f, %.1f", ori[0], ori[1], ori[2]);
            }
            case ACTIVE_BALLS: {
                float[] b = data.getActiveBalls();
                return "Balles actives: " + (int)b[0] + "/" + (int)b[1];
            }
            case ACTIVE_ENEMIES: {
                float[] e = data.getActiveEnemies();
                return "Ennemis actifs: " + (int)e[0] + "/" + (int)e[1];
            }
            case DISTANCE_TARGET:
                return String.format("Distance cible: %.1f", data.getDistanceTarget());
        }
        return getText();
    }

    private String getText() {
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
}
