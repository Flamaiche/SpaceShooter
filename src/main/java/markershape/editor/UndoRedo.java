package markershape.editor;

import java.util.ArrayDeque;
import markershape.shape.ShapeData;
import learngl.LogFile;

public class UndoRedo {
    private final ArrayDeque<ShapeData> undoStack = new ArrayDeque<>();
    private final ArrayDeque<ShapeData> redoStack = new ArrayDeque<>();
    private static final int MAX_UNDO = 50;

    public void snapshot(ShapeData data) {
        if (data == null) return;
        undoStack.push(data.copy());
        if (undoStack.size() > MAX_UNDO) undoStack.removeLast();
        redoStack.clear();
    }

    public ShapeData undo(ShapeData cur) {
        if (undoStack.isEmpty()) return null;
        redoStack.push(cur.copy());
        LogFile.log("[MarkerShape] undo");
        return undoStack.pop();
    }

    public ShapeData redo(ShapeData cur) {
        if (redoStack.isEmpty()) return null;
        undoStack.push(cur.copy());
        LogFile.log("[MarkerShape] redo");
        return redoStack.pop();
    }
}
