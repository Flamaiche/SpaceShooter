package learnGL.tools;

import gameGl.state.GameStateManager;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class Commande {
    private Camera camera;
    private long window;
    private ArrayList<Touche> touches = new ArrayList<>();
    private GameStateManager gsm;

    public Commande(Camera camera, long window, GameStateManager gsm) {
        this.camera = camera;
        this.window = window;
        this.gsm = gsm;

        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    public void setTouches(ArrayList<Touche> touches) {
        setActiveAllTouche(false, this.touches);
        this.touches = touches;
        setActiveAllTouche(true, this.touches);
    }

    public void setActiveAllTouche(boolean active, ArrayList<Touche> t) {
        if (this.touches != null) {
            for (Touche touche : t) {
                touche.setActive(active);
            }
        }
    }

    public void update() {
        for (Touche t : touches) {
            t.update(window);
        }
    }

    public Camera getCamera() { return camera; }
    public long getWindow() { return window; }
    public GameStateManager getGameStateManager() { return gsm; }
    public void setGameStateManager(GameStateManager gsm) { this.gsm = gsm; }

    // utilitaire pour menu
    private int upDownMenu = 0;
    public void upDownMenu(int delta) { upDownMenu += delta; }
    public int getUpDownMenu() { return upDownMenu; }
}
