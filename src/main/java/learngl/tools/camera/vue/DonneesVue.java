package learngl.tools.camera.vue;

import org.joml.Vector3f;

public class DonneesVue {
    private final String nom;
    private final Vector3f params;

    public DonneesVue(String nom, Vector3f params) {
        this.nom = nom;
        this.params = params;
    }

    public String nom() {
        return nom;
    }

    public Vector3f params() {
        return params;
    }
}
