package learngl.tools.camera.vue;

import gamegl.utils.ConfigVaisseau;
import learngl.tools.camera.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.List;

public class GestionnaireVue {

    public enum ModeVue {
        PREMIERE_PERSONNE {
            public List<Vector3f> params() {
                return List.of(new Vector3f(), new Vector3f());
            }
        },
        TROISIEME_PERSONNE {
            public List<Vector3f> params() {
                ConfigVaisseau cfg = ConfigVaisseau.get();
                return List.of(new Vector3f(cfg.thirdPersonOffset), new Vector3f());
            }
        },
        TROISIEME_PERSONNE_AVANT {
            public List<Vector3f> params() {
                ConfigVaisseau cfg = ConfigVaisseau.get();
                return List.of(new Vector3f(cfg.thirdPersonOffset).mul(-1, 1, 1), new Vector3f(0, 0, 1));
            }
        };

        public abstract List<Vector3f> params();

        public ModeVue suivant(int pas) {
            ModeVue[] vals = values();
            return vals[((ordinal() + pas) % vals.length + vals.length) % vals.length];
        }
    }

    private ModeVue modeActuel = ModeVue.PREMIERE_PERSONNE;
    private final Vector3f worldOffset = new Vector3f();

    public void mettreAJour(Camera camera, Vector3f posJoueur) {
        mettreAJour(camera, posJoueur, 1);
    }

    public void mettreAJour(Camera camera, Vector3f posJoueur, int pas) {
        modeActuel = modeActuel.suivant(pas);
        if (pas != 0) {
            Vector3f front = camera.getFront();
            Vector3f right = camera.getRight();
            Vector3f offsetPos = modeActuel.params().getFirst();
            worldOffset.set(front).negate().mul(offsetPos.x)
                    .add(0, offsetPos.y, 0)
                    .add(new Vector3f(right).mul(offsetPos.z));
        }
        camera.setPosition(new Vector3f(posJoueur).add(worldOffset));
    }

    public Matrix4f obtenirVue(Camera camera, Vector3f posJoueur) {
        List<Vector3f> p = modeActuel.params();
        Vector3f regard = p.get(1);
        if (regard.z == 1) {
            return new Matrix4f().lookAt(
                    camera.getPosition(),
                    posJoueur,
                    new Vector3f(0, 1, 0)
            );
        }
        return camera.getViewMatrix();
    }

    public boolean estPremierePersonne() {
        return modeActuel == ModeVue.PREMIERE_PERSONNE;
    }

}
