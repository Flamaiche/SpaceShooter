package markershape.editor.action;

import markershape.editor.Context;
import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * Shape manipulation toolkit: duplicate, weld, split edge, extrude edge,
 * fill a selected loop, clean the shape, and copy/paste selection.
 */
public class ShapeTools {
    private final Context ctx;
    private final FaceUtils faceUtils;

    private static ArrayList<Vertex> clipVerts = new ArrayList<>();
    private static ArrayList<int[]> clipEdges = new ArrayList<>();

    public ShapeTools(Context ctx, FaceUtils faceUtils) {
        this.ctx = ctx;
        this.faceUtils = faceUtils;
    }

    private int nextVertexId(ShapeData d) {
        if (d.vertices.isEmpty()) return 0;
        int max = -1;
        for (int id : d.vertices.keySet()) if (id > max) max = id;
        return max + 1;
    }

    private int nextEdgeId(ShapeData d) {
        if (d.edges.isEmpty()) return 0;
        int max = -1;
        for (int id : d.edges.keySet()) if (id > max) max = id;
        return max + 1;
    }

    private TreeSet<Integer> activeVertices() {
        TreeSet<Integer> set = new TreeSet<>(ctx.selection.multiVertices);
        if (set.isEmpty() && ctx.selection.selectedVertex >= 0) set.add(ctx.selection.selectedVertex);
        return set;
    }

    private TreeSet<Integer> activeEdges() {
        TreeSet<Integer> set = new TreeSet<>(ctx.selection.multiEdges);
        if (set.isEmpty() && ctx.selection.selectedEdge >= 0) set.add(ctx.selection.selectedEdge);
        return set;
    }

    private float bboxSize(ShapeData data) {
        if (data == null || data.vertices.isEmpty()) return 1f;
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        for (Vertex v : data.vertices.values()) {
            minX = Math.min(minX, v.x); maxX = Math.max(maxX, v.x);
            minY = Math.min(minY, v.y); maxY = Math.max(maxY, v.y);
            minZ = Math.min(minZ, v.z); maxZ = Math.max(maxZ, v.z);
        }
        return Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
    }

    private static boolean contains(int[] arr, int v) {
        for (int x : arr) if (x == v) return true;
        return false;
    }

    public void duplicateSelected() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        TreeSet<Integer> verts = activeVertices();
        if (verts.isEmpty()) {
            System.out.println("[MarkerShape] rien a dupliquer (selectionnez des sommets)");
            return;
        }
        ctx.undoredo.snapshot(data);
        float off = Math.max(bboxSize(data) * 0.05f, 0.05f);
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int id : verts) {
            Vertex v = data.vertices.get(id);
            if (v == null) continue;
            int nid = nextVertexId(data);
            data.addVertex(new Vertex(nid, v.x + off, v.y + off, v.z + off, v.r, v.g, v.b));
            map.put(id, nid);
        }
        List<Edge> toAdd = new ArrayList<>();
        for (Edge e : data.edges.values()) {
            Integer na = map.get(e.a), nb = map.get(e.b);
            if (na != null && nb != null && !na.equals(nb)) {
                Edge ne = e.copy();
                ne.id = nextEdgeId(data);
                ne.a = na;
                ne.b = nb;
                toAdd.add(ne);
            }
        }
        for (Edge ne : toAdd) data.addEdge(ne);

        ctx.selection.selectDuplicateVertices(map.values());
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] duplicated %d vertices", map.size());
    }

    public void weldSelected() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        TreeSet<Integer> verts = activeVertices();
        if (verts.size() < 2) {
            System.out.println("[MarkerShape] fusionner demande au moins 2 sommets selectionnes");
            return;
        }
        ctx.undoredo.snapshot(data);
        int keep = verts.first();
        Vertex k = data.vertices.get(keep);
        if (k == null) return;

        float sx = k.x, sy = k.y, sz = k.z, sr = k.r, sg = k.g, sb = k.b;
        int count = 1;
        for (int id : verts) {
            if (id == keep) continue;
            Vertex v = data.vertices.get(id);
            if (v == null) continue;
            sx += v.x; sy += v.y; sz += v.z;
            sr += v.r; sg += v.g; sb += v.b;
            count++;
        }
        k.x = sx / count; k.y = sy / count; k.z = sz / count;
        k.r = sr / count; k.g = sg / count; k.b = sb / count;

        List<Edge> loops = new ArrayList<>();
        for (int id : verts) {
            if (id == keep) continue;
            Vertex r = data.vertices.get(id);
            if (r == null) continue;
            for (int eid : new ArrayList<>(r.edgeIds)) {
                Edge e = data.edges.get(eid);
                if (e == null) continue;
                if (e.a == id) e.a = keep;
                else if (e.b == id) e.b = keep;
                if (e.a == e.b) { loops.add(e); continue; }
                k.edgeIds.add(eid);
            }
            data.vertices.remove(id);
        }
        for (Edge e : loops) data.removeEdge(e.id);
        removeDuplicateEdges(data);
        faceUtils.cleanupFaces(data);

        ctx.selection.selectVertex(keep);
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] welded %d vertices into %d", count, keep);
    }

    private void removeDuplicateEdges(ShapeData data) {
        HashMap<String, Integer> seen = new HashMap<>();
        for (Edge e : new ArrayList<>(data.edges.values())) {
            int lo = Math.min(e.a, e.b), hi = Math.max(e.a, e.b);
            String key = lo + "-" + hi;
            if (seen.containsKey(key)) data.removeEdge(e.id);
            else seen.put(key, e.id);
        }
    }

    /** Splits the given edge at its midpoint into two edges + adjusted faces. */
    public void splitEdge(int edgeId) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        Edge e = data.edges.get(edgeId);
        if (e == null) return;
        Vertex va = data.vertices.get(e.a);
        Vertex vb = data.vertices.get(e.b);
        if (va == null || vb == null) return;
        ctx.undoredo.snapshot(data);

        int mid = nextVertexId(data);
        data.addVertex(new Vertex(mid,
            (va.x + vb.x) / 2f, (va.y + vb.y) / 2f, (va.z + vb.z) / 2f,
            (va.r + vb.r) / 2f, (va.g + vb.g) / 2f, (va.b + vb.b) / 2f));

        data.removeEdge(edgeId);
        Edge e1 = new Edge(nextEdgeId(data), e.a, mid, e.mode, e.thickness);
        Edge e2 = new Edge(nextEdgeId(data), mid, e.b, e.mode, e.thickness);
        data.addEdge(e1);
        data.addEdge(e2);

        List<int[]> updated = new ArrayList<>();
        for (int[] tri : data.faces) {
            if (contains(tri, e.a) && contains(tri, e.b)) {
                int c = -1;
                for (int x : tri) if (x != e.a && x != e.b) { c = x; break; }
                if (c < 0) continue;
                if (!faceUtils.triExists(data, e.a, mid, c)) updated.add(new int[]{e.a, mid, c});
                if (!faceUtils.triExists(data, mid, e.b, c)) updated.add(new int[]{mid, e.b, c});
            } else {
                updated.add(tri);
            }
        }
        data.faces = updated;

        ctx.selection.selectVertex(mid);
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] split edge %d -> vertex %d", edgeId, mid);
    }

    public void extrudeSelectedEdge() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        TreeSet<Integer> edges = activeEdges();
        if (edges.isEmpty()) {
            System.out.println("[MarkerShape] extrude demande une arete selectionnee");
            return;
        }
        int edgeId = ctx.selection.selectedEdge >= 0 ? ctx.selection.selectedEdge : edges.first();
        Edge e = data.edges.get(edgeId);
        if (e == null) return;
        Vertex va = data.vertices.get(e.a);
        Vertex vb = data.vertices.get(e.b);
        if (va == null || vb == null) return;
        ctx.undoredo.snapshot(data);

        Vector3f dir = extrudeDirection(data, e);
        float len = new Vector3f(vb.x - va.x, vb.y - va.y, vb.z - va.z).length();
        float off = len;
        if (off < 1e-5f) off = 0.1f;
        Vector3f delta = new Vector3f(dir).mul(off);

        int nA = nextVertexId(data);
        int nB = nextVertexId(data);
        data.addVertex(new Vertex(nA, va.x + delta.x, va.y + delta.y, va.z + delta.z, va.r, va.g, va.b));
        data.addVertex(new Vertex(nB, vb.x + delta.x, vb.y + delta.y, vb.z + delta.z, vb.r, vb.g, vb.b));

        Edge nAB = new Edge(nextEdgeId(data), nA, nB, e.mode, e.thickness);
        Edge eA = new Edge(nextEdgeId(data), e.a, nA, e.mode, e.thickness);
        Edge eB = new Edge(nextEdgeId(data), e.b, nB, e.mode, e.thickness);
        data.addEdge(nAB);
        data.addEdge(eA);
        data.addEdge(eB);

        if (!faceUtils.triExists(data, e.a, e.b, nB)) data.faces.add(new int[]{e.a, e.b, nB});
        if (!faceUtils.triExists(data, e.a, nB, nA)) data.faces.add(new int[]{e.a, nB, nA});

        ctx.selection.selectEdge(nAB.id);
        ctx.selection.multiVertices.add(nA);
        ctx.selection.multiVertices.add(nB);
        ctx.selection.refreshSelectionVisual();
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] extruded edge %d", edgeId);
    }

    private Vector3f extrudeDirection(ShapeData data, Edge e) {
        Vertex va = data.vertices.get(e.a);
        Vertex vb = data.vertices.get(e.b);
        Vector3f ab = new Vector3f(vb.x - va.x, vb.y - va.y, vb.z - va.z);
        Vector3f normal = null;
        for (int[] tri : data.faces) {
            if (!contains(tri, e.a) || !contains(tri, e.b)) continue;
            int c = -1;
            for (int x : tri) if (x != e.a && x != e.b) { c = x; break; }
            if (c < 0) continue;
            Vertex vc = data.vertices.get(c);
            if (vc == null) continue;
            Vector3f cross = new Vector3f(ab)
                .cross(new Vector3f(vc.x - va.x, vc.y - va.y, vc.z - va.z));
            if (cross.lengthSquared() < 1e-12f) continue;
            cross.normalize();
            if (normal == null) normal = new Vector3f(cross);
            else normal.add(cross);
        }
        if (normal != null && normal.lengthSquared() > 1e-6f) return normal.normalize();
        Vector3f dir = new Vector3f(ab).normalize();
        Vector3f perp = new Vector3f(dir).cross(new Vector3f(0, 1, 0));
        if (perp.lengthSquared() < 1e-6f) return new Vector3f(1, 0, 0);
        return perp.normalize();
    }

    /** Fills the selected edges (a single closed loop) with fan triangles. */
    public void fillSelection() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        TreeSet<Integer> edges = activeEdges();
        if (edges.size() < 3) {
            System.out.println("[MarkerShape] remplissage demande 3+ aretes (contour ferme)");
            return;
        }
        List<Integer> loop = buildLoop(data, edges);
        if (loop == null) {
            System.out.println("[MarkerShape] les aretes selectionnees ne forment pas une seule boucle fermee");
            return;
        }
        ctx.undoredo.snapshot(data);
        int v0 = loop.get(0);
        int added = 0;
        for (int i = 1; i + 1 < loop.size(); i++) {
            int vA = loop.get(i);
            int vB = loop.get(i + 1);
            if (vB == v0) break;
            if (!faceUtils.triExists(data, v0, vA, vB)) {
                data.faces.add(new int[]{v0, vA, vB});
                added++;
            }
        }
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] filled loop with %d faces", added);
    }

    /** Builds the ordered vertex loop described by the selected edges, or null. */
    private List<Integer> buildLoop(ShapeData data, TreeSet<Integer> edgeIds) {
        HashMap<Integer, Integer> deg = new HashMap<>();
        for (int eid : edgeIds) {
            Edge e = data.edges.get(eid);
            if (e == null) return null;
            deg.merge(e.a, 1, Integer::sum);
            deg.merge(e.b, 1, Integer::sum);
        }
        for (int d : deg.values()) if (d != 2) return null;

        int start = deg.keySet().iterator().next();
        List<Integer> loop = new ArrayList<>();
        loop.add(start);
        int prev = -1, cur = start;
        while (true) {
            Vertex v = data.vertices.get(cur);
            if (v == null) return null;
            int next = -1;
            for (int eid : v.edgeIds) {
                if (!edgeIds.contains(eid)) continue;
                int other = faceUtils.edgeOther(data, eid, cur);
                if (other < 0 || other == prev) continue;
                next = other;
                break;
            }
            if (next < 0) return null;
            if (next == start) break;
            if (loop.contains(next)) return null;
            loop.add(next);
            prev = cur;
            cur = next;
        }
        if (deg.size() != loop.size()) return null;
        loop.add(start);
        return loop;
    }

    public void selectAll() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null || data.vertices.isEmpty()) return;
        ctx.selection.multiVertices.clear();
        ctx.selection.multiEdges.clear();
        ctx.selection.selectedEdge = -1;
        ctx.selection.multiVertices.addAll(data.vertices.keySet());
        ctx.selection.selectedVertex = ctx.selection.multiVertices.first();
        ctx.selection.refreshSelectionVisual();
        learngl.LogFile.logf("[MarkerShape] selected all %d vertices", ctx.selection.multiVertices.size());
    }

    public void copySelected() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        TreeSet<Integer> verts = activeVertices();
        if (verts.isEmpty()) {
            TreeSet<Integer> edges = activeEdges();
            for (int eid : edges) {
                Edge e = data.edges.get(eid);
                if (e != null) { verts.add(e.a); verts.add(e.b); }
            }
        }
        if (verts.isEmpty()) {
            System.out.println("[MarkerShape] rien a copier");
            return;
        }
        clipVerts.clear();
        clipEdges.clear();
        HashMap<Integer, Integer> idx = new HashMap<>();
        for (int id : new TreeSet<>(verts)) {
            Vertex v = data.vertices.get(id);
            if (v == null) continue;
            idx.put(id, clipVerts.size());
            clipVerts.add(v.copy());
        }
        for (Edge e : data.edges.values()) {
            Integer ia = idx.get(e.a), ib = idx.get(e.b);
            if (ia != null && ib != null) clipEdges.add(new int[]{ia, ib});
        }
        learngl.LogFile.logf("[MarkerShape] copied %d vertices, %d edges", clipVerts.size(), clipEdges.size());
    }

    public void pasteSelected() {
        if (clipVerts.isEmpty()) {
            System.out.println("[MarkerShape] presse-papier vide (Ctrl+C d'abord)");
            return;
        }
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return;
        ctx.undoredo.snapshot(data);
        float off = Math.max(bboxSize(data) * 0.05f, 0.05f);
        ArrayList<Integer> newIds = new ArrayList<>();
        for (Vertex cv : clipVerts) {
            int nid = nextVertexId(data);
            data.addVertex(new Vertex(nid, cv.x + off, cv.y + off, cv.z + off, cv.r, cv.g, cv.b));
            newIds.add(nid);
        }
        for (int[] ce : clipEdges) {
            int nA = newIds.get(ce[0]), nB = newIds.get(ce[1]);
            if (nA == nB) continue;
            boolean dup = false;
            for (Edge e : data.edges.values()) {
                if ((e.a == nA && e.b == nB) || (e.a == nB && e.b == nA)) { dup = true; break; }
            }
            if (dup) continue;
            data.addEdge(new Edge(nextEdgeId(data), nA, nB, "stun", 0.02f));
        }
        ctx.selection.selectDuplicateVertices(newIds);
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] pasted %d vertices", newIds.size());
    }

    /** Removes orphan vertices / isolated dangling edges / invalid faces. Returns a report. */
    public String cleanupShape() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return "pas de forme chargee";
        ctx.undoredo.snapshot(data);

        int orphans = removeOrphanVertices(data);
        int isolated = 0;
        List<Integer> removeE = new ArrayList<>();
        for (Edge e : data.edges.values()) {
            Vertex va = data.vertices.get(e.a), vb = data.vertices.get(e.b);
            if (va == null || vb == null) { removeE.add(e.id); isolated++; continue; }
            if (va.edgeIds.size() == 1 && vb.edgeIds.size() == 1) { removeE.add(e.id); isolated++; }
        }
        for (int id : removeE) data.removeEdge(id);
        orphans += removeOrphanVertices(data);

        int facesBefore = data.faces.size();
        faceUtils.cleanupFaces(data);
        int facesRemoved = facesBefore - data.faces.size();

        int dupPos = countDuplicatePositions(data);

        ctx.selection.reset();
        ctx.renderer.rebuild();
        String report = String.format(
            "Clean: %d sommets orphelins, %d aretes isolees, %d faces invalides, %d doublons de position",
            orphans, isolated, facesRemoved, dupPos);
        learngl.LogFile.logf("[MarkerShape] %s", report);
        return report;
    }

    private int removeOrphanVertices(ShapeData data) {
        List<Integer> remove = new ArrayList<>();
        for (Vertex v : data.vertices.values()) {
            if (v.edgeIds.isEmpty()) remove.add(v.id);
        }
        for (int id : remove) data.removeVertex(id);
        return remove.size();
    }

    private int countDuplicatePositions(ShapeData data) {
        int dup = 0;
        List<Vertex> list = new ArrayList<>(data.vertices.values());
        for (int i = 0; i < list.size(); i++) {
            Vertex a = list.get(i);
            if (!data.vertices.containsKey(a.id)) continue;
            for (int j = i + 1; j < list.size(); j++) {
                Vertex b = list.get(j);
                if (!data.vertices.containsKey(b.id)) continue;
                if (a.x == b.x && a.y == b.y && a.z == b.z) dup++;
            }
        }
        return dup;
    }
}