package gameGl.state;

import gameGl.gestion.texte.TextHUD;
import gameGl.gestion.texte.TextManager;
import learnGL.tools.Commande;
import learnGL.tools.Shader;
import learnGL.tools.Touche;

import java.util.ArrayList;

import static gameGl.gestion.texte.TextManager.uniformTextScale;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class MainMenuState extends GameState {
    private ArrayList<TextHUD> texts;
    private String[] textMenu = {"JOUER", "PARAMETRE", "QUITTER"};
    private ArrayList<TextHUD> textsVollatile;
    private TextManager textManagerVolatille;
    private Shader textShader;
    private int indexSelection;
    private int nbStarCircle1 = 5;

    private float rotationRadius = 145; // rayon du cercle
    private float totalTime = 0;       // temps écoulé pour l'animation

    public MainMenuState(Commande commande, int width, int height) {
        super(commande, width, height);
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
        textManagerVolatille = new TextManager(width, height);
    }

    @Override
    public void init(Commande commande, int width, int height) {
        texts = new ArrayList<>();
        textsVollatile = new ArrayList<>();
        super.init(commande, width, height);
        initTouches();
        initHud();
        initMouseCallbacks();
    }

    private void initMouseCallbacks() {
        // Callback pour suivre la souris et mettre à jour la sélection
        glfwSetCursorPosCallback(commande.getWindow(), (window, xpos, ypos) -> {
            for (int i = 0; i < texts.size(); i++) {
                TextHUD t = texts.get(i);
                if (TextHUD.isMouseOver(t, (float)xpos, (float)ypos)) {
                    indexSelection = i; // priorité souris
                    break;
                }
            }
        });
    }

    @Override
    public void initTouches() {
        ArrayList<Touche> touches = new ArrayList<>();

        // Navigation menu clavier
        touches.add(new Touche(GLFW_KEY_UP, () -> indexSelection--, null, null));
        touches.add(new Touche(GLFW_KEY_DOWN, () -> indexSelection++, null, null));
        touches.add(new Touche(GLFW_KEY_ENTER, () -> actionBySelection(indexSelection), null, null));
        touches.add(new Touche(GLFW_KEY_ESCAPE, () -> actionBySelection(2), null, null));

        // Clic souris
        touches.add(new Touche(GLFW_MOUSE_BUTTON_LEFT, true, () -> actionBySelection(indexSelection), null, null));

        commande.setTouches(touches);
    }

    @Override
    public void initHud() {
        // Textes menu
        for (String t : textMenu) {
            texts.add(new TextHUD(null, t, TextHUD.HorizontalAlignment.CENTER, TextHUD.VerticalAlignment.CENTER,
                    (float)(uniformTextScale*1.2), 1.0f, 1.0f, 1.0f));
        }
        hud.setTexts(texts);

        // HUD animé
        for (int i = 0; i < nbStarCircle1; i++) {
            textsVollatile.add(new TextHUD(null, "*", null, null, uniformTextScale, 1.0f, 0f, 0f));
        }
        textManagerVolatille.setTexts(textsVollatile);
    }

    public void actionBySelection(int indexSelection) {
        switch (indexSelection) {
            case 0 -> commande.getGameStateManager().setState(GameStateManager.GameStateEnum.NEWPLAY);
            case 1 -> System.out.println("Paramètre");
            case 2 -> glfwSetWindowShouldClose(commande.getWindow(), true);
        }
    }

    public void updateHUD(float deltaTime) {
        // normalise l'index
        indexSelection = ((indexSelection % textMenu.length) + textMenu.length) % textMenu.length;

        for (int i = 0; i < textMenu.length; i++) {
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

        // Animation du HUD tournant
        totalTime += deltaTime / 2;
        int degres = 360 / textsVollatile.size();
        for (int i = 0; i < textsVollatile.size(); i++) {
            circularText(textsVollatile.get(i), totalTime + ((float) (degres * (i + 1)) / 360));
        }

        hud.update(deltaTime, width, height);
        textManagerVolatille.update(deltaTime, width, height);
    }

    private void circularText(TextHUD rotatingText, float totalTime) {
        float scaleX = (float) width / hud.getBaseWidth();
        float scaleY = (float) height / hud.getBaseHeight();
        float uniformScale = Math.min(scaleX, scaleY);

        double[] pos = gameGl.utils.PosDeltaTime.circle(
                totalTime,
                rotationRadius * uniformScale, // rayon scalé
                width / 2.0,
                height / 2.0,
                1
        );
        rotatingText.setX((float) pos[0]);
        rotatingText.setY((float) pos[1]);
    }


    @Override
    public void update(float deltaTime) {
        commande.update();
        updateHUD(deltaTime);
    }

    @Override
    public void render() {
        glClearColor(0f, 0f, 0f, 1f);
        hud.render(textShader);
        textManagerVolatille.render(textShader);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
