package gameGl.entites;

import org.joml.Matrix4f;

/**
 * Classe abstraite pour tous les objets 2D fixes à l'écran.
 */
public abstract class Entity2D {
    /**
     * Met à jour l'objet 2D (logique interne)
     */
    public abstract void update(int width, int height);

    /**
     * Rendu de l'objet 2D
     * @param orthoProjection projection orthographique 2D
     */
    public abstract void render(Matrix4f orthoProjection);

    /**
     * Libération des ressources
     */
    public abstract void cleanup();
}
