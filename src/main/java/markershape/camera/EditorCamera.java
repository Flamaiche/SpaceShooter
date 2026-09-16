package markershape.camera;

import learngl.LogFile;
import learngl.camera.AxesCalculator;
import learngl.camera.OrbitController;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Orbital camera for the shape editor, based on the engine's
 * {@code learngl.camera.OrbitController} (spherical orbit with a movable target).
 * Uses {@code AxesCalculator} to rebuild the view axes for panning.
 */
public class EditorCamera {
    private final Vector3f target = new Vector3f(0, 0, 0);
    private final Vector3f position = new Vector3f();
    private final Vector3f front = new Vector3f(0, 0, -1);
    private final Vector3f right = new Vector3f(1, 0, 0);
    private final Vector3f up = new Vector3f(0, 1, 0);
    private static final Vector3f WORLD_UP = new Vector3f(0, 1, 0);

    private final OrbitController orbit = new OrbitController();

    private float radius = 3f;
    private float fov = 60f;
    private float near = 0.1f;
    private float far = 100f;
    private int width = 800, height = 600;
    private float zoomSpeed = 0.5f;
    private float orbitSpeed = 2.0f;

    private float frontYaw;
    private float frontPitch;

    /** Sets the zoom speed factor (multiplier applied to zoom deltas). */
    public void setZoomSpeed(float v) { zoomSpeed = v; }
    /** Sets the orbit rotation speed factor. */
    public void setOrbitSpeed(float v) { orbitSpeed = v; }
    /** Returns the zoom speed factor. */
    public float getZoomSpeed() { return zoomSpeed; }
    /** Returns the orbit rotation speed factor. */
    public float getOrbitSpeed() { return orbitSpeed; }

    /** Constructs an orbital camera with default position and radius. */
    public EditorCamera() {
        pose(0f, 0f, 3f);
    }

    /**
     * Positions the camera on the sphere around the target using explicit yaw,
     * pitch (degrees) and radius, then syncs the engine orbit controller.
     */
    public void pose(float yawDeg, float pitchDeg, float newRadius) {
        radius = Math.max(0.5f, Math.min(50f, newRadius));
        float rp = (float) Math.toRadians(pitchDeg);
        float ry = (float) Math.toRadians(yawDeg);
        float cpp = (float) Math.cos(rp);
        position.set(target).add(
            cpp * (float) Math.sin(ry) * radius,
            (float) Math.sin(rp) * radius,
            cpp * (float) Math.cos(ry) * radius);
        orbit.init(position, target);
        updateAxes();
    }

    /**
     * Applies yaw and pitch rotation offsets through the engine's orbit controller.
     * Pitch is clamped by the controller to the configured orbit limits.
     *
     * @param dyaw   the yaw offset in degrees
     * @param dpitch the pitch offset in degrees
     */
    public void rotate(float dyaw, float dpitch) {
        orbit.rotate(dyaw * orbitSpeed, dpitch * orbitSpeed, position);
        updateAxes();
        LogFile.logf("[Camera] rotate: yaw=%.1f pitch=%.1f pos=(%.2f,%.2f,%.2f) radius=%.2f",
            getYaw(), getPitch(), position.x, position.y, position.z, radius);
    }

    /**
     * Zooms in or out by adjusting the orbital radius.
     * Radius is clamped to [0.5, 50] units.
     *
     * @param amount the zoom delta (positive = zoom in)
     */
    public void zoom(float amount) {
        float newRadius = Math.max(0.5f, Math.min(50f, radius - amount * zoomSpeed));
        pose(getYaw(), getPitch(), newRadius);
        LogFile.logf("[Camera] zoom: radius=%.2f pos=(%.2f,%.2f,%.2f)",
            radius, position.x, position.y, position.z);
    }

    /**
     * Zooms toward the point under the cursor: the world point that was under
     * the mouse before zooming stays under the mouse afterwards.
     * Dolly toward the pivot point along the view ray, then re-anchor the
     * target and the camera together by the screen-space offset of the pivot.
     *
     * @param amount the zoom delta (positive = zoom in)
     * @param mx     cursor x in pixels
     * @param my     cursor y in pixels
     */
    public void zoomToward(float amount, float mx, float my) {
        float newRadius = Math.max(0.5f, Math.min(50f, radius - amount * zoomSpeed));
        if (Math.abs(newRadius - radius) < 1e-4f) return;

        Vector3f pivot = unprojectAtPoint(mx, my);
        Vector3f viewDir = new Vector3f(target).sub(position).normalize();

        // Move the camera along its view direction so the distance to the
        // target becomes the new radius (positive delta = zoom in).
        float delta = radius - newRadius;
        position.x += viewDir.x * delta;
        position.y += viewDir.y * delta;
        position.z += viewDir.z * delta;
        radius = newRadius;

        // Re-anchor: translate target and camera together so the pivot point
        // stays exactly under the cursor. Measure at the pivot's depth.
        float depth = ndcDepth(pivot);
        float[] pix = projectPixel(pivot);
        Vector3f atNow = unprojectAtDepth(pix[0], pix[1], depth);
        Vector3f atCursor = unprojectAtDepth(mx, my, depth);
        target.x += atCursor.x - atNow.x;
        target.y += atCursor.y - atNow.y;
        target.z += atCursor.z - atNow.z;
        position.x += atCursor.x - atNow.x;
        position.y += atCursor.y - atNow.y;
        position.z += atCursor.z - atNow.z;

        orbit.init(position, target);
        updateAxes();
        LogFile.logf("[Camera] zoomToward: radius=%.2f target=(%.2f,%.2f,%.2f)",
            radius, target.x, target.y, target.z);
    }

    /**
     * Pans the camera target inside the plane of view (perpendicular to the
     * look direction). Drags the scene along with the cursor.
     *
     * @param dx the horizontal cursor delta in pixels
     * @param dy the vertical cursor delta in pixels
     */
    public void pan(float dx, float dy) {
        float worldPerPixel = 2f * radius * (float) Math.tan(Math.toRadians(fov * 0.5f)) / height;
        updateAxes();
        float hAmount = -dx * worldPerPixel;
        float vAmount = dy * worldPerPixel;
        target.x += right.x * hAmount + up.x * vAmount;
        target.y += right.y * hAmount + up.y * vAmount;
        target.z += right.z * hAmount + up.z * vAmount;
        orbit.init(position, target);
        LogFile.logf("[Camera] pan: target=(%.3f,%.3f,%.3f)", target.x, target.y, target.z);
    }

    /** Moves the camera target to a new point (keeps the current orientation). */
    public void setTarget(Vector3f newTarget) {
        target.set(newTarget);
        orbit.init(position, target);
        updateAxes();
    }

    /**
     * Re-anchors the orbit around a pivot point (e.g. the world point under the
     * cursor at the start of an orbit gesture). Keeps the current orientation
     * (yaw/pitch) and adapts the radius to the distance to the new pivot.
     *
     * @param pivot the new orbit center
     */
    public void setOrbitPivot(Vector3f pivot) {
        target.set(pivot);
        radius = Math.max(0.5f, Math.min(50f, new Vector3f(position).sub(pivot).length()));
        orbit.init(position, target);
        updateAxes();
    }

    /**
     * Frames a bounding box: sets the target to the center and adapts the radius
     * so the whole box fits on screen, keeping the current orientation.
     *
     * @param center the bounding box center
     * @param size   the bounding box largest dimension
     */
    public void frame(Vector3f center, float size) {
        target.set(center);
        pose(getYaw(), getPitch(), fitRadius(size));
    }

    /**
     * Resets the view in front of the shape's fixed "front" direction.
     *
     * @param center the bounding box center
     * @param size   the bounding box largest dimension
     */
    public void resetToFront(Vector3f center, float size) {
        target.set(center);
        pose(frontYaw, frontPitch, fitRadius(size));
    }

    /** Restores the fixed "front" angles (default camera framing direction). */
    public void setFront(float yawDeg, float pitchDeg) {
        frontYaw = yawDeg;
        frontPitch = pitchDeg;
    }

    /** Returns the fixed "front" yaw in degrees. */
    public float getFrontYaw() { return frontYaw; }

    /** Returns the fixed "front" pitch in degrees. */
    public float getFrontPitch() { return frontPitch; }

    /**
     * Returns the normalized direction of the fixed default camera vector:
     * the direction the camera looks at when placed at the "front" angles.
     *
     * @return a new normalized Vector3f
     */
    public Vector3f getFrontDirection() {
        float ry = (float) Math.toRadians(frontYaw);
        float rp = (float) Math.toRadians(frontPitch);
        float opx = (float) Math.cos(rp) * (float) Math.sin(ry);
        float opy = (float) Math.sin(rp);
        float opz = (float) Math.cos(rp) * (float) Math.cos(ry);
        Vector3f dir = new Vector3f(-opx, -opy, -opz);
        if (dir.lengthSquared() < 1e-8f) return new Vector3f(0, 0, -1);
        return dir.normalize();
    }

    /** Computes a radius that fits the given bounding size, clamped to [0.5, 50] units. */
    private float fitRadius(float size) {
        float fit = (size <= 0f) ? 3f : size * 1.1f;
        return Math.max(0.5f, Math.min(50f, fit));
    }

    /** Recomputes the front/right/up axes from the current position and target. */
    private void updateAxes() {
        AxesCalculator.fromTarget(position, target, WORLD_UP, front, right, up);
    }

    /**
     * Returns the view matrix based on the current camera position.
     *
     * @return the view matrix
     */
    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, target, WORLD_UP);
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

    /** Returns a copy of the camera front (look) direction. */
    public Vector3f getFront() { return new Vector3f(front); }

    /** Returns a copy of the camera right direction. */
    public Vector3f getRight() { return new Vector3f(right); }

    /** Returns a copy of the camera up direction. */
    public Vector3f getUp() { return new Vector3f(up); }

    /** Returns the current yaw in degrees, derived from the orbit position. */
    public float getYaw() {
        Vector3f rel = new Vector3f(position).sub(target);
        if (rel.lengthSquared() < 1e-8f) return 0f;
        return (float) Math.toDegrees(Math.atan2(rel.x, rel.z));
    }

    /** Returns the current pitch in degrees, derived from the orbit position. */
    public float getPitch() {
        Vector3f rel = new Vector3f(position).sub(target);
        float r = rel.length();
        if (r < 1e-8f) return 0f;
        return (float) Math.toDegrees(Math.asin(rel.y / r));
    }

    /** Returns the current orbit radius. */
    public float getRadius() { return radius; }

    /** Returns the world point under the cursor at the depth of the target. */
    private Vector3f unprojectAtPoint(float mx, float my) {
        Matrix4f pv = new Matrix4f(getProjection()).mul(getViewMatrix());
        Vector4f t = new Vector4f(target.x, target.y, target.z, 1f).mul(pv);
        float ndcZ = 1f;
        if (t.w != 0f) ndcZ = t.z / t.w;
        return unprojectAtDepth(mx, my, ndcZ);
    }

    /** Returns the NDC depth of a world point under the current view. */
    private float ndcDepth(Vector3f world) {
        Matrix4f pv = new Matrix4f(getProjection()).mul(getViewMatrix());
        Vector4f t = new Vector4f(world.x, world.y, world.z, 1f).mul(pv);
        return (t.w != 0f) ? t.z / t.w : 0f;
    }

    /** Projects a world point back to screen pixels (origin top-left). */
    private float[] projectPixel(Vector3f world) {
        Matrix4f pv = new Matrix4f(getProjection()).mul(getViewMatrix());
        Vector4f t = new Vector4f(world.x, world.y, world.z, 1f).mul(pv);
        if (t.w != 0f) t.div(t.w);
        float sx = (t.x * 0.5f + 0.5f) * width;
        float sy = (1f - (t.y * 0.5f + 0.5f)) * height;
        return new float[]{sx, sy};
    }

    /** Unprojects a screen point at the given NDC depth back to world space. */
    private Vector3f unprojectAtDepth(float mx, float my, float ndcZ) {
        Matrix4f pv = new Matrix4f(getProjection()).mul(getViewMatrix());
        Matrix4f inv = new Matrix4f(pv).invert();
        float ndcX = (2f * mx) / width - 1f;
        float ndcY = 1f - (2f * my) / height;
        Vector4f p = new Vector4f(ndcX, ndcY, ndcZ, 1f).mul(inv);
        if (p.w != 0f) p.div(p.w);
        return new Vector3f(p.x, p.y, p.z);
    }
}