package gameGl.state;

import gameGl.entites.*;
import gameGl.gestion.GameData;
import gameGl.gestion.Manager2D;
import gameGl.gestion.Manager3D;
import gameGl.gestion.texte.TextHUD;
import gameGl.utils.PreVerticesTable;
import learnGL.tools.*;
import learnGL.tools.commandes.ComboTouche;
import learnGL.tools.commandes.Commande;
import learnGL.tools.commandes.Touche;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static gameGl.gestion.texte.TextManager.uniformTextScale;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PlayingState extends GameState {
    private ArrayList<TextHUD> texts;
    private Joueur joueur;
    private ArrayList<Ennemis> ennemis;
    private ArrayList<Ball> balls;
    private ArrayList<Entity2D> uiElements;
    // Shaders
    private Shader ennemisShader, ballShader, crosshairShader, textShader;

    // HUD / debug
    private GameData data;

    private double lastTime;
    private int score, ballsFiredTotal, enemiesKilledTotal;

    private final int MAX_BALLS = 20;

    // Déplacement / rotation
    private final float vitesse = 0.05f;
    private final float vitesseRotation = 1.0f;
    private final float rollSpeed = 1.0f;

    // Gestion du curseur / orbite
    private boolean mouseLocked = true;
    private boolean firstMouseInput = true;
    private double lastMouseX;
    private double lastMouseY;
    private final float mouseSensitivity = 0.1f;

    private double shootCooldown = 0.3;

    // Alt pour combos
    private Touche alt;

    public PlayingState(Commande commande, int width, int height) {
        super(commande, width, height);
        camera.restValues();

        score = ballsFiredTotal = enemiesKilledTotal = 0;
        // Shaders
        ennemisShader = new Shader("shaders/EnnemisVertex.glsl", "shaders/EnnemisFragment.glsl");
        ballShader = new Shader("shaders/DefaultVertex.glsl", "shaders/DefaultFragment.glsl");
        crosshairShader = new Shader("shaders/DefaultVertex.glsl", "shaders/DefaultFragment.glsl");
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");

        // Joueur
        joueur = new Joueur(ballShader, camera, commande, 0.25f);

        // Ennemis
        Ennemis.setDespawnDistance(camera.getRenderSimulation());
        ennemis = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ennemis.add(new Ennemis(ennemisShader,
                    new float[]{camera.getPosition().x, camera.getPosition().y, camera.getPosition().z},
                    PreVerticesTable.generateCubeSimple(1f), camera));
        }

        // Balles
        Ball.setMaxDistance(camera.getRenderSimulation());
        balls = new ArrayList<>();
        for (int i = 0; i < MAX_BALLS; i++) balls.add(new Ball(ballShader, 0.35f));

        // UI
        uiElements = new ArrayList<>();
        uiElements.add(new Crosshair(crosshairShader, camera));

        data = GameData.getInstance();
        data.resetVal();
        // Temps
        lastTime = glfwGetTime();
    }

    @Override
    public void init(Commande commande, int width, int height) {
        firstMouseInput = true;

        texts = new ArrayList<>();
        super.init(commande, width, height);

        // Initialisation touches
        initTouches();

        // HUD
        initHud();
    }

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

        // Espace -> mode orbite
        touches.add(new Touche(GLFW_KEY_SPACE,
                null,
                () -> camera.setOrbitMode(false),
                () -> camera.setOrbitMode(true)
        ));

        // Alt + roll
        alt = new Touche(GLFW_KEY_LEFT_ALT, null, null, null);
        touches.add(alt);
        touches.add(new ComboTouche(alt, GLFW_KEY_Q, null, null, () -> camera.addRoll(-rollSpeed)));
        touches.add(new ComboTouche(alt, GLFW_KEY_E, null, null, () -> camera.addRoll(rollSpeed)));
        touches.add(new ComboTouche(alt, GLFW_KEY_R, () -> camera.setRoll(0), null, null));
        touches.add(new ComboTouche(alt, GLFW_KEY_L, () -> camera.setRollEnabled(!camera.isRollEnabled()), null, null));

        // Déplacements WASD + SHIFT/CTRL
        touches.add(new Touche(GLFW_KEY_W, null, null, () -> { if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getFront()).mul(vitesse)); }));
        touches.add(new Touche(GLFW_KEY_S, null, null, () -> { if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getFront()).mul(-vitesse)); }));
        touches.add(new Touche(GLFW_KEY_D, null, null, () -> { if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getRight()).mul(vitesse)); }));
        touches.add(new Touche(GLFW_KEY_A, null, null, () -> { if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getRight()).mul(-vitesse)); }));
        touches.add(new Touche(GLFW_KEY_LEFT_SHIFT, null, null, () -> { if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getUp()).mul(vitesse)); }));
        touches.add(new Touche(GLFW_KEY_LEFT_CONTROL, null, null, () -> { if (!camera.isOrbitMode()) camera.move(new Vector3f(camera.getUp()).mul(-vitesse)); }));

        // Flèches -> rotation caméra
        touches.add(new Touche(GLFW_KEY_LEFT, null, null, () -> camera.rotate(-vitesseRotation, 0f)));
        touches.add(new Touche(GLFW_KEY_RIGHT, null, null, () -> camera.rotate(vitesseRotation, 0f)));
        touches.add(new Touche(GLFW_KEY_UP, null, null, () -> camera.rotate(0f, vitesseRotation)));
        touches.add(new Touche(GLFW_KEY_DOWN, null, null, () -> camera.rotate(0f, -vitesseRotation)));

        touches.add(new Touche(GLFW_MOUSE_BUTTON_LEFT, true, null, null, () -> shoot()));
        touches.add(new Touche(GLFW_KEY_GRAVE_ACCENT, null, null, () -> shoot()));

        // Pause
        touches.add(new Touche(GLFW_KEY_ESCAPE, () -> commande.getGameStateManager().setState(GameStateManager.GameStateEnum.PAUSE), null, null));

        commande.setTouches(touches);
    }

    public void initHud() {
        // HUD joueur (left/top)
        texts.add(new TextHUD(TextHUD.TextType.SCORE, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.LIVES, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.TIME, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.BALLS, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.ENEMIES, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));

        // Debug (right/top)
        texts.add(new TextHUD(TextHUD.TextType.FPS, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.POSITION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.ORIENTATION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.ACTIVE_BALLS, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.ACTIVE_ENEMIES, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.DISTANCE_TARGET, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        hud.setTexts(texts);
        hud.setDebugMode(true);
    }

    @Override
    public void update(float deltaTime) {
        commande.update();

        // Update 3D
        int point = Manager3D.updateAll(ennemis, balls, joueur, deltaTime, camera.getPosition());
        if (point > 0) {
            score += point;
            enemiesKilledTotal++;
        }

        // Update 2D
        Manager2D.updateAll(uiElements, width, height, ennemis);

        // HUD
        updateHUD(deltaTime);
    }

    private void updateHUD(float deltaTime) {
        int activeBalls = 0;
        for (Ball b : balls) if (b.isActive()) activeBalls++;
        int activeEnemies = 0;
        for (Ennemis e : ennemis) if (e.getVie() > 0) activeEnemies++;

        float distanceTarget = 0;
        for (Ennemis e : ennemis) {
            if (e.isHighlighted()) distanceTarget = camera.getPosition().distance(e.getPosition());
        }

        data.setScore(score);
        data.setLives(joueur.getVie());
        data.setBallsFired(ballsFiredTotal);
        data.setEnemiesKilled(enemiesKilledTotal);
        data.setPlayerPosition(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
        data.setPlayerOrientation(camera.getPitch(), camera.getYaw(), camera.getRoll());
        data.setActiveBalls(activeBalls, balls.size());
        data.setActiveEnemies(activeEnemies, ennemis.size());
        data.setDistanceTarget(distanceTarget);
        data.setElapsedTime(data.getElapsedTime() + deltaTime);
        data.setFPS(1.0f / deltaTime);

        hud.update(deltaTime, width, height);
    }

    @Override
    public void render() {
        glClearColor(1f, 1f, 0f, 0f);

        Matrix4f view = camera.getViewMatrix();
        Matrix4f projection = camera.getProjection(width, height);

        // 3D
        Manager3D.renderAll(ennemis, balls, view, projection);

        // 2D
        Matrix4f ortho = new Matrix4f().ortho2D(-1, 1, -1, 1);
        Manager2D.renderAll(uiElements, ortho);

        hud.render(textShader);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        Manager3D.cleanupAll(ennemis, balls);
        Manager2D.cleanupAll(uiElements);
        joueur.cleanup();
    }

    private void shoot() {
        double currentTime = glfwGetTime();
        if (currentTime - lastTime < shootCooldown) return; // cooldown
        lastTime = currentTime;

        Vector3f spawnPos = new Vector3f(camera.getPosition()).add(new Vector3f(camera.getFront()).mul(0.5f));
        for (Ball b : balls) {
            if (!b.isActive()) {
                b.activate(spawnPos, camera.getFront());
                ballsFiredTotal++;
                break;
            }
        }
    }
}
