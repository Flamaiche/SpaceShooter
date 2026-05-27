package gamegl.entites.ennemis;

import learngl.tools.Camera;
import learngl.tools.Shader;

public class EnnemisBasic extends Ennemis {
    public EnnemisBasic(Shader shader, float[] centerPlayer, float[] verticesShape, Camera camera) {
        super(shader, centerPlayer, verticesShape, camera);
    }
}
