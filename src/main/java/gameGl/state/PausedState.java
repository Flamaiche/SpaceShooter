package gameGl.state;

import gameGl.gestion.texte.TextHUD;
import learnGL.tools.Commande;
import learnGL.tools.Shader;
import learnGL.tools.Touche;

import java.util.ArrayList;

import static gameGl.gestion.texte.TextManager.uniformTextScale;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PausedState extends GameState {
    private ArrayList<TextHUD> texts;
    private String[] textMenu = {"CONTINUER", "RECOMMENCER", "QUITTER"};
    private Shader textShader;
    private int indexSelection;

    public PausedState(Commande commande, int width, int height) {
        super(commande, width, height);
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
    }

    @Override
    public void init(Commande commande, int width, int height) {
        texts = new ArrayList<>();
        super.init(commande, width, height);
        initTouches();
        initHud();
        indexSelection = 0;
    }

    @Override
    public void initTouches() {
        ArrayList<Touche> touches = new ArrayList<>();

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
