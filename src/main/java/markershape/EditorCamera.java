package markershape;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class EditorCamera {
    private final Vector3f target = new Vector3f(0, 0, 0);
    private final Vector3f position = new Vector3f();
    private float yaw, pitch;
    private float radius = 3f;
    private float fov = 60f;
    private float near = 0.1f;
    private float far = 100f;
    private int width = 800, height = 600;

    public EditorCamera() {
        updatePosition();
    }

    public void rotate(float dyaw, float dpitch) {
        yaw += dyaw;
        pitch = Math.max(-89f, Math.min(89f, pitch + dpitch));
        updatePosition();
    }

    public void zoom(float amount) {
        radius = Math.max(0.5f, Math.min(50f, radius - amount));
        updatePosition();
    }

    private void updatePosition() {
        float rad = (float) Math.toRadians(yaw);
        float rp = (float) Math.toRadians(pitch);
        position.x = target.x + radius * (float) (Math.cos(rp) * Math.sin(rad));
        position.y = target.y + radius * (float) Math.sin(rp);
        position.z = target.z + radius * (float) (Math.cos(rp) * Math.cos(rad));
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, target, new Vector3f(0, 1, 0));
    }

    public Matrix4f getProjection() {
        float aspect = (float) width / height;
        return new Matrix4f().perspective((float) Math.toRadians(fov), aspect, near, far);
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public Vector3f getTarget() {
        return new Vector3f(target);
    }
}
