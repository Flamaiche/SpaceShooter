package gameGl.gestion;

public class GameData {

    private static GameData instance;

    // --- HUD joueur ---
    private float score = 0f;
    private float lives = 0f;
    private float ballsFired = 0f;
    private float enemiesKilled = 0f;
    private float elapsedTime = 0f;

    // --- Position et orientation ---
    private float[] playerPosition = new float[3];   // x, y, z
    private float[] playerOrientation = new float[3]; // pitch, yaw, roll

    // --- Balls / enemies actifs ---
    private float[] ballsActive = new float[2];     // active, max
    private float[] enemiesActive = new float[2];   // active, max

    // --- Debug ---
    private float distanceTarget = 0f;
    private float fps = 0f;

    private GameData() {}

    public static GameData getInstance() {
        if (instance == null) instance = new GameData();
        return instance;
    }

    public void resetVal() {
        this.score = 0f;
        this.lives = 0f;
        this.ballsFired = 0f;
        this.enemiesKilled = 0f;
        this.elapsedTime = 0f;

        // --- Position et orientation ---
        this.playerPosition = new float[3];   // x, y, z
        this.playerOrientation = new float[3]; // pitch, yaw, roll

        // --- Balls / enemies actifs ---
        this.ballsActive = new float[2];     // active, max
        this.enemiesActive = new float[2];   // active, max

        // --- Debug ---
        this.distanceTarget = 0f;
        this.fps = 0f;
    }

    // --- Setters / getters HUD joueur ---
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

    // --- Position et orientation ---
    public void setPlayerPosition(float x, float y, float z) {
        playerPosition[0] = x;
        playerPosition[1] = y;
        playerPosition[2] = z;
    }
    public float[] getPlayerPosition() { return playerPosition; }

    public void setPlayerOrientation(float pitch, float yaw, float roll) {
        playerOrientation[0] = pitch;
        playerOrientation[1] = yaw;
        playerOrientation[2] = roll;
    }
    public float[] getPlayerOrientation() { return playerOrientation; }

    // --- Balls / enemies ---
    public void setActiveBalls(float active, float max) {
        ballsActive[0] = active;
        ballsActive[1] = max;
    }
    public float[] getActiveBalls() { return ballsActive; }

    public void setActiveEnemies(float active, float max) {
        enemiesActive[0] = active;
        enemiesActive[1] = max;
    }
    public float[] getActiveEnemies() { return enemiesActive; }

    // --- Debug ---
    public void setDistanceTarget(float distance) { distanceTarget = distance; }
    public float getDistanceTarget() { return distanceTarget; }

    public void setFPS(float fps) { this.fps = fps; }
    public float getFPS() { return fps; }
}
