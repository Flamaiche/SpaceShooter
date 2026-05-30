package gamegl.state;

import gamegl.gestion.donnees.GameData;
import gamegl.gestion.texte.AnimatedText;
import gamegl.gestion.texte.TextHUD;
import gamegl.gestion.texte.TextManager;
import gamegl.utils.ConfigJeu;
import gamegl.utils.PosDeltaTime;
import learngl.commandes.Commande;
import learngl.Shader;
import learngl.commandes.Touche;

import org.joml.Vector4f;
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
    private final String[] textMenu = {"JOUER", "PARAMETRE", "QUITTER"};

    private final Shader textShader;

    private int indexSelection;

    private final ArrayList<AnimatedText> animatedTexts = new ArrayList<>();
    private final TextManager animatedTextManager;

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
        ConfigJeu cfgMenu = ConfigJeu.get();
        float menuItemScale = cfgMenu.menuItemScale;
        float menuAnimatedTextScale = cfgMenu.menuAnimatedTextScale;
        float menuUnselR = cfgMenu.menuUnselectedColor.x;
        float menuUnselG = cfgMenu.menuUnselectedColor.y;
        float menuUnselB = cfgMenu.menuUnselectedColor.z;
        for (String t : textMenu) {
            texts.add(new TextHUD(null, t, TextHUD.HorizontalAlignment.CENTER, TextHUD.VerticalAlignment.CENTER,
                    uniformTextScale * menuItemScale, menuUnselR, menuUnselG, menuUnselB));
        }
        texts.add(new TextHUD(TextHUD.TextType.TOTALSCORE, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP,
                uniformTextScale, menuUnselR, menuUnselG, menuUnselB));
        texts.add(new TextHUD(TextHUD.TextType.VERSION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP,
                uniformTextScale, menuUnselR, menuUnselG, menuUnselB));
        hud.setTexts(texts);

        float menuTextRadius = cfgMenu.menuTextRadius;
        float menuTextLetterSpacing = cfgMenu.menuTextLetterSpacing;
        float menuTextFrequency = cfgMenu.menuTextFrequency;
        float menuToursPerSecond = cfgMenu.menuToursPerSecond;
        float menuStarRadius = cfgMenu.menuStarRadius;
        float animR = cfgMenu.menuTextColor.x;
        float animG = cfgMenu.menuTextColor.y;
        float animB = cfgMenu.menuTextColor.z;
        AnimatedText MenuText = new AnimatedText(
                textMenu[indexSelection],
                uniformTextScale * menuAnimatedTextScale,
                animR, animG, animB,
                menuTextRadius,
                width / 2.0, height / 2.0,
                menuToursPerSecond,
                (time, amplitude, cx, cy, tps, i) -> {
                    double x = cx + (i - textMenu[indexSelection].length() / 2.0) * menuTextLetterSpacing;

                    double y = cy + amplitude * Math.sin(time * menuTextFrequency + i * tps);

                    return new double[]{x, y};
                },
                width, height,
                data
        );
        animatedTexts.add(MenuText);

        String starsText = "*****";
        int nbStars = starsText.length();
        AnimatedText stars = new AnimatedText(
                starsText,
                uniformTextScale,
                cfgMenu.menuStarColor.x, cfgMenu.menuStarColor.y, cfgMenu.menuStarColor.z,
                menuStarRadius,
                width / 2.0, height / 2.0,
                menuToursPerSecond,
                (time, radius, cx, cy, tps, i) ->
                        PosDeltaTime.circle(time, radius, cx, cy, tps, i, nbStars),
                width, height,
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

        ConfigJeu cfg = ConfigJeu.get();
        float menuSelectedScale = cfg.menuSelectedScale;
        float menuItemScale = cfg.menuItemScale;
        float selR = cfg.menuSelectedColor.x;
        float selG = cfg.menuSelectedColor.y;
        float selB = cfg.menuSelectedColor.z;
        float unselR = cfg.menuUnselectedColor.x;
        float unselG = cfg.menuUnselectedColor.y;
        float unselB = cfg.menuUnselectedColor.z;
        for (int i = 0; i < textMenu.length; i++) {
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
    }

    private void updateAnimatedTexts(double deltaTime) {
        animatedTextManager.getTexts().clear();

        animatedTexts.getFirst().setText(textMenu[indexSelection]);

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
        hud.update(width, height);
        animatedTextManager.update(width, height);
    }

    @Override
    public void render() {
        Vector4f bgColorMenu = ConfigJeu.get().bgColorMenu;
        glClearColor(bgColorMenu.x, bgColorMenu.y, bgColorMenu.z, bgColorMenu.w);
        hud.render(textShader);
        animatedTextManager.render(textShader);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
