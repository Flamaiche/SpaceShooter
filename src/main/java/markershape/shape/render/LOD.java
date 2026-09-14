package markershape.shape.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Level-of-Detail utility: reduces the number of faces rendered based on the
 * camera distance to the mesh. Closer = full detail, further = fewer triangles.
 *
 * <p>Levels:
 * <pre>
 *   0 — 100 %  faces  (distance < 8)
 *   1 —  50 %  faces  (distance 8–18)
 *   2 —  25 %  faces  (distance 18–35)
 *   3 —  12 %  faces  (distance > 35)
 * </pre>
 */
public final class LOD {
    public static final int MAX_LEVEL = 3;

    private static final float[] THRESHOLDS = { 8f, 18f, 35f };
    private static final float[] FACTORS    = { 1f, 0.5f, 0.25f, 0.12f };

    private LOD() {}

    /** Returns the LOD level (0–3) for the given camera distance. */
    public static int level(float distance) {
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (distance < THRESHOLDS[i]) return i;
        }
        return MAX_LEVEL;
    }

    /** Returns the face fraction (0–1) for the given LOD level. */
    public static float factor(int level) {
        return FACTORS[Math.max(0, Math.min(level, MAX_LEVEL))];
    }

    /**
     * Returns a reduced copy of the face list, keeping approximately
     * {@code factor(level)} of the original faces (stride-based selection).
     */
    public static List<int[]> reduce(int[][] faces, int level) {
        if (level <= 0 || faces.length == 0) return Arrays.asList(faces);
        float f = factor(level);
        int keep = Math.max(1, (int) (faces.length * f));
        int stride = Math.max(1, faces.length / keep);
        List<int[]> out = new ArrayList<>();
        for (int i = 0; i < faces.length; i += stride) {
            out.add(faces[i]);
        }
        return out;
    }

    /** Human-readable label for the HUD. */
    public static String label(int level) {
        return "LOD " + level + " (" + (int) (factor(level) * 100) + "%)";
    }
}