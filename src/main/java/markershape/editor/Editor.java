package markershape.editor;

import learngl.LogFile;
import markershape.camera.EditorCamera;
import markershape.editor.action.*;
import markershape.editor.input.*;
import markershape.shape.*;
import markershape.shape.render.ShapeRenderer;
import markershape.editor.ui.EditorUI;
import markershape.editor.ui.UIResources;
import markershape.editor.ui.menu.MenuUI;
import markershape.editor.ui.overlay.EdgeOverlay;
import markershape.editor.ui.overlay.SiblingPicker;
import markershape.editor.ui.overlay.VertexOverlay;
import org.joml.Matrix4f;

public class Editor {
    public EditorCamera camera;
    public ShapeRenderer renderer;
    public EditorUI editorUI;
    public MenuUI menuUI;

    public final Context ctx;
    public final FaceUtils faceUtils;
    public final EdgeAction edge;
    public final VertexAction vertex;
    public final DeleteAction del;
    public final ShapeIO io;
    public final HoverManager hover;
    public final InputManager input;

    public int width, height;
    public long window;
    public String currentFile;
    private final UIResources uiResources;

    public Editor(long window, int w, int h, UIResources uiResources) {
        this.window = window;
        this.width = w;
        this.height = h;
        this.uiResources = uiResources;

        camera = new EditorCamera();
        renderer = new ShapeRenderer();
        renderer.setScreenSize(w, h);
        PickUtils pick = new PickUtils();
        pick.setRenderer(renderer);
        pick.setCamera(camera);
        pick.setSize(w, h);

        VertexOverlay vertexOverlay = new VertexOverlay(uiResources);
        EdgeOverlay edgeOverlay = new EdgeOverlay(uiResources);
        SiblingPicker siblingPicker = new SiblingPicker(uiResources);
        SelectionManager selection = new SelectionManager(vertexOverlay, edgeOverlay, siblingPicker);
        selection.setRenderer(renderer);

        UndoRedo undoredo = new UndoRedo();

        ctx = new Context(renderer, null, undoredo, selection, pick);
        ctx.window = window;
        ctx.windowWidth = w;
        ctx.windowHeight = h;

        io = new ShapeIO(ctx);

        editorUI = new EditorUI(uiResources, w, h,
            () -> io.save(),
            () -> org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose(window, true));
        editorUI.setFilterCallback(this::applyFilterSettings);
        ctx.ui = editorUI;

        faceUtils = new FaceUtils();
        edge = new EdgeAction(ctx, faceUtils);
        vertex = new VertexAction(ctx);
        del = new DeleteAction(ctx, faceUtils);
        hover = new HoverManager(ctx);
        input = new InputManager(ctx, hover, vertex, edge, del, io, camera);

        vertexOverlay.setPreEditCallback(() -> ctx.undoredo.snapshot(renderer.getShapeData()));
        edgeOverlay.setPreEditCallback(() -> ctx.undoredo.snapshot(renderer.getShapeData()));
        vertexOverlay.setEditCallback(() -> {
            renderer.rebuild();
            if (selection.selectedVertex >= 0) {
                Vertex v = renderer.getShapeData().vertices.get(selection.selectedVertex);
                if (v != null) {
                    selection.crosshairPos.set(v.x, v.y, v.z);
                    selection.crosshairValid = true;
                }
            }
        });
        vertexOverlay.setDeleteCallback(() -> del.deleteVertexFromOverlay());
        edgeOverlay.setEditCallback(() -> renderer.rebuild());
        edgeOverlay.setDeleteCallback(() -> del.deleteEdgeFromOverlay());
    }

    private void applyFilterSettings() {
        boolean[] fv = editorUI.getFilterValues();
        float[] sv = editorUI.getSliderValues();
        renderer.setShowFaces(fv[0]);
        renderer.setShowEdges(fv[1]);
        renderer.setShowPoints(fv[2]);
        renderer.setShowAxisX(fv[3]);
        renderer.setShowAxisY(fv[4]);
        renderer.setShowAxisZ(fv[5]);
        renderer.setPointSize(sv[0]);
        renderer.setLineWidth(sv[1]);
        renderer.setFaceAlpha(sv[2]);
        renderer.setGridStep(sv[3]);
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
        ctx.pick.setSize(w, h);
        renderer.setScreenSize(w, h);
        editorUI.setSize(w, h);
        if (menuUI != null) menuUI.setSize(w, h);
        ctx.windowWidth = w;
        ctx.windowHeight = h;
    }

    public void loadShape(String filename) {
        ctx.currentFilename = filename;
        ctx.exitModes();
        ctx.selection.reset();
        ctx.hoveredVertexId = -1;
        ctx.hoveredEdgeId = -1;
        ctx.hoveredPositionIds = new java.util.HashSet<>();
        renderer.setHoveredVertex(-1);
        renderer.setHoveredEdge(-1);
        renderer.setHoveredPositionIds(ctx.hoveredPositionIds);
        renderer.loadShape(filename);
        boolean[] fv = editorUI.getFilterValues();
        float[] sv = editorUI.getSliderValues();
        renderer.setShowFaces(fv[0]);
        renderer.setShowEdges(fv[1]);
        renderer.setShowPoints(fv[2]);
        renderer.setShowAxisX(fv[3]);
        renderer.setShowAxisY(fv[4]);
        renderer.setShowAxisZ(fv[5]);
        renderer.setPointSize(sv[0]);
        renderer.setLineWidth(sv[1]);
        renderer.setFaceAlpha(sv[2]);
        renderer.setGridStep(sv[3]);
        frameToShape();
    }

    /**
     * Frames the camera onto the loaded shape (bounding box center + size),
     * preserving the current orientation.
     */
    public void frameToShape() {
        if (renderer.getShapeData() == null || renderer.getShapeData().vertices.isEmpty()) return;
        float[] bb = bounds(renderer.getShapeData());
        org.joml.Vector3f center = new org.joml.Vector3f(
            (bb[0] + bb[3]) / 2f, (bb[1] + bb[4]) / 2f, (bb[2] + bb[5]) / 2f);
        float size = Math.max(bb[3] - bb[0], Math.max(bb[4] - bb[1], bb[5] - bb[2]));
        camera.frame(center, size);
    }

private float[] bounds(markershape.shape.ShapeData data) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        for (markershape.shape.Vertex v : data.vertices.values()) {
            minX = Math.min(minX, v.x); maxX = Math.max(maxX, v.x);
            minY = Math.min(minY, v.y); maxY = Math.max(maxY, v.y);
            minZ = Math.min(minZ, v.z); maxZ = Math.max(maxZ, v.z);
        }
        return new float[]{minX, minY, minZ, maxX, maxY, maxZ};
    }

    private void updateFrontArrow() {
        org.joml.Vector3f center = null;
        float size = 0f;
        if (renderer.getShapeData() != null && !renderer.getShapeData().vertices.isEmpty()) {
            float[] bb = bounds(renderer.getShapeData());
            center = new org.joml.Vector3f(
                (bb[0] + bb[3]) / 2f, (bb[1] + bb[4]) / 2f, (bb[2] + bb[5]) / 2f);
            size = Math.max(bb[3] - bb[0], Math.max(bb[4] - bb[1], bb[5] - bb[2]));
        }
        boolean show = center != null;
        float arrowLen = show ? Math.max(size * 0.35f, 0.5f) : 1f;
        renderer.setFrontArrow(show, center == null ? new org.joml.Vector3f() : center,
            camera.getFrontDirection(), arrowLen);
    }

    public void render(Matrix4f view, Matrix4f projection) {
        updateFrontArrow();
        renderer.render(view, projection);

        editorUI.entityList.setData(ctx.renderer.getShapeData());
        editorUI.render(currentFile);

        if (ctx.selection.vertexOverlay.isVisible()) {
            ctx.selection.vertexOverlay.render();
        }

        if (ctx.selection.edgeOverlay.isVisible()) {
            ctx.selection.edgeOverlay.render();
        }

        if (ctx.selection.siblingPicker.isVisible()) {
            ctx.selection.siblingPicker.render();
        }

        editorUI.renderEntityList();
    }

    public void goToMenu() {
        currentFile = null;
        ctx.currentFilename = null;
        ctx.exitModes();
        ctx.selection.reset();
        ctx.hoveredVertexId = -1;
        ctx.hoveredEdgeId = -1;
        ctx.selection.hideOverlays();
        ctx.ui.closeNewMenu();
        ctx.ui.closeConfirmSave();
        ctx.ui.setActiveMode(-1);
        if (menuUI != null) menuUI.refresh();
    }

    public void processInput(float mx, float my) {
        input.process(mx, my);
    }

    public void setKeyState(int key, int action) {
        input.setKeyState(key, action);
    }

    public boolean isTyping() {
        return ctx.selection.vertexOverlay.isTyping() || ctx.selection.edgeOverlay.isTyping();
    }

    public void handleChar(int codepoint) {
        if (ctx.selection.vertexOverlay.isTyping()) ctx.selection.vertexOverlay.charTyped(codepoint);
        if (ctx.selection.edgeOverlay.isTyping()) ctx.selection.edgeOverlay.charTyped(codepoint);
    }

    public void processKeys() {
        input.processFrameKeys();
    }

    public void onMouseButton(int btn, int action, float mx, float my) {
        input.onMouseButton(btn, action, mx, my);
    }

    public void handleKey(int key, int scancode, int action, int mods) {
        if (ctx.selection.vertexOverlay.isTyping()) {
            ctx.selection.vertexOverlay.keyTyped(key, action);
            return;
        }
        if (ctx.selection.edgeOverlay.isTyping()) {
            ctx.selection.edgeOverlay.keyTyped(key, action);
            return;
        }
        input.handleKey(key, scancode, action, mods);
    }

    public void cleanup() {
        renderer.cleanup();
        gamegl.gestion.texte.Text.cleanup();
    }
}
