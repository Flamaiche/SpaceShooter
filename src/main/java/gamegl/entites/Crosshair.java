package gamegl.entites;

import gamegl.entites.ennemis.Ennemis;
import learngl.tools.camera.Camera;
import learngl.tools.Shader;
import learngl.tools.shape.Shape;
import learngl.tools.VertexUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11C.*;

/**
 * A 2D crosshair overlay rendered on screen.
 * Includes a dynamic gap that widens based on the player's movement speed,
 * and raycasting to highlight the targeted enemy.
 */
public class Crosshair extends Entity2D {

    private final Shape shapeCross;
    private final Shape shapeOblique;
    private final Shader shader;
    private final Camera camera;

    private final Vector3f rayOrigin = new Vector3f();
    private final Vector3f rayDir = new Vector3f();

    private int lastWidth = 800, lastHeight = 600;

    private final float longueurSegment = 0.04f;
    private final float espaceCentral = 0.02f;
    private final float epaisseurLigne = 0.005f;

    private float playerSpeed = 0f;

    /**
     * Constructs a Crosshair with the given shader and camera.
     *
     * @param shader the shader used for rendering the crosshair
     * @param camera the camera used for orientation and roll
     */
    public Crosshair(Shader shader, Camera camera) {
        this.shader = shader;
        this.camera = camera;

        shapeCross = new Shape(VertexUtils.autoAddSlotColor(createCrosshairRectangle(longueurSegment, espaceCentral, epaisseurLigne)));
        shapeCross.setColor(1f, 0f, 1f);
        shapeCross.setShader(shader);

        shapeOblique = new Shape(VertexUtils.autoAddSlotColor(createCrosshairOblique(longueurSegment, espaceCentral, epaisseurLigne)));
        shapeOblique.setColor(1f, 0f, 1f);
        shapeOblique.setShader(shader);
    }

    /**
     * Updates the player speed used to dynamically adjust the crosshair gap.
     *
     * @param velocity the player's velocity vector
     */
    public void setPlayerSpeed(Vector3f velocity) {
        this.playerSpeed = velocity.length();
    }

    private static float[] createCrosshairOblique(float len, float gap, float t) {
        float[] v = new float[2 * 6 * 3];
        int[] idx = new int[]{0};
        float halfT = t * 0.5f;
        float halfGap = gap * 0.5f;
        float topY = halfGap + len;
        float midY = halfGap;

        addRotatedRect(v, idx, -halfGap, midY, -len - halfGap, topY, halfT);
        addRotatedRect(v, idx, +halfGap, midY, len + halfGap, topY, halfT);

        return v;
    }

    private static float[] createCrosshairRectangle(float len, float gap, float t) {
        float[] v = new float[4 * 6 * 3];
        int[] idx = new int[]{0};
        float halfT = t * 0.5f;
        float halfGap = gap * 0.5f;
        float topY = halfGap + len;
        float midY = halfGap;

        putRect(v, idx, -halfT, midY, +halfT, topY);
        putRect(v, idx, -(halfGap + len), -halfT, -halfGap, +halfT);
        putRect(v, idx, +halfGap, -halfT, +(halfGap + len), +halfT);
        putRect(v, idx, -halfT, -(halfGap + len), +halfT, -halfGap);

        return v;
    }

    private static void addRotatedRect(float[] v, int[] idx, float x1, float y1, float x2, float y2, float thickness) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        float offsetX = -dy / len * thickness / 2f;
        float offsetY = dx / len * thickness / 2f;

        float cx1 = x1 + offsetX;
        float cy1 = y1 + offsetY;
        float cx2 = x2 + offsetX;
        float cy2 = y2 + offsetY;
        float cx3 = x2 - offsetX;
        float cy3 = y2 - offsetY;
        float cx4 = x1 - offsetX;
        float cy4 = y1 - offsetY;

        int i = idx[0];
        v[i++] = cx1; v[i++] = cy1; v[i++] = 0f;
        v[i++] = cx2; v[i++] = cy2; v[i++] = 0f;
        v[i++] = cx3; v[i++] = cy3; v[i++] = 0f;

        v[i++] = cx1; v[i++] = cy1; v[i++] = 0f;
        v[i++] = cx3; v[i++] = cy3; v[i++] = 0f;
        v[i++] = cx4; v[i++] = cy4; v[i++] = 0f;

        idx[0] = i;
    }

    private static void putRect(float[] a, int[] idx, float x1, float y1, float x2, float y2) {
        int i = idx[0];
        a[i++] = x1; a[i++] = y1; a[i++] = 0f;
        a[i++] = x2; a[i++] = y1; a[i++] = 0f;
        a[i++] = x2; a[i++] = y2; a[i++] = 0f;
        a[i++] = x1; a[i++] = y1; a[i++] = 0f;
        a[i++] = x2; a[i++] = y2; a[i++] = 0f;
        a[i++] = x1; a[i++] = y2; a[i++] = 0f;
        idx[0] = i;
    }

    @Override
    /**
     * Renders the crosshair overlay. Disables depth testing so the crosshair
     * always appears on top. Applies camera roll rotation and aspect ratio scaling.
     *
     * @param orthoProjection the orthographic projection matrix
     */
    public void render(Matrix4f orthoProjection) {
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        if (depth) glDisable(GL_DEPTH_TEST);

        shader.bind();

        float scaleX = (float) lastHeight / (float) lastWidth;
        float scaleY = 1.0f;
        float rollRad = (float)Math.toRadians(camera.getRoll());

        Matrix4f model = new Matrix4f()
                .identity()
                .scale(scaleX, scaleY, 1.0f)
                .rotateZ(rollRad);

        shader.setUniformMat4f("model", model);
        shader.setUniformMat4f("view", new Matrix4f().identity());
        shader.setUniformMat4f("projection", orthoProjection);

        shapeCross.render();
        shapeOblique.render();
        shader.unbind();

        if (depth) glEnable(GL_DEPTH_TEST);
    }

    /**
     * Updates the crosshair dimensions and dynamic gap based on window size and player speed.
     *
     * @param width  the current window width
     * @param height the current window height
     */
    public void update(int width, int height) {
        lastWidth = width;
        lastHeight = height;

        float minDim = Math.min(width, height);

        float longueur = (minDim / 600f) * longueurSegment;
        float epaisseur = (minDim / 600f) * epaisseurLigne;
        float baseGap = (minDim / 600f) * espaceCentral;

        float maxSpeed = 17.5f;
        float normalizedSpeed = Math.min(playerSpeed / maxSpeed, 1f);
        float dynamicGap = baseGap * (1f + normalizedSpeed * 3f);

        shapeCross.updatePositions(VertexUtils.autoAddSlotColor(createCrosshairRectangle(longueur, baseGap, epaisseur)));
        shapeOblique.updatePositions(VertexUtils.autoAddSlotColor(createCrosshairOblique(longueur, dynamicGap, epaisseur*2.5f)));
    }

    /**
     * Raycasts against all enemies and highlights the closest one under the crosshair.
     *
     * @param ennemis the list of enemies to check
     */
    public void updateHighlightedEnemy(java.util.ArrayList<Ennemis> ennemis) {
        for (Ennemis e : ennemis) e.setHighlighted(false);

        Ennemis closest = null;
        float minDistance = Float.MAX_VALUE;

        rayOrigin.set(camera.getPosition());
        rayDir.set(camera.getFront()).normalize();

        for (Ennemis e : ennemis) {
            float t = e.getBody().intersectRayDistance(rayOrigin, rayDir, e.getModelMatrix());
            if (t >= 0 && t < minDistance) {
                minDistance = t;
                closest = e;
            }
        }

        if (closest != null) closest.setHighlighted(true);
    }

    @Override
    public void cleanup() {
        shapeCross.cleanup();
        shapeOblique.cleanup();
    }

    /**
     * Returns a copy of the ray origin used for targeting.
     *
     * @return the ray origin vector
     */
    public Vector3f getRayOrigin() {
        return new Vector3f(rayOrigin);
    }

    /**
     * Returns a copy of the ray direction used for targeting.
     *
     * @return the ray direction vector
     */
    public Vector3f getRayDir() {
        return new Vector3f(rayDir);
    }
}
