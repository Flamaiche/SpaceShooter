package gamegl.entites.ennemis;

import gamegl.entites.Entity;
import gamegl.utils.config.ConfigEnnemis;
import gamegl.utils.config.ConfigJeu;
import learngl.Shader;
import learngl.shape.Shape;
import learngl.VertexUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11C.*;

/**
 * Abstract base class for enemy entities.
 * Handles spawning, movement, respawning after death, rendering with highlight outlines,
 * and random mutation of speed, size, and respawn time.
 */
public abstract class Ennemis extends Entity {
    protected static Random rand = new Random();

    protected Shader shader;
    protected Vector3f position;
    protected Vector3f direction;
    protected Vector3f target;
    protected float speed = 2.5f;
    protected boolean highlighted = false;

    protected int vie;
    protected float mutationTimer = 0f;
    protected float respawn_time = -1f;
    protected float deathTime = -1f;
    protected Vector3f bodyColor = new Vector3f(0f, 0f, 0f);

    /**
     * Constructs an enemy with the given shader, player-centered spawn area, shape vertices, and camera.
     *
     * @param shader        the shader used for rendering
     * @param centerPlayer  the player's position {x, y, z} used as spawn origin
     * @param verticesShape the vertex data defining the enemy's shape
     */
    public Ennemis(Shader shader, float[] centerPlayer, float[] verticesShape) {
        corps = new Shape(VertexUtils.autoAddSlotColor(verticesShape));
        corps.setShader(shader);
        corps.setColor(0f,0f,0f);
        this.shader = shader;
        this.vie = ConfigEnnemis.get().enemyMaxVie;
        setDeplacement(centerPlayer);
        updateModelMatrix();
    }

    /**
     * Sets a random spawn position and target direction relative to the player.
     *
     * @param centerPlayer the player's position as {x, y, z}
     */
    public void setDeplacement(float[] centerPlayer) {
        float[] coors = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        position = new Vector3f(coors[0], coors[1], coors[2]);

        float[] targetCoords = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        target = new Vector3f(targetCoords[0], targetCoords[1], targetCoords[2]);

        direction = new Vector3f(target).sub(position).normalize();
        updateModelMatrix();
    }

    /**
     * Generates a random spawn position within a cube around the player, excluding a central exclusion zone.
     *
     * @param playerX the player's X position
     * @param playerY the player's Y position
     * @param playerZ the player's Z position
     * @return an array {x, y, z} with the absolute spawn coordinates
     */
    public float[] generateSpawn(float playerX, float playerY, float playerZ) {
        ConfigEnnemis cfg = ConfigEnnemis.get();
        float x,y,z;
        float spawnSize = cfg.spawnZone.x;
        float exclusionSize = cfg.spawnZone.y;
        do {
            x = rand.nextFloat() * (2*spawnSize) - spawnSize;
            y = rand.nextFloat() * (2*spawnSize) - spawnSize;
            z = rand.nextFloat() * (2*spawnSize) - spawnSize;
        } while (x > -exclusionSize && x < exclusionSize &&
                y > -exclusionSize && y < exclusionSize &&
                z > -exclusionSize && z < exclusionSize);
        return new float[]{playerX + x,playerY + y,playerZ + z};
    }

    /**
     * Checks whether the enemy should despawn based on distance from the camera.
     *
     * @param cameraPos the camera's position
     * @return true if the enemy is beyond despawn distance
     */
    public boolean shouldDespawn(Vector3f cameraPos) {
        return position.distance(cameraPos) > ConfigJeu.get().renderSimulation;
    }

    /**
     * Updates the enemy: triggers mutation periodically, handles death/respawn timing,
     * and updates the model matrix.
     *
     * @param deltaTime time elapsed since the last update
     */
    public void update(float deltaTime) {
        ConfigEnnemis cfg = ConfigEnnemis.get();
        if (vie <= 0) {
            if (deathTime < 0) {
                deathTime = (float) glfwGetTime();
                mutationTimer = 0f;
                respawn_time = rand.nextFloat(cfg.respawnTime.y - cfg.respawnTime.x + 1) + cfg.respawnTime.x;
            } else {
                float currentTime = (float) glfwGetTime();
                if (currentTime - deathTime >= respawn_time) {
                    resetVie();
                    deathTime = -1f;
                    mutationTimer = 0f;
                }
            }
            return;
        }

        mutationTimer += deltaTime;
        if (mutationTimer >= cfg.mutationDeltaTimeInterval) {
            mutationTimer = 0f;
            mutation();
        }

        updateModelMatrix();
    }

    private void updateModelMatrix() {
        modelMatrix.identity().translate(position);
    }

    /**
     * Renders the enemy. If highlighted, draws a red wireframe outline on top.
     *
     * @param view       the view matrix
     * @param projection the projection matrix
     */
    public void render(Matrix4f view, Matrix4f projection) {
        if (vie <= 0) return;
        if (!corps.isVisible(projection, view, modelMatrix)) {
            return;
        }
        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", modelMatrix);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        corps.render();

        if (highlighted) {
            ConfigEnnemis cfg = ConfigEnnemis.get();
            float outlineScale = cfg.enemyOutline.x;
            float outlineWidth = cfg.enemyOutline.y;
            Matrix4f outlineModel = new Matrix4f(modelMatrix).scale(outlineScale);
            shader.setUniformMat4f("model", outlineModel);

            glEnable(GL_DEPTH_TEST);
            glDepthMask(false);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glLineWidth(outlineWidth);
            corps.setColor(cfg.enemyHighlightColor.x, cfg.enemyHighlightColor.y, cfg.enemyHighlightColor.z);
            corps.render();
            corps.setColor(bodyColor.x, bodyColor.y, bodyColor.z);

            glDepthMask(true);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            shader.setUniformMat4f("model", modelMatrix);
        }

        shader.unbind();
    }

    /**
     * Called when the enemy is hit. Decrements health and returns score if killed.
     *
     * @return the score value if the enemy dies, or 0 otherwise
     */
    public int touched() {
        decrementVie();
        if (getVie() <= 0) {
            float d = ConfigJeu.get().renderSimulation * ConfigEnnemis.get().respawnDistanceMultiplier;
            setDeplacement(new float[]{d, d, d});
            return ConfigEnnemis.get().enemyScore;
        }
        return 0;
    }

    /** Releases the shape resources. */
    public void cleanup() { corps.cleanup(); }

    public int getVie() { return vie; }
    public void decrementVie() { if (vie>0) vie--; }
    public void resetVie() { vie = ConfigEnnemis.get().enemyMaxVie; }
    public void setHighlighted(boolean h) { highlighted = h; }

    /**
     * Sets the movement speed for this enemy.
     *
     * @param s the speed value
     */
    public void setSpeed(float s) { speed = s; }
    public void setBodyColor(float r, float g, float b) {
        bodyColor.set(r, g, b);
        corps.setColor(r, g, b);
    }

    /**
     * Returns whether this enemy is currently highlighted.
     *
     * @return true if highlighted
     */
    public boolean isHighlighted() {
        return highlighted;
    }

    /**
     * Returns the current position of this enemy.
     *
     * @return the position vector
     */
    public Vector3f getPosition() { return position;}

    /**
     * Applies random mutations to speed, size, and respawn time with defined probabilities.
     */
    public void mutation() {
        ConfigEnnemis cfg = ConfigEnnemis.get();

        if (testMutation(cfg.mutationVitesseProb))
            speed *= 1f + rand.nextFloat() * rand.nextFloat() * cfg.mutationSpeedRange.x;
        else if (!testMutation(100 - cfg.mutationVitesseProb))
            speed *= 1f - rand.nextFloat() * rand.nextFloat() * cfg.mutationSpeedRange.y;

        if (testMutation(cfg.mutationTailleProb))
            corps.setScale(1f + rand.nextFloat() * cfg.mutationSizeRange.x);
        else if (!testMutation(100 - cfg.mutationTailleProb))
            corps.setScale(1f - rand.nextFloat() * cfg.mutationSizeRange.y);

        if (testMutation(cfg.mutationSleepProb))
            respawn_time *= 1f + rand.nextFloat() * cfg.mutationSleepRange.x;
        else if (!testMutation(100 - cfg.mutationSleepProb))
            respawn_time *= Math.max(cfg.mutationSleepClampMin, 1f - rand.nextFloat() * cfg.mutationSleepRange.y);
    }

    private boolean testMutation(float chance) {
        return rand.nextFloat() * 100 < chance;
    }

}
