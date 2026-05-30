package gamegl.state;

import gamegl.gestion.donnees.GameData;
import gamegl.gestion.texte.Text;
import gamegl.gestion.texte.TextManager;
import gamegl.utils.ConfigVaisseau;
import learngl.tools.camera.Camera;
import learngl.tools.commandes.Commande;
import org.joml.Vector3f;

/**
 * Abstract base class for all game states (main menu, playing, paused, etc.).
 * Provides shared state such as the command handler, camera, HUD text manager,
 * game data, and window dimensions.
 */
public abstract class GameState {
    protected Commande commande;
    protected Camera camera;
    protected TextManager hud;
    protected GameData data;
    protected int width, height;

    /**
     * Constructs a GameState with the given command handler, game data, and window dimensions.
     *
     * @param commande the command handler for input processing
     * @param data     the shared game data
     * @param width    the initial window width
     * @param height   the initial window height
     */
    public GameState(Commande commande, GameData data, int width, int height) {
        this.commande = commande;
        this.data = data;
        ConfigVaisseau cfg = ConfigVaisseau.get();
        this.camera = new Camera(new Vector3f(cfg.cameraSpawn));
        commande.setCamera(camera);
        this.width = width;
        this.height = height;

        hud = new TextManager(data, width, height);
    }

    /**
     * Initializes the state with the given command handler and window dimensions.
     *
     * @param commande the command handler for input processing
     * @param width    the window width
     * @param height   the window height
     */
    public void init(Commande commande, int width, int height) {
        commande.setCamera(camera);
        this.width = width;
        this.height = height;

        hud.setWindowSize(width, height);
    }

    /** Initializes key and mouse bindings for this state. */
    public abstract void initTouches();

    /** Initializes the HUD elements for this state. */
    public abstract void initHud();

    /**
     * Updates the state logic.
     *
     * @param deltaTime time elapsed since the last update
     */
    public abstract void update(float deltaTime);

    /** Renders the state to the screen. */
    public abstract void render();

    /** Releases resources held by this state. */
    public void cleanup() {
        Text.cleanup();
    }

    /**
     * Updates the stored window dimensions.
     *
     * @param width  the new window width
     * @param height the new window height
     */
    public void setWidthHeight(int width, int height) {
        this.height = height;
        this.width = width;
    }
}
