package gamegl.entites;

import gamegl.entites.balls.Balls;
import learngl.tools.shape.PreVerticesTable;
import learngl.tools.camera.Camera;
import learngl.tools.commandes.Commande;
import learngl.tools.Shader;
import learngl.tools.shape.Shape;
import learngl.tools.VertexUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11C.*;

public class Joueur extends Entity {
    private Shape corps;
    private Shader shader;
    private Matrix4f modelMatrix;
    private int vie;
    private Vector3f position;
    private boolean visible = true;

    public Commande cmd;

    public Joueur(Shader shader, Commande cmd, float tailleCorps) {
        this.cmd = cmd;
        this.position = new Vector3f(0, 0, 0);

        this.corps = new Shape(VertexUtils.autoAddSlotColor(PreVerticesTable.generatePlayerShip(tailleCorps)));
        this.corps.setShader(shader);

        this.shader = shader;
        this.modelMatrix = new Matrix4f().identity().translate(position);

        this.vie = 3;
    }

    @Override
    public void update(float deltaTime) {}

    public void update(float deltaTime, Camera camera) {
        cmd.update();

        Vector3f front = camera.getFront();
        Vector3f up = camera.getUp();

        if (front.lengthSquared() < 1e-6f) front.set(0, 0, -1);
        if (up.lengthSquared() < 1e-6f) up.set(0, 1, 0);

        Matrix4f rot = new Matrix4f()
                .lookAt(new Vector3f(0, 0, 0), new Vector3f(front), new Vector3f(up))
                .invert();

        modelMatrix.identity()
                .translate(position)
                .mul(rot);
    }

    public void render(Matrix4f view, Matrix4f projection) {
        if (!visible) return;
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

    public void setVisible(boolean v) { visible = v; }

    public void cleanup() {
        corps.cleanup();
    }

    public Entity checkCollision(ArrayList<Entity> entities) {
        for (Entity e : entities) {
            if (!(e instanceof Joueur) && !(e instanceof Balls)) {
                if (corps.intersectsOptimized(e.getBody(), modelMatrix, e.getModelMatrix()))
                    return e;
            }
        }
        return null;
    }

    public void setVie(int v) { vie = v; }
    public int getVie() { return vie; }
    public void decrementVie() { if (vie > 0) vie--; }

    public Matrix4f getModelMatrix() { return modelMatrix; }
    public Shape getBody() { return corps; }
    public Vector3f getPosition() { return position; }

    public void setPosition(Vector3f pos) { position.set(pos); }
}
