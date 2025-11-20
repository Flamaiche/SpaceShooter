package gameGl.entites;

import gameGl.entites.Balls.Balls;
import learnGL.tools.Camera;
import learnGL.tools.commandes.Commande;
import learnGL.tools.Shader;
import learnGL.tools.Shape;
import gameGl.utils.PreVerticesTable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11C.*;

public class Joueur extends Entity {
    private Shape corps;
    private Shader shader;
    private Matrix4f modelMatrix;
    private int vie;

    private Camera camera;
    private Vector3f lastPos = new Vector3f();
    public Commande cmd;

    public Joueur(Shader shader, Camera camera, Commande cmd, float tailleCorps) {
        this.camera = camera;
        this.cmd = cmd;

        this.corps = new Shape(Shape.autoAddSlotColor(PreVerticesTable.generateCubeSimple(tailleCorps)));
        this.corps.setShader(shader);

        this.shader = shader;
        this.modelMatrix = new Matrix4f().identity().translate(camera.getPosition());

        this.lastPos = camera.getPosition();

        this.vie = 3;
    }

    public void update(float deltaTime) {
        cmd.update();

        modelMatrix.identity().translate(distanceVectorTo(camera.getPosition()));
    }

    public void render(Matrix4f view, Matrix4f projection) {
        if (!corps.isVisible(projection, view, modelMatrix)) return;

        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", modelMatrix);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        corps.setColor(0f, 1f, 0f);
        corps.render();

        shader.unbind();
    }

    public void cleanup() {
        corps.cleanup();
    }

    public Entity checkCollision(ArrayList<Entity> entities) {
        for (Entity e : entities) {
            if (!(e instanceof Joueur) && !(e instanceof Balls)) {
                if (corps.intersectsOptimized(e.getCorps(), modelMatrix, e.getModelMatrix()))
                    return e;
            }
        }
        return null;
    }

    // --- Gestion de la vie ---
    public void setVie(int v) { vie = v; }
    public int getVie() { return vie; }
    public void decrementVie() { if (vie > 0) vie--; }

    // --- Utilitaires ---
    public Matrix4f getModelMatrix() { return modelMatrix; }
    public Shape getCorps() { return corps; }
    public Vector3f distanceVectorTo(Vector3f point) {
        return new Vector3f(point).sub(lastPos);
    }
    public Vector3f getLastPos() {return lastPos; }
}
