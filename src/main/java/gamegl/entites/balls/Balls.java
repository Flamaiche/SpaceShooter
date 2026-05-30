package gamegl.entites.balls;

import gamegl.entites.ennemis.Ennemis;
import gamegl.entites.Entity;
import gamegl.utils.ConfigJeu;
import gamegl.utils.ConfigVaisseau;
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
        corps.setColor(ConfigJeu.get().ballColor.x, ConfigJeu.get().ballColor.y, ConfigJeu.get().ballColor.z);
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
        float rMax = ConfigVaisseau.get().ballRotationSpeedMax;
        rotationSpeed.set(rand.nextFloat()*rMax*2-rMax, rand.nextFloat()*rMax*2-rMax, rand.nextFloat()*rMax*2-rMax);
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

        Vector3f delta = new Vector3f(direction).mul(ConfigVaisseau.get().ballSpeed * deltaTime);

        float maxStep = ConfigVaisseau.get().ballCollisionStep;
        int steps = (int) Math.ceil(delta.length() / maxStep);
        Vector3f step = new Vector3f(delta).div(steps);

        for (int i = 0; i < steps; i++) {
            position.add(step);
        }

        float rotMult = ConfigVaisseau.get().ballRotationMultiplier;
        rotation.x += rotationSpeed.x * deltaTime * rotMult;
        rotation.y += rotationSpeed.y * deltaTime * rotMult;
        rotation.z += rotationSpeed.z * deltaTime * rotMult;

        if (position.distance(spawnPos) > ConfigVaisseau.get().ballDistanceMax) deactivate();

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

}
