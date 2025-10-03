package gameGl.state;

import gameGl.gestion.texte.Text;
import gameGl.gestion.texte.TextManager;
import learnGL.tools.Camera;
import learnGL.tools.commandes.Commande;
import org.joml.Vector3f;

public abstract class GameState {
    protected Commande commande;
    protected Camera camera;
    protected TextManager hud;
    protected int width, height;

    public GameState(Commande commande, int width, int height) {
        this.commande = commande;
        this.camera = new Camera(new Vector3f(0, 0, 3));
        commande.setCamera(camera);
        this.width = width;
        this.height = height;

        hud = new TextManager(width, height);
    }

    public void init(Commande commande, int width, int height) {
        commande.setCamera(camera);
        this.width = width;
        this.height = height;

        hud.setWindowSize(width, height);
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
