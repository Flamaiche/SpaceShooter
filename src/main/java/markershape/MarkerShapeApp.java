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

import markershape.ui.EditorUI;
import markershape.ui.MenuUI;

public class MarkerShapeApp {
    private long window;
    private int width = 1200, height = 800;
    private String currentFile;

    private EditorCamera camera;
    private ShapeRenderer renderer;
    private EditorUI editorUI;
    private MenuUI menuUI;

    private boolean dragging = false;
    private double lastMX, lastMY;

    private enum Mode { MENU, EDITOR }
    private Mode mode = Mode.MENU;

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

        window = glfwCreateWindow(width, height, "MarkerShape", NULL, NULL);
        if (window == NULL) throw new RuntimeException("glfwCreateWindow failed");

        glfwSetFramebufferSizeCallback(window, (_, newW, newH) -> {
            width = newW;
            height = newH;
            glViewport(0, 0, newW, newH);
            if (camera != null) camera.setSize(newW, newH);
            if (editorUI != null) editorUI.setSize(newW, newH);
            if (menuUI != null) menuUI.setSize(newW, newH);
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
        menuUI = new MenuUI(width, height);
        editorUI = new EditorUI(width, height);
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
            if (mode == Mode.EDITOR && camera != null)
                camera.zoom((float) yo * 0.5f);
        });

        glfwSetMouseButtonCallback(window, (_, btn, action, _) -> {
            if (btn != GLFW_MOUSE_BUTTON_LEFT) return;
            double[] mx = new double[1], my = new double[1];
            glfwGetCursorPos(window, mx, my);
            float fx = (float) mx[0], fy = (float) my[0];

            if (action == GLFW_PRESS) {
                if (mode == Mode.MENU) {
                    handleMenuClick(fx, fy);
                } else {
                    if (editorUI.isQuitClicked(fx, fy)) {
                        goToMenu();
                    } else if (editorUI.isSaveClicked(fx, fy)) {
                        saveCurrent();
                    } else {
                        dragging = true;
                        lastMX = mx[0];
                        lastMY = my[0];
                    }
                }
            } else {
                dragging = false;
            }
        });

        glfwSetCursorPosCallback(window, (_, x, y) -> {
            if (dragging && mode == Mode.EDITOR) {
                double dx = x - lastMX;
                double dy = y - lastMY;
                if (camera != null) camera.rotate((float) -dx * 0.3f, (float) dy * 0.3f);
                lastMX = x;
                lastMY = y;
            }
        });

        glfwSetKeyCallback(window, (_, key, _, action, mods) -> {
            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_ESCAPE) {
                    if (mode == Mode.EDITOR) goToMenu();
                    else glfwSetWindowShouldClose(window, true);
                }
                if (key == GLFW_KEY_S && (mods & GLFW_MOD_CONTROL) != 0) saveCurrent();
            }
        });

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (mode == Mode.MENU) {
                menuUI.render();
            } else {
                renderer.render(camera.getViewMatrix(), camera.getProjection());
                editorUI.render(currentFile);
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void handleMenuClick(float mx, float my) {
        if (menuUI.isQuitterClicked(mx, my)) {
            glfwSetWindowShouldClose(window, true);
            return;
        }
        if (menuUI.isParametresClicked(mx, my)) {
            System.out.println("[MarkerShape] Parametres (not implemented yet)");
            return;
        }
        String sel = menuUI.clickShape(mx, my);
        if (sel != null) openShape(sel);
    }

    private void openShape(String filename) {
        renderer.cleanup();
        boolean ok = renderer.loadShape(filename);
        if (ok) {
            currentFile = filename;
            mode = Mode.EDITOR;
            System.out.println("[MarkerShape] opened: " + filename);
            glfwSetWindowTitle(window, "MarkerShape - " + filename);
        } else {
            System.out.println("[MarkerShape] FAILED: " + filename);
        }
    }

    private void goToMenu() {
        renderer.cleanup();
        currentFile = null;
        mode = Mode.MENU;
        glfwSetWindowTitle(window, "MarkerShape");
        menuUI.refresh();
    }

    private void saveCurrent() {
        if (currentFile == null || !renderer.hasShape()) return;
        System.out.println("[MarkerShape] Save: " + currentFile + " (stub)");
    }

    private void cleanup() {
        renderer.cleanup();
        editorUI.cleanup();
        menuUI.cleanup();
        gamegl.gestion.texte.Text.cleanup();
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        GLFWErrorCallback.createPrint(System.err).close();
    }
}
