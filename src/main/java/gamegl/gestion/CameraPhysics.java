package gamegl.gestion;

import learngl.tools.Camera;
import org.joml.Vector3f;

public class CameraPhysics {
    private Vector3f velocity = new Vector3f(0, 0, 0);
    private Vector3f moveDirection = new Vector3f(0, 0, 0);
    private final float maxSpeed = 17.5f;
    private final float tempsMaxSpeed = 4.0f;
    private final float acceleration = maxSpeed / tempsMaxSpeed;
    private final float deceleration = acceleration / 2;

    public void addFront(float amount, Camera camera) {
        moveDirection.add(new Vector3f(camera.getFront()).mul(amount));
    }

    public void addRight(float amount, Camera camera) {
        moveDirection.add(new Vector3f(camera.getRight()).mul(amount));
    }

    public void addUp(float amount, Camera camera) {
        moveDirection.add(new Vector3f(camera.getUp()).mul(amount));
    }

    public void update(Camera camera, float deltaTime) {
        if (camera.isOrbitMode()) return;

        if (moveDirection.lengthSquared() > 0) {
            moveDirection.normalize();

            Vector3f desiredVelocity = new Vector3f(moveDirection).mul(maxSpeed);
            Vector3f deltaV = new Vector3f(desiredVelocity).sub(velocity);

            float maxChange = acceleration * deltaTime;
            if (deltaV.length() > maxChange)
                deltaV.normalize().mul(maxChange);
            velocity.add(deltaV);
        } else {
            float speed = velocity.length();
            if (speed > 0) {
                float decel = deceleration * deltaTime;
                if (decel > speed) velocity.zero();
                else velocity.mul(1 - decel / speed);
            }
        }

        if (velocity.lengthSquared() > 0)
            camera.move(new Vector3f(velocity).mul(deltaTime));

        moveDirection.zero();
    }

    public Vector3f getVelocity() {
        return velocity;
    }
}
