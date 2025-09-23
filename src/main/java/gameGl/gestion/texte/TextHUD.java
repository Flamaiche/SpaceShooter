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
    private final float scale;
    private final float r, g, b;
    private boolean active = true;

    public TextHUD(TextType type, HorizontalAlignment hAlign, VerticalAlignment vAlign, float scale, float r, float g, float b) {
        this.type = type;
        this.hAlign = hAlign;
        this.vAlign = vAlign;
        this.scale = scale;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public TextType getType() { return type; }
    public HorizontalAlignment getHAlign() { return hAlign; }
    public VerticalAlignment getVAlign() { return vAlign; }
    public float getScale() { return scale; }
    public float getR() { return r; }
    public float getG() { return g; }
    public float getB() { return b; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // Génère le texte à afficher selon le type et GameData
    public String getText(GameData data) {
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
        return "";
    }
}
