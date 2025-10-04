package gameGl.state;

import gameGl.SpaceShooter;
import gameGl.gestion.donnees.Sauvegardable;
import gameGl.gestion.donnees.SaveClassPatron;
import gameGl.utils.GetDonnee;
import learnGL.tools.commandes.Commande;

import java.util.List;

public class GameStateManager {
    private GameState currentState;
    private Commande commande;
    private int width;
    private int height;
    private GameState playing;
    private float bestScore;

    public enum GameStateEnum {
        MAIN,
        PLAY,
        NEWPLAY,
        PAUSE
    }

    public GameStateManager(Commande commande, int width, int height) {
        this.commande = commande;
        this.width = width;
        this.height = height;
        playing = null;

        bestScore = 0f;
        List<Object> saveObject = GetDonnee.readJson(SpaceShooter.filenameSaveScore);
        if (saveObject != null) {
            for (Object obj : saveObject) { // Récupération des lieux
                if (obj instanceof SaveClassPatron) {
                    if (((SaveClassPatron) obj).getScore() > bestScore) {
                        bestScore = ((SaveClassPatron) obj).getScore();
                    }
                }
            }
        }
    }

    public void setState(GameStateEnum gState) {
        if (currentState != null && currentState != playing) currentState.cleanup();
        choiceGameState(gState);

    }

    private void choiceGameState(GameStateEnum gState) {
        switch (gState) {
            case PLAY: currentState = playing;
                break;
            case MAIN:
                MainMenuState mms = new MainMenuState(commande, width, height);
                if (playing != null) {
                    Sauvegardable p = (Sauvegardable) playing;
                    sauvegarde(p);
                    if (p.getGameData().getScore() > bestScore) {
                        bestScore = p.getGameData().getScore();
                    }
                    if (p.getGameData().getLives() == 0) mms.setTextTitle(1);
                }
                currentState = mms;
                break;
            case NEWPLAY:
                playing = new PlayingState(commande, width, height);
                ((Sauvegardable)playing).getGameData().setBestScore(bestScore);
                currentState = playing;
                break;
            case PAUSE: currentState = new PausedState(commande, width, height);
                break;
        }
        currentState.init(commande, width, height);
    }

    public void update(float deltaTime, int width, int height) {
        this.width = width;
        this.height = height;
        currentState.setWidthHeight(width, height);
        if (currentState != null) currentState.update(deltaTime);
    }

    public void render() {
        if (currentState != null) currentState.render();
    }

    public void sauvegarde(Sauvegardable partie) {
        SaveClassPatron SCP = new SaveClassPatron(partie.getGameData());
        SCP.saveDonnees();
    }
}
