package markershape;

import gamegl.gestion.texte.Text;

import markershape.config.ConfigParametres;
import markershape.editor.Editor;
import markershape.editor.ui.menu.BlurBackground;
import markershape.editor.ui.menu.MenuUI;
import markershape.editor.ui.menu.ParametresUI;
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
    private ParametresUI parametresUI;
    private boolean inMenu;
    private float mouseX, mouseY;
    private float bgR = 0.1f, bgG = 0.1f, bgB = 0.12f;
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
            if (parametresUI != null && parametresUI.visible) {
                parametresUI.handleKey(key, action);
            } else if (editor != null) {
                editor.handleKey(key, scancode, action, mods);
            }
        });
        glfwSetCharCallback(window, (w, codepoint) -> {
            if (parametresUI != null && parametresUI.visible) {
                parametresUI.handleChar(codepoint);
            }
        });
        glfwSetFramebufferSizeCallback(window, (w, w2, h2) -> {
            width = w2;
            height = h2;
            glViewport(0, 0, width, height);
            if (editor != null) editor.setSize(width, height);
            if (menuUI != null) menuUI.setSize(width, height);
            if (parametresUI != null) parametresUI.setSize(width, height);
        });

        glfwSetScrollCallback(window, (w, xo, yo) -> {
            if (!inMenu && editor != null) {
                if (editor.editorUI.entityList.contains(mouseX, mouseY)) {
                    if (yo < 0) editor.editorUI.entityList.pageNext();
                    else editor.editorUI.entityList.pagePrev();
                } else {
                    editor.camera.zoom((float) yo);
                }
            }
        });

        glfwSetCursorPosCallback(window, (w, x, y) -> {
            mouseX = (float) x;
            mouseY = (float) y;
        });

        glfwSetMouseButtonCallback(window, (w, btn, action, mods) -> {
            if (btn != GLFW_MOUSE_BUTTON_LEFT) return;
            if (action != GLFW_PRESS) return;
            if (parametresUI != null && parametresUI.visible) {
                parametresUI.click(mouseX, mouseY);
            } else if (inMenu) {
                String clicked = menuUI.clickShape(mouseX, mouseY);
                if (clicked != null) {
                    editor.currentFile = clicked;
                    editor.loadShape(clicked);
                    applyConfig();
                    editor.setSize(width, height);
                    editor.camera.setSize(width, height);
                    inMenu = false;
                    return;
                }
                if (menuUI.isParametresClicked(mouseX, mouseY)) return;
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
        editor.ctx.onGoToMenu = () -> { editor.goToMenu(); inMenu = true; if (parametresUI != null) parametresUI.visible = false; };

        parametresUI = new ParametresUI(() -> { parametresUI.visible = false; });
        parametresUI.setOnApply(this::applyConfig);
        parametresUI.setSize(width, height);

        menuUI = new MenuUI(width, height, () -> glfwSetWindowShouldClose(window, true), () -> {
            parametresUI.loadFromConfig();
            parametresUI.visible = true;
        });
        editor.menuUI = menuUI;
        inMenu = true;
    }

    private void applyConfig() {
        ConfigParametres cfg = ConfigParametres.get();

        bgR = cfg.getFloat("bgR");
        bgG = cfg.getFloat("bgG");
        bgB = cfg.getFloat("bgB");

        if (editor != null) {
            editor.renderer.setPointSize(cfg.getFloat("pointSize"));
            editor.renderer.setLineWidth(cfg.getFloat("lineWidth"));
            editor.renderer.setFaceAlpha(cfg.getFloat("faceAlpha"));
            editor.renderer.setShowAxisX(cfg.getBool("axisX"));
            editor.renderer.setShowAxisY(cfg.getBool("axisY"));
            editor.renderer.setShowAxisZ(cfg.getBool("axisZ"));
            editor.renderer.setGridVisible(cfg.getBool("gridVisible"));

            editor.camera.setZoomSpeed(cfg.getFloat("zoomSpeed"));
            editor.camera.setOrbitSpeed(cfg.getFloat("orbitSpeed"));

            editor.editorUI.setSnapEnabled(cfg.getBool("snapEnabled"));
            editor.editorUI.setSnapStep(cfg.getFloat("snapStep"));
        }

        BlurBackground.transparentUI = cfg.getBool("transparentUI");
        BlurBackground.menuR = cfg.getFloat("menuR");
        BlurBackground.menuG = cfg.getFloat("menuG");
        BlurBackground.menuB = cfg.getFloat("menuB");

        if (editor != null) editor.editorUI.syncFromConfig();
    }

    private void loop() {
        Matrix4f view = new Matrix4f();
        Matrix4f projection = new Matrix4f();

        while (!glfwWindowShouldClose(window)) {
            glClearColor(bgR, bgG, bgB, 1f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (parametresUI != null && parametresUI.visible) {
                parametresUI.render();
            } else if (inMenu) {
                menuUI.render();
            } else {
                view.set(editor.camera.getViewMatrix());
                projection.set(editor.camera.getProjection());
                editor.render(view, projection);
                editor.processInput(mouseX, mouseY);
                for (int k : pressedKeys) {
                    switch (k) {
                        case GLFW_KEY_UP    -> editor.camera.rotate(0f, 1f);
                        case GLFW_KEY_DOWN  -> editor.camera.rotate(0f, -1f);
                        case GLFW_KEY_LEFT  -> editor.camera.rotate(1f, 0f);
                        case GLFW_KEY_RIGHT -> editor.camera.rotate(-1f, 0f);
                        case GLFW_KEY_O     -> editor.camera.zoom(1f);
                        case GLFW_KEY_P     -> editor.camera.zoom(-1f);
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
        if (parametresUI != null) parametresUI.cleanup();
        Text.cleanup();
        glfwDestroyWindow(window);
        glfwTerminate();
        GLFWErrorCallback.createPrint(System.err).close();
    }
}
