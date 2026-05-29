package gamegl.entites;

import gamegl.entites.balls.Balls;
import learngl.tools.shape.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Joueur extends Entity {
    private int vie;
    private final Vector3f position = new Vector3f(0, 0, 0);

    private float hBias, vBias;

    private final Matrix4f rotMatrix = new Matrix4f();

    private static final float STEP = 0.15f;
    private static final float BIAS_MAX = 10f;
    private static final float BANK_FACTOR = 0.8f;

    public Joueur(Shape corps) {
        this.corps = corps;
        this.vie = 3;
    }

    @Override
    public void update(float deltaTime) {}

    public void update(int[] axes, float deltaTime) {
        float hInput = axes[0];
        float vInput = axes[1];

        if (hInput == 1)
            hBias = Math.min(BIAS_MAX, hBias + STEP);
        else if (hInput == -1)
            hBias = Math.max(-BIAS_MAX, hBias - STEP);
        else if (hBias > 0)
            hBias = Math.max(0, hBias - STEP);
        else if (hBias < 0)
            hBias = Math.min(0, hBias + STEP);

        if (vInput == 1)
            vBias = Math.min(BIAS_MAX, vBias + STEP);
        else if (vInput == -1)
            vBias = Math.max(-BIAS_MAX, vBias - STEP);
        else if (vBias > 0)
            vBias = Math.max(0, vBias - STEP);
        else if (vBias < 0)
            vBias = Math.min(0, vBias + STEP);

        float bankDeg = -hBias * BANK_FACTOR;

        rotMatrix.identity()
                .rotateY((float) Math.toRadians(90))
                .rotateY((float) Math.toRadians(hBias))
                .rotateX((float) Math.toRadians(vBias))
                .rotateZ((float) Math.toRadians(bankDeg));

        modelMatrix.identity()
                .translate(position)
                .mul(rotMatrix);
    }

    @Override
    public void render(Matrix4f view, Matrix4f projection) {}

    @Override
    public void cleanup() {
        corps.cleanup();
    }

    public Entity checkCollision(ArrayList<Entity> entities) {
        for (Entity e : entities) {
            if (!(e instanceof Joueur) && !(e instanceof Balls)) {
                if (corps.intersectsOptimized(e.getBody(), modelMatrix, e.getModelMatrix()))
                    return e;
            }
        }
        return null;
    }

    public void setVie(int v) { vie = v; }
    public int getVie() { return vie; }
    public void decrementVie() { if (vie > 0) vie--; }
    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f pos) { position.set(pos); }
}
