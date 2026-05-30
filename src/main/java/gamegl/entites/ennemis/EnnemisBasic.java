package gamegl.entites.ennemis;

import learngl.tools.Shader;

/**
 * A basic enemy type with default behavior.
 * Extends {@link Ennemis} without additional modifications.
 */
public class EnnemisBasic extends Ennemis {
    /**
     * Constructs an EnnemisBasic with the given shader, player position, shape vertices, and camera.
     *
     * @param shader        the shader used for rendering
     * @param centerPlayer  the player's current position as {x, y, z}
     * @param verticesShape the vertex data defining the enemy's shape
     */
    public EnnemisBasic(Shader shader, float[] centerPlayer, float[] verticesShape) {
        super(shader, centerPlayer, verticesShape);
    }
}
