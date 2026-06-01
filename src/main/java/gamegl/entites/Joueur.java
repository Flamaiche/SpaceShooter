package gamegl.entites;

import gamegl.entites.balls.Balls;
import gamegl.utils.config.ConfigVaisseau;
import learngl.shape.Shape;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Joueur extends Entity {
    private int vie;
    private final Vector3f position = new Vector3f(0, 0, 0);

    private float hBias, vBias;
    private int lastHInput, lastVInput;

    private final Matrix4f rotMatrix = new Matrix4f();
    private final Quaternionf smoothRot = new Quaternionf().identity();
    private final Quaternionf rawRot = new Quaternionf();
    private final Vector3f zero = new Vector3f(0, 0, 0);

    public Joueur(Shape corps) {
        this.corps = corps;
        this.vie = ConfigVaisseau.get().initialLives;
    }

    @Override
    public void update(float deltaTime) {}

    public void update(int[] axes, float deltaTime, Vector3f camFront, Vector3f camUp) {
        int hInput = axes[0];
        int vInput = axes[1];

        ConfigVaisseau cfg = ConfigVaisseau.get();

        float hRate = cfg.rotationRate.x;
        float vRate = cfg.rotationRate.y;

        if (hInput != lastHInput && hInput != 0 && lastHInput != 0)
            hBias = 0;
        lastHInput = hInput;

        if (hInput == 1)
            hBias = Math.min(cfg.biasMax, hBias + hRate * deltaTime);
        else if (hInput == -1)
            hBias = Math.max(-cfg.biasMax, hBias - hRate * deltaTime);
        else if (hBias > 0)
            hBias = Math.max(0, hBias - hRate * deltaTime);
        else if (hBias < 0)
            hBias = Math.min(0, hBias + hRate * deltaTime);

        if (vInput != lastVInput && vInput != 0 && lastVInput != 0)
            vBias = 0;
        lastVInput = vInput;

        if (vInput == 1)
            vBias = Math.min(cfg.biasMax, vBias + vRate * deltaTime);
        else if (vInput == -1)
            vBias = Math.max(-cfg.biasMax, vBias - vRate * deltaTime);
        else if (vBias > 0)
            vBias = Math.max(0, vBias - vRate * deltaTime);
        else if (vBias < 0)
            vBias = Math.min(0, vBias + vRate * deltaTime);

        float bankDeg = hBias * cfg.bankFactor;

        rotMatrix.identity()
                .lookAt(zero, camFront, camUp)
                .invert()
                .rotateY((float) Math.toRadians(hBias))
                .rotateX((float) Math.toRadians(vBias))
                .rotateZ((float) Math.toRadians(bankDeg));

        rawRot.setFromNormalized(rotMatrix);
        smoothRot.slerp(rawRot, cfg.slerpFactor);
        smoothRot.get(rotMatrix);

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

    public int getVie() { return vie; }
    public void decrementVie() { if (vie > 0) vie--; }
    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f pos) { position.set(pos); }
}
