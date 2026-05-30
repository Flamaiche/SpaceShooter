package learngl.tools.camera;

import gamegl.utils.ConfigVaisseau;
import org.joml.Vector3f;

/**
 * Controls a camera or object that orbits around a target point using
 * spherical coordinates (theta, phi, radius). Supports rotation via yaw/pitch
 * offsets and reset to default state.
 */
public final class OrbitController {
    private final Vector3f target = new Vector3f(0, 0, 0);
    private float theta;
    private float phi;
    private float radius = 1f;

    /**
     * Initializes the orbit controller from a position and target, computing
     * the initial spherical coordinates. Clamps the radius to a minimum of 0.1.
     *
     * @param position the initial position
     * @param newTarget the orbit target point
     */
    public void init(Vector3f position, Vector3f newTarget) {
        target.set(newTarget);
        Vector3f rel = new Vector3f(position).sub(target);
        radius = rel.length();
        if (radius < ConfigVaisseau.get().orbitLimits.x) radius = ConfigVaisseau.get().orbitLimits.x;
        rel.div(radius);
        theta = (float) Math.atan2(rel.z, rel.x);
        phi = (float) Math.asin(rel.y);
    }

    /**
     * Applies yaw and pitch offset rotations and writes the resulting orbital
     * position into outPosition. The pitch is clamped to [-90, 90] degrees.
     *
     * @param offsetYawDeg   the yaw offset in degrees
     * @param offsetPitchDeg the pitch offset in degrees
     * @param outPosition    output vector receiving the new position
     */
    public void rotate(float offsetYawDeg, float offsetPitchDeg, Vector3f outPosition) {
        theta += (float) Math.toRadians(offsetYawDeg);
        phi += (float) Math.toRadians(offsetPitchDeg);
        float limit = (float) Math.toRadians(ConfigVaisseau.get().orbitLimits.y);
        phi = Math.clamp(phi, -limit, limit);

        float cosPhi = (float) Math.cos(phi);
        float sinPhi = (float) Math.sin(phi);
        float cosTh = (float) Math.cos(theta);
        float sinTh = (float) Math.sin(theta);

        outPosition.x = target.x + radius * cosPhi * cosTh;
        outPosition.y = target.y + radius * sinPhi;
        outPosition.z = target.z + radius * cosPhi * sinTh;
    }

    /**
     * Returns a copy of the current orbit target.
     *
     * @return a new Vector3f representing the target
     */
    public Vector3f getTarget() {
        return new Vector3f(target);
    }

}
