package gameGl.state;

import gameGl.gestion.donnees.GameData;
import gameGl.gestion.donnees.Sauvegardable;
import gameGl.gestion.donnees.SaveClassPatron;
import gameGl.utils.GetDonnee;
import learnGL.tools.commandes.Commande;

public class GameStateManager {
    private GameState currentState;
    private Commande commande;
    private int width;
    private int height;
    private GameState playing;

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
                if (playing != null) sauvegarde((Sauvegardable) playing);
                currentState = new MainMenuState(commande, width, height);
                break;
            case NEWPLAY:
                playing = new PlayingState(commande, width, height);
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
