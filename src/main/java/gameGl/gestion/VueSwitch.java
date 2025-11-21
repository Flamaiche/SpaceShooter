package gameGl.gestion;

import gameGl.entites.Joueur;
import learnGL.tools.Camera;
import org.joml.Vector3f;

public class VueSwitch {

    enum Mode {
        FIRST_PERSON,
        THIRD_PERSON;

        public Vector3f getDistance() {
            return this == FIRST_PERSON ? new Vector3f(0f, 0f, 0f) : new Vector3f(0f, 2f, 5f);
        }
    }

    private Mode mode = Mode.FIRST_PERSON;
    private static final VueSwitch instance = new VueSwitch();

    private VueSwitch() {}

    public static VueSwitch getInstance() {
        return instance;
    }

    public void switchMode(Camera camera, Joueur joueur) {
        Vector3f playerPos = joueur.getLastPos();
        camera.move(new Vector3f(playerPos).sub(camera.getPosition()));

        if (mode == Mode.FIRST_PERSON) {
            mode = Mode.THIRD_PERSON;
        } else {
            mode = Mode.FIRST_PERSON;
        }

        camera.move(mode.getDistance());
        joueur.setLastPos(camera.getPosition());
    }
}
