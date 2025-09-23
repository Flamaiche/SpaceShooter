package gameGl.entites;

import learnGL.tools.Camera;
import learnGL.tools.Commande;
import learnGL.tools.Shader;
import learnGL.tools.Shape;
import gameGl.utils.PreVerticesTable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11C.*;

public class Joueur extends Entity {
    private Shape corps;        // corps = joueur
    private Shader shader;
    private Vector3f position;
    private Matrix4f modelMatrix;
    private int vie;

    public Commande cmd;
    private Camera camera;

    public Joueur(Shader shader, Camera camera, long window, float taillecorps) {
        cmd = new Commande(camera, window);
        this.corps = new Shape(Shape.autoAddSlotColor(PreVerticesTable.generateCubeSimple(taillecorps)));
        this.corps.setShader(shader);
        this.corps.setColor(0f, 0f, 0f, 1f); // invisible ou debug

        this.shader = shader;
        this.position = new Vector3f(0f, 0f, 0f);
        this.modelMatrix = new Matrix4f().identity().translate(position);

        this.camera = camera;
        vie = 3;
    }

    public void update(float deltaTime) {
        cmd.update();
        position.set(camera.getPosition());
        modelMatrix.identity().translate(position);
    }

    public void render(Matrix4f view, Matrix4f projection) {
        if (!corps.isVisible(projection, view, modelMatrix)) {
            return;
        }

        shader.bind();
        shader.setUniformMat4f("view", view);
        shader.setUniformMat4f("projection", projection);
        shader.setUniformMat4f("model", modelMatrix);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        corps.setColor(0f, 0f, 0f, 1f);
        corps.render();

        shader.unbind();
    }

    public void cleanup(){
        corps.cleanup();
    }

    public Entity checkCollision(ArrayList<Entity> entities){
        for(Entity e : entities){
            if (!(e instanceof Joueur) && !(e instanceof Ball)) {
                if (corps.intersectsOptimized(e.getCorps(), modelMatrix, e.getModelMatrix())) return e;
            }
        }
        return null;
    }

    public void setVie(int v) { vie = v; }
    public int getVie() { return vie;}
    public void decrementVie() { if (vie>0) vie--;}
}
