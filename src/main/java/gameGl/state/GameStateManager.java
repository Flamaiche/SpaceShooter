package gameGl.state;

import learnGL.tools.Commande;

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
        playing = new PlayingState(commande, width, height);
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
}
