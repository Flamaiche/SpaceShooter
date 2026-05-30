package gamegl.state;

import gamegl.entites.*;
import gamegl.entites.balls.Balls;
import gamegl.entites.balls.BallsBasic;
import gamegl.entites.ennemis.Ennemis;
import gamegl.entites.ennemis.EnnemisBasic;
import gamegl.gestion.donnees.GameData;
import gamegl.gestion.Manager2D;
import gamegl.gestion.Manager3D;
import gamegl.utils.ConfigEnnemis;
import gamegl.utils.ConfigJeu;
import gamegl.utils.ConfigVaisseau;
import learngl.camera.CameraPhysics;
import learngl.camera.vue.GestionnaireVue;
import learngl.shape.PreVerticesTable;
import learngl.shape.Shape;
import learngl.VertexUtils;
import gamegl.gestion.texte.TextHUD;
import learngl.Shader;

import learngl.commandes.Commande;
import learngl.commandes.Touche;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;

import static gamegl.gestion.texte.TextManager.uniformTextScale;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class PlayingState extends GameState {
    private ArrayList<TextHUD> texts;
    private final Joueur joueur;
    private final ArrayList<Ennemis> ennemis;
    private final ArrayList<Balls> balls;
    private final ArrayList<Entity2D> uiElements;
    private final Crosshair crosshair;

    private final Shader ballShader;
    private final Shader textShader;

    private double lastTime;
    private int score, ballsFiredTotal, enemiesKilledTotal;
    private final CameraPhysics cameraPhysics = new CameraPhysics();
    private final GestionnaireVue gestionnaireVue = new GestionnaireVue();

    private final boolean mouseLocked = true;
    private boolean firstMouseInput = true;
    private double lastMouseX, lastMouseY;

    private final int[] inputAxes = new int[2];

    private final Manager3D manager3D = new Manager3D();
    private final Manager2D manager2D = new Manager2D();

    /**
     * @param width  initial window width
     * @param height initial window height
     */
    public PlayingState(Commande commande, GameData data, int width, int height) {
        super(commande, data, width, height);
        camera.resetValues();

        score = ballsFiredTotal = enemiesKilledTotal = 0;

        Shader ennemisShader = new Shader("shaders/EnnemisVertex.glsl", "shaders/EnnemisFragment.glsl");
        ballShader = new Shader("shaders/DefaultVertex.glsl", "shaders/DefaultFragment.glsl");
        Shader crosshairShader = new Shader("shaders/DefaultVertex.glsl", "shaders/DefaultFragment.glsl");
        textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");

        Shape joueurShape = new Shape(VertexUtils.autoAddSlotTexture(PreVerticesTable.generatePlayerShip(ConfigVaisseau.get().playerShipScale)));
        joueurShape.setShader(ballShader);
        joueur = new Joueur(joueurShape);
        joueur.setPosition(camera.getPosition());

        ennemis = new ArrayList<>();
        for (int i = 0; i < ConfigEnnemis.get().nbEnnemis; i++) {
            ConfigEnnemis cfgEnemy = ConfigEnnemis.get();
            Ennemis e = new EnnemisBasic(
                    ennemisShader,
                    new float[]{camera.getPosition().x, camera.getPosition().y, camera.getPosition().z},
                    PreVerticesTable.generateCubeSimple(cfgEnemy.enemyBaseSize)
            );
            float speed = cfgEnemy.enemyBaseSpeed * (cfgEnemy.enemySpeedRandomBase + (float)Math.random() * cfgEnemy.enemySpeedRandomRange);
            if (i > cfgEnemy.enemySpeedPowerThreshold) {
                for (int puissance = 0; puissance < i / cfgEnemy.enemySpeedPowerGroupSize; puissance++)
                    speed += speed * cfgEnemy.enemySpeedPowerMultiplier;
            }
            e.setSpeed(speed);
            float colorBase = cfgEnemy.enemyColorConfig.x;
            float colorRange = cfgEnemy.enemyColorConfig.y;
            e.setBodyColor(
                    colorBase + (float)Math.random() * colorRange,
                    colorBase + (float)Math.random() * colorRange,
                    colorBase + (float)Math.random() * colorRange
            );
            ennemis.add(e);
        }

        balls = new ArrayList<>();
        for (int i = 0; i < ConfigVaisseau.get().ballsMax; i++) balls.add(new BallsBasic(ballShader, ConfigVaisseau.get().ballSize));

        uiElements = new ArrayList<>();
        crosshair = new Crosshair(crosshairShader, camera);
        crosshair.initDirection();
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

        glfwSetCursorPosCallback(commande.getWindow(), (_, xpos, ypos) -> {
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
            camera.rotate((float)(deltaX * ConfigVaisseau.get().mouseSensitivity), (float)(deltaY * ConfigVaisseau.get().mouseSensitivity));
        });

        glfwSetWindowFocusCallback(commande.getWindow(), (_, focused) -> {
            if (!focused) firstMouseInput = true;
        });

        touches.add(new Touche(GLFW_KEY_TAB, null,
                () -> camera.setOrbitMode(false),
                () -> camera.setOrbitMode(true)));

        touches.add(new Touche(GLFW_KEY_Q, null, null, () -> camera.rotateRoll(-ConfigVaisseau.get().rollSpeed)));
        touches.add(new Touche(GLFW_KEY_E, null, null, () -> camera.rotateRoll(ConfigVaisseau.get().rollSpeed)));

        touches.add(new Touche(GLFW_KEY_W, null, null, () -> cameraPhysics.addFront(1, camera)));
        touches.add(new Touche(GLFW_KEY_S, null, null, () -> cameraPhysics.addFront(-1, camera)));
        touches.add(new Touche(GLFW_KEY_D, null, null, () -> cameraPhysics.addRight(1, camera)));
        touches.add(new Touche(GLFW_KEY_A, null, null, () -> cameraPhysics.addRight(-1, camera)));
        touches.add(new Touche(GLFW_KEY_SPACE, null, null, () -> cameraPhysics.addUp(1, camera)));
        touches.add(new Touche(GLFW_KEY_LEFT_CONTROL, null, null, () -> cameraPhysics.addUp(-1, camera)));

        Touche shift = new Touche(GLFW_KEY_LEFT_SHIFT, null, null, null);
        touches.add(shift);

        touches.add(new Touche(GLFW_MOUSE_BUTTON_LEFT, true, null, null, this::shoot));
        touches.add(new Touche(GLFW_KEY_GRAVE_ACCENT, null, null, this::shoot));

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

        float fr = ConfigJeu.get().targetFramerate;
        float vr = ConfigVaisseau.get().vitesseRotation;
        if (inputAxes[0] == 1) camera.rotate(-vr * fr * deltaTime, 0);
        else if (inputAxes[0] == -1) camera.rotate(vr * fr * deltaTime, 0);
        if (inputAxes[1] == 1) camera.rotate(0, vr * fr * deltaTime);
        else if (inputAxes[1] == -1) camera.rotate(0, -vr * fr * deltaTime);

        cameraPhysics.update(joueur.getPosition(), camera, deltaTime);
        gestionnaireVue.mettreAJour(camera, joueur.getPosition(), 0);

        crosshair.updateLag(camera.getFront(), deltaTime);
        joueur.update(inputAxes, deltaTime, camera.getFront(), camera.getUp());
        Entity collised = joueur.checkCollision(new ArrayList<>(ennemis));
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

        crosshair.setRayOrigin(joueur.getPosition());
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
        data.setPlayerOrientation(camera.getPitch(), camera.getYaw(), 0);
        data.setActiveBalls(activeBalls, balls.size());
        data.setActiveEnemies(activeEnemies, ennemis.size());
        data.setDistanceTarget(distanceTarget);
        data.setElapsedTime(data.getElapsedTime() + deltaTime);
        data.setFPS(1.0f / deltaTime);
        data.setSpeed(cameraPhysics.getVelocity().length());

        hud.update(width, height);
    }

    @Override
    public void render() {
        Vector4f bgColorGameplay = ConfigJeu.get().bgColorGameplay;
        glClearColor(bgColorGameplay.x, bgColorGameplay.y, bgColorGameplay.z, bgColorGameplay.w);

        Matrix4f view = gestionnaireVue.obtenirVue(camera, joueur.getPosition());
        Matrix4f projection = camera.getProjection(width, height);

        if (!gestionnaireVue.estPremierePersonne()) {
            Vector3f shipFixedPos = gestionnaireVue.getDernierePosNavire();

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
        if (currentTime - lastTime < ConfigVaisseau.get().shootCooldown) return;
        lastTime = currentTime;

        Vector3f rayDir = crosshair.getRayDir();

        ConfigVaisseau vaisseau = ConfigVaisseau.get();
        Vector3f spawnPos = new Vector3f(joueur.getPosition());

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
        ConfigJeu cfg = ConfigJeu.get();

        float lr = cfg.hudLeftColor.x, lg = cfg.hudLeftColor.y, lb = cfg.hudLeftColor.z;
        float rr = cfg.hudRightColor.x, rg = cfg.hudRightColor.y, rb = cfg.hudRightColor.z;

        texts.add(new TextHUD(TextHUD.TextType.BESTSCORE, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, lr, lg, lb));
        texts.add(new TextHUD(TextHUD.TextType.SCORE, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, lr, lg, lb));
        texts.add(new TextHUD(TextHUD.TextType.LIVES, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, lr, lg, lb));
        texts.add(new TextHUD(TextHUD.TextType.TIME, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, lr, lg, lb));
        texts.add(new TextHUD(TextHUD.TextType.BALLS, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, lr, lg, lb));
        texts.add(new TextHUD(TextHUD.TextType.ENEMIES, TextHUD.HorizontalAlignment.LEFT, TextHUD.VerticalAlignment.TOP, uniformTextScale, lr, lg, lb));

        texts.add(new TextHUD(TextHUD.TextType.VERSION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, rr, rg, rb, true));
        texts.add(new TextHUD(TextHUD.TextType.FPS, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, rr, rg, rb, true));
        texts.add(new TextHUD(TextHUD.TextType.POSITION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, rr, rg, rb, true));
        texts.add(new TextHUD(TextHUD.TextType.SPEED, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, rr, rg, rb, true));
        texts.add(new TextHUD(TextHUD.TextType.ORIENTATION, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, rr, rg, rb, true));
        texts.add(new TextHUD(TextHUD.TextType.ACTIVE_BALLS, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, rr, rg, rb, true));
        texts.add(new TextHUD(TextHUD.TextType.ACTIVE_ENEMIES, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, rr, rg, rb, true));
        texts.add(new TextHUD(TextHUD.TextType.DISTANCE_TARGET, TextHUD.HorizontalAlignment.RIGHT, TextHUD.VerticalAlignment.TOP, uniformTextScale, rr, rg, rb, true));

        hud.setTexts(texts);
    }
}
