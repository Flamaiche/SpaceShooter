package gamegl.entites;

import gamegl.entites.balls.Balls;
import learngl.tools.camera.Camera;
import learngl.tools.shape.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Joueur extends Entity {
    private int vie;
    private final Vector3f position = new Vector3f(0, 0, 0);

    private float prevYaw;
    private float prevPitch;
    private boolean firstUpdate = true;

    private final Matrix4f rotMatrix = new Matrix4f();

    private static final float MAX_BIAS = 35f;
    private static final float BANK_FACTOR = 0.08f;
    private static final float PITCH_AMP = 2f;

    public Joueur(Shape corps) {
        this.corps = corps;
        this.vie = 3;
    }

    @Override
    public void update(float deltaTime) {}

    public void update(Camera camera, float deltaTime) {
        Vector3f up = camera.getUp();

        if (up.lengthSquared() < 1e-6f) up.set(0, 1, 0);

        float yaw = camera.getYaw();
        float pitch = camera.getPitch();

        if (firstUpdate || yaw != prevYaw || pitch != prevPitch) {
            float yawRate = !firstUpdate ? (yaw - prevYaw) / deltaTime : 0;
            prevYaw = yaw;
            prevPitch = pitch;

            float yawRad = (float) Math.toRadians(yaw + 90);
            float pitchRad = (float) Math.toRadians(pitch * PITCH_AMP);
            pitchRad = (float) Math.max(-Math.PI / 2 + 0.01, Math.min(Math.PI / 2 - 0.01, pitchRad));
            float cp = (float) Math.cos(pitchRad);
            float sp = (float) Math.sin(pitchRad);
            float cy = (float) Math.cos(yawRad);
            float sy = (float) Math.sin(yawRad);

            Vector3f shipFront = new Vector3f(-cy * cp, sp, -sy * cp);

            float bank = (float) Math.toRadians(
                    Math.max(-MAX_BIAS, Math.min(MAX_BIAS, -yawRate * BANK_FACTOR))
            );

            Vector3f shipUp = new Vector3f(up);
            if (Math.abs(bank) > 1e-4f)
                shipUp.rotateAxis(bank, shipFront.x, shipFront.y, shipFront.z);

            rotMatrix.identity();
            new Matrix4f()
                    .lookAt(new Vector3f(0, 0, 0), new Vector3f(shipFront), shipUp)
                    .invert(rotMatrix);

            firstUpdate = false;
        }

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
