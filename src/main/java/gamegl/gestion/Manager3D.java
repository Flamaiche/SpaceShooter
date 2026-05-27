package gamegl.gestion;

import gamegl.entites.Joueur;
import gamegl.entites.balls.Balls;
import gamegl.entites.ennemis.Ennemis;
import gamegl.entites.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Manager3D {

    // Update toutes les entités et retourne le score gagné
    public int updateAll(ArrayList<Ennemis> ennemis, ArrayList<Balls> balls, Joueur joueur, float deltaTime, Vector3f playerPos) {
        int score = 0;

        for (Ennemis e : ennemis) {
            if (e.shouldDespawn(playerPos)) {
                e.setDeplacement(new float[] {playerPos.x, playerPos.y, playerPos.z});
            }
            e.update(deltaTime);
        }

        joueur.update(deltaTime);
        Entity collised = joueur.checkCollision(new ArrayList<Entity>(ennemis));
        if (collised != null) {
            joueur.decrementVie();
            if (collised instanceof Ennemis) {
                score += ((Ennemis) collised).touched();
            }
        }

        for (Balls b : balls) {
            if (!b.isActive()) continue;

            b.update(deltaTime);

            score += b.checkCollision(ennemis);
        }

        return score;
    }

    public void renderAll(ArrayList<Ennemis> ennemis, ArrayList<Balls> balls, Matrix4f view, Matrix4f projection) {
        for (Ennemis e : ennemis) e.render(view, projection);
        for (Balls b : balls) b.render(view, projection);
    }

    public void cleanupAll(ArrayList<Ennemis> ennemis, ArrayList<Balls> balls) {
        for (Ennemis e : ennemis) e.cleanup();
        for (Balls b : balls) b.cleanup();
    }
}
