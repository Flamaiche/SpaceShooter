package gameGl.entites;

import learnGL.tools.Shape;
import learnGL.tools.Shader;
import learnGL.tools.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11C.*;

public class Crosshair extends Entity2D {

    private final Shape shape;
    private final Shader shader;
    private final Camera camera;

    private final Vector3f rayOrigin = new Vector3f();
    private final Vector3f rayDir = new Vector3f();

    // Dimensions actuelles de la fenêtre
    private int lastWidth = 800, lastHeight = 600;

    // Paramètres de base du réticule (en unités OpenGL)
    private final float longueurSegment = 0.04f;    // Longueur des 4 branches
    private final float espaceCentral = 0.02f;      // Espace vide au centre
    private final float epaisseurLigne = 0.005f;    // Épaisseur des lignes

    public Crosshair(Shader shader, Camera camera) {
        this.shader = shader;
        this.camera = camera;


        // Création initiale du crosshair centré
        float[] verts = createCrosshairRotated(longueurSegment, espaceCentral, epaisseurLigne);
        verts = Shape.autoAddSlotColor(verts); // Ajout des slots pour la couleur RGBA

        shape = new Shape(verts);
        shape.setColor(1f, 0f, 1f, 1f); // Violet opaque
        shape.setShader(shader);
    }

    /** Crosshair avec diagonales réelles inclinées en GL_TRIANGLES */
    private static float[] createCrosshairRotated(float len, float gap, float t) {
        float[] v = new float[48 * 3]; // 6 rectangles
        int[] idx = new int[]{0};

        float halfT = t * 0.5f;
        float halfGap = gap * 0.5f;

        float topY = halfGap + len;
        float midY = halfGap;

        // Barre verticale centrale |
        putRect(v, idx, -halfT, midY, +halfT, topY);

        // Diagonale gauche \ (calcul des coins après rotation)
        addRotatedRect(v, idx, -halfGap, midY, -len - halfGap, topY, halfT);

        // Diagonale droite / (calcul des coins après rotation)
        addRotatedRect(v, idx, +halfGap, midY, len + halfGap, topY, halfT);

        // Branches classiques
        putRect(v, idx, -(halfGap + len), -halfT, -halfGap, +halfT); // gauche
        putRect(v, idx, +halfGap, -halfT, +(halfGap + len), +halfT); // droite
        putRect(v, idx, -halfT, -(halfGap + len), +halfT, -halfGap); // bas

        return v;
    }

    /** Crée un rectangle incliné entre deux points (x1,y1 -> x2,y2) avec épaisseur t */
    private static void addRotatedRect(float[] v, int[] idx, float x1, float y1, float x2, float y2, float thickness) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        float offsetX = -dy / len * thickness / 2f;
        float offsetY = dx / len * thickness / 2f;

        // Quatre coins
        float cx1 = x1 + offsetX;
        float cy1 = y1 + offsetY;
        float cx2 = x2 + offsetX;
        float cy2 = y2 + offsetY;
        float cx3 = x2 - offsetX;
        float cy3 = y2 - offsetY;
        float cx4 = x1 - offsetX;
        float cy4 = y1 - offsetY;

        int i = idx[0];
        // Deux triangles
        v[i++] = cx1; v[i++] = cy1; v[i++] = 0f;
        v[i++] = cx2; v[i++] = cy2; v[i++] = 0f;
        v[i++] = cx3; v[i++] = cy3; v[i++] = 0f;

        v[i++] = cx1; v[i++] = cy1; v[i++] = 0f;
        v[i++] = cx3; v[i++] = cy3; v[i++] = 0f;
        v[i++] = cx4; v[i++] = cy4; v[i++] = 0f;

        idx[0] = i;
    }



    /** Ajoute un rectangle dans le tableau de vertices */
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

    /** Rendu du crosshair */
    @Override
    public void render(Matrix4f orthoProjection) {
        boolean depth = glIsEnabled(GL_DEPTH_TEST);
        if (depth) glDisable(GL_DEPTH_TEST); // HUD doit être au-dessus de tout

        shader.bind();

        // Correction du ratio pour éviter la déformation
        float scaleX = (float) lastHeight / (float) lastWidth; // ajustement horizontal
        float scaleY = 1.0f;                                   // vertical reste normal
        float rollRad = (float)Math.toRadians(camera.getRoll()); // angle de roll en radians


        Matrix4f model = new Matrix4f()
                .identity()
                .scale(scaleX, scaleY, 1.0f) // conserve un crosshair carré
                .rotateZ(rollRad); // compense le roll de la caméra

        shader.setUniformMat4f("model", model);
        shader.setUniformMat4f("view", new Matrix4f().identity());
        shader.setUniformMat4f("projection", orthoProjection);

        shape.render();
        shader.unbind();

        if (depth) glEnable(GL_DEPTH_TEST); // réactiver la profondeur
    }

    /** Met à jour dynamiquement la taille du crosshair selon la fenêtre */
    public void update(int width, int height) {
        // Ne rien faire si la taille est inchangée
        if (width == lastWidth && height == lastHeight) return;

        lastWidth = width;
        lastHeight = height;

        // Utilise la plus petite dimension pour garder les proportions
        float minDim = Math.min(width, height);

        // Calcule les dimensions proportionnelles
        float longueur = (minDim / 600f) * longueurSegment;
        float espace = (minDim / 600f) * espaceCentral;
        float epaisseur = (minDim / 600f) * epaisseurLigne;

        // Génération des nouvelles positions
        float[] newVerts = createCrosshairRotated(longueur, espace, epaisseur);
        newVerts = Shape.autoAddSlotColor(newVerts);

        // Mise à jour dans le VBO via Shape
        shape.updatePositions(newVerts);
    }

    /** Détecte et met en surbrillance l'ennemi le plus proche au centre de l'écran */
    public void updateHighlightedEnemy(java.util.ArrayList<Ennemis> ennemis) {
        for (Ennemis e : ennemis) e.setHighlighted(false);

        Ennemis closest = null;
        float minDistance = Float.MAX_VALUE;

        rayOrigin.set(camera.getPosition());
        rayDir.set(camera.getFront()).normalize();

        for (Ennemis e : ennemis) {
            float t = e.getCorps().intersectRayDistance(rayOrigin, rayDir, e.getModelMatrix());
            if (t >= 0 && t < minDistance) {
                minDistance = t;
                closest = e;
            }
        }

        if (closest != null) closest.setHighlighted(true);
    }

    @Override
    public void cleanup() {
        shape.cleanup();
    }
}
