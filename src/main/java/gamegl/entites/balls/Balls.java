package gamegl.entites.balls;

import gamegl.entites.ennemis.Ennemis;
import gamegl.entites.Entity;
import learngl.tools.shape.PreVerticesTable;
import learngl.tools.Shader;
import learngl.tools.shape.Shape;
import learngl.tools.VertexUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Random;

/**
 * Abstract base class for projectile balls fired by the player.
 * Handles movement, rotation, collision detection, and lifecycle (activate/deactivate).
 */
public abstract class Balls extends Entity {
    protected Shape corps;
    protected Shader shader;
    protected Vector3f position = new Vector3f();
    protected Vector3f direction = new Vector3f();
    protected Vector3f rotation = new Vector3f();
    protected Vector3f rotationSpeed = new Vector3f();

    protected static float speed = 25f;
    protected static float maxDistance = 150f;
    protected static float rotationMultiplier = 2f;

    protected boolean active = false;
    protected Random rand = new Random();
    protected boolean modelDirty = true;
    protected Vector3f spawnPos;

    /**
     * Constructs a Balls projectile with the given shader and base size.
     *
     * @param shader   the shader used for rendering
     * @param baseSize the base size of the ball's geometry
     */
    public Balls(Shader shader, float baseSize) {
        this.shader = shader;
        corps = new Shape(VertexUtils.autoAddSlotColor(PreVerticesTable.generatePyramid(baseSize)));
        corps.setShader(shader);
        corps.setColor(1f,0f,0f);
    }

    /**
     * Activates the projectile at the given position, flying in the specified direction.
     *
     * @param startPos   the starting position
     * @param forwardDir the direction of travel
     */
    public void activate(Vector3f startPos, Vector3f forwardDir) {
        position.set(startPos);
        spawnPos = new Vector3f(startPos);
        direction.set(forwardDir).normalize();
        rotation.set(0f,0f,0f);
        rotationSpeed.set(rand.nextFloat()*720-360f, rand.nextFloat()*720-360f, rand.nextFloat()*720-360f);
        active = true;
        modelDirty = true;
    }

    /** Deactivates the projectile, making it inactive. */
    public void deactivate() { active = false; }

    /**
     * Returns whether this projectile is currently active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() { return active; }

    @Override
    public void update(float deltaTime) {
        if (!active) return;

        Vector3f delta = new Vector3f(direction).mul(speed * deltaTime);

        float maxStep = 0.5f;
        int steps = (int) Math.ceil(delta.length() / maxStep);
        Vector3f step = new Vector3f(delta).div(steps);

        for (int i = 0; i < steps; i++) {
            position.add(step);
        }

        rotation.x += rotationSpeed.x * deltaTime * rotationMultiplier;
        rotation.y += rotationSpeed.y * deltaTime * rotationMultiplier;
        rotation.z += rotationSpeed.z * deltaTime * rotationMultiplier;

        if (position.distance(spawnPos) > maxDistance) deactivate();

        modelDirty = true;
        updateModelMatrix();
    }

    protected void updateModelMatrix() {
        if (!modelDirty) return;
        modelMatrix.identity()
                .translate(position)
                .rotateX((float)Math.toRadians(rotation.x))
                .rotateY((float)Math.toRadians(rotation.y))
                .rotateZ((float)Math.toRadians(rotation.z));
        modelDirty = false;
    }

    /**
     * Renders the projectile if active.
     *
     * @param view       the view matrix
     * @param projection the projection matrix
     */
    public void render(Matrix4f view, Matrix4f projection) {
        if (!active) return;
        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", modelMatrix);
        corps.render();
        shader.unbind();
    }

    /** Releases the shape resources. */
    public void cleanup() { corps.cleanup(); }

    /**
     * Returns the model matrix for this projectile.
     *
     * @return the model matrix
     */
    public Matrix4f getModelMatrix() {
        Matrix4f m = new Matrix4f();
        m.identity()
                .translate(position)
                .rotateX((float)Math.toRadians(rotation.x))
                .rotateY((float)Math.toRadians(rotation.y))
                .rotateZ((float)Math.toRadians(rotation.z));
        return m;
    }

    /**
     * Checks collision against a list of enemies. Deactivates on first hit.
     *
     * @param enemies the list of enemies to check against
     * @return the score gained from the hit, or 0 if no collision
     */
    public int checkCollision(ArrayList<Ennemis> enemies) {
        if (!active) return 0;
        int score = 0;

        for (Ennemis enemy : enemies) {
            if (!(enemy.getVie() > 0)) continue;

            if (corps.intersectsOptimized(enemy.getBody(), getModelMatrix(), enemy.getModelMatrix())) {
                deactivate();
                score += enemy.touched();
                break;
            }
        }

        return score;
    }

    /**
     * Returns the current position of the projectile.
     *
     * @return the position vector
     */
    public Vector3f getPosition() { return position; }

    /**
     * Sets the maximum travel distance for all projectile instances.
     *
     * @param d the maximum distance
     */
    public static void setMaxDistance(float d) { maxDistance = d; }

    /**
     * Sets the speed for all projectile instances.
     *
     * @param s the speed value
     */
    public static void setSpeed(float s) { speed = s; }

    /**
     * Sets the rotation multiplier for all projectile instances.
     *
     * @param r the rotation multiplier
     */
    public static void setRotationMultiplier(float r) { rotationMultiplier = r; }
}
