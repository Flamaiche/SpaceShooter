package markershape.editor.action;

import markershape.editor.Context;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;

/** Dragging: moves a vertex — or the whole multi-selection group — on the screen plane. */
public class DragAction {
    private final Context ctx;
    public int dragVertexId = -1;
    public double dragStartMX, dragStartMY;
    public final org.joml.Vector3f dragOrigPos = new org.joml.Vector3f();
    public float dragNdcZ;
    /**
     * Movement constraint while dragging, projected onto the screen plane:
     * 0 = free, 1 = X axis only, 2 = Y axis only, 3 = Z axis only.
     */
    public int axis = 0;

    private final ArrayList<Integer> group = new ArrayList<>();
    private final HashMap<Integer, Vector3f> origins = new HashMap<>();

    /** Creates the drag action bound to the given editor context. */
    public DragAction(Context ctx) { this.ctx = ctx; }
    /** Returns whether a drag is currently in progress. */
    public boolean isDragging() { return dragVertexId >= 0; }

    /** Begins dragging the given vertex (or the whole multi-selection group containing it). */
    public void start(int id, float mx, float my) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        ctx.undoredo.snapshot(data);
        Vertex v = data.vertices.get(id);
        if (v == null) return;

        group.clear();
        origins.clear();
        if (ctx.selection.multiVertices.contains(id)) {
            group.addAll(ctx.selection.multiVertices);
        } else {
            group.add(id);
        }
        for (int gid : new ArrayList<>(group)) {
            Vertex gv = data.vertices.get(gid);
            if (gv == null) { group.remove((Integer) gid); continue; }
            origins.put(gid, new Vector3f(gv.x, gv.y, gv.z));
        }

        dragVertexId = id;
        dragStartMX = mx;
        dragStartMY = my;
        dragOrigPos.set(v.x, v.y, v.z);
        axis = 0;
        ctx.selection.crosshairPos.set(v.x, v.y, v.z);
        ctx.selection.crosshairValid = true;
        Vector4f clip = new Vector4f(v.x, v.y, v.z, 1f)
            .mul(new Matrix4f(ctx.pick.getProjection()).mul(ctx.pick.getView()));
        dragNdcZ = clip.z / clip.w;
    }

    /** Moves all dragged vertices along the screen plane to follow the cursor. */
    public void update(float mx, float my) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null || dragVertexId < 0) return;

        org.joml.Vector3f startWorld = ctx.pick.unprojectAtDepth((float) dragStartMX, (float) dragStartMY, dragNdcZ);
        org.joml.Vector3f curWorld = ctx.pick.unprojectAtDepth(mx, my, dragNdcZ);
        float dx = curWorld.x - startWorld.x;
        float dy = curWorld.y - startWorld.y;
        float dz = curWorld.z - startWorld.z;
        if (axis >= 1 && axis <= 3) {
            float ax = axis == 1 ? 1f : 0f;
            float ay = axis == 2 ? 1f : 0f;
            float az = axis == 3 ? 1f : 0f;
            float proj = dx * ax + dy * ay + dz * az;
            dx = proj * ax; dy = proj * ay; dz = proj * az;
        }
        for (int gid : group) {
            Vertex gv = data.vertices.get(gid);
            if (gv == null) continue;
            Vector3f o = origins.get(gid);
            if (o == null) continue;
            org.joml.Vector3f p = new org.joml.Vector3f(o.x + dx, o.y + dy, o.z + dz);
            ctx.snapIfEnabled(p);
            gv.x = p.x; gv.y = p.y; gv.z = p.z;
        }
        Vertex v = data.vertices.get(dragVertexId);
        if (v != null) ctx.selection.crosshairPos.set(v.x, v.y, v.z);
    }

    /** Finalises the drag, keeping the new positions. */
    public void end() {
        dragVertexId = -1;
        ctx.selection.crosshairValid = false;
        group.clear();
        origins.clear();
        ctx.renderer.rebuild();
        ctx.selection.refreshSelectionVisual();
    }

    /** Cancels the drag, restoring every vertex to its pre-drag position. */
    public void cancel() {
        if (dragVertexId < 0) return;
        ShapeData data = ctx.renderer.getShapeData();
        if (data != null) {
            for (var e : origins.entrySet()) {
                Vertex v = data.vertices.get(e.getKey());
                if (v != null) {
                    Vector3f o = e.getValue();
                    v.x = o.x; v.y = o.y; v.z = o.z;
                }
            }
        }
        end();
        ctx.undoredo.discardLastSnapshot();
    }
}