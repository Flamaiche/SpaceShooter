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
    private final float[] rectFull = new float[4 * 6 * VertexUtils.FLOATS_PER_VERTEX];
    private final float[] obliqueFull = new float[2 * 6 * VertexUtils.FLOATS_PER_VERTEX];

    private float playerSpeed = 0f;

    private final Vector3f crosshairDir = new Vector3f(0, 0, -1);
    private final Vector3f crosshairVel = new Vector3f();

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

    public void initDirection() {
        crosshairDir.set(camera.getFront());
        crosshairVel.set(0);
    }

    public void updateLag(Vector3f targetDir, float deltaTime) {
        ConfigVaisseau cfg = ConfigVaisseau.get();

        Vector3f error = new Vector3f(targetDir).sub(crosshairDir);
        float errorMag = error.length();

        float forceMag = cfg.crosshairStiffness * errorMag;
        Vector3f force = new Vector3f(error).mul(forceMag);

        force.add(new Vector3f(crosshairVel).mul(-cfg.crosshairLagDamping));

        crosshairVel.fma(deltaTime, force);

        float speed = crosshairVel.length();
        if (speed > cfg.crosshairLagMaxSpeed) {
            crosshairVel.mul(cfg.crosshairLagMaxSpeed / speed);
        }

        crosshairDir.fma(deltaTime, crosshairVel).normalize();
    }

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

    private static void putRectFull(float[] a, int[] idx, float x1, float y1, float x2, float y2) {
        int i = idx[0];
        for (int v = 0; v < 6; v++) {
            float px, py;
            switch (v) {
                case 0: px = x1; py = y1; break;
                case 1: px = x2; py = y1; break;
                case 2: px = x2; py = y2; break;
                case 3: px = x1; py = y1; break;
                case 4: px = x2; py = y2; break;
                default: px = x1; py = y2; break;
            }
            a[i++] = px; a[i++] = py; a[i++] = 0f;
            a[i++] = 1f; a[i++] = 1f; a[i++] = 1f;
            a[i++] = 0f; a[i++] = 0f;
        }
        idx[0] = i;
    }

    private static void addRotatedRectFull(float[] v, int[] idx, float x1, float y1, float x2, float y2, float thickness) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        float offsetX = -dy / len * thickness / 2f;
        float offsetY = dx / len * thickness / 2f;

        float cx1 = x1 + offsetX, cy1 = y1 + offsetY;
        float cx2 = x2 + offsetX, cy2 = y2 + offsetY;
        float cx3 = x2 - offsetX, cy3 = y2 - offsetY;
        float cx4 = x1 - offsetX, cy4 = y1 - offsetY;

        int i = idx[0];
        float[][] verts = {{cx1, cy1}, {cx2, cy2}, {cx3, cy3},
                           {cx1, cy1}, {cx3, cy3}, {cx4, cy4}};
        for (float[] p : verts) {
            v[i++] = p[0]; v[i++] = p[1]; v[i++] = 0f;
            v[i++] = 1f; v[i++] = 1f; v[i++] = 1f;
            v[i++] = 0f; v[i++] = 0f;
        }
        idx[0] = i;
    }

    @Override
    public void render(Matrix4f orthoProjection) {
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        if (depth) glDisable(GL_DEPTH_TEST);

        shader.bind();

        Vector3f front = camera.getFront();
        Vector3f right = camera.getRight();
        Vector3f up = camera.getUp();

        float x = crosshairDir.dot(right);
        float y = crosshairDir.dot(up);
        float z = crosshairDir.dot(front);
        float halfFovTan = (float)Math.tan(Math.toRadians(ConfigJeu.get().fov / 2));
        float aspect = (float) lastWidth / (float) lastHeight;
        float ndcX = Math.max(-1f, Math.min(1f, x / (z * halfFovTan * aspect)));
        float ndcY = Math.max(-1f, Math.min(1f, y / (z * halfFovTan)));

        float scaleX = (float) lastHeight / (float) lastWidth;
        float scaleY = 1.0f;

        Matrix4f model = new Matrix4f()
                .identity()
                .translate(ndcX, ndcY, 0)
                .scale(scaleX, scaleY, 1.0f);

        shader.setUniformMat4f("model", model);
        shader.setUniformMat4f("view", new Matrix4f().identity());
        shader.setUniformMat4f("projection", orthoProjection);

        shapeCross.render();
        shapeOblique.render();
        shader.unbind();

        if (depth) glEnable(GL_DEPTH_TEST);
    }

    private void fillRectFull(float len, float gap, float t, float[] out) {
        int[] idx = new int[]{0};
        float halfT = t * 0.5f;
        float halfGap = gap * 0.5f;
        float topY = halfGap + len;
        putRectFull(out, idx, -halfT, halfGap, +halfT, topY);
        putRectFull(out, idx, -(halfGap + len), -halfT, -halfGap, +halfT);
        putRectFull(out, idx, +halfGap, -halfT, (halfGap + len), +halfT);
        putRectFull(out, idx, -halfT, -(halfGap + len), +halfT, -halfGap);
    }

    private void fillObliqueFull(float len, float gap, float t, float[] out) {
        int[] idx = new int[]{0};
        float halfT = t * 0.5f;
        float halfGap = gap * 0.5f;
        float topY = halfGap + len;
        addRotatedRectFull(out, idx, -halfGap, halfGap, -len - halfGap, topY, halfT);
        addRotatedRectFull(out, idx, +halfGap, halfGap, len + halfGap, topY, halfT);
    }

    @Override
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

        fillRectFull(longueur, baseGap, epaisseur, rectFull);
        shapeCross.updatePositions(rectFull);
        fillObliqueFull(longueur, dynamicGap, epaisseur * vaisseau.crosshairMult.y, obliqueFull);
        shapeOblique.updatePositions(obliqueFull);
    }

    public void setRayOrigin(Vector3f origin) {
        rayOrigin.set(origin);
    }

    public void updateHighlightedEnemy(java.util.ArrayList<Ennemis> ennemis) {
        for (Ennemis e : ennemis) e.setHighlighted(false);

        Ennemis closest = null;
        float minDistance = Float.MAX_VALUE;

        rayDir.set(crosshairDir).normalize();

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

    public Vector3f getRayDir() {
        return new Vector3f(crosshairDir);
    }
}
