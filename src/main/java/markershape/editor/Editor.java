package markershape.editor;

import learngl.LogFile;
import markershape.camera.EditorCamera;
import markershape.editor.action.*;
import markershape.editor.input.*;
import markershape.shape.*;
import markershape.shape.render.ShapeRenderer;
import markershape.editor.ui.EditorUI;
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

    public Editor(long window, int w, int h) {
        this.window = window;
        this.width = w;
        this.height = h;

        camera = new EditorCamera();
        renderer = new ShapeRenderer();
        renderer.setScreenSize(w, h);
        PickUtils pick = new PickUtils();
        pick.setRenderer(renderer);
        pick.setCamera(camera);
        pick.setSize(w, h);

        VertexOverlay vertexOverlay = new VertexOverlay();
        EdgeOverlay edgeOverlay = new EdgeOverlay();
        SiblingPicker siblingPicker = new SiblingPicker();
        SelectionManager selection = new SelectionManager(vertexOverlay, edgeOverlay, siblingPicker);
        selection.setRenderer(renderer);

        UndoRedo undoredo = new UndoRedo();

        ctx = new Context(renderer, null, undoredo, selection, pick);
        ctx.window = window;
        ctx.windowWidth = w;
        ctx.windowHeight = h;

        io = new ShapeIO(ctx);

        editorUI = new EditorUI(w, h,
            () -> io.save(),
            () -> org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose(window, true),
            () -> { ctx.creatingEdge = true; ctx.creatingVertex = false; ctx.edgeFirstVertex = -1; editorUI.setActiveMode(1); editorUI.closeNewMenu(); selection.hideOverlays(); selection.selectedVertex = -1; selection.selectedEdge = -1; },
            () -> { ctx.creatingVertex = true; ctx.creatingEdge = false; ctx.edgeFirstVertex = -1; editorUI.setActiveMode(0); editorUI.closeNewMenu(); selection.hideOverlays(); selection.selectedVertex = -1; selection.selectedEdge = -1; });
        editorUI.setFilterCallback(this::applyFilterSettings);
        ctx.ui = editorUI;

        faceUtils = new FaceUtils();
        edge = new EdgeAction(ctx, faceUtils);
        vertex = new VertexAction(ctx);
        del = new DeleteAction(ctx, faceUtils);
        hover = new HoverManager(ctx);
        input = new InputManager(ctx, hover, vertex, edge, del, io);

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
    }

    public void render(Matrix4f view, Matrix4f projection) {
        renderer.render(view, projection);

        editorUI.entityList.setData(ctx.renderer.getShapeData());
        editorUI.render(currentFile);

        if (ctx.selection.vertexOverlay.isVisible()) {
            ctx.selection.vertexOverlay.render(editorUI.shader(), editorUI.textShader(),
                editorUI.ortho(), editorUI.buf(), editorUI.vao(), editorUI.vbo());
        }

        if (ctx.selection.edgeOverlay.isVisible()) {
            ctx.selection.edgeOverlay.render(editorUI.shader(), editorUI.textShader(),
                editorUI.ortho(), editorUI.buf(), editorUI.vao(), editorUI.vbo());
        }

        if (ctx.selection.siblingPicker.isVisible()) {
            ctx.selection.siblingPicker.render(editorUI.shader(), editorUI.textShader(),
                editorUI.ortho(), editorUI.buf(), editorUI.vao(), editorUI.vbo());
        }

        editorUI.renderEntityList(width, height);
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

    public void onMouseButton(int btn, int action, float mx, float my) {
        input.onMouseButton(btn, action, mx, my);
    }

    public void handleKey(int key, int scancode, int action, int mods) {
        input.handleKey(key, scancode, action, mods);
    }

    public void cleanup() {
        renderer.cleanup();
        editorUI.cleanup();
        if (menuUI != null) menuUI.cleanup();
        gamegl.gestion.texte.Text.cleanup();
    }
}
