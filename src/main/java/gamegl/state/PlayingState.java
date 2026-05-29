package gamegl.state;

import gamegl.entites.*;
import gamegl.entites.balls.Balls;
import gamegl.entites.balls.BallsBasic;
import gamegl.entites.ennemis.Ennemis;
import gamegl.entites.ennemis.EnnemisBasic;
import gamegl.gestion.donnees.GameData;
import gamegl.gestion.Manager2D;
import gamegl.gestion.Manager3D;
import learngl.tools.camera.CameraPhysics;
import learngl.tools.camera.vue.GestionnaireVue;
import learngl.tools.shape.PreVerticesTable;
import learngl.tools.shape.Shape;
import learngl.tools.VertexUtils;
import gamegl.gestion.texte.TextHUD;
import learngl.tools.Shader;
import learngl.tools.commandes.ComboTouche;
import learngl.tools.commandes.Commande;
import learngl.tools.commandes.Touche;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static gamegl.gestion.texte.TextManager.uniformTextScale;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PlayingState extends GameState {
    private ArrayList<TextHUD> texts;
    private Joueur joueur;
    private ArrayList<Ennemis> ennemis;
    private ArrayList<Balls> balls;
    private ArrayList<Entity2D> uiElements;
    private Crosshair crosshair;

    private Shader ennemisShader, ballShader, crosshairShader, textShader;

    private double lastTime;
    private int score, ballsFiredTotal, enemiesKilledTotal;
    private final int MAX_BALLS = 20;

    private final CameraPhysics cameraPhysics = new CameraPhysics();
    private final GestionnaireVue gestionnaireVue = new GestionnaireVue();

    private final float vitesseRotation = 1.0f;
    private final float rollSpeed = 1.0f;

    private boolean mouseLocked = true;
    private boolean firstMouseInput = true;
    private double lastMouseX, lastMouseY;
    private final float mouseSensitivity = 0.1f;

    private final int[] inputAxes = new int[2];

    private double shootCooldown = 0.5;
    private int nbEnnemis = 35;
    private final Manager3D manager3D = new Manager3D();
    private final Manager2D manager2D = new Manager2D();
    private Touche alt;
    private Touche shift;

    /**
     * @param width  initial window width
     * @param height initial window height
     */
    public PlayingState(Commande commande, GameData data, int width, int height) {
        super(commande, data, width, height);
        camera.resetValues();

        score = ballsFiredTotal = enemiesKilledTotal = 0;

        ennemisShader = new Shader("shaders/EnnemisVertex.glsl", "shaders/EnnemisFragment.glsl");
        ballShader = new Shader("shaders/DefaultVertex.glsl", "shaders/DefaultFragment.glsl");
        crosshairShader = new Shader("shaders/DefaultVertex.glsl", "shaders/DefaultFragment.glsl");
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");

        Shape joueurShape = new Shape(VertexUtils.autoAddSlotTexture(PreVerticesTable.generatePlayerShip(0.7f)));
        joueurShape.setShader(ballShader);
        joueur = new Joueur(joueurShape);
        joueur.setPosition(camera.getPosition());

        Ennemis.setDespawnDistance(camera.getRenderSimulation());
        ennemis = new ArrayList<>();
        for (int i = 0; i < nbEnnemis; i++) {
            Ennemis e = new EnnemisBasic(
                    ennemisShader,
                    new float[]{camera.getPosition().x, camera.getPosition().y, camera.getPosition().z},
                    PreVerticesTable.generateCubeSimple(1f),
                    camera
            );
            float speed = 2.5f * (0.85f + (float)Math.random() * 0.5f);
            if (i > 10) {
                for (int puissance = 0; puissance < i/10; puissance++)
                    speed += speed*1.5f;
            }
            e.setSpeed(speed);
            e.setBodyColor(
                    0.3f + (float)Math.random() * 0.7f,
                    0.3f + (float)Math.random() * 0.7f,
                    0.3f + (float)Math.random() * 0.7f
            );
            ennemis.add(e);
        }

        Balls.setMaxDistance(camera.getRenderSimulation());
        balls = new ArrayList<>();
        for (int i = 0; i < MAX_BALLS; i++) balls.add(new BallsBasic(ballShader, 0.35f));

        uiElements = new ArrayList<>();
        crosshair = new Crosshair(crosshairShader, camera);
        uiElements.add(crosshair);

        data.resetVal();
        lastTime = glfwGetTime();
    }

    @Override
    public void init(Commande commande, int width, int height) {
        firstMouseInput = true;
        texts = new ArrayList<>();
        super.init(commande, width, height);

        initTouches();
        initHud();
    }

    public void initTouches() {
        ArrayList<Touche> touches = new ArrayList<>();
        glfwSetInputMode(commande.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

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
            camera.rotate((float)(deltaX * mouseSensitivity), (float)(deltaY * mouseSensitivity));
        });

        glfwSetWindowFocusCallback(commande.getWindow(), (window, focused) -> {
            if (!focused) firstMouseInput = true;
        });

        touches.add(new Touche(GLFW_KEY_TAB, null,
                () -> camera.setOrbitMode(false),
                () -> camera.setOrbitMode(true)));

        alt = new Touche(GLFW_KEY_LEFT_ALT, null, null, null);
        touches.add(alt);
        touches.add(new ComboTouche(alt, GLFW_KEY_Q, null, null, () -> camera.addRoll(-rollSpeed)));
        touches.add(new ComboTouche(alt, GLFW_KEY_E, null, null, () -> camera.addRoll(rollSpeed)));
        touches.add(new ComboTouche(alt, GLFW_KEY_R, () -> camera.setRoll(0), null, null));
        touches.add(new ComboTouche(alt, GLFW_KEY_L, () -> camera.setRollEnabled(!camera.isRollEnabled()), null, null));

        touches.add(new Touche(GLFW_KEY_W, null, null, () -> cameraPhysics.addFront(1, camera)));
        touches.add(new Touche(GLFW_KEY_S, null, null, () -> cameraPhysics.addFront(-1, camera)));
        touches.add(new Touche(GLFW_KEY_D, null, null, () -> cameraPhysics.addRight(1, camera)));
        touches.add(new Touche(GLFW_KEY_A, null, null, () -> cameraPhysics.addRight(-1, camera)));
        touches.add(new Touche(GLFW_KEY_SPACE, null, null, () -> cameraPhysics.addUp(1, camera)));
        touches.add(new Touche(GLFW_KEY_LEFT_CONTROL, null, null, () -> cameraPhysics.addUp(-1, camera)));

        shift = new Touche(GLFW_KEY_LEFT_SHIFT, null, null, null);
        touches.add(shift);

        touches.add(new Touche(GLFW_MOUSE_BUTTON_LEFT, true, null, null, () -> shoot()));
        touches.add(new Touche(GLFW_KEY_GRAVE_ACCENT, null, null, () -> shoot()));

        touches.add(new Touche(GLFW_KEY_ESCAPE,
                () -> commande.getGameStateManager().setState(GameStateManager.GameStateEnum.PAUSE),
                null, null));
        touches.add(new Touche(GLFW_KEY_U, () -> hud.setDebugMode(!hud.getDebugMode()), null, null));

        touches.add(new Touche(GLFW_KEY_V, () -> gestionnaireVue.mettreAJour(camera, joueur.getPosition()), null, null));

        commande.setTouches(touches);
    }

    @Override
    public void update(float deltaTime) {
        commande.update();

        long window = commande.getWindow();
        inputAxes[0] = 0;
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) inputAxes[0]++;
        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) inputAxes[0]--;
        inputAxes[1] = 0;
        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) inputAxes[1]++;
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) inputAxes[1]--;

        if (inputAxes[0] == 1) camera.rotate(-vitesseRotation * 60f * deltaTime, 0);
        else if (inputAxes[0] == -1) camera.rotate(vitesseRotation * 60f * deltaTime, 0);
        if (inputAxes[1] == 1) camera.rotate(0, vitesseRotation * 60f * deltaTime);
        else if (inputAxes[1] == -1) camera.rotate(0, -vitesseRotation * 60f * deltaTime);

        cameraPhysics.update(joueur.getPosition(), camera, deltaTime);
        gestionnaireVue.mettreAJour(camera, joueur.getPosition(), 0);

        joueur.update(inputAxes, deltaTime, camera.getYaw(), camera.getPitch());
        Entity collised = joueur.checkCollision(new ArrayList<Entity>(ennemis));
        if (collised != null) {
            joueur.decrementVie();
            if (collised instanceof Ennemis) {
                score += ((Ennemis) collised).touched();
            }
        }

        int point = manager3D.updateAll(ennemis, balls, deltaTime, camera.getPosition());
        if (point > 0) {
            score += point;
            enemiesKilledTotal++;
        }

        manager2D.updateAll(uiElements, width, height, ennemis, cameraPhysics.getVelocity());
        updateHUD(deltaTime);

        if (data.getLives() == 0)
            commande.getGameStateManager().setState(GameStateManager.GameStateEnum.MAIN);
    }

    private void updateHUD(float deltaTime) {
        int activeBalls = 0;
        for (Balls b : balls) if (b.isActive()) activeBalls++;
        int activeEnemies = 0;
        for (Ennemis e : ennemis) if (e.getVie() > 0) activeEnemies++;

        float distanceTarget = 0;
        for (Ennemis e : ennemis)
            if (e.isHighlighted()) distanceTarget = camera.getPosition().distance(e.getPosition());

        data.setScore(score);
        data.setLives(joueur.getVie());
        data.setBallsFired(ballsFiredTotal);
        data.setEnemiesKilled(enemiesKilledTotal);
        data.setPlayerPosition(joueur.getPosition().x, joueur.getPosition().y, joueur.getPosition().z);
        data.setPlayerOrientation(camera.getPitch(), camera.getYaw(), camera.getRoll());
        data.setActiveBalls(activeBalls, balls.size());
        data.setActiveEnemies(activeEnemies, ennemis.size());
        data.setDistanceTarget(distanceTarget);
        data.setElapsedTime(data.getElapsedTime() + deltaTime);
        data.setFPS(1.0f / deltaTime);
        data.setSpeed(cameraPhysics.getVelocity().length());

        hud.update(deltaTime, width, height);
    }

    @Override
    public void render() {
        glClearColor(1f, 1f, 0f, 0f);

        Matrix4f view = gestionnaireVue.obtenirVue(camera, joueur.getPosition());
        Matrix4f projection = camera.getProjection(width, height);

        if (!gestionnaireVue.estPremierePersonne()) {
            Vector3f shipFixedPos = new Vector3f(camera.getPosition())
                    .add(new Vector3f(camera.getFront()).mul(1.5f))
                    .sub(new Vector3f(camera.getUp()).mul(0.3f));

            Matrix4f shipModel = new Matrix4f(joueur.getModelMatrix());
            shipModel.setTranslation(shipFixedPos);

            if (joueur.getBody().isVisible(projection, view, shipModel)) {
                ballShader.bind();
                ballShader.setUniformMat4f("view", view);
                ballShader.setUniformMat4f("projection", projection);
                ballShader.setUniformMat4f("model", shipModel);
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                joueur.getBody().render();
                ballShader.unbind();
            }
        }
        manager3D.renderAll(ennemis, balls, view, projection);

        Matrix4f ortho = new Matrix4f().ortho2D(-1, 1, -1, 1);
        manager2D.renderAll(uiElements, ortho);

        hud.render(textShader);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        manager3D.cleanupAll(ennemis, balls);
        manager2D.cleanupAll(uiElements);
        joueur.cleanup();
    }

    private void shoot() {
        double currentTime = glfwGetTime();
        if (currentTime - lastTime < shootCooldown) return;
        lastTime = currentTime;

        Vector3f rayDir = crosshair.getRayDir();

        Vector3f spawnPos = new Vector3f(camera.getPosition())
                .add(new Vector3f(camera.getFront()).mul(1.5f))
                .sub(new Vector3f(camera.getUp()).mul(0.3f));

        for (Balls b : balls) {
            if (!b.isActive()) {
                b.activate(spawnPos, rayDir);
                ballsFiredTotal++;
                break;
            }
        }
    }

    public void initHud() {
        texts = new ArrayList<>();

        texts.add(new TextHUD(TextHUD.TextType.BESTSCORE, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.SCORE, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.LIVES, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.TIME, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.BALLS, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));
        texts.add(new TextHUD(TextHUD.TextType.ENEMIES, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 0.5f, 0f, 0.5f));

        texts.add(new TextHUD(TextHUD.TextType.VERSION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.FPS, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.POSITION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.SPEED, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.ORIENTATION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.ACTIVE_BALLS, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.ACTIVE_ENEMIES, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));
        texts.add(new TextHUD(TextHUD.TextType.DISTANCE_TARGET, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, 1f, 0f, 0f, true));

        hud.setTexts(texts);
    }
}
