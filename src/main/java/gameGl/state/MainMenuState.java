package gameGl.state;

import gameGl.gestion.texte.AnimatedText;
import gameGl.gestion.texte.TextHUD;
import gameGl.gestion.texte.TextManager;
import gameGl.utils.PosDeltaTime;
import learnGL.tools.commandes.Commande;
import learnGL.tools.Shader;
import learnGL.tools.commandes.Touche;

import java.util.ArrayList;

import static gameGl.gestion.texte.TextManager.uniformTextScale;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class MainMenuState extends GameState {

    private ArrayList<TextHUD> texts;
    private String[] textMenu = {"JOUER", "PARAMETRE", "QUITTER"};
    private String[] textSplashList = {"", "PERDU"};
    private int textSplash = 0;

    private Shader textShader;

    private int indexSelection;
    private float rotationRadius = 175;

    // Liste globale des textes animés
    private ArrayList<AnimatedText> animatedTexts = new ArrayList<>();
    private TextManager animatedTextManager; // Manager unique pour tous les AnimatedText

    public MainMenuState(Commande commande, int width, int height) {
        super(commande, width, height);
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
        animatedTextManager = new TextManager(width, height);
    }

    @Override
    public void init(Commande commande, int width, int height) {
        texts = new ArrayList<>();
        super.init(commande, width, height);

        initTouches();
        initHud();
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
        touches.add(new Touche(GLFW_KEY_UP, () -> indexSelection--, null, null));
        touches.add(new Touche(GLFW_KEY_DOWN, () -> indexSelection++, null, null));
        touches.add(new Touche(GLFW_KEY_ENTER, () -> actionBySelection(indexSelection), null, null));
        touches.add(new Touche(GLFW_KEY_ESCAPE, () -> actionBySelection(2), null, null));
        touches.add(new Touche(GLFW_MOUSE_BUTTON_LEFT, true, () -> actionBySelection(indexSelection), null, null));
        commande.setTouches(touches);
    }

    @Override
    public void initHud() {
        // Menu fixe
        for (String t : textMenu) {
            texts.add(new TextHUD(null, t, TextHUD.HorizontalAlignment.CENTER, TextHUD.VerticalAlignment.CENTER,
                    (float) (uniformTextScale * 1.2), 1.0f, 1.0f, 1.0f));
        }
        texts.add(new TextHUD(TextHUD.TextType.TOTALSCORE, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP,
                uniformTextScale, 1.0f, 1.0f, 1.0f));
        texts.add(new TextHUD(TextHUD.TextType.VERSION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP,
                uniformTextScale, 1.0f, 1.0f, 1.0f));
        hud.setTexts(texts);

        // AnimatedText HELLO (position 0)
        AnimatedText MenuText = new AnimatedText(
                textMenu[indexSelection],
                uniformTextScale * 1.5f,
                0f, 1f, 0f,
                145,
                width / 2.0, height / 2.0,
                0.5,
                (time, amplitude, cx, cy, tps, i) -> {
                    // Espacement horizontal pour chaque lettre
                    double letterSpacing = 20;
                    double x = cx + (i - textMenu[indexSelection].length() / 2.0) * letterSpacing;

                    // Position y selon la vague
                    double y = cy + amplitude * Math.sin(time * 2 + i * tps);

                    return new double[]{x, y};
                },
                width, height, 1
        );
        animatedTexts.add(MenuText);


        // AnimatedText titre "PERDU" (position 1)
        AnimatedText titleText = new AnimatedText(
                textSplashList[textSplash],
                uniformTextScale * 3f,
                1f, 0f, 0f,
                145,
                width / 2.0, height / 2.0,
                0.5,
                (time, radius, cx, cy, tps, i) -> PosDeltaTime.circle(time + i * 0.1, radius, cx, cy, tps, 0, 1),
                width, height, textSplashList[textSplash].length(), TextHUD.HorizontalAlignment.CENTER, null
        );
        animatedTexts.add(titleText);

        // Étoiles rouges animées en cercle (solution 2)
        String starsText = "*****";
        int nbStars = starsText.length();
        AnimatedText stars = new AnimatedText(
                starsText,
                uniformTextScale,
                1f, 0f, 0f,
                rotationRadius,
                width / 2.0, height / 2.0,
                0.5,
                (time, radius, cx, cy, tps, i) ->
                        PosDeltaTime.circle(time, radius, cx, cy, tps, i, nbStars),
                width, height, 1
        );
        animatedTexts.add(stars);

        // Ajouter toutes les lettres de tous les AnimatedText au manager centralisé
        for (AnimatedText at : animatedTexts) {
            animatedTextManager.getTexts().addAll(at.getLetters());
        }
    }

    public void actionBySelection(int indexSelection) {
        switch (indexSelection) {
            case 0 -> commande.getGameStateManager().setState(GameStateManager.GameStateEnum.NEWPLAY);
            case 1 -> System.out.println("Paramètre");
            case 2 -> glfwSetWindowShouldClose(commande.getWindow(), true);
        }
    }

    private void updateMenuSelection() {
        indexSelection = ((indexSelection % textMenu.length) + textMenu.length) % textMenu.length;

        for (int i = 0; i < textMenu.length; i++) {
            TextHUD t = texts.get(i);
            if (i == indexSelection) {
                t.setText(">> " + textMenu[i]);
                t.setScale((float) (uniformTextScale * 2.5));
                t.setRGB(1f, 1f, 0f);
            } else {
                t.setText(textMenu[i]);
                t.setScale(uniformTextScale * 1.2f);
                t.setRGB(1f, 1f, 1f);
            }
        }
    }

    private void updateAnimatedTexts(double deltaTime) {
        animatedTextManager.getTexts().clear();

        // HELLO dynamique (position 0)
        animatedTexts.get(0).setText(textMenu[indexSelection]);

        // Titre (position 1)
        animatedTexts.get(1).setText(textSplashList[textSplash]);
        animatedTexts.get(1).setLettrePack(textSplashList[textSplash].length());

        for (AnimatedText at : animatedTexts) {
            at.update(deltaTime, width, height);
            animatedTextManager.getTexts().addAll(at.getLetters());
        }
    }

    @Override
    public void update(float deltaTime) {
        commande.update();
        updateMenuSelection();
        updateAnimatedTexts(deltaTime);
        hud.update(deltaTime, width, height);
        animatedTextManager.update(deltaTime, width, height);
    }

    @Override
    public void render() {
        glClearColor(0f, 0f, 0f, 1f);
        hud.render(textShader);
        animatedTextManager.render(textShader);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    public void setTextSplash(int textSplash) {
        this.textSplash = textSplash;
    }
}
