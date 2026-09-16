package markershape.editor.action;

import markershape.editor.Context;
import markershape.shape.Edge;
import markershape.shape.Face;
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
    private static ArrayList<Edge> clipEdges = new ArrayList<>();

    /** Creates the toolkit with the shared context and face utilities. */
    public ShapeTools(Context ctx, FaceUtils faceUtils) {
        this.ctx = ctx;
        this.faceUtils = faceUtils;
    }

    private int vertexSeq = Integer.MIN_VALUE;
    private int edgeSeq = Integer.MIN_VALUE;

    /** Returns the largest vertex id in the shape, or -1. */
    private int maxVertexId(ShapeData d) {
        int max = -1;
        if (d == null) return max;
        for (int id : d.vertices.keySet()) if (id > max) max = id;
        return max;
    }

    /** Returns the largest edge id in the shape, or -1. */
    private int maxEdgeId(ShapeData d) {
        int max = -1;
        if (d == null) return max;
        for (int id : d.edges.keySet()) if (id > max) max = id;
        return max;
    }

    /** Returns a unique vertex ID, monotonic across calls even before insertion. */
    private int nextVertexId(ShapeData d) {
        vertexSeq = Math.max(vertexSeq, maxVertexId(d));
        return ++vertexSeq;
    }

    /** Returns a unique edge ID, monotonic across calls even before insertion. */
    private int nextEdgeId(ShapeData d) {
        edgeSeq = Math.max(edgeSeq, maxEdgeId(d));
        return ++edgeSeq;
    }

    /** Face with the colour of the seed face but new indices. */
    private static Face newFaceLike(Face seed, int a, int b, int c) {
        return new Face(a, b, c, seed.r, seed.g, seed.b);
    }

    /** Face with the current creation colour. */
    private Face newFace(int a, int b, int c) {
        return new Face(a, b, c,
            markershape.config.ConfigParametres.get().getFloat("createColorR"),
            markershape.config.ConfigParametres.get().getFloat("createColorG"),
            markershape.config.ConfigParametres.get().getFloat("createColorB"));
    }

    /** Edge with the current creation colour and a fresh id. */
    private Edge newEdgeWithCurrentColor(int a, int b) {
        markershape.config.ConfigParametres cfg = markershape.config.ConfigParametres.get();
        return new Edge(nextEdgeId(ctx.renderer.getShapeData()), a, b, "stun", 0.02f,
            cfg.getFloat("createColorR"),
            cfg.getFloat("createColorG"),
            cfg.getFloat("createColorB"));
    }

    /** Returns the active vertex selection (multi-selection, or the single selected vertex). */
    private TreeSet<Integer> activeVertices() {
        TreeSet<Integer> set = new TreeSet<>(ctx.selection.multiVertices);
        if (set.isEmpty() && ctx.selection.selectedVertex >= 0) set.add(ctx.selection.selectedVertex);
        return set;
    }

    /** Returns the active edge selection (multi-selection, or the single selected edge). */
    private TreeSet<Integer> activeEdges() {
        TreeSet<Integer> set = new TreeSet<>(ctx.selection.multiEdges);
        if (set.isEmpty() && ctx.selection.selectedEdge >= 0) set.add(ctx.selection.selectedEdge);
        return set;
    }

    /** Returns the largest extent of the model's bounding box, or 1f when empty. */
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

    /** Duplicates the selected vertices (with their internal edges) offset by 5% of the model size. */
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

    /** Welds the selected vertices into a single average-position vertex, merging edges and faces. */
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
        data.purgeOrphanVertices();

        ctx.selection.selectVertex(keep);
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] welded %d vertices into %d", count, keep);
    }

    /** Removes edges that connect the same pair of vertices more than once. */
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
        Edge e1 = new Edge(nextEdgeId(data), e.a, mid, e.mode, e.thickness, e.r, e.g, e.bl);
        Edge e2 = new Edge(nextEdgeId(data), mid, e.b, e.mode, e.thickness, e.r, e.g, e.bl);
        data.addEdge(e1);
        data.addEdge(e2);

        List<Face> updated = new ArrayList<>();
        for (Face tri : data.faces) {
            if (tri.contains(e.a) && tri.contains(e.b)) {
                int c = -1;
                if (tri.a != e.a && tri.a != e.b) c = tri.a;
                else if (tri.b != e.a && tri.b != e.b) c = tri.b;
                else c = tri.c;
                if (c < 0) continue;
                if (!faceUtils.triExists(data, e.a, mid, c)) updated.add(newFaceLike(tri, e.a, mid, c));
                if (!faceUtils.triExists(data, mid, e.b, c)) updated.add(newFaceLike(tri, mid, e.b, c));
            } else {
                updated.add(tri.copy());
            }
        }
        data.faces = updated;

        ctx.selection.selectVertex(mid);
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] split edge %d -> vertex %d", edgeId, mid);
    }

    /** Extrudes the selected edge into a quad, pushing copies of its endpoints along the local normal. */
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

        Edge nAB = new Edge(nextEdgeId(data), nA, nB, e.mode, e.thickness, e.r, e.g, e.bl);
        Edge eA = new Edge(nextEdgeId(data), e.a, nA, e.mode, e.thickness, e.r, e.g, e.bl);
        Edge eB = new Edge(nextEdgeId(data), e.b, nB, e.mode, e.thickness, e.r, e.g, e.bl);
        data.addEdge(nAB);
        data.addEdge(eA);
        data.addEdge(eB);

        Face f1 = newFace(e.a, e.b, nB);
        Face f2 = newFace(e.a, nB, nA);
        if (!faceUtils.triExists(data, e.a, e.b, nB)) data.faces.add(f1);
        if (!faceUtils.triExists(data, e.a, nB, nA)) data.faces.add(f2);

        ctx.selection.selectEdge(nAB.id);
        ctx.selection.multiVertices.add(nA);
        ctx.selection.multiVertices.add(nB);
        ctx.selection.refreshSelectionVisual();
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] extruded edge %d", edgeId);
    }

    /** Computes the extrusion direction: averaged adjacent-face normal, falling back to a perpendicular. */
    private Vector3f extrudeDirection(ShapeData data, Edge e) {
        Vertex va = data.vertices.get(e.a);
        Vertex vb = data.vertices.get(e.b);
        Vector3f ab = new Vector3f(vb.x - va.x, vb.y - va.y, vb.z - va.z);
        Vector3f normal = null;
        for (Face tri : data.faces) {
            if (!tri.contains(e.a) || !tri.contains(e.b)) continue;
            int c = -1;
            if (tri.a != e.a && tri.a != e.b) c = tri.a;
            else if (tri.b != e.a && tri.b != e.b) c = tri.b;
            else c = tri.c;
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
                data.faces.add(newFace(v0, vA, vB));
                added++;
            }
        }
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] filled loop with %d faces", added);
    }

    /**
     * Closes a traced vertex loop (closing edge created if needed) and fills it
     * with fan triangles. Assumes the caller already took an undo snapshot.
     * @return number of faces created
     */
    public int closeTraceLoop(java.util.List<Integer> vids) {
        ShapeData d = ctx.renderer.getShapeData();
        if (d == null || vids.size() < 3) return 0;
        int first = vids.get(0);
        int last = vids.get(vids.size() - 1);
        if (first != last && !faceUtils.connectedBetween(d, first, last)) {
            d.addEdge(newEdgeWithCurrentColor(last, first));
        }
        int added = 0;
        for (int i = 1; i + 1 < vids.size(); i++) {
            int a = vids.get(i), b = vids.get(i + 1);
            if (b == first) break;
            if (!faceUtils.triExists(d, first, a, b)) {
                d.faces.add(newFace(first, a, b));
                added++;
            }
        }
        return added;
    }

    /**
     * Orders the selection into a contour loop. A face can be built from points,
     * from edges, or from any mix: the vertices of each selected edge are
     * automatically added to the loop (so there is no need to select the edge
     * endpoints separately, and each edge is only used once). When the selected
     * edges already form a single closed circuit that order is used, otherwise
     * the vertices are ordered angularly around the selection centroid.
     */
    public List<Integer> orderSelectionIntoLoop() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return null;
        TreeSet<Integer> edges = activeEdges();
        TreeSet<Integer> verts = new TreeSet<>(activeVertices());
        for (int eid : edges) {
            Edge e = data.edges.get(eid);
            if (e == null) continue;
            verts.add(e.a);
            verts.add(e.b);
        }
        if (verts.size() < 3) {
            System.out.println("[MarkerShape] selectionnez 3+ sommets et/ou aretes");
            return null;
        }
        // Prefer the exact contour when the selected edges already close a loop.
        List<Integer> loop = null;
        if (edges.size() >= 3) {
            List<Integer> eLoop = buildLoop(data, edges);
            if (eLoop != null) {
                loop = new ArrayList<>(eLoop);
                if (!loop.isEmpty() && loop.get(loop.size() - 1).equals(loop.get(0))) {
                    loop.remove(loop.size() - 1);
                }
            }
        }
        if (loop == null) loop = angularOrder(data, verts);
        if (loop == null || loop.size() < 3) {
            System.out.println("[MarkerShape] impossible d'ordonner la selection en un contour");
            return null;
        }
        return loop;
    }

    /** Enters the "face by selection" pending state: computes and previews the contour. */
    public boolean prepareFaceFromSelection() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return false;
        List<Integer> loop = orderSelectionIntoLoop();
        if (loop == null) return false;
        ctx.faceSelectLoop.clear();
        ctx.faceSelectLoop.addAll(loop);
        ctx.faceSelectPending = true;
        ctx.renderer.setFaceSelectPreview(loop);
        ctx.renderer.rebuild();
        System.out.println("[MarkerShape] contour prevu : " + loop.size() + " sommets (Entree valide, Echap annule)");
        return true;
    }

    /** Validates the pending face-by-selection contour (fan triangulation). */
    public int confirmFaceFromSelection() {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null) return 0;
        if (!ctx.faceSelectPending || ctx.faceSelectLoop.size() < 3) {
            cancelFaceFromSelection();
            return 0;
        }
        ctx.undoredo.snapshot(data);
        TreeSet<Integer> removedEdges = activeEdges();
        int v0 = ctx.faceSelectLoop.get(0);
        int added = 0;
        int n = ctx.faceSelectLoop.size();
        for (int i = 1; i + 1 < ctx.faceSelectLoop.size(); i++) {
            int vA = ctx.faceSelectLoop.get(i);
            int vB = ctx.faceSelectLoop.get(i + 1);
            if (vB == v0) break;
            if (!faceUtils.triExists(data, v0, vA, vB)) {
                data.faces.add(newFace(v0, vA, vB));
                added++;
            }
        }
        // The selected edges are removed and replaced by the face's own edges:
        // this avoids stacking a second identical edge over an existing one.
        for (int eid : new ArrayList<>(removedEdges)) data.removeEdge(eid);
        for (int i = 1; i + 1 < n; i++) {
            int a = ctx.faceSelectLoop.get(i);
            int b = ctx.faceSelectLoop.get(i + 1);
            if (b == v0) break;
            if (!faceUtils.connectedBetween(data, v0, a)) data.addEdge(newEdgeWithCurrentColor(v0, a));
            if (!faceUtils.connectedBetween(data, a, b)) data.addEdge(newEdgeWithCurrentColor(a, b));
            if (!faceUtils.connectedBetween(data, v0, b)) data.addEdge(newEdgeWithCurrentColor(v0, b));
        }
        ctx.renderer.rebuild();
        cancelFaceFromSelection();
        System.out.println("[MarkerShape] face creee : " + n + " sommets, " + added + " triangles, "
            + removedEdges.size() + " arete(s) remplacee(s)");
        return added;
    }

    /** Cancels the pending face-by-selection state and clears its preview. */
    public void cancelFaceFromSelection() {
        ctx.faceSelectPending = false;
        ctx.faceSelectLoop.clear();
        ctx.renderer.clearFaceSelectPreview();
    }

    /** Orders a vertex set angularly around its centroid (best-effort contour). */
    private List<Integer> angularOrder(ShapeData data, TreeSet<Integer> verts) {
        if (verts.size() < 3) return null;
        Vector3f c = new Vector3f();
        for (int id : verts) {
            Vertex v = data.vertices.get(id);
            if (v != null) c.add(v.x, v.y, v.z);
        }
        c.mul(1f / verts.size());
        List<Integer> list = new ArrayList<>(verts);
        list.sort((i, j) -> {
            Vertex vi = data.vertices.get(i);
            Vertex vj = data.vertices.get(j);
            if (vi == null || vj == null) return 0;
            double ai = Math.atan2(vi.z - c.z, vi.x - c.x);
            double aj = Math.atan2(vj.z - c.z, vj.x - c.x);
            return Double.compare(ai, aj);
        });
        return list;
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

    /** Translates ALL the model's vertices by the given delta (world origin shift). */
    public void translateAll(float dx, float dy, float dz) {
        ShapeData data = ctx.renderer.getShapeData();
        if (data == null || data.vertices.isEmpty()) return;
        ctx.undoredo.snapshot(data);
        for (Vertex v : data.vertices.values()) {
            v.x += dx;
            v.y += dy;
            v.z += dz;
        }
        ctx.renderer.rebuild();
        learngl.LogFile.logf("[MarkerShape] origine deplacee de (%.2f, %.2f, %.2f)", dx, dy, dz);
    }

    /** Selects every vertex of the model as a multi-selection. */
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

    /** Copies the selected vertices (and edges linking them) into the internal clipboard. */
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
            if (idx.containsKey(e.a) && idx.containsKey(e.b)) {
                clipEdges.add(e.copy());
            }
        }
        learngl.LogFile.logf("[MarkerShape] copied %d vertices, %d edges", clipVerts.size(), clipEdges.size());
    }

    /** Pastes the clipboard contents, offset like a duplicate, and selects the new vertices. */
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
        for (Edge ce : clipEdges) {
            int nA = newIds.get(ce.a), nB = newIds.get(ce.b);
            if (nA == nB) continue;
            boolean dup = false;
            for (Edge e : data.edges.values()) {
                if ((e.a == nA && e.b == nB) || (e.a == nB && e.b == nA)) { dup = true; break; }
            }
            if (dup) continue;
            data.addEdge(new Edge(nextEdgeId(data), nA, nB, ce.mode, ce.thickness, ce.r, ce.g, ce.bl));
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

    /** Removes vertices that are not referenced by any edge, returning the count. */
    private int removeOrphanVertices(ShapeData data) {
        List<Integer> remove = new ArrayList<>();
        for (Vertex v : data.vertices.values()) {
            if (v.edgeIds.isEmpty()) remove.add(v.id);
        }
        for (int id : remove) data.removeVertex(id);
        return remove.size();
    }

    /** Counts pairs of distinct vertices that share the same position. */
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