package gamegl.state;

import gamegl.entites.ennemis.Ennemis;
import gamegl.entites.ennemis.EnnemisBasic;
import gamegl.gestion.donnees.GameData;
import gamegl.gestion.texte.TextHUD;
import gamegl.utils.ConfigEnnemis;
import gamegl.utils.ConfigJeu;
import gamegl.utils.ConfigVaisseau;
import learngl.shape.PreVerticesTable;
import learngl.commandes.Commande;
import learngl.Shader;
import learngl.commandes.Touche;

import org.joml.Vector4f;
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
    private final String[] textMenu = {"CONTINUER", "RECOMMENCER", "QUITTER"};
    private final Shader textShader;
    private final Shader ennemisShader;
    private final Ennemis[] listeFakeEnnemis;
    private int indexSelection;

    private final boolean mouseLocked = true;
    private boolean firstMouseInput = true;
    private double lastMouseX;
    private double lastMouseY;
    private final float mouseSensitivity = ConfigVaisseau.get().mouseSensitivity;

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
        int nbFakeEnnemis = ConfigJeu.get().menuFakeEnnemisCount;
        listeFakeEnnemis = new Ennemis[nbFakeEnnemis];
        for (int i=0; i < listeFakeEnnemis.length; i++) {
            listeFakeEnnemis[i] = generateEnnemis((i/10+1)*2);
        }
    }

    @Override
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
    public void initTouches() {
        ArrayList<Touche> touches = new ArrayList<>();

        glfwSetInputMode(commande.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwSetCursorPosCallback(commande.getWindow(), (_, xpos, ypos) -> {
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

        glfwSetWindowFocusCallback(commande.getWindow(), (_, focused) -> {
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
    public void initHud() {
        ConfigJeu cfg = ConfigJeu.get();
        float menuItemScale = cfg.menuItemScale;
        float unselR = cfg.menuUnselectedColor.x;
        float unselG = cfg.menuUnselectedColor.y;
        float unselB = cfg.menuUnselectedColor.z;
        for (String t : textMenu)
            texts.add(new TextHUD(null, t, TextHUD.HorizontalAlignment.CENTER, TextHUD.VerticalAlignment.CENTER,
                    uniformTextScale * menuItemScale, unselR, unselG, unselB));

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
     */
    public void updateHUD() {
        indexSelection = ((indexSelection % textMenu.length) + textMenu.length) % textMenu.length;

        ConfigJeu cfg = ConfigJeu.get();
        float menuSelectedScale = cfg.menuSelectedScale;
        float menuItemScale = cfg.menuItemScale;
        float selR = cfg.menuSelectedColor.x;
        float selG = cfg.menuSelectedColor.y;
        float selB = cfg.menuSelectedColor.z;
        float unselR = cfg.menuUnselectedColor.x;
        float unselG = cfg.menuUnselectedColor.y;
        float unselB = cfg.menuUnselectedColor.z;
        for (int i = 0; i < texts.size(); i++) {
            TextHUD t = texts.get(i);

            if (i == indexSelection) {
                t.setText(">> " + textMenu[i]);
                t.setScale(uniformTextScale * menuSelectedScale);
                t.setRGB(selR, selG, selB);
            } else {
                t.setText(textMenu[i]);
                t.setScale(uniformTextScale * menuItemScale);
                t.setRGB(unselR, unselG, unselB);
            }
        }
        hud.update(width, height);
    }

    @Override
    public void update(float deltaTime) {
        commande.update();
        updateHUD();
        for (Ennemis listeFakeEnnemi : listeFakeEnnemis) {
            listeFakeEnnemi.update(deltaTime);
        }
    }

    @Override
    public void render() {
        Vector4f bgColorPause = ConfigJeu.get().bgColorPause;
        glClearColor(bgColorPause.x, bgColorPause.y, bgColorPause.z, bgColorPause.w);
        hud.render(textShader);
        for (Ennemis listeFakeEnnemi : listeFakeEnnemis) {
            listeFakeEnnemi.render(camera.getViewMatrix(), camera.getProjection(width, height));
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        textShader.cleanup();
        ennemisShader.cleanup();
        for (Ennemis e : listeFakeEnnemis) e.cleanup();
    }

    /**
     * Generates a fake enemy for decorative background rendering.
     *
     * @param speed the movement speed of the generated enemy
     * @return a new Ennemis instance
     */
    public Ennemis generateEnnemis(int speed) {
        Ennemis e = new EnnemisBasic(ennemisShader, new float[]{camera.getPosition().x, camera.getPosition().y, camera.getPosition().z}, PreVerticesTable.generateCubeSimple(ConfigEnnemis.get().enemyBaseSize));
        e.setSpeed(speed);
        return e;
    }
}
