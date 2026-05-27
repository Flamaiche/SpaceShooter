package learngl.tools.camera.vue;

import learngl.tools.camera.Camera;
import org.joml.Vector3f;

public class GestionnaireVue {
    private ModeVue modeActuel = ModeVue.PREMIERE_PERSONNE;

    public void suivant() {
        int prochain = (modeActuel.ordinal() + 1) % ModeVue.values().length;
        modeActuel = ModeVue.values()[prochain];
    }

    public void mettreAJour(Camera camera, Vector3f posJoueur) {
        switch (modeActuel) {
            case PREMIERE_PERSONNE:
                camera.setPosition(posJoueur);
                break;
            case TROISIEME_PERSONNE: {
                float distance = modeActuel.donnees().params().x;
                float hauteur = modeActuel.donnees().params().y;
                Vector3f offset = new Vector3f(camera.getFront())
                        .negate().mul(distance)
                        .add(0, hauteur, 0);
                camera.setPosition(new Vector3f(posJoueur).add(offset));
                break;
            }
        }
    }

    public boolean estPremierePersonne() {
        return modeActuel == ModeVue.PREMIERE_PERSONNE;
    }

    public ModeVue modeActuel() {
        return modeActuel;
    }
}
