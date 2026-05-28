package gamegl.entites.ennemis;

import gamegl.entites.Entity;
import learngl.tools.camera.Camera;
import learngl.tools.Shader;
import learngl.tools.shape.Shape;
import learngl.tools.VertexUtils;
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
    protected float spawnSize = 10f;
    protected float exclusionSize = 5f;

    protected Shape corps;
    protected Shader shader;
    protected Vector3f position;
    protected Vector3f direction;
    protected Vector3f target;
    protected float speed = 2.5f;
    protected static float despawnDistance = 150f;
    protected boolean highlighted = false;

    protected final int MAX_VIE = 1;
    protected int vie = MAX_VIE;
    protected int score = 10;
    protected final float RESPAWN_TIME_MIN = 1f;
    protected final float RESPAWN_TIME_MAX = 9f;
    protected float respawn_time = -1f;
    protected float deathTime = -1f;

    protected final int moduloMutationDeltaTime = 6;
    protected Vector3f bodyColor = new Vector3f(0f, 0f, 0f);

    /**
     * Constructs an enemy with the given shader, player-centered spawn area, shape vertices, and camera.
     *
     * @param shader        the shader used for rendering
     * @param centerPlayer  the player's position {x, y, z} used as spawn origin
     * @param verticesShape the vertex data defining the enemy's shape
     * @param camera        the camera (unused in constructor but kept for subclasses)
     */
    public Ennemis(Shader shader, float[] centerPlayer, float[] verticesShape, Camera camera) {
        corps = new Shape(VertexUtils.autoAddSlotColor(verticesShape));
        corps.setShader(shader);
        corps.setColor(0f,0f,0f);
        this.shader = shader;
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
        float x,y,z;
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
        return position.distance(cameraPos) > despawnDistance;
    }

    /**
     * Updates the enemy: triggers mutation periodically, handles death/respawn timing,
     * and updates the model matrix.
     *
     * @param deltaTime time elapsed since the last update
     */
    public void update(float deltaTime) {
        if (deltaTime%moduloMutationDeltaTime == 0) mutation();
        if (vie <= 0) {
            if (deathTime < 0) {
                deathTime = (float) glfwGetTime();
                respawn_time = rand.nextFloat(RESPAWN_TIME_MAX - RESPAWN_TIME_MIN + 1) + RESPAWN_TIME_MIN;
            } else {
                float currentTime = (float) glfwGetTime();
                if (currentTime - deathTime >= respawn_time) {
                    resetVie();
                    deathTime = -1f;
                }
            }
            return;
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
        corps.setColor(bodyColor.x, bodyColor.y, bodyColor.z);
        corps.render();

        if (highlighted) {
            Matrix4f outlineModel = new Matrix4f(modelMatrix).scale(1.05f);
            shader.setUniformMat4f("model", outlineModel);

            glEnable(GL_DEPTH_TEST);
            glDepthMask(false);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glLineWidth(2.5f);
            corps.setColor(1f,0f,0f);
            corps.render();

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
            setDeplacement(new float[]{getDespawnDistance()*2, getDespawnDistance()*2, getDespawnDistance()*2});
            return getScore();
        }
        return 0;
    }

    /** Releases the shape resources. */
    public void cleanup() { corps.cleanup(); }

    public Shape getBody() { return corps; }
    public int getVie() { return vie; }
    public void decrementVie() { if (vie>0) vie--; }
    public void resetVie() { vie = MAX_VIE; }
    public int getScore() { return score; }
    public float getDespawnDistance() { return despawnDistance; }
    public void setHighlighted(boolean h) { highlighted = h; }

    /**
     * Sets the despawn distance for all enemy instances.
     *
     * @param d the despawn distance
     */
    public static void setDespawnDistance(float d) { despawnDistance = d; }

    /**
     * Sets the movement speed for this enemy.
     *
     * @param s the speed value
     */
    public void setSpeed(float s) { speed = s; }
    public void setBodyColor(float r, float g, float b) { bodyColor.set(r, g, b); }

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
        float MUTATIONVITESSE = 0.02f;
        float MUTATIONTAILLE = 0.02f;
        float MUTATIONSLEEP = 0.03f;
        float MUTATIONSHAPE = 0.01f;

        if (testMutation(MUTATIONVITESSE))
            speed *= 1f + rand.nextFloat() * rand.nextFloat();
        else if (!testMutation(100 - MUTATIONVITESSE))
            speed *= 1f - rand.nextFloat() * rand.nextFloat();

        if (testMutation(MUTATIONTAILLE))
            corps.setScale((1f + rand.nextFloat() / 6f));
        else if (!testMutation(100 - MUTATIONTAILLE))
            corps.setScale((1f - rand.nextFloat() / 6f));

        if (testMutation(MUTATIONSLEEP))
            respawn_time *= 1f + rand.nextFloat();
        else if (!testMutation(100 - MUTATIONSLEEP))
            respawn_time *= Math.min(0.00001f, 1f - rand.nextFloat());

        if (testMutation(MUTATIONSHAPE)) {
        }
    }

    private boolean testMutation(float chance) {
        return rand.nextFloat() * 100 < chance;
    }

    /**
     * Returns the maximum health value for this enemy type.
     *
     * @return the maximum health
     */
    public int getMAX_VIE() {
        return MAX_VIE;
    }
}
