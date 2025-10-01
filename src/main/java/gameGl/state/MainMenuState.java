package gameGl.state;

import gameGl.gestion.texte.TextHUD;
import gameGl.gestion.texte.TextManager;
import learnGL.tools.Commande;
import learnGL.tools.Shader;
import learnGL.tools.Touche;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class MainMenuState extends GameState {
    private Shader textShader;
    private int menuSelectNum;

    private boolean mouseLocked = true;

    @Override
    public void init(Commande commande, int width, int height) {
        super.init(commande, width, height);
        initTouches();
        initHud();
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
    }

    @Override
    public void initTouches() {
        ArrayList<Touche> touches = new ArrayList<>();

        // Navigation menu
        touches.add(new Touche(GLFW_KEY_UP, () -> menuSelectNum++, null, null));
        touches.add(new Touche(GLFW_KEY_DOWN, () -> menuSelectNum--, null, null));
        touches.add(new Touche(GLFW_KEY_ENTER, () -> {
            commande.getGameStateManager().setState(new PlayingState());
        }, null, null));
        touches.add(new Touche(GLFW_KEY_ESCAPE, () -> {
            glfwSetWindowShouldClose(commande.getWindow(), true);
        }, null, null));

        commande.setTouches(touches);

    }

    @Override
    public void initHud() {
        ArrayList<TextHUD> texts = new ArrayList<>();


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
