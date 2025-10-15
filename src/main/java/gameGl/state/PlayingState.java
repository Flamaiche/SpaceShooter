package gameGl.state;

import gameGl.entites.*;
import gameGl.gestion.donnees.GameData;
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
    private Crosshair crosshair;

    private Shader ennemisShader, ballShader, crosshairShader, textShader;

    private double lastTime;
    private int score, ballsFiredTotal, enemiesKilledTotal;
    private final int MAX_BALLS = 20;

    // Physique du déplacement
    private Vector3f velocity = new Vector3f(0, 0, 0);
    private Vector3f moveDirection = new Vector3f(0, 0, 0);
    private final float maxSpeed = 17.5f;
    private final float tempsMaxSpeed = 4.0f; // temps pour atteindre la vitesse max
    private final float acceleration = maxSpeed/tempsMaxSpeed;
    private final float deceleration = acceleration/2;

    // Rotation / roll
    private final float vitesseRotation = 1.0f;
    private final float rollSpeed = 1.0f;

    // Souris
    private boolean mouseLocked = true;
    private boolean firstMouseInput = true;
    private double lastMouseX, lastMouseY;
    private final float mouseSensitivity = 0.1f;

    // Autres
    private double shootCooldown = 0.5;
    private int nbEnnemis = 35;
    private GameData data = GameData.getInstance();
    private Touche alt;
    private Touche shift;

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
        for (int i = 0; i < nbEnnemis; i++) {
            Ennemis e = new Ennemis(
                    ennemisShader,
                    new float[]{camera.getPosition().x, camera.getPosition().y, camera.getPosition().z},
                    PreVerticesTable.generateCubeSimple(1f),
                    camera
            );
            float speed = 2.5f * (0.85f + (float)Math.random() * 0.5f); //2.5f *0.85 <= speed <= 2.5f *(0.85+0.5) == 2.125 <= speed <= 3.375
            if (i > 10) {
                for (int puissance = 0; puissance < i/10; puissance++)
                    speed += speed*1.5f;
            }
            e.setSpeed(speed);
            ennemis.add(e);
        }

        // Balles
        Ball.setMaxDistance(camera.getRenderSimulation());
        balls = new ArrayList<>();
        for (int i = 0; i < MAX_BALLS; i++) balls.add(new Ball(ballShader, 0.35f));

        // UI
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

        // MOUVEMENT SOURIS
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

        // TOUCHES
        touches.add(new Touche(GLFW_KEY_TAB, null,
                () -> camera.setOrbitMode(false),
                () -> camera.setOrbitMode(true)));

        // ALT + Roll
        alt = new Touche(GLFW_KEY_LEFT_ALT, null, null, null);
        touches.add(alt);
        touches.add(new ComboTouche(alt, GLFW_KEY_Q, null, null, () -> camera.addRoll(-rollSpeed)));
        touches.add(new ComboTouche(alt, GLFW_KEY_E, null, null, () -> camera.addRoll(rollSpeed)));
        touches.add(new ComboTouche(alt, GLFW_KEY_R, () -> camera.setRoll(0), null, null));
        touches.add(new ComboTouche(alt, GLFW_KEY_L, () -> camera.setRollEnabled(!camera.isRollEnabled()), null, null));

        // Déplacements (avec inertie fluide)
        touches.add(new Touche(GLFW_KEY_W, null, null, () -> moveDirection.add(camera.getFront())));
        touches.add(new Touche(GLFW_KEY_S, null, null, () -> moveDirection.sub(camera.getFront())));
        touches.add(new Touche(GLFW_KEY_D, null, null, () -> moveDirection.add(camera.getRight())));
        touches.add(new Touche(GLFW_KEY_A, null, null, () -> moveDirection.sub(camera.getRight())));
        touches.add(new Touche(GLFW_KEY_SPACE, null, null, () -> moveDirection.add(camera.getUp())));
        touches.add(new Touche(GLFW_KEY_LEFT_CONTROL, null, null, () -> moveDirection.sub(camera.getUp())));

        // Rotation flèches
        touches.add(new Touche(GLFW_KEY_LEFT, null, null, () -> camera.rotate(-vitesseRotation, 0f)));
        touches.add(new Touche(GLFW_KEY_RIGHT, null, null, () -> camera.rotate(vitesseRotation, 0f)));
        touches.add(new Touche(GLFW_KEY_UP, null, null, () -> camera.rotate(0f, vitesseRotation)));
        touches.add(new Touche(GLFW_KEY_DOWN, null, null, () -> camera.rotate(0f, -vitesseRotation)));


        shift = new Touche(GLFW_KEY_LEFT_SHIFT, null, null, null);
        touches.add(shift);
        touches.add(new ComboTouche(shift, GLFW_KEY_LEFT, null, null, () -> camera.rotate(vitesseRotation/2.0f, 0f)));
        touches.add(new ComboTouche(shift, GLFW_KEY_RIGHT, null, null, () -> camera.rotate(-vitesseRotation/2.0f, 0f)));
        touches.add(new ComboTouche(shift, GLFW_KEY_UP, null, null, () -> camera.rotate(0f, vitesseRotation/2.0f)));
        touches.add(new ComboTouche(shift, GLFW_KEY_DOWN, null, null, () -> camera.rotate(0f, -vitesseRotation/2.0f)));

        // Tir
        touches.add(new Touche(GLFW_MOUSE_BUTTON_LEFT, true, null, null, () -> shoot()));
        touches.add(new Touche(GLFW_KEY_GRAVE_ACCENT, null, null, () -> shoot()));

        // Pause / Debug
        touches.add(new Touche(GLFW_KEY_ESCAPE,
                () -> commande.getGameStateManager().setState(GameStateManager.GameStateEnum.PAUSE),
                null, null));
        touches.add(new Touche(GLFW_KEY_U, () -> hud.setDebugMode(!hud.getDebugMode()), null, null));

        commande.setTouches(touches);
    }

    @Override
    public void update(float deltaTime) {
        commande.update();
        updateCameraPhysics(deltaTime);

        int point = Manager3D.updateAll(ennemis, balls, joueur, deltaTime, camera.getPosition());
        if (point > 0) {
            score += point;
            enemiesKilledTotal++;
        }

        Manager2D.updateAll(uiElements, width, height, ennemis, velocity);
        updateHUD(deltaTime);

        if (data.getLives() == 0)
            commande.getGameStateManager().setState(GameStateManager.GameStateEnum.MAIN);
    }

    private void updateCameraPhysics(float deltaTime) {
        if (camera.isOrbitMode()) return;

        if (moveDirection.lengthSquared() > 0) {
            moveDirection.normalize();

            Vector3f desiredVelocity = new Vector3f(moveDirection).mul(maxSpeed);
            Vector3f deltaV = new Vector3f(desiredVelocity).sub(velocity);

            float maxChange = acceleration * deltaTime;
            if (deltaV.length() > maxChange)
                deltaV.normalize().mul(maxChange);
            velocity.add(deltaV);
        } else {
            float speed = velocity.length();
            if (speed > 0) {
                float decel = deceleration * deltaTime;
                if (decel > speed) velocity.zero();
                else velocity.mul(1 - decel / speed);
            }
        }

        if (velocity.lengthSquared() > 0)
            camera.move(new Vector3f(velocity).mul(deltaTime));

        moveDirection.zero();
    }

    private void updateHUD(float deltaTime) {
        int activeBalls = 0;
        for (Ball b : balls) if (b.isActive()) activeBalls++;
        int activeEnemies = 0;
        for (Ennemis e : ennemis) if (e.getVie() > 0) activeEnemies++;

        float distanceTarget = 0;
        for (Ennemis e : ennemis)
            if (e.isHighlighted()) distanceTarget = camera.getPosition().distance(e.getPosition());

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
        data.setSpeed(velocity.length());

        hud.update(deltaTime, width, height);
    }

    @Override
    public void render() {
        glClearColor(1f, 1f, 0f, 0f);

        Matrix4f view = camera.getViewMatrix();
        Matrix4f projection = camera.getProjection(width, height);

        Manager3D.renderAll(ennemis, balls, view, projection);

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
        if (currentTime - lastTime < shootCooldown) return;
        lastTime = currentTime;

        Vector3f rayOrigin = crosshair.getRayOrigin();
        Vector3f rayDir = crosshair.getRayDir();

        // Point de spawn légèrement devant la caméra (pour éviter les collisions internes)
        Vector3f spawnPos = new Vector3f(rayOrigin).add(new Vector3f(rayDir).mul(0.8f));

        for (Ball b : balls) {
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
