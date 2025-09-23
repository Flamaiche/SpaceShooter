package gameGl;

import gameGl.entites.*;
import gameGl.gestion.GameData;
import gameGl.gestion.Manager2D;
import gameGl.gestion.Manager3D;
import gameGl.gestion.texte.Text;
import gameGl.gestion.texte.TextManager;
import gameGl.utils.PreVerticesTable;
import learnGL.tools.Camera;
import learnGL.tools.Commande;
import learnGL.tools.GameState;
import learnGL.tools.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.ArrayList;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class SpaceShooter {

    private long window;
    private int width = 800;
    private int height = 600;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        init();
        loop();

        // cleanup fenêtre
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Space Shooter", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // callback viewport
        glfwSetFramebufferSizeCallback(window, (win, newWidth, newHeight) -> {
            width = newWidth;
            height = newHeight;
            glViewport(0, 0, width, height);
        });

        // centrer fenêtre
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    private void loop() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glClearColor(1f, 1f, 0f, 0f); // fond jaune
        glViewport(0, 0, width, height);

        // Camera
        Camera camera = new Camera(new Vector3f(0, 0, 3));
        camera.setRollEnabled(true);
        Commande commande = new Commande(camera, window);

        // Shaders
        Shader ennemisShader = new Shader("shaders/EnnemisVertex.glsl", "shaders/EnnemisFragment.glsl");
        Shader ballShader = new Shader("shaders/DefaultVertex.glsl", "shaders/DefaultFragment.glsl");
        Shader crosshairShader = new Shader("shaders/DefaultVertex.glsl", "shaders/DefaultFragment.glsl");
        Shader textShader = new Shader("shaders/TextVertex.glsl", "shaders/TextFragment.glsl");

        Ennemis.setDespawnDistance(camera.getRenderSimulation());

        // Ennemis
        ArrayList<Ennemis> ennemis = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ennemis.add(new Ennemis(ennemisShader,
                    new float[]{camera.getPosition().x, camera.getPosition().y, camera.getPosition().z},
                    PreVerticesTable.generateCubeSimple(1f), camera));
        }

        // Crosshair 2D
        ArrayList<Entity2D> uiElements = new ArrayList<>();
        uiElements.add(new Crosshair(crosshairShader, camera));

        // Balles
        Ball.setMaxDistance(camera.getRenderSimulation());
        final int MAX_BALLS = 20;
        ArrayList<Ball> balls = new ArrayList<>();
        for (int i = 0; i < MAX_BALLS; i++) balls.add(new Ball(ballShader, 0.35f));

        // HUD / debug
        TextManager hud = new TextManager(width, height);
        hud.setDebugMode(true);
        GameData data = GameData.getInstance();
        Joueur joueur = new Joueur(ballShader, camera, window, 0.25f);

        // Variables temps et état du jeu
        double lastTime = glfwGetTime();
        double pauseStartTime = 0;

        int score = 0;
        int ballsFiredTotal = 0;
        int enemiesKilledTotal = 0;
        Ennemis enn = new Ennemis(ennemisShader,
                new float[]{camera.getPosition().x, camera.getPosition().y, camera.getPosition().z},
                PreVerticesTable.generateCubeSimple(1f), camera);
        Ennemis.setSpeed(0f); // ennemi de test immobile

        Matrix4f orthoProjection = new Matrix4f().ortho2D(-1, 1, -1, 1);
        joueur.cmd.setGameState(GameState.MAIN_MENU);

        // Boucle principale
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            joueur.cmd.update();

            double currentTime = glfwGetTime();
            float deltaTime = 0;

            switch (joueur.cmd.getGameState()) {

                case MAIN_MENU:
                    glClearColor(0f, 0f, 0f, 0f); // fond noir
                    //renderMainMenu(textShader);
                    break;

                case PLAYING:
                    glClearColor(1f, 1f, 0f, 0f); // fond jaune

                    deltaTime = (float)(currentTime - lastTime);
                    lastTime = currentTime;

                    // --- Gestion du tir ---
                    if (joueur.cmd.canShoot()) {
                        Vector3f spawnPos = new Vector3f(camera.getPosition()).add(new Vector3f(camera.getFront()).mul(0.5f));
                        for (Ball b : balls) {
                            if (!b.isActive()) {
                                b.activate(spawnPos, camera.getFront());
                                ballsFiredTotal++;
                                break;
                            }
                        }
                    }

                    // --- Update / rendu 3D ---
                    Matrix4f viewMatrix = camera.getViewMatrix();
                    Matrix4f projection = camera.getProjection(width, height);

                    int point = Manager3D.updateAll(ennemis, balls, joueur, deltaTime, camera.getPosition());
                    if (point > 0) {
                        score += point;
                        enemiesKilledTotal++;
                    }
                    Manager3D.renderAll(ennemis, balls, viewMatrix, projection);

                    // --- Update / rendu 2D ---
                    Manager2D.updateAll(uiElements, width, height, ennemis);
                    Manager2D.renderAll(uiElements, orthoProjection);
                    enn.render(viewMatrix, projection); // ennemi de test

                    // --- HUD / debug ---
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
                    hud.render(textShader);
                    break;

                case PAUSED:
                    glClearColor(1f, 1f, 1f, 0f); // fond blanc
                    lastTime = glfwGetTime(); // on décale lastTime pour éviter un delta énorme
                    pauseStartTime = 0;
                    break;
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        // --- Cleanup ---
        Manager3D.cleanupAll(ennemis, balls);
        Manager2D.cleanupAll(uiElements);
        Text.cleanup();
    }

    public static void main(String[] args) {
        new SpaceShooter().run();
    }
}
