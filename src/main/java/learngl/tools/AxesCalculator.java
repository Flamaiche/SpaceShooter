package learngl.tools;

import org.joml.Vector3f;

public final class AxesCalculator {

    private static final float EPSILON = 1e-8f;

    private AxesCalculator() {}

    public static void fromYawPitch(float yawDeg, float pitchDeg, Vector3f worldUp,
                                    Vector3f outFront, Vector3f outRight, Vector3f outUp) {
        double yawRad = Math.toRadians(yawDeg);
        double pitchRad = Math.toRadians(pitchDeg);
        double cosPitch = Math.cos(pitchRad);
        double sinPitch = Math.sin(pitchRad);
        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);

        outFront.set((float) (cosYaw * cosPitch), (float) sinPitch, (float) (sinYaw * cosPitch)).normalize();
        computeRightAndUp(outFront, worldUp, outRight, outUp);
    }

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
