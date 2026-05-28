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
    private int frameCount;

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
        float yaw = camera.getYaw();
        float pitch = camera.getPitch();

        if (firstUpdate) {
            float yawRad = (float) Math.toRadians(yaw + 90);
            float targetPitch = pitch * PITCH_AMP;
            float clampedPitch = Math.max(-89.99f, Math.min(89.99f, targetPitch));
            float pitchRad = (float) Math.toRadians(clampedPitch);
            float cp = (float) Math.cos(pitchRad);
            float sp = (float) Math.sin(pitchRad);
            float cy = (float) Math.cos(yawRad);
            float sy = (float) Math.sin(yawRad);

            Vector3f shipFront = new Vector3f(-cy * cp, sp, -sy * cp);

            Vector3f worldUp = new Vector3f(0, 1, 0);
            Vector3f right = new Vector3f(shipFront).cross(worldUp);
            if (right.lengthSquared() < 1e-6f)
                right.set(1, 0, 0);
            right.normalize();
            Vector3f shipUp = new Vector3f(right).cross(shipFront).normalize();

            rotMatrix.identity();
            new Matrix4f()
                    .lookAt(new Vector3f(0, 0, 0), new Vector3f(shipFront), shipUp)
                    .invert(rotMatrix);

            prevYaw = yaw;
            prevPitch = pitch;
            firstUpdate = false;
            frameCount = 0;

        } else if (yaw != prevYaw || pitch != prevPitch) {
            float dyaw = yaw - prevYaw;
            float dpitch = pitch - prevPitch;
            prevYaw = yaw;
            prevPitch = pitch;

            Vector3f localRight = new Vector3f(rotMatrix.m00, rotMatrix.m10, rotMatrix.m20);
            rotMatrix.rotate((float) Math.toRadians(dpitch * PITCH_AMP), localRight);

            Matrix4f yawRot = new Matrix4f().identity()
                    .rotate((float) Math.toRadians(-dyaw), 0, 1, 0);
            yawRot.mul(rotMatrix, rotMatrix);

            float yawRate = dyaw / deltaTime;
            float bankDeg = Math.max(-MAX_BIAS, Math.min(MAX_BIAS, -yawRate * BANK_FACTOR));
            if (Math.abs(bankDeg) > 1e-4f) {
                Vector3f localFront = new Vector3f(-rotMatrix.m02, -rotMatrix.m12, -rotMatrix.m22);
                rotMatrix.rotate((float) Math.toRadians(bankDeg), localFront);
            }

            frameCount++;
            if (frameCount > 300) {
                rotMatrix.normalize3x3();
                frameCount = 0;
            }
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
