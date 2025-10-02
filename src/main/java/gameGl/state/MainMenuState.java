package gameGl.state;

import gameGl.gestion.texte.TextHUD;
import learnGL.tools.Commande;
import learnGL.tools.Shader;
import learnGL.tools.Touche;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class MainMenuState extends GameState {
    private ArrayList<TextHUD> texts;
    private Shader textShader;
    private int menuSelectNum;

    public MainMenuState(Commande commande, int width, int height) {
        super(commande, width, height);
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
    }

    @Override
    public void init(Commande commande, int width, int height) {
        texts = new ArrayList<>();
        super.init(commande, width, height);
        initTouches();
        initHud();
    }

    @Override
    public void initTouches() {
        ArrayList<Touche> touches = new ArrayList<>();

        // Navigation menu
        touches.add(new Touche(GLFW_KEY_UP, () -> menuSelectNum++, null, null));
        touches.add(new Touche(GLFW_KEY_DOWN, () -> menuSelectNum--, null, null));
        touches.add(new Touche(GLFW_KEY_ENTER, () -> {
            commande.getGameStateManager().setState(GameStateManager.GameStateEnum.NEWPLAY);
        }, null, null));
        touches.add(new Touche(GLFW_KEY_ESCAPE, () -> {
            glfwSetWindowShouldClose(commande.getWindow(), true);
        }, null, null));

        commande.setTouches(touches);

    }

    @Override
    public void initHud() {

        hud.setTexts(texts);
    }

    @Override
    public void update(float deltaTime) {
        commande.update();
    }

    @Override
    public void render() {
        glClearColor(0f, 0f, 0f, 1f);
        hud.render(textShader);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
