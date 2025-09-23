package gameGl.gestion;

import gameGl.entites.Crosshair;
import gameGl.entites.Ennemis;
import gameGl.entites.Entity2D;
import org.joml.Matrix4f;
import java.util.ArrayList;

/**
 * Manager pour tous les objets 2D fixes à l'écran.
 */
public class Manager2D {

    public static void updateAll(ArrayList<? extends Entity2D> entities, int width, int height, ArrayList<Ennemis> ennemis) {
        for (Entity2D e : entities) {
            e.update(width, height);
            if (e instanceof Crosshair) {
                ((Crosshair) e).updateHighlightedEnemy(ennemis); // logique spécifique crosshair
            }
        }
    }

    public static void renderAll(ArrayList<? extends Entity2D> entities, Matrix4f orthoProjection) {
        for (Entity2D e : entities) {
            e.render(orthoProjection);
        }
    }

    public static void cleanupAll(ArrayList<? extends Entity2D> entities) {
        for (Entity2D e : entities) {
            e.cleanup();
        }
    }
}
