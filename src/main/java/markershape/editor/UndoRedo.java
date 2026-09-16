package markershape.editor;

import java.util.ArrayDeque;
import markershape.shape.ShapeData;
import learngl.LogFile;

/** Stack-based undo/redo history for shape edits (bounded to 50 snapshots each). */
public class UndoRedo {
    private final ArrayDeque<ShapeData> undoStack = new ArrayDeque<>();
    private final ArrayDeque<ShapeData> redoStack = new ArrayDeque<>();
    private static final int MAX_UNDO = 50;
    private static final int MAX_REDO = 50;

    /** Stores a copy of the current shape as an undo point and clears the redo stack. */
    public void snapshot(ShapeData data) {
        if (data == null) return;
        undoStack.push(data.copy());
        if (undoStack.size() > MAX_UNDO) undoStack.removeLast();
        redoStack.clear();
    }

    /** Pops the last undo snapshot and pushes the current shape for redo; returns null if nothing to undo. */
    public ShapeData undo(ShapeData cur) {
        if (undoStack.isEmpty()) return null;
        redoStack.push(cur.copy());
        if (redoStack.size() > MAX_REDO) redoStack.removeLast();
        LogFile.log("[MarkerShape] undo");
        return undoStack.pop();
    }

    /** Pops the last redo snapshot and pushes the current shape for undo; returns null if nothing to redo. */
    public ShapeData redo(ShapeData cur) {
        if (redoStack.isEmpty()) return null;
        undoStack.push(cur.copy());
        if (undoStack.size() > MAX_UNDO) undoStack.removeLast();
        LogFile.log("[MarkerShape] redo");
        return redoStack.pop();
    }

    /** Drops the most recent undo snapshot (used when an action is cancelled). */
    public void discardLastSnapshot() {
        if (!undoStack.isEmpty()) undoStack.pop();
    }
}
