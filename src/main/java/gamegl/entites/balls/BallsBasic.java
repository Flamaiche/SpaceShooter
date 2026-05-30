package gamegl.entites.balls;

import learngl.Shader;

/**
 * A basic ball projectile with default behavior.
 * Extends {@link Balls} without additional modifications.
 */
public class BallsBasic extends Balls{
    /**
     * Constructs a BallsBasic with the given shader and base size.
     *
     * @param shader   the shader used for rendering
     * @param baseSize the base size of the ball's geometry
     */
    public BallsBasic(Shader shader, float baseSize) {
        super(shader, baseSize);
    }
}
