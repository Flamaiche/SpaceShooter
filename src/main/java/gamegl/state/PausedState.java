package gamegl.state;

import gamegl.entites.ennemis.Ennemis;
import gamegl.entites.ennemis.EnnemisBasic;
import gamegl.gestion.donnees.GameData;
import gamegl.gestion.texte.TextHUD;
import learngl.tools.shape.PreVerticesTable;
import learngl.tools.commandes.Commande;
import learngl.tools.Shader;
import learngl.tools.commandes.Touche;

import java.util.ArrayList;

import static gamegl.gestion.texte.TextManager.uniformTextScale;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * The paused state shown when the game is interrupted.
 * Displays a menu (CONTINUER, RECOMMENCER, QUITTER) and renders
 * decorative fake enemies rotating in the background.
 * Supports both mouse and keyboard navigation with camera orbit control.
 */
public class PausedState extends GameState {
    private ArrayList<TextHUD> texts;
    private String[] textMenu = {"CONTINUER", "RECOMMENCER", "QUITTER"};
    private Shader textShader;
    private Shader ennemisShader;
    private Ennemis[] listeFakeEnnemis;
    private int indexSelection;

    private boolean mouseLocked = true;
    private boolean firstMouseInput = true;
    private double lastMouseX;
    private double lastMouseY;
    private final float mouseSensitivity = 0.1f;

    /**
     * Constructs the paused state, loading shaders and generating fake enemies.
     *
     * @param commande the command handler for input
     * @param data     the shared game data
     * @param width    the initial window width
     * @param height   the initial window height
     */
    public PausedState(Commande commande, GameData data, int width, int height) {
        super(commande, data, width, height);
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
        ennemisShader = new Shader("shaders/EnnemisVertex.glsl", "shaders/EnnemisFragment.glsl");
        listeFakeEnnemis = new Ennemis[50];
        for (int i=0; i < listeFakeEnnemis.length; i++) {
            listeFakeEnnemis[i] = generateEnnemis((i/10+1)*2);
        }
    }

    @Override
    /**
     * Initializes the paused state, including input bindings, HUD, and mouse callbacks.
     *
     * @param commande the command handler for input
     * @param width    the window width
     * @param height   the window height
     */
    public void init(Commande commande, int width, int height) {
        firstMouseInput = true;
        texts = new ArrayList<>();
        super.init(commande, width, height);
        initTouches();
        initHud();
        indexSelection = 0;
        initMouseCallbacks();
    }

    private void initMouseCallbacks() {
        glfwSetCursorPosCallback(commande.getWindow(), (window, xpos, ypos) -> {
            if (glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED) return;

            for (int i = 0; i < texts.size(); i++) {
                TextHUD t = texts.get(i);
                if (TextHUD.coodsMouseOn(t, (float) xpos, (float) ypos)) {
                    indexSelection = i;
                    break;
                }
            }
        });
    }


    @Override
    /**
     * Initializes keyboard and mouse input bindings for the pause menu.
     * Locks the cursor and sets up camera orbit via mouse movement.
     */
    public void initTouches() {
        ArrayList<Touche> touches = new ArrayList<>();

        glfwSetInputMode(commande.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwSetCursorPosCallback(commande.getWindow(), (window, xpos, ypos) -> {
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
            camera.rotate((float)(deltaX * mouseSensitivity),
                    (float)(deltaY * mouseSensitivity));
        });

        glfwSetWindowFocusCallback(commande.getWindow(), (window, focused) -> {
            if (!focused) firstMouseInput = true;
        });

        touches.add(new Touche(GLFW_KEY_ESCAPE, () -> actionBySelection(0), null, null));
        touches.add(new Touche(GLFW_KEY_ENTER, () -> actionBySelection(indexSelection), null, null));
        touches.add(new Touche(GLFW_KEY_UP, () -> indexSelection--, null, null));
        touches.add(new Touche(GLFW_KEY_DOWN, () -> indexSelection++, null, null));
        touches.add(new Touche(GLFW_MOUSE_BUTTON_LEFT, true, () -> actionBySelection(indexSelection), null, null));

        commande.setTouches(touches);
    }

    @Override
    /**
     * Initializes the HUD text elements for the pause menu.
     */
    public void initHud() {
        for (String t : textMenu)
            texts.add(new TextHUD(null, t, TextHUD.HorizontalAlignment.CENTER, TextHUD.VerticalAlignment.CENTER, (float)(uniformTextScale*1.2), 1.0f, 1.0f, 1.0f));

        hud.setTexts(texts);
    }

    /**
     * Performs the action associated with the given menu selection index.
     *
     * @param indexSelection the selected menu index (0 = CONTINUER, 1 = RECOMMENCER, 2 = QUITTER)
     */
    public void actionBySelection(int indexSelection) {
        switch (indexSelection) {
            case 0 -> commande.getGameStateManager().setState(GameStateManager.GameStateEnum.PLAY);
            case 1 -> commande.getGameStateManager().setState(GameStateManager.GameStateEnum.NEWPLAY);
            case 2 -> commande.getGameStateManager().setState(GameStateManager.GameStateEnum.MAIN);
        }
    }

    /**
     * Updates the HUD, normalizing the selection index and highlighting the selected item.
     *
     * @param deltaTime time elapsed since the last update
     */
    public void updateHUD(float deltaTime) {
        indexSelection = ((indexSelection % textMenu.length) + textMenu.length) % textMenu.length;

        for (int i = 0; i < texts.size(); i++) {
            TextHUD t = texts.get(i);

            if (i == indexSelection) {
                t.setText(">> " + textMenu[i]);
                t.setScale((float)(uniformTextScale * 2.5));
                t.setRGB(1.0f, 1.0f, 0f);
            } else {
                t.setText(textMenu[i]);
                t.setScale(uniformTextScale * 1.2f);
                t.setRGB(1.0f, 1.0f, 1.0f);
            }
        }
        hud.update(deltaTime, width, height);
    }

    @Override
    /**
     * Updates the paused state: processes input, updates HUD, and updates fake enemies.
     *
     * @param deltaTime time elapsed since the last update
     */
    public void update(float deltaTime) {
        commande.update();
        updateHUD(deltaTime);
        for (int i=0; i < listeFakeEnnemis.length; i++) {
            listeFakeEnnemis[i].update(deltaTime);
        }
    }

    @Override
    /**
     * Renders the pause menu background, HUD text, and fake enemies in 3D.
     */
    public void render() {
        glClearColor(0.2f, 0.2f, 0.2f, 1f);
        hud.render(textShader);
        for (int i=0; i < listeFakeEnnemis.length; i++) {
            listeFakeEnnemis[i].render(camera.getViewMatrix(), camera.getProjection(width, height));
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    /**
     * Generates a fake enemy for decorative background rendering.
     *
     * @param speed the movement speed of the generated enemy
     * @return a new Ennemis instance
     */
    public Ennemis generateEnnemis(int speed) {
        Ennemis e = new EnnemisBasic(ennemisShader, new float[]{camera.getPosition().x, camera.getPosition().y, camera.getPosition().z}, PreVerticesTable.generateCubeSimple(1f), camera);
        e.setSpeed(speed);
        return e;
    }
}
