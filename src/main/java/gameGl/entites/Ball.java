package gameGl.entites;

import gameGl.utils.PreVerticesTable;
import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Random;

public class Ball extends Entity {
    private Shape corps;
    private Shader shader;
    private Vector3f position = new Vector3f();
    private Vector3f direction = new Vector3f();
    private Vector3f rotation = new Vector3f();
    private Vector3f rotationSpeed = new Vector3f();

    public static float speed = 25f;
    public static float maxDistance = 150f;
    public static float rotationMultiplier = 2f;

    private boolean active = false;
    private Random rand = new Random();
    private boolean modelDirty = true;
    private Vector3f spawnPos;

    public Ball(Shader shader, float baseSize) {
        this.shader = shader;
        corps = new Shape(Shape.autoAddSlotColor(PreVerticesTable.generatePyramid(baseSize)));
        corps.setShader(shader);
        corps.setColor(1f,0f,0f,1f);
    }

    public void activate(Vector3f startPos, Vector3f forwardDir) {
        position.set(startPos);
        spawnPos = new Vector3f(startPos); // mémorise le point de départ
        direction.set(forwardDir).normalize();
        rotation.set(0f,0f,0f);
        rotationSpeed.set(rand.nextFloat()*720-360f, rand.nextFloat()*720-360f, rand.nextFloat()*720-360f);
        active = true;
        modelDirty = true;
    }

    public void deactivate() { active = false; }
    public boolean isActive() { return active; }

    @Override
    public void update(float deltaTime) {
        if (!active) return;

        // Déplacement total
        Vector3f delta = new Vector3f(direction).mul(speed * deltaTime);

        // Découpage en mini-steps
        float maxStep = 0.5f; // ajustable selon la taille des ennemis
        int steps = (int) Math.ceil(delta.length() / maxStep);
        Vector3f step = new Vector3f(delta).div(steps);

        // Avance par mini-steps
        for (int i = 0; i < steps; i++) {
            position.add(step);
        }

        // Rotation
        rotation.x += rotationSpeed.x * deltaTime * rotationMultiplier;
        rotation.y += rotationSpeed.y * deltaTime * rotationMultiplier;
        rotation.z += rotationSpeed.z * deltaTime * rotationMultiplier;

        // Désactivation si trop loin
        if (position.distance(spawnPos) > maxDistance) deactivate();

        modelDirty = true;
        updateModelMatrix();
    }

    private void updateModelMatrix() {
        if (!modelDirty) return;
        modelMatrix.identity()
                .translate(position)
                .rotateX((float)Math.toRadians(rotation.x))
                .rotateY((float)Math.toRadians(rotation.y))
                .rotateZ((float)Math.toRadians(rotation.z));
        modelDirty = false;
    }

    public void render(Matrix4f view, Matrix4f projection) {
        if (!active) return;
        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", modelMatrix);
        corps.render();
        shader.unbind();
    }

    public void cleanup() { corps.cleanup(); }

    public Matrix4f getModelMatrix() {
        Matrix4f m = new Matrix4f();
        m.identity()
                .translate(position)
                .rotateX((float)Math.toRadians(rotation.x))
                .rotateY((float)Math.toRadians(rotation.y))
                .rotateZ((float)Math.toRadians(rotation.z));
        return m;
    }

    public int checkCollision(ArrayList<Ennemis> enemies) {
        if (!active) return 0;
        int score = 0;

        for (Ennemis enemy : enemies) {
            if (!(enemy.getVie() > 0)) continue;

            if (corps.intersectsOptimized(enemy.getCorps(), getModelMatrix(), enemy.getModelMatrix())) {
                deactivate();
                score += enemy.touched();
                break; // stop après la première collision
            }
        }

        return score;
    }

    public Vector3f getPosition() { return position; }

    public static void setMaxDistance(float d) { maxDistance = d; }
    public static void setSpeed(float s) { speed = s; }
    public static void setRotationMultiplier(float r) { rotationMultiplier = r; }
}
