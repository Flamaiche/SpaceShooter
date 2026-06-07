package markershape;

import gamegl.gestion.texte.Text;
import markershape.editor.Editor;
import markershape.editor.ui.menu.MenuUI;
import org.joml.Matrix4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class App {
    private long window;
    private Editor editor;
    private int width, height;
    private MenuUI menuUI;
    private boolean inMenu;
    private boolean camDragging;
    private double camLastX, camLastY;
    private float mouseX, mouseY;
    private boolean camButtonDown;
    private final Set<Integer> pressedKeys = new HashSet<>();

    public static void main(String[] args) {
        new App().start();
    }

    private void start() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit()) {
            System.err.println("Failed to init GLFW");
            System.exit(-1);
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        width = 1280;
        height = 720;
        window = glfwCreateWindow(width, height, "MarkerShape Editor", 0, 0);
        if (window == 0) {
            System.err.println("Failed to create window");
            glfwTerminate();
            System.exit(-1);
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(1);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) pressedKeys.add(key);
            else if (action == GLFW_RELEASE) pressedKeys.remove(key);
            if (editor != null) editor.handleKey(key, scancode, action, mods);
        });
        glfwSetFramebufferSizeCallback(window, (w, w2, h2) -> {
            width = w2;
            height = h2;
            glViewport(0, 0, width, height);
            if (editor != null) editor.setSize(width, height);
            if (menuUI != null) menuUI.setSize(width, height);
        });

        glfwSetScrollCallback(window, (w, xo, yo) -> {
            if (!inMenu && editor != null) editor.camera.zoom((float) yo * 0.5f);
        });

        glfwSetCursorPosCallback(window, (w, x, y) -> {
            mouseX = (float) x;
            mouseY = (float) y;
            if (inMenu || editor == null) return;
            if (camButtonDown) {
                if (camDragging) {
                    float dx = (float) (x - camLastX);
                    float dy = (float) (y - camLastY);
                    editor.camera.rotate(-dx * 0.3f, dy * 0.3f);
                }
                camLastX = x;
                camLastY = y;
                camDragging = true;
            } else {
                camDragging = false;
            }
        });

        glfwSetMouseButtonCallback(window, (w, btn, action, mods) -> {
            if (btn == GLFW_MOUSE_BUTTON_MIDDLE || btn == GLFW_MOUSE_BUTTON_RIGHT) {
                camButtonDown = action == GLFW_PRESS;
                return;
            }
            if (btn != GLFW_MOUSE_BUTTON_LEFT || action != GLFW_PRESS) return;
            if (inMenu) {
                String clicked = menuUI.clickShape(mouseX, mouseY);
                if (clicked != null) {
                    editor.currentFile = clicked;
                    editor.loadShape(clicked);
                    editor.setSize(width, height);
                    editor.camera.setSize(width, height);
                    inMenu = false;
                    return;
                }
                if (menuUI.isQuitterClicked(mouseX, mouseY)) {
                    glfwSetWindowShouldClose(window, true);
                    return;
                }
            } else if (editor != null) {
                editor.onMouseButton(btn, action, mouseX, mouseY);
            }
        });

        System.out.println("LWJGL " + Version.getVersion());

        editor = new Editor(window, width, height);
        editor.camera.setSize(width, height);
        editor.ctx.onGoToMenu = () -> { editor.goToMenu(); inMenu = true; };

        menuUI = new MenuUI(width, height, () -> glfwSetWindowShouldClose(window, true));
        editor.menuUI = menuUI;
        inMenu = true;
    }

    private void loop() {
        Matrix4f view = new Matrix4f();
        Matrix4f projection = new Matrix4f();

        while (!glfwWindowShouldClose(window)) {
            glClearColor(0.1f, 0.1f, 0.12f, 1f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (inMenu) {
                menuUI.render();
            } else {
                view.set(editor.camera.getViewMatrix());
                projection.set(editor.camera.getProjection());
                editor.render(view, projection);
                editor.processInput(mouseX, mouseY);
                for (int k : pressedKeys) {
                    switch (k) {
                        case GLFW_KEY_UP    -> editor.camera.rotate(0f, 2f);
                        case GLFW_KEY_DOWN  -> editor.camera.rotate(0f, -2f);
                        case GLFW_KEY_LEFT  -> editor.camera.rotate(2f, 0f);
                        case GLFW_KEY_RIGHT -> editor.camera.rotate(-2f, 0f);
                        case GLFW_KEY_O     -> editor.camera.zoom(0.05f);
                        case GLFW_KEY_P     -> editor.camera.zoom(-0.05f);
                    }
                }
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void cleanup() {
        if (editor != null) editor.cleanup();
        if (menuUI != null) menuUI.cleanup();
        Text.cleanup();
        glfwDestroyWindow(window);
        glfwTerminate();
        GLFWErrorCallback.createPrint(System.err).close();
    }
}
