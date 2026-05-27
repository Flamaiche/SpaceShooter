package gamegl.gestion;

import gamegl.entites.balls.Balls;
import gamegl.entites.ennemis.Ennemis;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Manager3D {

    /**
     * Updates all enemies (including despawn/respawn logic) and active balls.
     * Ball collision against enemies is evaluated and the cumulative score is returned.
     *
     * @param deltaTime frame delta in seconds
     * @param playerPos current camera/player position used for enemy despawning
     * @return total score earned this frame from destroyed enemies
     */
    public int updateAll(ArrayList<Ennemis> ennemis, ArrayList<Balls> balls, float deltaTime, Vector3f playerPos) {
        int score = 0;

        for (Ennemis e : ennemis) {
            if (e.shouldDespawn(playerPos)) {
                e.setDeplacement(new float[] {playerPos.x, playerPos.y, playerPos.z});
            }
            e.update(deltaTime);
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
