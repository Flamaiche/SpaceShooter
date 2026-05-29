package learngl.tools.camera;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import learngl.tools.LogFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Camera {
    private Vector3f position;
    private Vector3f startPosition;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private Vector3f worldUp;

    private float yaw;
    private float pitch;
    private float fov;

    private final Quaternionf orientation = new Quaternionf();
    private boolean orbitMode = false;

    private final OrbitController orbitController = new OrbitController();

    private float renderDistance = 100f;
    private float renderSimulation = 150f;

    private final Matrix4f rotationMatrix = new Matrix4f();


    public Camera(Vector3f position) {
        LogFile.init();
        LogFile.log("[Camera] " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " — init");
        this.position = new Vector3f(position);
        this.startPosition = position;
        this.front = new Vector3f(0, 0, -1);
        this.up = new Vector3f(0, 1, 0);
        this.worldUp = new Vector3f(0, 1, 0);
        this.right = new Vector3f();
        this.yaw = -90f;
        this.pitch = 0f;
        this.fov = 60f;
        reconstruireAxes();
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
        this.fov = 60f;
        reconstruireAxes();
        orbitController.init(position, new Vector3f(0, 0, 0));
    }

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
            reconstruireAxes();
        }
        orbitMode = active;
    }

    public boolean isOrbitMode() { return orbitMode; }

    public void rotateRoll(float deltaDeg) {
        if (!orbitMode) {
            rotationMatrix.mul(new Matrix4f().rotationZ((float) Math.toRadians(deltaDeg)));
            extractAxes();
        }
    }

    public Matrix4f getViewMatrix() {
        if (orbitMode) updateAxesToTarget();

        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }

    public Matrix4f getProjection(int width, int height) {
        float aspect = (float) width / height;
        return new Matrix4f().perspective((float) Math.toRadians(fov), aspect, 0.1f, renderDistance);
    }

    public void move(Vector3f offset) {
        if (!orbitMode) position.add(offset);
    }

    public void rotate(float offsetYaw, float offsetPitch) {
        if (!orbitMode) {
            LogFile.logf("--- rotate(offsetYaw=%.1f, offsetPitch=%.1f)", offsetYaw, offsetPitch);
            LogFile.logf("yaw=%.1f pitch=%.1f (before)", yaw, pitch);

            rotationMatrix.mul(new Matrix4f().rotationY((float) Math.toRadians(-offsetYaw)));
            rotationMatrix.mul(new Matrix4f().rotationX((float) Math.toRadians(offsetPitch)));

            extractAxes();

            yaw += offsetYaw;
            pitch += offsetPitch;
            pitch = pitch % 360f;
            if (pitch > 180f) pitch -= 360f;
            if (pitch < -180f) pitch += 360f;
            yaw = ((yaw % 360f) + 360f) % 360f;

            LogFile.logf("yaw=%.1f pitch=%.1f (after)", yaw, pitch);
            LogFile.logf("front=(%.6f,%.6f,%.6f) right=(%.6f,%.6f,%.6f) up=(%.6f,%.6f,%.6f)",
                front.x, front.y, front.z,
                right.x, right.y, right.z,
                up.x, up.y, up.z);
        } else {
            orbitController.rotate(offsetYaw, offsetPitch, position);
            updateAxesToTarget();
        }
    }

    private void reconstruireAxes() {
        LogFile.logf("reconstruireAxes yaw=%.1f pitch=%.1f", yaw, pitch);
        rotationMatrix.identity()
                .rotateY((float) Math.toRadians(-yaw))
                .rotateX((float) Math.toRadians(pitch));
        extractAxes();

        orientation.identity().rotateY((float) Math.toRadians(-yaw)).rotateX((float) Math.toRadians(pitch));
    }

    private void extractAxes() {
        rotationMatrix.getColumn(0, right);
        rotationMatrix.getColumn(1, up);
        rotationMatrix.getColumn(2, front);
        front.negate();
        LogFile.logf("  front=(%.6f,%.6f,%.6f) right=(%.6f,%.6f,%.6f) up=(%.6f,%.6f,%.6f)",
            front.x, front.y, front.z,
            right.x, right.y, right.z,
            up.x, up.y, up.z);
    }

    private void updateAxesToTarget() {
        AxesCalculator.fromTarget(position, orbitController.getTarget(), worldUp, front, right, up);
    }

    public Vector3f getPosition() { return new Vector3f(position); }
    public void setPosition(Vector3f pos) { position.set(pos); }
    public Vector3f getFront() { return new Vector3f(front); }
    public Vector3f getRight() { return new Vector3f(right); }
    public Vector3f getUp() { return new Vector3f(up); }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    public void setYawPitch(float yawDeg, float pitchDeg) {
        yaw = yawDeg;
        pitch = pitchDeg;
        reconstruireAxes();
    }

    public float distanceTo(Vector3f point) { return position.distance(point); }

    public float getRenderDistance() { return renderDistance; }
    public void setRenderDistance(float d) { renderDistance = d; }

    public float getRenderSimulation() { return renderSimulation; }
    public void setRenderSimulation(float s) { renderSimulation = s; }

    public float getFov() { return fov; }
    public void setFov(float fovDeg) { fov = fovDeg; }
}
