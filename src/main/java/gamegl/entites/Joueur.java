package gamegl.entites;

import gamegl.entites.balls.Balls;
import learngl.tools.shape.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Joueur extends Entity {
    private int vie;
    private final Vector3f position = new Vector3f(0, 0, 0);

    private int frameCount;
    private float hBias, vBias;

    private final Matrix4f rotMatrix = new Matrix4f();

    private static final float H_SPEED = 120f;
    private static final float V_SPEED = 60f;
    private static final float BANK_FACTOR = 0.8f;
    private static final float BANK_MAX = 35f;

    public Joueur(Shape corps) {
        this.corps = corps;
        this.vie = 3;
        rotMatrix.rotateY((float) Math.toRadians(90));
    }

    @Override
    public void update(float deltaTime) {}

    public void update(int[] axes, float deltaTime) {
        float hInput = axes[0];
        float vInput = axes[1];

        if (hInput == 1)
            hBias = H_SPEED;
        else if (hInput == -1)
            hBias = -H_SPEED;
        else
            hBias = 0;

        if (vInput == 1)
            vBias = V_SPEED;
        else if (vInput == -1)
            vBias = -V_SPEED;
        else
            vBias = 0;

        if (Math.abs(hBias) > 0.1f)
            rotMatrix.rotate((float) Math.toRadians(hBias * deltaTime), 0, 1, 0);

        if (Math.abs(vBias) > 0.1f)
            rotMatrix.rotate((float) Math.toRadians(vBias * deltaTime), 1, 0, 0);

        float bankDeg = Math.max(-BANK_MAX, Math.min(BANK_MAX, -hBias * BANK_FACTOR));
        if (Math.abs(bankDeg) > 0.1f)
            rotMatrix.rotate((float) Math.toRadians(bankDeg), 0, 0, -1);

        modelMatrix.identity()
                .translate(position)
                .mul(rotMatrix);

        frameCount++;
        if (frameCount > 300) {
            rotMatrix.normalize3x3();
            frameCount = 0;
        }
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
