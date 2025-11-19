package gameGl.entites;

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
    public Commande cmd;

    public Joueur(Shader shader, Camera camera, Commande cmd, float tailleCorps) {
        this.camera = camera;
        this.cmd = cmd;

        this.corps = new Shape(Shape.autoAddSlotColor(PreVerticesTable.generateCubeSimple(tailleCorps)));
        this.corps.setShader(shader);

        this.shader = shader;
        this.modelMatrix = new Matrix4f().identity().translate(camera.getPosition());

        this.vie = 3;
    }

    public void update(float deltaTime) {
        cmd.update();

        Vector3f pos = camera.getPosition();
        for (float c : corps.center()) System.out.print(c + " ");
        System.out.println(" | Camera Pos: " + pos);
        for (int i = 0 ; i < corps.getVertices().length; i++) System.out.print(((i+3)%3==0?"| ":"") + corps.getVertices()[i] + " ");
        System.out.println();
        for (Vector3f v : corps.getPoints()) System.out.print(v.x + " " + v.y + " " + v.z + " | ");
        System.out.println();
        float yaw   = camera.getYaw();
        float pitch = camera.getPitch();
        float roll  = camera.getRoll();

        modelMatrix.identity()
                .translate(pos)
                .rotateX((float) Math.toRadians(pitch))
                .rotateY((float) Math.toRadians(yaw))
                .rotateZ((float) Math.toRadians(roll));
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
            if (!(e instanceof Joueur) && !(e instanceof Ball)) {
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
}
