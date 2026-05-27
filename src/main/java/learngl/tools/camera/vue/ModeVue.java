package learngl.tools.camera.vue;

import org.joml.Vector3f;

public enum ModeVue {
    PREMIERE_PERSONNE(new DonneesVue("1ère personne", new Vector3f())),
    TROISIEME_PERSONNE(new DonneesVue("3ème personne", new Vector3f(3.0f, 1.0f, 0)));

    private final DonneesVue donnees;

    ModeVue(DonneesVue donnees) {
        this.donnees = donnees;
    }

    public DonneesVue donnees() {
        return donnees;
    }
}
