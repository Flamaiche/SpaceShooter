package markershape.editor.action;

import markershape.editor.Context;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class DragAction {
    private final Context ctx;
    public int dragVertexId = -1;
    public double dragStartMX, dragStartMY;
    public final org.joml.Vector3f dragOrigPos = new org.joml.Vector3f();
    public float dragNdcZ;

    public DragAction(Context ctx) { this.ctx = ctx; }
    public boolean isDragging() { return dragVertexId >= 0; }

    public void start(int id, float mx, float my) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        ctx.undoredo.snapshot(data);
        Vertex v = data.vertices.get(id);
        if (v == null) return;
        dragVertexId = id;
        dragStartMX = mx;
        dragStartMY = my;
        dragOrigPos.set(v.x, v.y, v.z);
        ctx.selection.crosshairPos.set(v.x, v.y, v.z);
        ctx.selection.crosshairValid = true;
        Vector4f clip = new Vector4f(v.x, v.y, v.z, 1f)
            .mul(new Matrix4f(ctx.pick.getProjection()).mul(ctx.pick.getView()));
        dragNdcZ = clip.z / clip.w;
    }

    public void update(float mx, float my) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null || dragVertexId < 0) return;
        Vertex v = data.vertices.get(dragVertexId);
        if (v == null) return;

        org.joml.Vector3f startWorld = ctx.pick.unprojectAtDepth((float) dragStartMX, (float) dragStartMY, dragNdcZ);
        org.joml.Vector3f curWorld = ctx.pick.unprojectAtDepth(mx, my, dragNdcZ);
        v.x = dragOrigPos.x + (curWorld.x - startWorld.x);
        v.y = dragOrigPos.y + (curWorld.y - startWorld.y);
        v.z = dragOrigPos.z + (curWorld.z - startWorld.z);
        org.joml.Vector3f snapped = new org.joml.Vector3f(v.x, v.y, v.z);
        ctx.snapIfEnabled(snapped);
        v.x = snapped.x; v.y = snapped.y; v.z = snapped.z;
        ctx.selection.crosshairPos.set(v.x, v.y, v.z);
    }

    public void end() {
        dragVertexId = -1;
        ctx.selection.crosshairValid = false;
        ctx.renderer.rebuild();
    }
}
