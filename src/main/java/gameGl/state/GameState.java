package gameGl.state;

import gameGl.gestion.texte.Text;
import gameGl.gestion.texte.TextManager;
import learnGL.tools.Camera;
import learnGL.tools.Commande;

public abstract class GameState {
    protected Commande commande;
    protected Camera camera;
    protected TextManager hud;
    protected int width, height;

    public GameState(Commande commande, int width, int height) {
        this.commande = commande;
        this.camera = commande.getCamera();
        this.width = width;
        this.height = height;

        hud = new TextManager(width, height);
    }

    public void init(Commande commande, int width, int height) {

    }

    public abstract void initTouches();

    public abstract void initHud();

    public abstract void update(float deltaTime);

    public abstract void render();

    public void cleanup() {
        Text.cleanup();
    }

    public void setWidthHeight(int width, int height) {
        this.height = height;
        this.width = width;
    }
}
