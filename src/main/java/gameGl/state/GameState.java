package gameGl.state;

import learnGL.tools.Commande;

public abstract class GameState {
    protected Commande commande;

    public void init(Commande commande) {
        this.commande = commande;
    }

    public abstract void update(float deltaTime);

    public abstract void render();

    public abstract void cleanup();
}
