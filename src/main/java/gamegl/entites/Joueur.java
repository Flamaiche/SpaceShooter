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
    private int lastHInput, lastVInput;

    private final Matrix4f rotMatrix = new Matrix4f();

    private static final float H_RATE = 20f;
    private static final float V_RATE = 10f;
    private static final float BIAS_MAX = 10f;
    private static final float BANK_FACTOR = 0.8f;

    public Joueur(Shape corps) {
        this.corps = corps;
        this.vie = 3;
    }

    @Override
    public void update(float deltaTime) {}

    public void update(int[] axes, float deltaTime, float camYaw, float camPitch) {
        int hInput = axes[0];
        int vInput = axes[1];

        if (hInput != lastHInput && hInput != 0 && lastHInput != 0)
            hBias = 0;
        lastHInput = hInput;

        if (hInput == 1)
            hBias = Math.min(BIAS_MAX, hBias + H_RATE * deltaTime);
        else if (hInput == -1)
            hBias = Math.max(-BIAS_MAX, hBias - H_RATE * deltaTime);
        else if (hBias > 0)
            hBias = Math.max(0, hBias - H_RATE * deltaTime);
        else if (hBias < 0)
            hBias = Math.min(0, hBias + H_RATE * deltaTime);

        if (vInput != lastVInput && vInput != 0 && lastVInput != 0)
            vBias = 0;
        lastVInput = vInput;

        if (vInput == 1)
            vBias = Math.min(BIAS_MAX, vBias + V_RATE * deltaTime);
        else if (vInput == -1)
            vBias = Math.max(-BIAS_MAX, vBias - V_RATE * deltaTime);
        else if (vBias > 0)
            vBias = Math.max(0, vBias - V_RATE * deltaTime);
        else if (vBias < 0)
            vBias = Math.min(0, vBias + V_RATE * deltaTime);

        float bankDeg = hBias * BANK_FACTOR;

        rotMatrix.identity()
                .rotateY((float) Math.toRadians(-camYaw))
                .rotateX((float) Math.toRadians(camPitch))
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
