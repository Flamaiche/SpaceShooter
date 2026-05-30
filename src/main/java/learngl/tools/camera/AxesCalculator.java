package learngl.tools.camera;

import org.joml.Vector3f;

/**
 * Utility class for computing orthonormal camera axes (front, right, up)
 * from yaw/pitch angles or from a look-at target direction.
 */
public final class AxesCalculator {

    private static final float EPSILON = 1e-8f;

    private AxesCalculator() {}

    /**
     * Computes the front, right, and up vectors from a position and a target point.
     *
     * @param position the camera or entity position
     * @param target   the point to look at
     * @param worldUp  the world up reference vector
     * @param outFront output vector set to the normalized direction from position to target
     * @param outRight output vector set to the computed right direction
     * @param outUp    output vector set to the computed up direction
     */
    public static void fromTarget(Vector3f position, Vector3f target, Vector3f worldUp,
                                  Vector3f outFront, Vector3f outRight, Vector3f outUp) {
        outFront.set(new Vector3f(target).sub(position)).normalize();
        computeRightAndUp(outFront, worldUp, outRight, outUp);
    }

    private static void computeRightAndUp(Vector3f front, Vector3f worldUp,
                                          Vector3f outRight, Vector3f outUp) {
        outRight.set(new Vector3f(front).cross(worldUp)).normalize();
        if (outRight.lengthSquared() < EPSILON)
            outRight.set(new Vector3f(1, 0, 0).cross(front)).normalize();
        outUp.set(new Vector3f(outRight).cross(front)).normalize();
    }
}
