package learnGL.tools;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private Vector3f worldUp;

    private float yaw;
    private float pitch;
    private float roll = 0f; // degrees
    private float fov = 60f;

    private boolean orbitMode = false;
    private boolean rollEnabled = false;

    private Vector3f target = new Vector3f(0, 0, 0);
    private float orbitTheta = 0f;
    private float orbitPhi = 0f;
    private float orbitRadius = 1f;

    private float renderDistance = 100f;
    private float renderSimulation = 150f;

    private static final float EPSILON = 1e-4f;

    public Camera(Vector3f position) {
        this.position = new Vector3f(position);
        this.front = new Vector3f(0, 0, -1);
        this.up = new Vector3f(0, 1, 0);
        this.worldUp = new Vector3f(0, 1, 0);
        this.right = new Vector3f();
        this.yaw = -90f;
        this.pitch = 0f;
        updateAxes();
        initOrbitFromCurrentState();
    }

    // ---------------- Orbit Mode ----------------
    public void setOrbitMode(boolean active) {
        if (active == orbitMode) return;
        if (active) {
            initOrbitFromCurrentState();
            updateAxesToTarget();
        } else {
            Vector3f dir = new Vector3f(target).sub(position).normalize();
            pitch = (float) Math.toDegrees(Math.asin(dir.y));
            yaw = (float) Math.toDegrees(Math.atan2(dir.z, dir.x));
            updateAxes();
        }
        orbitMode = active;
    }

    public boolean isOrbitMode() { return orbitMode; }

    // ---------------- Roll ----------------
    public void setRollEnabled(boolean active) {
        rollEnabled = active;
        roll = 0f;
        updateAxes();
    }

    public boolean isRollEnabled() { return rollEnabled; }

    public void addRoll(float delta) {
        if (rollEnabled) {
            roll = (roll + delta) % 360f;
        }
    }

    public void setRoll(float angleDeg) {
        if (rollEnabled) roll = angleDeg % 360f;
    }

    public float getRoll() { return roll; }

    // ---------------- View / Projection ----------------
    public Matrix4f getViewMatrix() {
        if (orbitMode) updateAxesToTarget();

        Vector3f rolledUp = new Vector3f(up);
        if (rollEnabled && Math.abs(roll) > EPSILON)
            rolledUp.rotateAxis((float)Math.toRadians(roll), front.x, front.y, front.z);

        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), rolledUp);
    }

    public Matrix4f getProjection(int width, int height) {
        float aspect = (float) width / height;
        return new Matrix4f().perspective((float) Math.toRadians(fov), aspect, 0.1f, renderDistance);
    }

    // ---------------- Movement / Rotation ----------------
    public void move(Vector3f offset) {
        if (!orbitMode) position.add(offset);
    }

    public void rotate(float offsetYaw, float offsetPitch) {
        if (!orbitMode) {
            yaw += offsetYaw;
            pitch += offsetPitch;
            pitch = Math.max(-89.9f, Math.min(89.9f, pitch));
            updateAxes();
        } else {
            orbitTheta += Math.toRadians(offsetYaw);
            orbitPhi += Math.toRadians(offsetPitch);
            float limit = (float) (Math.PI / 2 - 0.001);
            orbitPhi = Math.max(-limit, Math.min(limit, orbitPhi));

            float cosPhi = (float)Math.cos(orbitPhi);
            float sinPhi = (float)Math.sin(orbitPhi);
            float cosTh = (float)Math.cos(orbitTheta);
            float sinTh = (float)Math.sin(orbitTheta);

            position.x = target.x + orbitRadius * cosPhi * cosTh;
            position.y = target.y + orbitRadius * sinPhi;
            position.z = target.z + orbitRadius * cosPhi * sinTh;

            updateAxesToTarget();
        }
    }

    // ---------------- Axes Calculation ----------------
    private void updateAxes() {
        // Pré-calcul des sinus/cosinus pour optimisation
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double cosPitch = Math.cos(pitchRad);
        double sinPitch = Math.sin(pitchRad);
        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);

        front.set((float)(cosYaw * cosPitch), (float) sinPitch, (float)(sinYaw * cosPitch)).normalize();
        right.set(new Vector3f(front).cross(worldUp).normalize());
        if (right.lengthSquared() < 1e-8f)
            right.set(new Vector3f(1, 0, 0).cross(front).normalize());
        up.set(new Vector3f(right).cross(front).normalize());
    }

    private void updateAxesToTarget() {
        front.set(new Vector3f(target).sub(position).normalize());
        right.set(new Vector3f(front).cross(worldUp).normalize());
        if (right.lengthSquared() < 1e-8f)
            right.set(new Vector3f(1, 0, 0).cross(front).normalize());
        up.set(new Vector3f(right).cross(front).normalize());
    }

    private void initOrbitFromCurrentState() {
        Vector3f rel = new Vector3f(position).sub(target);
        orbitRadius = rel.length();
        if (orbitRadius < 0.1f) orbitRadius = 0.1f; // seuil plus réaliste
        rel.div(orbitRadius);
        orbitTheta = (float) Math.atan2(rel.z, rel.x);
        orbitPhi = (float) Math.asin(rel.y);
    }

    // ---------------- Getters / Setters ----------------
    public Vector3f getPosition() { return new Vector3f(position); }
    public Vector3f getFront() { return new Vector3f(front); }
    public Vector3f getRight() { return new Vector3f(right); }
    public Vector3f getUp() { return new Vector3f(up); }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public void setYawPitch(float yawDeg, float pitchDeg) {
        yaw = yawDeg;
        pitch = pitchDeg;
        updateAxes();
    }

    public float distanceTo(Vector3f point) { return position.distance(point); }

    public float getRenderDistance() { return renderDistance; }
    public void setRenderDistance(float d) { renderDistance = d; }

    public float getRenderSimulation() { return renderSimulation; }
    public void setRenderSimulation(float s) { renderSimulation = s; }

    public float getFov() { return fov; }
    public void setFov(float fovDeg) { fov = fovDeg; }
}
