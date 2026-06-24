package markershape.camera;

import learngl.LogFile;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Orbital camera for the shape editor.
 * Rotates around a fixed target point using yaw/pitch angles.
 */
public class EditorCamera {
    private final Vector3f target = new Vector3f(0, 0, 0);
    private final Vector3f position = new Vector3f();
    private float yaw, pitch;
    private float radius = 3f;
    private float fov = 60f;
    private float near = 0.1f;
    private float far = 100f;
    private int width = 800, height = 600;
    private float zoomSpeed = 0.5f;
    private float orbitSpeed = 2.0f;

    public void setZoomSpeed(float v) { zoomSpeed = v; }
    public void setOrbitSpeed(float v) { orbitSpeed = v; }
    public float getZoomSpeed() { return zoomSpeed; }
    public float getOrbitSpeed() { return orbitSpeed; }

    /** Constructs an orbital camera with default position and radius. */
    public EditorCamera() {
        updatePosition();
    }

    /**
     * Applies yaw and pitch rotation offsets.
     * Pitch is clamped to [-89, 89] degrees.
     *
     * @param dyaw   the yaw offset in degrees
     * @param dpitch the pitch offset in degrees
     */
    public void rotate(float dyaw, float dpitch) {
        yaw += dyaw * orbitSpeed;
        pitch = Math.max(-89f, Math.min(89f, pitch + dpitch * orbitSpeed));
        updatePosition();
        LogFile.logf("[Camera] rotate: yaw=%.1f pitch=%.1f pos=(%.2f,%.2f,%.2f) radius=%.2f",
            yaw, pitch, position.x, position.y, position.z, radius);
    }

    /**
     * Zooms in or out by adjusting the orbital radius.
     * Radius is clamped to [0.5, 50] units.
     *
     * @param amount the zoom delta (positive = zoom in)
     */
    public void zoom(float amount) {
        radius = Math.max(0.5f, Math.min(50f, radius - amount * zoomSpeed));
        updatePosition();
        LogFile.logf("[Camera] zoom: radius=%.2f pos=(%.2f,%.2f,%.2f)",
            radius, position.x, position.y, position.z);
    }

    private void updatePosition() {
        float rad = (float) Math.toRadians(yaw);
        float rp = (float) Math.toRadians(pitch);
        position.x = target.x + radius * (float) (Math.cos(rp) * Math.sin(rad));
        position.y = target.y + radius * (float) Math.sin(rp);
        position.z = target.z + radius * (float) (Math.cos(rp) * Math.cos(rad));
    }

    /**
     * Returns the view matrix based on the current camera position.
     *
     * @return the view matrix
     */
    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, target, new Vector3f(0, 1, 0));
    }

    /**
     * Returns the perspective projection matrix.
     *
     * @return the projection matrix
     */
    public Matrix4f getProjection() {
        float aspect = (float) width / height;
        return new Matrix4f().perspective((float) Math.toRadians(fov), aspect, near, far);
    }

    /**
     * Updates the viewport size for the projection aspect ratio.
     *
     * @param w the new viewport width
     * @param h the new viewport height
     */
    public void setSize(int w, int h) {
        width = w;
        height = h;
    }

    /**
     * Returns a copy of the camera position.
     *
     * @return the camera position
     */
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    /** Returns a copy of the camera target point. */
    public Vector3f getTarget() {
        return new Vector3f(target);
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public float getRadius() { return radius; }
}
