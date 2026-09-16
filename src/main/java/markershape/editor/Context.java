package markershape.editor;

import markershape.shape.render.ShapeRenderer;
import markershape.editor.ui.EditorUI;
import markershape.editor.ui.overlay.HelpOverlay;
import org.joml.Vector3f;

/** Shared mutable state for the editor: renderer, selection, picking, interaction modes, and undo/redo. */
public class Context {
    public final ShapeRenderer renderer;
    public EditorUI ui;
    public HelpOverlay help;
    public final UndoRedo undoredo;
    public final SelectionManager selection;
    public final PickUtils pick;

    public long window;
    public int windowWidth, windowHeight;

    public int hoveredVertexId = -1;
    public int hoveredEdgeId = -1;
    public java.util.Set<Integer> hoveredPositionIds = new java.util.HashSet<>();

    public String currentFilename;

    public boolean creatingVertex;
    public boolean creatingEdge;
    public boolean creatingFace;
    public int edgeFirstVertex = -1;
    /** Vertex ids placed during a "Tracé (face)" session, in placement order. */
    public final java.util.ArrayList<Integer> traceVertices = new java.util.ArrayList<>();

    /** Ordered contour of a pending "face by selection" creation, awaiting validation. */
    public final java.util.ArrayList<Integer> faceSelectLoop = new java.util.ArrayList<>();
    public boolean faceSelectPending;

    public Runnable onGoToMenu;

    /** Stores the shared collaborators of the editor. */
    public Context(ShapeRenderer renderer, EditorUI ui, UndoRedo undoredo,
                   SelectionManager selection, PickUtils pick) {
        this.renderer = renderer;
        this.ui = ui;
        this.undoredo = undoredo;
        this.selection = selection;
        this.pick = pick;
    }

    /** Leaves every creation mode and clears their pending state. */
    public void exitModes() {
        creatingVertex = false;
        creatingEdge = false;
        creatingFace = false;
        edgeFirstVertex = -1;
        traceVertices.clear();
        faceSelectPending = false;
        faceSelectLoop.clear();
        renderer.clearFaceSelectPreview();
    }

    /** Returns whether a vertex/edge/face creation mode is active. */
    public boolean isInMode() { return creatingVertex || creatingEdge || creatingFace; }

    /** Snaps the given position to the grid step when grid snapping is enabled. */
    public void snapIfEnabled(Vector3f pos) {
        if (ui != null && ui.isSnapEnabled()) {
            float step = ui.getSnapStep();
            pos.x = Math.round(pos.x / step) * step;
            pos.y = Math.round(pos.y / step) * step;
            pos.z = Math.round(pos.z / step) * step;
        }
    }

    /** Magnetic snap: snaps pos to the nearest existing vertex near the cursor. */
    public void magnetIfEnabled(Vector3f pos, float mx, float my) {
        if (ui == null || !ui.isMagnetEnabled()) return;
        float radius = ui.getMagnetRadius();
        markershape.shape.Vertex v = pick.findVertexNearCursor(mx, my, radius);
        if (v != null) {
            pos.x = v.x; pos.y = v.y; pos.z = v.z;
        }
    }
}
