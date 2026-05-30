package gamegl;

import gamegl.gestion.donnees.GameData;
import gamegl.gestion.texte.Text;
import gamegl.state.GameStateManager;
import gamegl.utils.ConfigJeu;
import gamegl.utils.ConfigVaisseau;
import learngl.tools.camera.Camera;
import learngl.tools.commandes.Commande;
import learngl.tools.commandes.Touche;
import org.jetbrains.annotations.NotNull;
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

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Main entry point for the Space Shooter game application.
 * Initializes GLFW, creates a window, sets up the game loop with delta-time
 * updates, and manages global input (e.g. cursor lock toggle).
 */
public class SpaceShooter {

    private long window;
    private int width;
    private int height;
    private boolean mouseLocked = true;
    private static String gameVersion = "A1.1";
    public static String filenameSaveScore = "SauvegardeScore";

    /**
     * Runs the game: prints the LWJGL version, initialises the window, reads the
     * latest version from a file, enters the main loop, and cleans up the window.
     */
    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        init();
        majDerniereVersionDepuisFichier("AjoutVersion(SpaceShooter).txt");
        System.out.println("gameVersion : " + gameVersion);
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void init() {
        ConfigJeu cfg = ConfigJeu.get();
        width = cfg.windowWidth;
        height = cfg.windowHeight;
        boolean isWayland = System.getenv("WAYLAND_DISPLAY") != null;
        GLFWErrorCallback.createPrint(isWayland
                ? new java.io.PrintStream(System.err) {
                    @Override public java.io.PrintStream printf(@NotNull String format, Object... args) {
                        if (args != null && args.length > 0 && args[0] != null
                                && args[0].toString().contains("FEATURE_UNAVAILABLE"))
                            return this;
                        return super.printf(format, args);
                    }
                }
                : System.err
        ).set();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, "Space Shooter", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetFramebufferSizeCallback(window, (_, newWidth, newHeight) -> {
            width = newWidth;
            height = newHeight;
            glViewport(0, 0, width, height);
        });

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, width, height);

        if (!isWayland) {
            try (MemoryStack stack = stackPush()) {
                IntBuffer pWidth = stack.mallocInt(1);
                IntBuffer pHeight = stack.mallocInt(1);
                glfwGetWindowSize(window, pWidth, pHeight);
                GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
                assert vidmode != null;
                glfwSetWindowPos(
                        window,
                        (vidmode.width() - pWidth.get(0)) / 2,
                        (vidmode.height() - pHeight.get(0)) / 2
                );
            } catch (Exception ignored) {
            }
        }

        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    private void loop() {
        GameData gameData = new GameData();
        gameData.setVersion(gameVersion);

        ConfigJeu.get();
        ConfigVaisseau vaisseau = ConfigVaisseau.get();
        Camera camera = new Camera(new Vector3f(vaisseau.cameraSpawn));
        GameStateManager gsm;
        Commande commande = new Commande(camera, window, null);
        Commande commandeGlobal = new Commande(camera, window, null);
        touchesGlobal(commandeGlobal);

        gsm = new GameStateManager(commande, gameData, width, height);
        commande.setGameStateManager(gsm);

        gsm.setState(GameStateManager.GameStateEnum.MAIN);

        double lastTime = glfwGetTime();

        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            float deltaTime = (float)(currentTime - lastTime);
            lastTime = currentTime;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            commandeGlobal.update();
            gsm.update(deltaTime, width, height);
            gsm.render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        Text.cleanup();
    }

    /**
     * Registers global input bindings. Currently binds CAPS_LOCK to toggle
     * between cursor-disabled (locked) and cursor-normal (unlocked) modes.
     *
     * @param commandeGlobal the global command manager
     */
    public void touchesGlobal(Commande commandeGlobal) {
        ArrayList<Touche> touches = new ArrayList<>();

        touches.add(new Touche(GLFW_KEY_CAPS_LOCK,
                () -> {
                    if (mouseLocked) {
                        glfwSetInputMode(commandeGlobal.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                        mouseLocked = false;
                    } else {
                        glfwSetInputMode(commandeGlobal.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                        mouseLocked = true;
                    }
                },
                null, null
        ));
        commandeGlobal.setActiveAllTouche(true, touches);
        commandeGlobal.setTouches(touches);
    }

    /**
     * Returns the current game version string.
     *
     * @return the version identifier
     */
    public static String getGameVersion() {
        return gameVersion;
    }

    private void majDerniereVersionDepuisFichier(String nomFichier) {
        try {
            if (nomFichier == null || nomFichier.isBlank()) {
                System.err.println("Nom de fichier invalide.");
                return;
            }

            Path chemin = Path.of(nomFichier);
            if (!Files.exists(chemin)) {
                System.err.println("Le fichier '" + nomFichier + "' n'existe pas.");
                return;
            }

            String contenu = Files.readString(chemin);

            Pattern pattern = Pattern.compile("\\[(.*?)]");
            Matcher matcher = pattern.matcher(contenu);

            List<String> versions = new ArrayList<>();
            while (matcher.find()) {
                versions.add(matcher.group(1));
            }

            if (versions.isEmpty()) {
                System.err.println("Aucune version trouvée dans le fichier : " + nomFichier);
                return;
            }

            gameVersion = versions.getLast();

        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
        }
    }

    /**
     * Program entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        new SpaceShooter().run();
    }
}
