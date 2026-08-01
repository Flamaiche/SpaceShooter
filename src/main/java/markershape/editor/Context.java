package markershape.editor;

import markershape.shape.render.ShapeRenderer;
import markershape.editor.ui.EditorUI;
import org.joml.Vector3f;

public class Context {
    public final ShapeRenderer renderer;
    public EditorUI ui;
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
    public int edgeFirstVertex = -1;

    public Runnable onGoToMenu;

    public Context(ShapeRenderer renderer, EditorUI ui, UndoRedo undoredo,
                   SelectionManager selection, PickUtils pick) {
        this.renderer = renderer;
        this.ui = ui;
        this.undoredo = undoredo;
        this.selection = selection;
        this.pick = pick;
    }

    public void exitModes() {
        creatingVertex = false;
        creatingEdge = false;
        edgeFirstVertex = -1;
    }

    public boolean isInMode() { return creatingVertex || creatingEdge; }

    public void snapIfEnabled(Vector3f pos) {
        if (ui != null && ui.isSnapEnabled()) {
            float step = ui.getSnapStep();
            pos.x = Math.round(pos.x / step) * step;
            pos.y = Math.round(pos.y / step) * step;
            pos.z = Math.round(pos.z / step) * step;
        }
    }
}
