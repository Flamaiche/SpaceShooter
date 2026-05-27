package gamegl.state;

import gamegl.SpaceShooter;
import gamegl.gestion.donnees.GameData;
import gamegl.gestion.donnees.SaveClassPatron;
import gamegl.utils.GetDonnee;
import learngl.tools.commandes.Commande;

import java.util.List;

public class GameStateManager {
    private GameState currentState;
    private Commande commande;
    private int width;
    private int height;
    private GameState playing;
    private GameData gameData;

    public enum GameStateEnum {
        MAIN,
        PLAY,
        NEWPLAY,
        PAUSE
    }

    public GameStateManager(Commande commande, GameData gameData, int width, int height) {
        this.commande = commande;
        this.width = width;
        this.height = height;
        this.gameData = gameData;
        playing = null;
        List<Object> saveObject = GetDonnee.readJson(SpaceShooter.filenameSaveScore);
        if (saveObject != null) {
            for (Object obj : saveObject) { // Récupération des saves
                if (obj instanceof SaveClassPatron) {
                    if (((SaveClassPatron) obj).getScore() > gameData.getBestScore()) {
                        gameData.setBestScore(((SaveClassPatron) obj).getScore());
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
                MainMenuState mms = new MainMenuState(commande, gameData, width, height);
                if (playing != null) {
                    sauvegarde();
                    gameData.setTotalScore(gameData.getTotalScore() + gameData.getScore());
                    if (gameData.getScore() > gameData.getBestScore()) {
                        gameData.setBestScore(gameData.getScore());
                    }
                }
                currentState = mms;
                break;
            case NEWPLAY:
                playing = new PlayingState(commande, gameData, width, height);
                currentState = playing;
                break;
            case PAUSE: currentState = new PausedState(commande, gameData, width, height);
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

    public void sauvegarde() {
        SaveClassPatron SCP = new SaveClassPatron(gameData);
        SCP.saveDonnees();
    }
}
