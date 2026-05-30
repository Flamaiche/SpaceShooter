package learngl.camera.vue;

import gamegl.utils.ConfigVaisseau;
import learngl.camera.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class GestionnaireVue {

    public enum ModeVue {
        PREMIERE_PERSONNE {
            public Vector3f offsetVisuel() { return new Vector3f(); }
        },
        TROISIEME_PERSONNE {
            public Vector3f offsetVisuel() { return new Vector3f(ConfigVaisseau.get().thirdPersonOffset); }
        },
        TROISIEME_PERSONNE_AVANT {
            public Vector3f offsetVisuel() {
                Vector3f off = new Vector3f(ConfigVaisseau.get().thirdPersonOffset);
                return off.mul(-1, 1, 1);
            }
        };

        public abstract Vector3f offsetVisuel();

        public ModeVue suivant(int pas) {
            ModeVue[] vals = values();
            return vals[((ordinal() + pas) % vals.length + vals.length) % vals.length];
        }
    }

    private ModeVue modeActuel = ModeVue.PREMIERE_PERSONNE;
    private final Vector3f offsetReel = new Vector3f();
    private final Vector3f offsetFactice = new Vector3f();

    public GestionnaireVue() {
        ConfigVaisseau cfg = ConfigVaisseau.get();
        offsetReel.set(cfg.thirdPersonOffset);
    }

    public void mettreAJour(Camera camera, Vector3f posJoueur) {
        mettreAJour(camera, posJoueur, 1);
    }

    public void mettreAJour(Camera camera, Vector3f posJoueur, int pas) {
        modeActuel = modeActuel.suivant(pas);
        if (pas != 0) {
            Vector3f front = camera.getFront();
            Vector3f right = camera.getRight();
            Vector3f offsetVis = modeActuel.offsetVisuel();
            offsetFactice.set(front).negate().mul(offsetVis.x)
                    .add(0, offsetVis.y, 0)
                    .add(new Vector3f(right).mul(offsetVis.z));
        }
        camera.setPosition(new Vector3f(posJoueur).add(offsetReel));
    }

    public Matrix4f obtenirVue(Camera camera, Vector3f posJoueur) {
        Vector3f posFactice = new Vector3f(posJoueur).add(offsetFactice);
        if (modeActuel == ModeVue.TROISIEME_PERSONNE_AVANT) {
            return new Matrix4f().lookAt(posFactice, posJoueur, new Vector3f(0, 1, 0));
        }
        return new Matrix4f().lookAt(posFactice, new Vector3f(posFactice).add(camera.getFront()), camera.getUp());
    }

    public boolean estPremierePersonne() {
        return modeActuel == ModeVue.PREMIERE_PERSONNE;
    }

    public ModeVue getMode() { return modeActuel; }

}
