package learngl.camera.vue;

import gamegl.utils.config.ConfigVaisseau;
import learngl.LogFile;
import learngl.camera.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class GestionnaireVue {

    public enum ModeVue {
        PREMIERE_PERSONNE {
            public Vector3f offsetVisuel() { return new Vector3f(); }
        },
        TROISIEME_PERSONNE {
            public Vector3f offsetVisuel() {
                Vector3f off = new Vector3f(ConfigVaisseau.get().thirdPersonOffset);
                return new Vector3f(off.x, ConfigVaisseau.get().offsetVisuelY, off.z);
            }
        },
        TROISIEME_PERSONNE_AVANT {
            public Vector3f offsetVisuel() {
                Vector3f off = new Vector3f(ConfigVaisseau.get().thirdPersonOffset);
                return new Vector3f(-off.x, ConfigVaisseau.get().offsetVisuelY, off.z);
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
    private final Vector3f dernierePosFactice = new Vector3f();
    private final Vector3f dernierePosNavire = new Vector3f();

    public GestionnaireVue() {
        ConfigVaisseau cfg = ConfigVaisseau.get();
        offsetReel.set(cfg.thirdPersonOffset);
    }

    public void mettreAJour(Camera camera, Vector3f posJoueur) {
        mettreAJour(camera, posJoueur, 1);
    }

    public void mettreAJour(Camera camera, Vector3f posJoueur, int pas) {
        ModeVue ancien = modeActuel;
        modeActuel = modeActuel.suivant(pas);
        camera.setPosition(new Vector3f(posJoueur).add(offsetReel));
        if (pas != 0) {
            LogFile.printf("[GestionnaireVue] %s -> %s", ancien, modeActuel);
        }
    }

    public Matrix4f obtenirVue(Camera camera, Vector3f posJoueur) {
        Vector3f front = camera.getFront();
        Vector3f right = camera.getRight();
        Vector3f offsetVis = modeActuel.offsetVisuel();
        dernierePosFactice.set(posJoueur)
                .add(new Vector3f(front).negate().mul(offsetVis.x))
                .add(0, offsetVis.y, 0)
                .add(new Vector3f(right).mul(offsetVis.z));
        ConfigVaisseau cfg = ConfigVaisseau.get();
        dernierePosNavire.set(posJoueur)
                .sub(0, cfg.shipOffset.y, 0);
        if (modeActuel == ModeVue.TROISIEME_PERSONNE_AVANT) {
            return new Matrix4f().lookAt(dernierePosFactice, posJoueur, new Vector3f(0, 1, 0));
        }
        return new Matrix4f().lookAt(dernierePosFactice, new Vector3f(dernierePosFactice).add(camera.getFront()), camera.getUp());
    }

    public Vector3f getDernierePosFactice() { return dernierePosFactice; }
    public Vector3f getDernierePosNavire() { return dernierePosNavire; }

    public boolean estPremierePersonne() {
        return modeActuel == ModeVue.PREMIERE_PERSONNE;
    }

    public ModeVue getMode() { return modeActuel; }

}
