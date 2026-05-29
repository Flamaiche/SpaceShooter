package learngl.tools.camera;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Camera {
    private Vector3f position;
    private Vector3f startPosition;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private Vector3f worldUp;

    private float yaw;
    private float pitch;
    private float roll;
    private float fov;

    private final Quaternionf orientation = new Quaternionf();
    private boolean orbitMode = false;
    private boolean rollEnabled = false;

    private final OrbitController orbitController = new OrbitController();

    private float renderDistance = 100f;
    private float renderSimulation = 150f;

    private static final float EPSILON = 1e-4f;

    public Camera(Vector3f position) {
        System.out.println("[Camera] " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " — init");
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
        this.roll = 0f;
        this.fov = 60f;
        reconstruireAxes();
        orbitController.init(position, new Vector3f(0, 0, 0));
    }

    /**
     * Activates or deactivates orbit mode. When entering orbit, the camera
     * attaches to the orbit controller around the current target. When exiting,
     * the camera's yaw/pitch are derived from the direction toward the target.
     */
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

    /**
     * Enables or disables roll. Enabling resets the current roll angle to zero.
     */
    public void setRollEnabled(boolean active) {
        rollEnabled = active;
        roll = 0f;
        reconstruireAxes();
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

    /**
     * Computes the view matrix using a look-at transformation from the camera's
     * current position, looking in the front direction. In orbit mode the axes
     * are recalculated to face the orbit target first. If roll is enabled and
     * non-zero, the up vector is rotated around the front axis.
     */
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

    public void move(Vector3f offset) {
        if (!orbitMode) position.add(offset);
    }

    /**
     * Rotates the camera by the given yaw/pitch offsets. In orbit mode the
     * rotation is delegated to the orbit controller. In free-look mode the
     * yaw is applied around the world up axis and the pitch around the
     * camera's local right axis after yaw, eliminating the ellipse artifact.
     */
    public void rotate(float offsetYaw, float offsetPitch) {
        if (!orbitMode) {
            yaw += offsetYaw;
            pitch += offsetPitch;
            reconstruireAxes();
            System.out.printf(Locale.US, "[Camera] rotate(yaw=%+.1f pitch=%+.1f) → yaw=%.1f pitch=%.1f | front=(%.3f,%.3f,%.3f) right=(%.3f,%.3f,%.3f) up=(%.3f,%.3f,%.3f)%n",
                offsetYaw, offsetPitch, yaw, pitch,
                front.x, front.y, front.z,
                right.x, right.y, right.z,
                up.x, up.y, up.z);
        } else {
            orbitController.rotate(offsetYaw, offsetPitch, position);
            updateAxesToTarget();
        }
    }

    /**
     * Recalculates front/right/up using spherical coordinates.
     * When pitch is between 90° and 270° (cosine is negative), front
     * naturally points behind. The camera axes are flipped so that
     * right and up remain continuous through the zenith/nadir —
     * the camera loops smoothly without a sudden horizontal roll.
     */
    private void reconstruireAxes() {
        float yawRad  = (float) Math.toRadians(yaw + 90);
        float pitchRad = (float) Math.toRadians(pitch);
        float cp = (float) Math.cos(pitchRad);
        float sp = (float) Math.sin(pitchRad);
        float cy = (float) Math.cos(yawRad);
        float sy = (float) Math.sin(yawRad);

        front.set(-cy * cp, sp, -sy * cp);

        // When looking straight up/down use a yaw-dependent fallback
        // so right/up remain continuous with surrounding frames.
        Vector3f refUp;
        if (Math.abs(sp) > 0.9999f) {
            refUp = new Vector3f(sp > 0 ? cy : -cy, 0, sp > 0 ? sy : -sy);
        } else {
            refUp = worldUp;
        }
        right.set(front).cross(refUp, right).normalize();
        up.set(right).cross(front, up);

        // When cosine is negative (pitch 90°–270°), front's horizontal
        // components invert and would flip right/up. Negate them so
        // the camera axes keep rotating in the same direction.
        float pitchMod = ((pitch % 360f) + 360f) % 360f;
        if (pitchMod > 90 && pitchMod < 270 && Math.abs(sp) <= 0.9999f) {
            right.negate();
            up.negate();
        }

        orientation.identity().rotateY(-yawRad).rotateX(pitchRad);
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
