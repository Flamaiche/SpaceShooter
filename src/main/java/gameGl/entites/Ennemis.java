package gameGl.entites;

import learnGL.tools.Camera;
import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11C.*;

public class Ennemis extends Entity {
    private static Random rand = new Random();
    private float spawnSize = 10f;
    private float exclusionSize = 5f;

    private Shape corps;
    private Shader shader;
    private Vector3f position;
    private Vector3f direction;
    private Vector3f target;
    public float speed = 2.5f;
    public static float despawnDistance = 150f;
    private boolean highlighted = false;

    public final int MAX_VIE = 1;
    private int vie = MAX_VIE;
    private int score = 10;
    private final float RESPAWN_TIME_MIN = 1f;
    private final float RESPAWN_TIME_MAX = 9f;
    private float respawn_time = -1f;
    private float deathTime = -1f;

    private final int moduloMutationDeltaTime = 6;

    public Ennemis(Shader shader, float[] centerPlayer, float[] verticesShape, Camera camera) {
        corps = new Shape(Shape.autoAddSlotColor(verticesShape));
        corps.setShader(shader);
        corps.setColor(0f,0f,0f);
        this.shader = shader;
        setDeplacement(centerPlayer);
        updateModelMatrix();
    }

    public void setDeplacement(float[] centerPlayer) {
        float[] coors = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        position = new Vector3f(coors[0], coors[1], coors[2]);

        float[] targetCoords = generateSpawn(centerPlayer[0], centerPlayer[1], centerPlayer[2]);
        target = new Vector3f(targetCoords[0], targetCoords[1], targetCoords[2]);

        direction = new Vector3f(target).sub(position).normalize();
        updateModelMatrix();
    }

    public float[] generateSpawn(float playerX, float playerY, float playerZ) {
        float x,y,z;
        do {
            x = rand.nextFloat() * (2*spawnSize) - spawnSize;
            y = rand.nextFloat() * (2*spawnSize) - spawnSize;
            z = rand.nextFloat() * (2*spawnSize) - spawnSize;
        } while (x > -exclusionSize && x < exclusionSize &&
                y > -exclusionSize && y < exclusionSize &&
                z > -exclusionSize && z < exclusionSize);
        return new float[]{playerX + x,playerY + y,playerZ + z};
    }

    public boolean shouldDespawn(Vector3f cameraPos) {
        return position.distance(cameraPos) > despawnDistance;
    }

    public void update(float deltaTime) {
        if (deltaTime%moduloMutationDeltaTime == 0) mutation();
        if (vie <= 0) {
            if (deathTime < 0) {
                deathTime = (float) glfwGetTime(); // on note le moment de la mort
                respawn_time = rand.nextFloat(RESPAWN_TIME_MAX - RESPAWN_TIME_MIN + 1) + RESPAWN_TIME_MIN;
            } else {
                float currentTime = (float) glfwGetTime();
                if (currentTime - deathTime >= respawn_time) {
                    resetVie();
                    deathTime = -1f; // on réinitialise
                }
            }
            return; // pas de déplacement tant qu'il est mort
        }

        Vector3f deplace = new Vector3f(direction).mul(speed * deltaTime);
        position.add(deplace);
        updateModelMatrix();
    }

    private void updateModelMatrix() {
        modelMatrix.identity().translate(position);
    }

    public void render(Matrix4f view, Matrix4f projection) {
        if (vie <= 0) return;
        if (!corps.isVisible(projection, view, modelMatrix)) {
            return;
        }
        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", modelMatrix);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        corps.setColor(0f,0f,0f);
        corps.render();

        if (highlighted) {
            Matrix4f outlineModel = new Matrix4f(modelMatrix).scale(1.05f);
            shader.setUniformMat4f("model", outlineModel);

            glEnable(GL_DEPTH_TEST);
            glDepthMask(false);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glLineWidth(2.5f);
            corps.setColor(1f,0f,0f);
            corps.render();

            glDepthMask(true);
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            shader.setUniformMat4f("model", modelMatrix);
        }

        shader.unbind();
    }

    public int touched() {
        decrementVie();
        if (getVie() <= 0) {
            setDeplacement(new float[]{getDespawnDistance()*2, getDespawnDistance()*2, getDespawnDistance()*2});
            return getScore();
        }
        return 0;
    }

    public void cleanup() { corps.cleanup(); }

    public Shape getCorps() { return corps; }
    public int getVie() { return vie; }
    public void decrementVie() { if (vie>0) vie--; }
    public void resetVie() { vie = MAX_VIE; }
    public int getScore() { return score; }
    public float getDespawnDistance() { return despawnDistance; }
    public void setHighlighted(boolean h) { highlighted = h; }

    public static void setDespawnDistance(float d) { despawnDistance = d; }
    public void setSpeed(float s) { speed = s; }

    public boolean isHighlighted() {
        return highlighted;
    }

    public Vector3f getPosition() { return position;}

    public void mutation() {
        float MUTATIONVITESSE = 0.02f;
        float MUTATIONTAILLE = 0.02f;
        float MUTATIONSLEEP = 0.03f;
        float MUTATIONSHAPE = 0.01f;

        if (testMutation(MUTATIONVITESSE))
            speed *= 1f + rand.nextFloat() * rand.nextFloat();
        else if (!testMutation(100 - MUTATIONVITESSE))
            speed *= 1f - rand.nextFloat() * rand.nextFloat();

        if (testMutation(MUTATIONTAILLE))
            corps.setScale((1f + rand.nextFloat() / 6f));
        else if (!testMutation(100 - MUTATIONTAILLE))
            corps.setScale((1f - rand.nextFloat() / 6f));

        if (testMutation(MUTATIONSLEEP))
            respawn_time *= 1f + rand.nextFloat();
        else if (!testMutation(100 - MUTATIONSLEEP))
            respawn_time *= Math.min(0.00001f, 1f - rand.nextFloat());

        if (testMutation(MUTATIONSHAPE)) {
            ArrayList<Vector3f> points = new ArrayList<>(corps.getPoints());
            int n = points.size();
            if (n >= 3) {
                // Choisir un point de base aléatoire
                int baseIndex = rand.nextInt(n);
                Vector3f basePoint = points.get(baseIndex);

                // Créer sommet de la pyramide au-dessus du point de base
                float[] centerArray = corps.center();
                Vector3f center = new Vector3f(centerArray[0], centerArray[1], centerArray[2]);
                Vector3f dir = new Vector3f(basePoint).sub(center);
                if (dir.length() < 1e-6f)
                    dir.set(rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f, rand.nextFloat() - 0.5f);
                dir.normalize();
                Vector3f newVertex = new Vector3f(basePoint).add(dir.mul(Math.max(0.25f, basePoint.distance(center) * rand.nextFloat() * 0.10f)));

                // Construire faces de la pyramide
                Set<Integer> voisinsListVisite = new HashSet<>();
                Set<Integer> voisinsList;
                int parcours = 2;
                int lastIndex = baseIndex;
                float proba = 0.1f;
                voisinsListVisite.add(baseIndex);
                while (parcours > 0 || proba > rand.nextFloat()) {
                    points = new ArrayList<>(corps.getPoints());
                    voisinsList = giveVoisin(lastIndex, voisinsListVisite);
                    parcours --;
                    if (voisinsList.isEmpty()) break;

                    List<Integer> voisinsAsList = new ArrayList<>(voisinsList);
                    int v = voisinsAsList.get(rand.nextInt(voisinsAsList.size()));

                    ArrayList<Vector3f> connectTo = new ArrayList<>();
                    connectTo.add(points.get(lastIndex));   // point précédent
                    connectTo.add(points.get(v));           // voisin actuel
                    connectTo.add(newVertex);               // sommet de la pyramide
                    corps.addPoint(newVertex, connectTo);
                    lastIndex = v;
                    voisinsListVisite.add(v);
                }

                // Fermer la dernière face pour former un cycle
                ArrayList<Vector3f> connectTo = new ArrayList<>();
                connectTo.add(points.get(lastIndex));
                connectTo.add(basePoint);
                connectTo.add(newVertex);
                corps.addPoint(newVertex, connectTo);
            }
        } else if (!testMutation(100 - MUTATIONSHAPE)) {
            ArrayList<Vector3f> points = new ArrayList<>(corps.getPoints());
            int n = points.size();
            if (n > 4) {
                int i = rand.nextInt(n);
                Vector3f p = points.get(i);
                if (p.distance(new Vector3f(corps.center()[0], corps.center()[1], corps.center()[2])) > 1e-6f) {
                    corps.removePoint(p);
                }
            }
        }
    }

    private Set<Integer> giveVoisin(int indexPoint, Set<Integer> dejaPris) {
        Set<Integer> voisinsSet = corps.getVertexStructure().getVoisins(indexPoint);
        for (int dp : dejaPris) voisinsSet.remove(dp);
        return voisinsSet;
    }

    private boolean testMutation(float chance) {
        return rand.nextFloat() * 100 < chance;
    }
}
