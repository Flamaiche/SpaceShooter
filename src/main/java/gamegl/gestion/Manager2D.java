package gamegl.gestion;

import gamegl.entites.Crosshair;
import gamegl.entites.ennemis.Ennemis;
import gamegl.entites.Entity2D;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

/**
 * Manager pour tous les objets 2D fixes à l'écran.
 */
public class Manager2D {

    /**
     * Met à jour toutes les entités 2D.
     *
     * @param entities liste des entités à mettre à jour
     * @param width    largeur de l'écran
     * @param height   hauteur de l'écran
     * @param ennemis  liste des ennemis
     * @param velocity vélocité du joueur
     */
    public void updateAll(ArrayList<? extends Entity2D> entities, int width, int height, ArrayList<Ennemis> ennemis, Vector3f velocity) {
        for (Entity2D e : entities) {
            e.update(width, height);
            if (e instanceof Crosshair) {
                ((Crosshair) e).setPlayerSpeed(velocity);
                ((Crosshair) e).updateHighlightedEnemy(ennemis);
            }
        }
    }

    /**
     * Affiche toutes les entités 2D.
     *
     * @param entities         liste des entités à afficher
     * @param orthoProjection  matrice de projection orthographique
     */
    public void renderAll(ArrayList<? extends Entity2D> entities, Matrix4f orthoProjection) {
        for (Entity2D e : entities) {
            e.render(orthoProjection);
        }
    }

    /**
     * Nettoie les ressources de toutes les entités 2D.
     *
     * @param entities liste des entités à nettoyer
     */
    public void cleanupAll(ArrayList<? extends Entity2D> entities) {
        for (Entity2D e : entities) {
            e.cleanup();
        }
    }
}
