package gamegl.gestion.donnees;

import gamegl.SpaceShooter;
import gamegl.utils.GetDonnee;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Patron de sauvegarde contenant les données d'une partie à sérialiser en JSON.
 */
public class SaveClassPatron {
    String date;
    String time;
    String version;
    float score;
    float ballsFired;
    float enemiesKilled;
    float elapsedTime;

    /**
     * Construit une sauvegarde à partir des données actuelles du jeu.
     *
     * @param gameData données du jeu
     */
    public SaveClassPatron(GameData gameData) {
        date = LocalDate.now().toString();
        time = LocalTime.now().toString();
        version = SpaceShooter.getGameVersion();
        score = gameData.getScore();
        ballsFired = gameData.getBallsFired();
        enemiesKilled = gameData.getEnemiesKilled();
        elapsedTime = gameData.getElapsedTime();
    }

    /**
     * Sauvegarde les données dans un fichier JSON.
     */
    public void saveDonnees() {
        ArrayList<SaveClassPatron> array = new ArrayList<>();
        array.add(this);
        GetDonnee.writeJson(SpaceShooter.filenameSaveScore, array);
    }

    /**
     * Retourne le score sauvegardé.
     *
     * @return le score
     */
    public float getScore() {
        return score;
    }
}
