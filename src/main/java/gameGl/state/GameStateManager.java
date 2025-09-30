package gameGl.state;

import gameGl.state.GameState;
import learnGL.tools.Commande;

public class GameStateManager {
    private GameState currentState;
    private Commande commande;

    public GameStateManager(Commande commande) {
        this.commande = commande;
    }

    public void setState(GameState newState) {
        if (currentState != null) currentState.cleanup();
        currentState = newState;
        currentState.init(commande);
    }

    public void update(float deltaTime) {
        if (currentState != null) currentState.update(deltaTime);
    }

    public void render() {
        if (currentState != null) currentState.render();
    }
}
