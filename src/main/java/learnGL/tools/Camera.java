package learnGL.tools;

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

    private boolean rollEnabled = false;

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
    }

    public void restValues() {
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
    }

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
        position.add(offset);
    }

    public void rotate(float offsetYaw, float offsetPitch) {
        yaw += offsetYaw;
        pitch += offsetPitch;
        pitch = Math.max(-89.9f, Math.min(89.9f, pitch));
        yaw = ((yaw % 360) + 360) % 360;
        updateAxes();
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
    public Vector3f distanceVectorTo(Vector3f point) {
        return new Vector3f(point).sub(position);
    }

    public float getRenderDistance() { return renderDistance; }
    public void setRenderDistance(float d) { renderDistance = d; }

    public float getRenderSimulation() { return renderSimulation; }
    public void setRenderSimulation(float s) { renderSimulation = s; }

    public float getFov() { return fov; }
    public void setFov(float fovDeg) { fov = fovDeg; }
}
