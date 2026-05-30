package gamegl.entites;

import gamegl.entites.ennemis.Ennemis;
import gamegl.utils.ConfigJeu;
import gamegl.utils.ConfigVaisseau;
import learngl.camera.Camera;
import learngl.Shader;
import learngl.shape.Shape;
import learngl.VertexUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11C.*;

public class Crosshair extends Entity2D {

    private final Shape shapeCross;
    private final Shape shapeOblique;
    private final Shader shader;
    private final Camera camera;

    private final Vector3f rayOrigin = new Vector3f();
    private final Vector3f rayDir = new Vector3f();

    private int lastWidth, lastHeight;

    private float playerSpeed = 0f;

    public Crosshair(Shader shader, Camera camera) {
        this.shader = shader;
        this.camera = camera;

        ConfigVaisseau vaisseau = ConfigVaisseau.get();
        ConfigJeu cfg = ConfigJeu.get();
        float crossR = cfg.crosshairColor.x;
        float crossG = cfg.crosshairColor.y;
        float crossB = cfg.crosshairColor.z;
        float crossLen = vaisseau.crosshairGeom.x;
        float crossGap = vaisseau.crosshairGeom.y;
        float crossThick = vaisseau.crosshairGeom.z;
        shapeCross = new Shape(VertexUtils.autoAddSlotColor(createCrosshairRectangle(crossLen, crossGap, crossThick)));
        shapeCross.setColor(crossR, crossG, crossB);
        shapeCross.setShader(shader);

        shapeOblique = new Shape(VertexUtils.autoAddSlotColor(createCrosshairOblique(crossLen, crossGap, crossThick)));
        shapeOblique.setColor(crossR, crossG, crossB);
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

        addRotatedRect(v, idx, -halfGap, halfGap, -len - halfGap, topY, halfT);
        addRotatedRect(v, idx, +halfGap, halfGap, len + halfGap, topY, halfT);

        return v;
    }

    private static float[] createCrosshairRectangle(float len, float gap, float t) {
        float[] v = new float[4 * 6 * 3];
        int[] idx = new int[]{0};
        float halfT = t * 0.5f;
        float halfGap = gap * 0.5f;
        float topY = halfGap + len;

        putRect(v, idx, -halfT, halfGap, +halfT, topY);
        putRect(v, idx, -(halfGap + len), -halfT, -halfGap, +halfT);
        putRect(v, idx, +halfGap, -halfT, (halfGap + len), +halfT);
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
    public void render(Matrix4f orthoProjection) {
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        if (depth) glDisable(GL_DEPTH_TEST);

        shader.bind();

        float scaleX = (float) lastHeight / (float) lastWidth;
        float scaleY = 1.0f;

        Matrix4f model = new Matrix4f()
                .identity()
                .scale(scaleX, scaleY, 1.0f);

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

        ConfigJeu cfg = ConfigJeu.get();
        ConfigVaisseau vaisseau = ConfigVaisseau.get();

        float minDim = Math.min(width, height);

        float longueur = (minDim / cfg.crosshairRefHeight) * vaisseau.crosshairGeom.x;
        float epaisseur = (minDim / cfg.crosshairRefHeight) * vaisseau.crosshairGeom.z;
        float baseGap = (minDim / cfg.crosshairRefHeight) * vaisseau.crosshairGeom.y;

        float normalizedSpeed = Math.min(playerSpeed / vaisseau.cameraPhysics.x, 1f);
        float dynamicGap = baseGap * (1f + normalizedSpeed * vaisseau.crosshairMult.x);

        shapeCross.updatePositions(VertexUtils.autoAddSlotColor(createCrosshairRectangle(longueur, baseGap, epaisseur)));
        shapeOblique.updatePositions(VertexUtils.autoAddSlotColor(createCrosshairOblique(longueur, dynamicGap, epaisseur * vaisseau.crosshairMult.y)));
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
     * Returns a copy of the ray direction used for targeting.
     *
     * @return the ray direction vector
     */
    public Vector3f getRayDir() {
        return new Vector3f(rayDir);
    }
}
