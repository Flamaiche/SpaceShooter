package gamegl.state;

import gamegl.gestion.donnees.GameData;
import gamegl.gestion.texte.AnimatedText;
import gamegl.gestion.texte.TextHUD;
import gamegl.gestion.texte.TextManager;
import gamegl.utils.PosDeltaTime;
import learngl.tools.commandes.Commande;
import learngl.tools.Shader;
import learngl.tools.commandes.Touche;

import java.util.ArrayList;

import static gamegl.gestion.texte.TextManager.uniformTextScale;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * The main menu state displaying menu options (JOUER, PARAMETRE, QUITTER)
 * with animated text effects and mouse/keyboard navigation.
 */
public class MainMenuState extends GameState {

    private ArrayList<TextHUD> texts;
    private String[] textMenu = {"JOUER", "PARAMETRE", "QUITTER"};

    private Shader textShader;

    private int indexSelection;
    private float rotationRadius = 175;

    private ArrayList<AnimatedText> animatedTexts = new ArrayList<>();
    private TextManager animatedTextManager;

    /**
     * Constructs the main menu state.
     *
     * @param commande the command handler for input
     * @param data     the shared game data
     * @param width    the initial window width
     * @param height   the initial window height
     */
    public MainMenuState(Commande commande, GameData data, int width, int height) {
        super(commande, data, width, height);
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");
        animatedTextManager = new TextManager(data, width, height);
    }

    @Override
    /**
     * Initializes the menu state, including input bindings, HUD, and mouse callbacks.
     *
     * @param commande the command handler for input
     * @param width    the window width
     * @param height   the window height
     */
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
    /**
     * Initializes keyboard and mouse input bindings for menu navigation.
     */
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
    /**
     * Initializes the HUD text elements, including static menu items, score/version display,
     * and animated text (selection highlight and decorative stars).
     */
    public void initHud() {
        for (String t : textMenu) {
            texts.add(new TextHUD(null, t, TextHUD.HorizontalAlignment.CENTER, TextHUD.VerticalAlignment.CENTER,
                    (float) (uniformTextScale * 1.2), 1.0f, 1.0f, 1.0f));
        }
        texts.add(new TextHUD(TextHUD.TextType.TOTALSCORE, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP,
                uniformTextScale, 1.0f, 1.0f, 1.0f));
        texts.add(new TextHUD(TextHUD.TextType.VERSION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP,
                uniformTextScale, 1.0f, 1.0f, 1.0f));
        hud.setTexts(texts);

        AnimatedText MenuText = new AnimatedText(
                textMenu[indexSelection],
                uniformTextScale * 1.5f,
                0f, 1f, 0f,
                145,
                width / 2.0, height / 2.0,
                0.5,
                (time, amplitude, cx, cy, tps, i) -> {
                    double letterSpacing = 20;
                    double x = cx + (i - textMenu[indexSelection].length() / 2.0) * letterSpacing;

                    double y = cy + amplitude * Math.sin(time * 2 + i * tps);

                    return new double[]{x, y};
                },
                width, height, 1,
                data
        );
        animatedTexts.add(MenuText);

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
                width, height, 1,
                data
        );
        animatedTexts.add(stars);

        for (AnimatedText at : animatedTexts) {
            animatedTextManager.getTexts().addAll(at.getLetters());
        }
    }

    /**
     * Performs the action associated with the given menu selection index.
     *
     * @param indexSelection the selected menu index (0 = JOUER, 1 = PARAMETRE, 2 = QUITTER)
     */
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

        animatedTexts.get(0).setText(textMenu[indexSelection]);

        for (AnimatedText at : animatedTexts) {
            at.update(deltaTime, width, height);
            animatedTextManager.getTexts().addAll(at.getLetters());
        }
    }

    @Override
    /**
     * Updates the menu state, including selection highlighting and animated text.
     *
     * @param deltaTime time elapsed since the last update
     */
    public void update(float deltaTime) {
        commande.update();
        updateMenuSelection();
        updateAnimatedTexts(deltaTime);
        hud.update(deltaTime, width, height);
        animatedTextManager.update(deltaTime, width, height);
    }

    @Override
    /**
     * Renders the menu background, HUD text, and animated text elements.
     */
    public void render() {
        glClearColor(0f, 0f, 0f, 1f);
        hud.render(textShader);
        animatedTextManager.render(textShader);
    }

    @Override
    /**
     * Releases resources held by this menu state.
     */
    public void cleanup() {
        super.cleanup();
    }
}
