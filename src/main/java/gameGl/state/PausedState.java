package gameGl.state;

import gameGl.gestion.texte.TextHUD;
import gameGl.gestion.texte.TextManager;
import learnGL.tools.Commande;
import learnGL.tools.Shader;
import learnGL.tools.Touche;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PausedState extends GameState {
    private Shader textShader;

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

        // Reprendre
        touches.add(new Touche(GLFW_KEY_ESCAPE, () -> commande.getGameStateManager().setState(new PlayingState()), null, null));
        // Retour menu
        touches.add(new Touche(GLFW_KEY_ENTER, () -> commande.getGameStateManager().setState(new MainMenuState()), null, null));

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
        glClearColor(0.2f, 0.2f, 0.2f, 1f);
        hud.render(textShader);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
