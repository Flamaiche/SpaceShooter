package gamegl.gestion.donnees;

/**
 * Contient toutes les données dynamiques du jeu utilisées par le HUD et les systèmes de sauvegarde.
 */
public class GameData {

    private String gameVersion = "";

    private float score = 0f;
    private float lives = 0f;
    private float ballsFired = 0f;
    private float enemiesKilled = 0f;
    private float elapsedTime = 0f;

    private float[] playerPosition = new float[3];
    private float[] playerOrientation = new float[3];

    private float[] ballsActive = new float[2];
    private float[] enemiesActive = new float[2];

    private float distanceTarget = 0f;
    private float fps = 0f;
    private float speed = 0f;

    private float totalScore = 0f;
    private float bestScore = 0f;

    public GameData() {}

    /**
     * Réinitialise toutes les valeurs de jeu à leur état par défaut.
     */
    public void resetVal() {
        this.score = 0f;
        this.lives = 0f;
        this.ballsFired = 0f;
        this.enemiesKilled = 0f;
        this.elapsedTime = 0f;

        this.playerPosition = new float[3];
        this.playerOrientation = new float[3];

        this.ballsActive = new float[2];
        this.enemiesActive = new float[2];

        this.distanceTarget = 0f;
        this.fps = 0f;
    }

    public void setScore(float score) { this.score = score; }
    public float getScore() { return score; }

    public void setLives(float lives) { this.lives = lives; }
    public float getLives() { return lives; }

    public void setBallsFired(float ballsFired) { this.ballsFired = ballsFired; }
    public float getBallsFired() { return ballsFired; }

    public void setEnemiesKilled(float enemiesKilled) { this.enemiesKilled = enemiesKilled; }
    public float getEnemiesKilled() { return enemiesKilled; }

    public void setElapsedTime(float elapsedTime) { this.elapsedTime = elapsedTime; }
    public float getElapsedTime() { return elapsedTime; }

    /**
     * Définit la position du joueur.
     *
     * @param x coordonnée X
     * @param y coordonnée Y
     * @param z coordonnée Z
     */
    public void setPlayerPosition(float x, float y, float z) {
        playerPosition[0] = x;
        playerPosition[1] = y;
        playerPosition[2] = z;
    }
    public float[] getPlayerPosition() { return playerPosition; }

    /**
     * Définit l'orientation du joueur.
     *
     * @param pitch tangage
     * @param yaw   lacet
     * @param roll  roulis
     */
    public void setPlayerOrientation(float pitch, float yaw, float roll) {
        playerOrientation[0] = pitch;
        playerOrientation[1] = yaw;
        playerOrientation[2] = roll;
    }
    public float[] getPlayerOrientation() { return playerOrientation; }

    /**
     * Définit le nombre de balles actives et maximum.
     *
     * @param active balles actives
     * @param max    maximum de balles
     */
    public void setActiveBalls(float active, float max) {
        ballsActive[0] = active;
        ballsActive[1] = max;
    }
    public float[] getActiveBalls() { return ballsActive; }

    /**
     * Définit le nombre d'ennemis actifs et maximum.
     *
     * @param active ennemis actifs
     * @param max    maximum d'ennemis
     */
    public void setActiveEnemies(float active, float max) {
        enemiesActive[0] = active;
        enemiesActive[1] = max;
    }
    public float[] getActiveEnemies() { return enemiesActive; }

    public void setDistanceTarget(float distance) { distanceTarget = distance; }
    public float getDistanceTarget() { return distanceTarget; }

    public void setFPS(float fps) { this.fps = fps; }
    public float getFPS() { return fps; }

    public void setTotalScore(float totalScore) { this.totalScore = totalScore; }
    public  float getTotalScore() { return totalScore; }

    public void setBestScore(float bestScore) { this.bestScore = bestScore; }
    public float getBestScore() { return bestScore; }

    public void setVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }
    public String getVersion() { return gameVersion; }

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
}
