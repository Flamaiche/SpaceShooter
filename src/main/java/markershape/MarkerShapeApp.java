package markershape;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import learngl.LogFile;
import markershape.camera.EditorCamera;
import markershape.model.*;
import markershape.render.ShapeRenderer;
import markershape.ui.EdgeOverlay;
import markershape.ui.EditorUI;
import markershape.ui.MenuUI;
import markershape.ui.VertexOverlay;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Standalone editor application for 3D marker shapes.
 * Provides a menu to select a shape and an editor view with an orbital camera
 * and an overlay UI for saving and navigation.
 */
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
    private int hoveredVertex = -1, selectedVertex = -1;
    private int hoveredEdge = -1, selectedEdge = -1;
    private VertexOverlay vertexOverlay;
    private EdgeOverlay edgeOverlay;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private int frameCount;

    private enum Mode { MENU, EDITOR }
    private Mode mode = Mode.MENU;

    /** Program entry point. */
    public static void main(String[] args) {
        new MarkerShapeApp().run();
    }

    /** Runs the application lifecycle: init, loop, cleanup. */
    public void run() {
        init();
        loop();
        cleanup();
    }

    /** Initialises GLFW, creates the window, and sets up subsystems. */
    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("glfwInit failed");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

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

        if (System.getenv("WAYLAND_DISPLAY") == null) centerWindow();
        glfwShowWindow(window);

        LogFile.init();
        LogFile.log("[MarkerShape] init complete, window=" + width + "x" + height);

        camera = new EditorCamera();
        camera.setSize(width, height);
        menuUI = new MenuUI(width, height, () -> glfwSetWindowShouldClose(window, true));
        editorUI = new EditorUI(width, height, this::saveCurrent, this::goToMenu);
        renderer = new ShapeRenderer();
        vertexOverlay = new VertexOverlay();
        edgeOverlay = new EdgeOverlay();
    }

    /** Centres the window on the primary monitor. */
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

    /** Main loop: processes input and renders the current mode. */
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
                    if (vertexOverlay.isCloseClicked(fx, fy)) {
                        selectedVertex = -1;
                        vertexOverlay.hide();
                    } else if (edgeOverlay.isCloseClicked(fx, fy)) {
                        selectedEdge = -1;
                        edgeOverlay.hide();
                    } else if (!editorUI.isQuitClicked(fx, fy) && !editorUI.isSaveClicked(fx, fy)) {
                        if (hoveredVertex >= 0) {
                            selectVertex(hoveredVertex);
                            edgeOverlay.hide();
                            selectedEdge = -1;
                        } else if (hoveredEdge >= 0) {
                            selectEdge(hoveredEdge);
                            vertexOverlay.hide();
                            selectedVertex = -1;
                        } else {
                            selectedVertex = -1;
                            vertexOverlay.hide();
                            selectedEdge = -1;
                            edgeOverlay.hide();
                            dragging = true;
                            lastMX = mx[0];
                            lastMY = my[0];
                        }
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
            } else if (mode == Mode.EDITOR && !editorUI.isOverUI((float) x, (float) y)) {
                hoveredVertex = pickVertex((float) x, (float) y);
                if (hoveredVertex < 0) {
                    hoveredEdge = pickEdge((float) x, (float) y);
                } else {
                    hoveredEdge = -1;
                }
            }
        });

        glfwSetKeyCallback(window, (_, key, _, action, mods) -> {
            if (action == GLFW_PRESS) {
                pressedKeys.add(key);
                if (key == GLFW_KEY_ESCAPE) {
                    if (mode == Mode.EDITOR) goToMenu();
                    else glfwSetWindowShouldClose(window, true);
                }
                if (key == GLFW_KEY_S && (mods & GLFW_MOD_CONTROL) != 0) saveCurrent();
            } else if (action == GLFW_RELEASE) {
                pressedKeys.remove(key);
            }
        });

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (mode == Mode.MENU) {
                menuUI.render();
            } else {
                handleKeyboard();

                if (frameCount % 60 == 0) {
                    LogFile.logf("[Frame %d] camera pos=(%.2f,%.2f,%.2f) yaw=%.1f pitch=%.1f radius=%.2f",
                        frameCount,
                        camera.getPosition().x, camera.getPosition().y, camera.getPosition().z,
                        camera.getYaw(), camera.getPitch(), camera.getRadius());
                }
                frameCount++;

                renderer.setHoveredVertex(hoveredVertex);
                renderer.setHoveredEdge(hoveredEdge);
                renderer.render(camera.getViewMatrix(), camera.getProjection());
                editorUI.render(currentFile);
                if (vertexOverlay.isVisible()) {
                    vertexOverlay.render(editorUI.shader(), editorUI.textShader(),
                        editorUI.ortho(), editorUI.buf(), editorUI.vao(), editorUI.vbo());
                }
                if (edgeOverlay.isVisible()) {
                    edgeOverlay.render(editorUI.shader(), editorUI.textShader(),
                        editorUI.ortho(), editorUI.buf(), editorUI.vao(), editorUI.vbo());
                }
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void handleKeyboard() {
        if (camera == null || mode != Mode.EDITOR) return;
        for (int k : pressedKeys) {
            switch (k) {
                case GLFW_KEY_UP    -> camera.rotate(0f, 2f);
                case GLFW_KEY_DOWN  -> camera.rotate(0f, -2f);
                case GLFW_KEY_LEFT  -> camera.rotate(2f, 0f);
                case GLFW_KEY_RIGHT -> camera.rotate(-2f, 0f);
                case GLFW_KEY_P     -> camera.zoom(0.05f);
                case GLFW_KEY_O     -> camera.zoom(-0.05f);
            }
        }
    }

    /** Routes menu clicks to the appropriate action. */
    private void handleMenuClick(float mx, float my) {
        if (menuUI.isQuitterClicked(mx, my)) return;
        if (menuUI.isParametresClicked(mx, my)) return;
        String sel = menuUI.clickShape(mx, my);
        if (sel != null) openShape(sel);
    }

    /** Opens the given shape file and switches to editor mode. */
    private void openShape(String filename) {
        renderer.cleanup();
        boolean ok = renderer.loadShape(filename);
        if (ok) {
            currentFile = filename;
            mode = Mode.EDITOR;
            ShapeData data = renderer.getShapeData();
            if (data != null) {
                LogFile.logf("[MarkerShape] opened: %s | vertices=%d faces=%d edges=%d",
                    filename, data.vertices.size(), data.faces.size(), data.edges.size());
            }
            LogFile.logf("[MarkerShape] camera pos=(%.2f,%.2f,%.2f) yaw=%.1f pitch=%.1f radius=%.2f",
                camera.getPosition().x, camera.getPosition().y, camera.getPosition().z,
                camera.getYaw(), camera.getPitch(), camera.getRadius());
            glfwSetWindowTitle(window, "MarkerShape - " + filename);
        } else {
            System.out.println("[MarkerShape] FAILED: " + filename);
        }
    }

    /** Returns to the menu, cleaning up the current shape. */
    private void goToMenu() {
        renderer.cleanup();
        currentFile = null;
        mode = Mode.MENU;
        hoveredVertex = -1;
        selectedVertex = -1;
        hoveredEdge = -1;
        selectedEdge = -1;
        vertexOverlay.hide();
        edgeOverlay.hide();
        glfwSetWindowTitle(window, "MarkerShape");
        menuUI.refresh();
    }

    /** Picks the vertex under the mouse cursor using depth-buffer-aware picking. */
    private int pickVertex(float mx, float my) {
        if (!renderer.hasShape()) return -1;
        ShapeData data = renderer.getShapeData();
        if (data == null || data.vertices.isEmpty()) return -1;

        FloatBuffer depthBuf = BufferUtils.createFloatBuffer(1);
        glReadPixels((int)mx, height - (int)my - 1, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, depthBuf);
        float depth = depthBuf.get(0);
        if (depth >= 1.0f) return -1;

        Matrix4f invProjView = new Matrix4f(camera.getProjection());
        invProjView.mul(camera.getViewMatrix());
        invProjView.invert();

        float ndcX = (2f * mx) / width - 1f;
        float ndcY = 1f - (2f * my) / height;
        float ndcZ = depth * 2f - 1f;

        Vector4f worldP = new Vector4f(ndcX, ndcY, ndcZ, 1f).mul(invProjView);
        worldP.div(worldP.w);

        Vector3f worldPos = new Vector3f(worldP.x, worldP.y, worldP.z);
        float bestDist = 0.06f;
        int bestId = -1;

        for (Vertex v : data.vertices.values()) {
            float d = worldPos.distance(v.x, v.y, v.z);
            if (d < bestDist) {
                bestDist = d;
                bestId = v.id;
            }
        }
        return bestId;
    }

    private void selectVertex(int id) {
        ShapeData data = renderer.getShapeData();
        if (data == null) return;
        Vertex v = data.vertices.get(id);
        if (v == null) return;
        selectedVertex = id;
        int edgeCount = v.edgeIds.size();
        vertexOverlay.show(v, edgeCount);
        vertexOverlay.setPosition(width - 310, 50);
    }

    private void selectEdge(int id) {
        ShapeData data = renderer.getShapeData();
        if (data == null) return;
        Edge e = data.edges.get(id);
        if (e == null) return;
        selectedEdge = id;
        edgeOverlay.show(e, e.a, e.b);
        edgeOverlay.setPosition(width - 310, 50);
    }

    private float pointToSegDist(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay;
        float lenSq = dx * dx + dy * dy;
        if (lenSq < 1e-8f) return (float) Math.hypot(px - ax, py - ay);
        float t = ((px - ax) * dx + (py - ay) * dy) / lenSq;
        t = Math.max(0f, Math.min(1f, t));
        return (float) Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    private int pickEdge(float mx, float my) {
        if (!renderer.hasShape()) return -1;
        ShapeData data = renderer.getShapeData();
        if (data == null || data.edges.isEmpty()) return -1;

        Matrix4f mvp = new Matrix4f(camera.getProjection());
        mvp.mul(camera.getViewMatrix());

        float bestDist = 8f;
        int bestId = -1;
        Vector4f p = new Vector4f();

        for (Edge e : data.edges.values()) {
            Vertex va = data.vertices.get(e.a);
            Vertex vb = data.vertices.get(e.b);
            if (va == null || vb == null) continue;

            p.set(va.x, va.y, va.z, 1f).mul(mvp);
            float ax = (p.x / p.w * 0.5f + 0.5f) * width;
            float ay = (1f - (p.y / p.w * 0.5f + 0.5f)) * height;

            p.set(vb.x, vb.y, vb.z, 1f).mul(mvp);
            float bx = (p.x / p.w * 0.5f + 0.5f) * width;
            float by = (1f - (p.y / p.w * 0.5f + 0.5f)) * height;

            float d = pointToSegDist(mx, my, ax, ay, bx, by);
            if (d < bestDist) {
                bestDist = d;
                bestId = e.id;
            }
        }
        return bestId;
    }

    /** Saves the current shape (stub, not yet implemented). */
    private void saveCurrent() {
        if (currentFile == null || !renderer.hasShape()) return;
        System.out.println("[MarkerShape] Save: " + currentFile + " (stub)");
    }

    /** Cleans up all resources and terminates GLFW. */
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
