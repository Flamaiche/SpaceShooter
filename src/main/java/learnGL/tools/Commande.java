package learnGL.tools;

import org.lwjgl.glfw.GLFW;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Commande {
    private Camera camera;
    private long window;
    public static float vitesse = 0.05f;
    public static float vitesseRotation = 1.0f;
    private float rollSpeed = 1f;

    private float mouseSensitivity = 0.1f;
    private double lastMouseX;
    private double lastMouseY;
    private boolean firstMouseInput = true;
    private boolean mouseLocked = true;

    private double lastShootTime = 0;
    private double shootCooldown = 0.3;

    private ArrayList<Touche> touches = new ArrayList<>(); // actuellement laissé mais pas utilisé
    private GameState gameStateLast = GameState.PLAYING;
    private GameState gameState = GameState.PLAYING;
    private Map<GameState, ArrayList<Touche>> toucheState = new HashMap<>();
    private int upDownMenu = 0;

    public Commande(Camera camera, long window) {
        this.camera = camera;
        this.window = window;

        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        GLFW.glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (!mouseLocked) return;
            if (firstMouseInput) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouseInput = false;
            }
            double deltaX = xpos - lastMouseX;
            double deltaY = lastMouseY - ypos;
            lastMouseX = xpos;
            lastMouseY = ypos;
            camera.rotate((float)(deltaX * mouseSensitivity), (float)(deltaY * mouseSensitivity));
        });

        initTouches();

        // Reset touches si la fenêtre perd le focus
        GLFW.glfwSetWindowFocusCallback(window, (win, focused) -> {
            if (!focused) {
                for (Touche t : touches) {
                    t.reset();
                }
            }
        });
    }

    private void initTouches() {
        int nbTouches = 0;

        // CAPS_LOCK -> lock/unlock souris
        touches.add(new Touche(GLFW.GLFW_KEY_CAPS_LOCK,
                () -> { // onPress
                    if (mouseLocked) {
                        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
                        mouseLocked = false;
                    } else {
                        firstMouseInput = true;
                        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
                        mouseLocked = true;
                    }
                },
                null, null
        ));

        // Espace -> mode orbite tant que pressée
        touches.add(new Touche(GLFW.GLFW_KEY_SPACE,
                null,                 // pas besoin d’action à l’appui unique
                () -> camera.setOrbitMode(false), // relâchement -> désactiver orbite
                () -> camera.setOrbitMode(true)   // tant que pressée -> orbite active
        ));
        Touche alt = new Touche(GLFW.GLFW_KEY_LEFT_ALT, null, null, null);
        touches.add(alt);
        // Roll ALT + Q/E/R/L
        touches.add(new ComboTouche(alt, GLFW.GLFW_KEY_Q, null, null, () -> camera.addRoll(-rollSpeed)));
        touches.add(new ComboTouche(alt, GLFW.GLFW_KEY_E, null, null, () -> camera.addRoll(rollSpeed)));
        touches.add(new ComboTouche(alt, GLFW.GLFW_KEY_R, null, null, () -> camera.setRoll(0)));
        touches.add(new ComboTouche(alt, GLFW.GLFW_KEY_L, () -> camera.setRollEnabled(!camera.isRollEnabled()), null,null));

        // Déplacements WASD + SHIFT/CTRL
        touches.add(new Touche(GLFW.GLFW_KEY_W, null, null, () -> {
            if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getFront()).mul(vitesse));
        }));
        touches.add(new Touche(GLFW.GLFW_KEY_S, null,   null, () -> {
            if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getFront()).mul(-vitesse));
        }));
        touches.add(new Touche(GLFW.GLFW_KEY_D, null, null, () -> {
            if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getRight()).mul(vitesse));
        }));
        touches.add(new Touche(GLFW.GLFW_KEY_A, null, null, () -> {
            if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getRight()).mul(-vitesse));
        }));
        touches.add(new Touche(GLFW.GLFW_KEY_LEFT_SHIFT, null, null, () -> {
            if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getUp()).mul(vitesse));
        }));
        touches.add(new Touche(GLFW.GLFW_KEY_LEFT_CONTROL, null, null, () -> {
            if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getUp()).mul(-vitesse));
        }));

        // Flèches -> rotation caméra
        touches.add(new Touche(GLFW.GLFW_KEY_LEFT, null, null, () -> camera.rotate(-vitesseRotation, 0f)));
        touches.add(new Touche(GLFW.GLFW_KEY_RIGHT, null, null, () -> camera.rotate(vitesseRotation, 0f)));
        touches.add(new Touche(GLFW.GLFW_KEY_UP, null, null, () -> camera.rotate(0f, vitesseRotation)));
        touches.add(new Touche(GLFW.GLFW_KEY_DOWN, null, null, () -> camera.rotate(0f, -vitesseRotation)));

        touches.add(new Touche(GLFW.GLFW_KEY_ESCAPE, () -> {
            if (gameState == GameState.PLAYING) {
                setGameState(GameState.PAUSED);
            } else if (gameState == GameState.MAIN_MENU) {
                GLFW.glfwSetWindowShouldClose(window, true);
            }
        }, null, null));

        // VOIR CANSHOOT()
        addMap(GameState.PLAYING, nbTouches);
        nbTouches = touches.size() - nbTouches;

        touches.add(new Touche(GLFW.GLFW_KEY_LEFT_SHIFT, () -> upDownMenu++, null, null));
        touches.add(new Touche(GLFW.GLFW_KEY_LEFT_CONTROL, () -> upDownMenu--, null, null));
        touches.add(new Touche(GLFW.GLFW_KEY_ENTER, () -> {
            if (gameState == GameState.MAIN_MENU) {
                setGameState(GameState.PLAYING);
            }
        }, null, null));

        // ESC -> fermer la fenêtre
        touches.add(new Touche(GLFW.GLFW_KEY_ESCAPE,
                () -> GLFW.glfwSetWindowShouldClose(window, true),
                null, null));

        // VOIR ISSELECTEDMENU()
        addMap(GameState.MAIN_MENU, nbTouches);
        setActiveAllTouche(false, toucheState.get(GameState.MAIN_MENU));
        nbTouches = touches.size() - nbTouches;

    }

    private void addMap(GameState gameState, int nbTouches) {
        ArrayList<Touche> tmpTouches = new ArrayList<>();
        for (int i = nbTouches; i < touches.size(); i++) {
            tmpTouches.add(touches.get(i));
        }
        toucheState.put(gameState, tmpTouches);
    }

    public void update() {
        for (Touche t : touches) {
            t.update(window);
        }
    }

    public boolean canShoot() {
        double currentTime = GLFW.glfwGetTime();
        if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
                && currentTime - lastShootTime >= shootCooldown) {
            lastShootTime = currentTime;
            return true;
        }
        return false;
    }

    public void setActiveAllTouche(boolean active, ArrayList<Touche> t) {
        for (Touche touche : t) {
            touche.setActive(active);
        }
    }

    public GameState getGameState() {
        return gameState;
    }
    public void setGameState(GameState newState) {
        if (newState != gameState) {
            ArrayList<Touche> oldTouches = toucheState.get(gameState);
            if (oldTouches != null) {
                setActiveAllTouche(false, oldTouches);
            }
            ArrayList<Touche> newTouches = toucheState.get(newState);
            if (newTouches != null) {
                setActiveAllTouche(true, newTouches);
            }
            gameStateLast = gameState;
            gameState = newState;
        }
    }
    
    public int getUpDownMenu() {
        return upDownMenu;
    }
    public void resetUpDownMenu() {
        upDownMenu = 0;
    }
    
}

