package learngl.tools.camera.vue;

import learngl.tools.camera.Camera;
import org.joml.Vector3f;

public class GestionnaireVue {

    public enum ModeVue {
        PREMIERE_PERSONNE {
            public Vector3f params() { return new Vector3f(); }
        },
        TROISIEME_PERSONNE {
            public Vector3f params() { return new Vector3f(3.0f, 1.0f, 0); }
        };

        public abstract Vector3f params();

        public ModeVue suivant(int pas) {
            ModeVue[] vals = values();
            return vals[((ordinal() + pas) % vals.length + vals.length) % vals.length];
        }
    }

    private ModeVue modeActuel = ModeVue.PREMIERE_PERSONNE;

    public void mettreAJour(Camera camera, Vector3f posJoueur) {
        mettreAJour(camera, posJoueur, 1);
    }

    public void mettreAJour(Camera camera, Vector3f posJoueur, int pas) {
        modeActuel = modeActuel.suivant(pas);
        Vector3f p = modeActuel.params();
        Vector3f offset = new Vector3f(camera.getFront())
                .negate().mul(p.x)
                .add(0, p.y, 0);
        camera.setPosition(new Vector3f(posJoueur).add(offset));
    }

    public boolean estPremierePersonne() {
        return modeActuel == ModeVue.PREMIERE_PERSONNE;
    }

    public ModeVue modeActuel() {
        return modeActuel;
    }
}
