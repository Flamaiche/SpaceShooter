package learngl.tools;

import org.joml.Vector3f;

public final class OrbitController {
    private final Vector3f target = new Vector3f(0, 0, 0);
    private float theta;
    private float phi;
    private float radius = 1f;

    public void init(Vector3f position, Vector3f newTarget) {
        target.set(newTarget);
        Vector3f rel = new Vector3f(position).sub(target);
        radius = rel.length();
        if (radius < 0.1f) radius = 0.1f;
        rel.div(radius);
        theta = (float) Math.atan2(rel.z, rel.x);
        phi = (float) Math.asin(rel.y);
    }

    public void rotate(float offsetYawDeg, float offsetPitchDeg, Vector3f outPosition) {
        theta += Math.toRadians(offsetYawDeg);
        phi += Math.toRadians(offsetPitchDeg);
        float limit = (float) (Math.PI / 2);
        phi = Math.max(-limit, Math.min(limit, phi));

        float cosPhi = (float) Math.cos(phi);
        float sinPhi = (float) Math.sin(phi);
        float cosTh = (float) Math.cos(theta);
        float sinTh = (float) Math.sin(theta);

        outPosition.x = target.x + radius * cosPhi * cosTh;
        outPosition.y = target.y + radius * sinPhi;
        outPosition.z = target.z + radius * cosPhi * sinTh;
    }

    public Vector3f getTarget() {
        return new Vector3f(target);
    }

    public void reset() {
        target.set(0, 0, 0);
        theta = 0;
        phi = 0;
        radius = 1f;
    }
}
