package gameGl;

import gameGl.gestion.texte.Text;
import gameGl.state.GameStateManager;
import learnGL.tools.*;
import learnGL.tools.commandes.Commande;
import learnGL.tools.commandes.Touche;
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
    private boolean mouseLocked = true;
    private static String gameVersion = "A1.0";
    public static String filenameSaveScore = "SauvegardeScore";

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        init();
        System.out.println("gameVersion : " + gameVersion);
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
        glViewport(0, 0, width, height);

        Camera camera = new Camera(new Vector3f(0, 0, 3));
        GameStateManager gsm = null;
        Commande commande = new Commande(camera, window, gsm);
        Commande commandeGlobal = new Commande(camera, window, gsm);
        touchesGlobal(commandeGlobal);

        gsm = new GameStateManager(commande, width, height);
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

    public void touchesGlobal(Commande commandeGlobal) {
        ArrayList<Touche> touches = new ArrayList<>();

        // CAPS_LOCK -> lock/unlock souris
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

    public static String getGameVersion() {
        return gameVersion;
    }

    public static void main(String[] args) {
        new SpaceShooter().run();
    }
}
