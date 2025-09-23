package gameGl.gestion;

import gameGl.entites.Joueur;
import gameGl.entites.Ball;
import gameGl.entites.Ennemis;
import gameGl.entites.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Manager3D {

    private Manager3D() {}

    // Update toutes les entités et retourne le score gagné
    public static int updateAll(ArrayList<Ennemis> ennemis, ArrayList<Ball> balls, Joueur joueur, float deltaTime, Vector3f playerPos) {
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
            System.out.println("Collision Joueur-Ennemi !");
            joueur.decrementVie();
            if (collised instanceof Ennemis) {
                score += ((Ennemis) collised).touched();
            }
        }

        for (Ball b : balls) {
            if (!b.isActive()) continue;

            b.update(deltaTime);

            score += b.checkCollision(ennemis);
        }

        return score;
    }

    public static void renderAll(ArrayList<Ennemis> ennemis, ArrayList<Ball> balls, Matrix4f view, Matrix4f projection) {
        for (Ennemis e : ennemis) e.render(view, projection);
        for (Ball b : balls) b.render(view, projection);
    }

    public static void cleanupAll(ArrayList<Ennemis> ennemis, ArrayList<Ball> balls) {
        for (Ennemis e : ennemis) e.cleanup();
        for (Ball b : balls) b.cleanup();
    }
}
