package markershape.shape.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Camera math extracted from a view/projection pair, used to build billboard
 * geometry that keeps a constant on-screen size regardless of depth.
 */
public final class Cam {
    /** Camera axes in world space (unit length for plain rotations). */
    public final Vector3f right = new Vector3f();
    public final Vector3f up = new Vector3f();
    public final Vector3f forward = new Vector3f();

    /** Perspective focal factor (1 / tan(fovY/2)) taken from the projection. */
    public float fy = 1f;
    public float screenH = 720f;
    private final Matrix4f viewCopy = new Matrix4f();
    private final Vector4f scratch = new Vector4f();

    public static Cam extract(Matrix4f view, Matrix4f projection, int screenHeight) {
        Cam c = new Cam();
        if (view != null) {
            c.right.set(view.m00(), view.m10(), view.m20());
            c.up.set(view.m01(), view.m11(), view.m21());
            c.forward.set(-view.m02(), -view.m12(), -view.m22());
            c.viewCopy.set(view);
        }
        if (projection != null) {
            c.fy = projection.m11();
        }
        c.screenH = Math.max(1, screenHeight);
        return c;
    }

    /** View-space depth (>0 in front of the camera). */
    public float viewDepth(float x, float y, float z) {
        scratch.set(x, y, z, 1f);
        viewCopy.transform(scratch);
        return -scratch.z;
    }

    /** On-screen size in pixels -> world half-extent at the given view depth. */
    public float halfSize(float pixels, float depth) {
        if (depth <= 1e-5f || fy <= 1e-5f) return 0f;
        return (pixels * 0.5f) * (2f * depth) / (screenH * fy);
    }

    /**
     * Unit thickness direction for a screen-constant-width line from A to B:
     * perpendicular to the line and to the view direction (stays in the view plane).
     */
    public void thickness(float ax, float ay, float az,
                          float bx, float by, float bz,
                          Vector3f out) {
        float dx = bx - ax, dy = by - ay, dz = bz - az;
        float r0 = dy * forward.z - dz * forward.y;
        float r1 = dz * forward.x - dx * forward.z;
        float r2 = dx * forward.y - dy * forward.x;
        float len = (float) Math.sqrt(r0 * r0 + r1 * r1 + r2 * r2);
        if (len < 1e-6f) {
            out.set(right);
            return;
        }
        out.set(r0 / len, r1 / len, r2 / len);
    }
}