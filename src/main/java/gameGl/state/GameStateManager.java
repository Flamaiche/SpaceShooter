package gameGl.state;

import gameGl.state.GameState;
import learnGL.tools.Camera;
import learnGL.tools.Commande;

public class GameStateManager {
    private GameState currentState;
    private Commande commande;
    private int width;
    private int height;

    public GameStateManager(Commande commande, int width, int height) {
        this.commande = commande;
        this.width = width;
        this.height = height;
    }

    public void setState(GameState newState) {
        if (currentState != null) currentState.cleanup();
        currentState = newState;
        currentState.init(commande, width, height);
    }

    public void update(float deltaTime, int width, int height) {
        currentState.setWidthHeight(width, height);
        if (currentState != null) currentState.update(deltaTime);
    }

    public void render() {
        if (currentState != null) currentState.render();
    }
}
