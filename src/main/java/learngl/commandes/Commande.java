package learngl.commandes;

import gamegl.state.GameStateManager;
import learngl.camera.Camera;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

/**
 * Manages a collection of input bindings (Touche) associated with a camera,
 * window, and game state manager. Provides methods to swap input profiles
 * and update all bindings each frame.
 */
public class Commande {
    private Camera camera;
    private final long window;
    private ArrayList<Touche> touches = new ArrayList<>();
    private GameStateManager gsm;

    /**
     * Creates a command manager and hides the cursor (GLFW_CURSOR_DISABLED).
     *
     * @param camera the camera controlled by these inputs
     * @param window the GLFW window handle
     * @param gsm    the game state manager
     */
    public Commande(Camera camera, long window, GameStateManager gsm) {
        this.camera = camera;
        this.window = window;
        this.gsm = gsm;

        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    /**
     * Replaces the current set of input bindings with the given list.
     * Disables the old bindings, sets the new ones, and marks them to ignore
     * the next press event.
     *
     * @param touches the new list of Touche bindings
     */
    public void setTouches(ArrayList<Touche> touches) {
        setActiveAllTouche(false, this.touches);
        this.touches = touches;
        for (Touche t : touches) {
            t.setIgnoreNextPress(true);
        }
        setActiveAllTouche(true, this.touches);
    }

    /**
     * Enables or disables all bindings in the given list.
     *
     * @param active true to enable, false to disable
     * @param t      the list of bindings
     */
    public void setActiveAllTouche(boolean active, ArrayList<Touche> t) {
        if (this.touches != null) {
            for (Touche touche : t) {
                touche.setActive(active);
            }
        }
    }

    /**
     * Updates every active binding for the current frame.
     */
    public void update() {
        for (Touche t : touches) {
            t.update(window);
        }
    }

    /**
     * Returns the managed camera.
     *
     * @return the camera
     */
    public Camera getCamera() { return camera; }

    /**
     * Sets the camera.
     *
     * @param camera the new camera
     */
    public void setCamera(Camera camera) {this.camera = camera;}

    /**
     * Returns the GLFW window handle.
     *
     * @return the window handle
     */
    public long getWindow() { return window; }

    /**
     * Returns the game state manager.
     *
     * @return the game state manager
     */
    public GameStateManager getGameStateManager() { return gsm; }

    /**
     * Sets the game state manager.
     *
     * @param gsm the new game state manager
     */
    public void setGameStateManager(GameStateManager gsm) { this.gsm = gsm; }
}
