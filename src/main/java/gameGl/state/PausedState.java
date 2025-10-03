package gameGl.state;

import gameGl.entites.Ennemis;
import gameGl.gestion.texte.TextHUD;
import gameGl.utils.PreVerticesTable;
import learnGL.tools.commandes.Commande;
import learnGL.tools.Shader;
import learnGL.tools.commandes.Touche;

import java.util.ArrayList;

import static gameGl.gestion.texte.TextManager.uniformTextScale;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PausedState extends GameState {
    private ArrayList<TextHUD> texts;
    private String[] textMenu = {"CONTINUER", "RECOMMENCER", "QUITTER"};
    private Shader textShader;
    private Shader ennemisShader;
    private Ennemis[] listeFakeEnnemis;
    private int indexSelection;

    // Gestion du curseur / orbite
    private boolean mouseLocked = true;
    private boolean firstMouseInput = true;
    private double lastMouseX;
    private double lastMouseY;
    private final float mouseSensitivity = 0.1f;

    public PausedState(Commande commande, int width, int height) {
        super(commande, width, height);
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
        ennemisShader = new Shader("shaders/EnnemisVertex.glsl", "shaders/EnnemisFragment.glsl");
        listeFakeEnnemis = new Ennemis[50];
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
        // Callback pour suivre la souris et mettre à jour la sélection
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

        // Verrouillage du curseur au début

        glfwSetInputMode(commande.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // Callback mouvement souris
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

        // Callback perte focus
        glfwSetWindowFocusCallback(commande.getWindow(), (window, focused) -> {
            if (!focused) firstMouseInput = true;
        });

        // Reprendre
        touches.add(new Touche(GLFW_KEY_ESCAPE, () -> actionBySelection(0), null, null));
        // Retour menu
        touches.add(new Touche(GLFW_KEY_ENTER, () -> actionBySelection(indexSelection), null, null));
        touches.add(new Touche(GLFW_KEY_UP, () -> indexSelection--, null, null));
        touches.add(new Touche(GLFW_KEY_DOWN, () -> indexSelection++, null, null));

        commande.setTouches(touches);
    }

    @Override
    public void initHud() {
        for (String t : textMenu)
            texts.add(new TextHUD(null, t, TextHUD.HorizontalAlignment.CENTER, TextHUD.VerticalAlignment.CENTER, (float)(uniformTextScale*1.2), 1.0f, 1.0f, 1.0f));

        hud.setTexts(texts);
    }

    public void actionBySelection(int indexSelection) {
        switch (indexSelection) {
            case 0 -> commande.getGameStateManager().setState(GameStateManager.GameStateEnum.PLAY);
            case 1 -> commande.getGameStateManager().setState(GameStateManager.GameStateEnum.NEWPLAY);
            case 2 -> commande.getGameStateManager().setState(GameStateManager.GameStateEnum.MAIN);
        }
    }

    public void updateHUD(float deltaTime) {
        // normalise l'index
        indexSelection = ((indexSelection % textMenu.length) + textMenu.length) % textMenu.length;

        for (int i = 0; i < texts.size(); i++) {
            TextHUD t = texts.get(i);

            if (i == indexSelection) {
                // texte sélectionné : plus grand et couleur jaune
                t.setText(">> " + textMenu[i]);
                t.setScale((float)(uniformTextScale * 2.5));
                t.setRGB(1.0f, 1.0f, 0f);
            } else {
                // textes non sélectionnés : scale normal
                t.setText(textMenu[i]);
                t.setScale(uniformTextScale * 1.2f);
                t.setRGB(1.0f, 1.0f, 1.0f);
            }
        }
        hud.update(deltaTime, width, height);
    }

    @Override
    public void update(float deltaTime) {
        commande.update();
        updateHUD(deltaTime);
        for (int i=0; i < listeFakeEnnemis.length; i++) {
            listeFakeEnnemis[i].update(deltaTime);
        }
    }

    @Override
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

    public Ennemis generateEnnemis(int speed) {
        Ennemis e = new Ennemis(ennemisShader, new float[]{camera.getPosition().x, camera.getPosition().y, camera.getPosition().z}, PreVerticesTable.generateCubeSimple(1f), camera);
        e.setSpeed(10);
        return e;
    }
}
