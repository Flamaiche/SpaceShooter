package gameGl.entites.Ennemis;

import learnGL.tools.Camera;
import learnGL.tools.Shader;

public class EnnemisBasic extends Ennemis {
    public EnnemisBasic(Shader shader, float[] centerPlayer, float[] verticesShape, Camera camera) {
        super(shader, centerPlayer, verticesShape, camera);
    }
}
