package markershape.editor.action;

import markershape.shape.Edge;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import learngl.LogFile;

public class FaceUtils {
    public boolean connectedBetween(ShapeData data, int x, int y) {
        Vertex vx = data.vertices.get(x);
        if (vx == null) return false;
        for (int eid : vx.edgeIds) {
            if (edgeOther(data, eid, x) == y) return true;
        }
        return false;
    }

    public int edgeOther(ShapeData data, int edgeId, int vertexId) {
        Edge e = data.edges.get(edgeId);
        if (e == null) return -1;
        return e.a == vertexId ? e.b : (e.b == vertexId ? e.a : -1);
    }

    public boolean triExists(ShapeData data, int a, int b, int c) {
        for (int[] tri : data.faces) {
            boolean hasA = false, hasB = false, hasC = false;
            for (int id : tri) {
                if (id == a) hasA = true;
                if (id == b) hasB = true;
                if (id == c) hasC = true;
            }
            if (hasA && hasB && hasC) return true;
        }
        return false;
    }

    public void detectAndCreateFaces(ShapeData data, int a, int b) {
        if (data == null) return;
        Vertex va = data.vertices.get(a);
        Vertex vb = data.vertices.get(b);
        if (va == null || vb == null) return;
        for (Vertex vc : data.vertices.values()) {
            int c = vc.id;
            if (c == a || c == b) continue;
            if ((vc.x == va.x && vc.y == va.y && vc.z == va.z) ||
                (vc.x == vb.x && vc.y == vb.y && vc.z == vb.z)) continue;
            if (connectedBetween(data, a, c) && connectedBetween(data, b, c)) {
                if (!triExists(data, a, b, c)) {
                    data.faces.add(new int[]{a, b, c});
                    LogFile.logf("[MarkerShape] auto-created face {%d,%d,%d}", a, b, c);
                }
            }
        }
    }

    public void cleanupFaces(ShapeData data) {
        if (data == null) return;
        data.faces.removeIf(tri -> {
            int a = tri[0], b = tri[1], c = tri[2];
            return !(connectedBetween(data, a, b)
                  && connectedBetween(data, b, c)
                  && connectedBetween(data, c, a));
        });
    }
}
