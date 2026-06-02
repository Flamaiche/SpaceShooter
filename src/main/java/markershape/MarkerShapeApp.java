package markershape;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class MarkerShapeApp {
    private long window;
    private int width = 1200, height = 800;
    private String currentFile;

    private EditorCamera camera;
    private ShapeRenderer renderer;
    private EditorUI ui;

    private boolean dragging = false;
    private double lastMX, lastMY;

    public static void main(String[] args) {
        new MarkerShapeApp().run();
    }

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("glfwInit failed");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(width, height, "MarkerShape Editor", NULL, NULL);
        if (window == NULL) throw new RuntimeException("glfwCreateWindow failed");

        glfwSetFramebufferSizeCallback(window, (_, newW, newH) -> {
            width = newW;
            height = newH;
            glViewport(0, 0, newW, newH);
            if (camera != null) camera.setSize(newW, newH);
            if (ui != null) ui.setSize(newW, newH);
        });

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glViewport(0, 0, width, height);
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.12f, 0.12f, 0.18f, 1.0f);
        glfwSwapInterval(1);

        centerWindow();
        glfwShowWindow(window);

        camera = new EditorCamera();
        camera.setSize(width, height);
        ui = new EditorUI(width, height);
        renderer = new ShapeRenderer();
    }

    private void centerWindow() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);

            IntBuffer pX = stack.mallocInt(1);
            IntBuffer pY = stack.mallocInt(1);
            glfwGetMonitorPos(glfwGetPrimaryMonitor(), pX, pY);

            GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vid != null) {
                glfwSetWindowPos(window,
                    pX.get(0) + (vid.width() - pWidth.get(0)) / 2,
                    pY.get(0) + (vid.height() - pHeight.get(0)) / 2);
            }
        }
    }

    private void loop() {
        glfwSetScrollCallback(window, (_, _, yo) -> {
            if (camera != null) camera.zoom((float) yo * 0.5f);
        });
        glfwSetMouseButtonCallback(window, (_, btn, action, _) -> {
            if (btn == GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW_PRESS) {
                    double[] mx = new double[1], my = new double[1];
                    glfwGetCursorPos(window, mx, my);
                    if (my[0] > 40) {
                        dragging = true;
                        lastMX = mx[0];
                        lastMY = my[0];
                    }
                } else {
                    dragging = false;
                }
            }
        });
        glfwSetCursorPosCallback(window, (_, x, y) -> {
            if (dragging) {
                double dx = x - lastMX;
                double dy = y - lastMY;
                if (camera != null) camera.rotate((float) -dx * 0.3f, (float) dy * 0.3f);
                lastMX = x;
                lastMY = y;
            }
        });
        glfwSetKeyCallback(window, (_, key, _, action, mods) -> {
            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_ESCAPE) glfwSetWindowShouldClose(window, true);
                if (key == GLFW_KEY_S && (mods & GLFW_MOD_CONTROL) != 0) saveCurrent();
            }
        });

        loadShape("cube.json");

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            renderer.render(camera.getViewMatrix(), camera.getProjection());
            ui.render();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void loadShape(String filename) {
        currentFile = filename;
        boolean ok = renderer.loadShape(filename);
        if (ok) {
            System.out.println("[MarkerShape] loaded: " + filename);
            glfwSetWindowTitle(window, "MarkerShape - " + filename);
        } else {
            System.out.println("[MarkerShape] FAILED to load: " + filename);
            glfwSetWindowTitle(window, "MarkerShape - [no shape]");
        }
    }

    private void saveCurrent() {
        if (currentFile == null || !renderer.hasShape()) return;
        System.out.println("[MarkerShape] Save: " + currentFile + " (stub)");
    }

    private void cleanup() {
        renderer.cleanup();
        ui.cleanup();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        GLFWErrorCallback.createPrint(System.err).close();
    }
}
