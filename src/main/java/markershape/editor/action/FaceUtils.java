package markershape.editor.action;

import markershape.shape.Edge;
import markershape.shape.Face;
import markershape.shape.ShapeData;
import markershape.shape.Vertex;
import learngl.LogFile;

/** Utilities for testing connectivity between vertices and auto-creating/cleaning triangular faces. */
public class FaceUtils {
    /** Returns whether two vertices are directly linked by an edge. */
    public boolean connectedBetween(ShapeData data, int x, int y) {
        Vertex vx = data.vertices.get(x);
        if (vx == null) return false;
        for (int eid : vx.edgeIds) {
            if (edgeOther(data, eid, x) == y) return true;
        }
        return false;
    }

    /** Returns the vertex at the other end of the edge, or -1 if edges/vertices are missing. */
    public int edgeOther(ShapeData data, int edgeId, int vertexId) {
        Edge e = data.edges.get(edgeId);
        if (e == null) return -1;
        return e.a == vertexId ? e.b : (e.b == vertexId ? e.a : -1);
    }

    /** Returns whether a triangle with the given three vertex ids already exists in the shape. */
    public boolean triExists(ShapeData data, int a, int b, int c) {
        for (Face tri : data.faces) {
            if (tri.contains(a) && tri.contains(b) && tri.contains(c)) return true;
        }
        return false;
    }

    /** After a new edge [a,b], auto-creates a triangular face for every vertex linked to both endpoints. */
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
                    data.faces.add(newFace(data, a, b, c));
                    LogFile.logf("[MarkerShape] auto-created face {%d,%d,%d}", a, b, c);
                }
            }
        }
    }

    /** Builds a Face with the current creation color. */
    public Face newFace(ShapeData data, int a, int b, int c) {
        markershape.config.ConfigParametres cfg = markershape.config.ConfigParametres.get();
        return new Face(a, b, c,
            cfg.getFloat("createColorR"),
            cfg.getFloat("createColorG"),
            cfg.getFloat("createColorB"));
    }

    /** Removes every face whose three boundary edges are no longer all present. */
    public void cleanupFaces(ShapeData data) {
        if (data == null) return;
        data.faces.removeIf(tri -> {
            return !(connectedBetween(data, tri.a, tri.b)
                  && connectedBetween(data, tri.b, tri.c)
                  && connectedBetween(data, tri.c, tri.a));
        });
    }
}
