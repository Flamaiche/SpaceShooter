package gameGl.gestion.donnees;

import gameGl.SpaceShooter;
import gameGl.utils.GetDonnee;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class SaveClassPatron {
    String date;
    String time;
    String version;
    float score;
    float ballsFired;
    float enemiesKilled;
    float elapsedTime;

    public SaveClassPatron(GameData gameData) {
        date = LocalDate.now().toString();
        time = LocalTime.now().toString();
        version = SpaceShooter.getGameVersion();
        score = gameData.getScore();
        ballsFired = gameData.getBallsFired();
        enemiesKilled = gameData.getEnemiesKilled();
        elapsedTime = gameData.getElapsedTime();
    }

    public void saveDonnees() {
        ArrayList<SaveClassPatron> array = new ArrayList<>();
        array.add(this);
        GetDonnee.writeJson("SauvegardeScore", array);
    }
}
