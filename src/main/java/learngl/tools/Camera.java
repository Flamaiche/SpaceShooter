package learngl.tools;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f startPosition;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private Vector3f worldUp;

    private float yaw;
    private float pitch;
    private float roll; // degrees
    private float fov;

    private boolean orbitMode = false;
    private boolean rollEnabled = false;

    private final OrbitController orbitController = new OrbitController();

    private float renderDistance = 100f;
    private float renderSimulation = 150f;

    private static final float EPSILON = 1e-4f;

    public Camera(Vector3f position) {
        this.position = new Vector3f(position);
        this.startPosition = position;
        this.front = new Vector3f(0, 0, -1);
        this.up = new Vector3f(0, 1, 0);
        this.worldUp = new Vector3f(0, 1, 0);
        this.right = new Vector3f();
        this.yaw = -90f;
        this.pitch = 0f;
        this.roll = 0f;
        this.fov = 60f;
        updateAxes();
        orbitController.init(position, new Vector3f(0, 0, 0));
    }

    public void resetValues() {
        this.position = new Vector3f(startPosition);
        this.front = new Vector3f(0, 0, -1);
        this.up = new Vector3f(0, 1, 0);
        this.worldUp = new Vector3f(0, 1, 0);
        this.right = new Vector3f();
        this.yaw = -90f;
        this.pitch = 0f;
        this.roll = 0f;
        this.fov = 60f;
        updateAxes();
        orbitController.init(position, new Vector3f(0, 0, 0));
    }

    // ---------------- Orbit Mode ----------------
    public void setOrbitMode(boolean active) {
        if (active == orbitMode) return;
        if (active) {
            orbitController.init(position, orbitController.getTarget());
            updateAxesToTarget();
        } else {
            Vector3f dir = new Vector3f(orbitController.getTarget()).sub(position);
            if (dir.lengthSquared() > 1e-8f) {
                dir.normalize();
                pitch = (float) Math.toDegrees(Math.asin(dir.y));
                yaw = (float) Math.toDegrees(Math.atan2(dir.z, dir.x));
                yaw = ((yaw % 360) + 360) % 360;
            }
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
            pitch = Math.max(-90f, Math.min(90f, pitch));
            yaw = ((yaw % 360) + 360) % 360;
            updateAxes();
        } else {
            orbitController.rotate(offsetYaw, offsetPitch, position);
            updateAxesToTarget();
        }
    }

    // ---------------- Axes Calculation ----------------
    private void updateAxes() {
        AxesCalculator.fromYawPitch(yaw, pitch, worldUp, front, right, up);
    }

    private void updateAxesToTarget() {
        AxesCalculator.fromTarget(position, orbitController.getTarget(), worldUp, front, right, up);
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
