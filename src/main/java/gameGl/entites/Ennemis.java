package gameGl.entites;

import learnGL.tools.Camera;
import learnGL.tools.Shader;
import learnGL.tools.Shape;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.Random;

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
    public static float speed = 2.5f;
    public static float despawnDistance = 150f;
    private boolean highlighted = false;

    public final int MAX_VIE = 1;
    private int vie = MAX_VIE;
    private int score = 10;
    private final float RESPAWN_TIME = 5f;
    private float deathTime = -1f;

    public Ennemis(Shader shader, float[] centerPlayer, float[] verticesShape, Camera camera) {
        corps = new Shape(Shape.autoAddSlotColor(verticesShape));
        corps.setShader(shader);
        corps.setColor(0f,0f,0f,1f);
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
        if (vie <= 0) {
            if (deathTime < 0) {
                deathTime = (float) glfwGetTime(); // on note le moment de la mort
            } else {
                float currentTime = (float) glfwGetTime();
                if (currentTime - deathTime >= RESPAWN_TIME) {
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
        corps.setColor(0f,0f,0f,1f);
        corps.render();

        if (highlighted) {
            Matrix4f outlineModel = new Matrix4f(modelMatrix).scale(1.05f);
            shader.setUniformMat4f("model", outlineModel);

            glEnable(GL_DEPTH_TEST);
            glDepthMask(false);
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glLineWidth(2.5f);
            corps.setColor(1f,0f,0f,1f);
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
    public static void setSpeed(float s) { speed = s; }

    public boolean isHighlighted() {
        return highlighted;
    }

    public Vector3f getPosition() { return position;}
}
