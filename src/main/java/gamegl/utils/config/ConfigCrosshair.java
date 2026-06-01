package gamegl.utils.config;

import java.util.List;

import gamegl.utils.GetDonnee;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ConfigCrosshair {

    public String name;

    /* === SHADER === */
    public String shaderVertex;
    public String shaderFragment;

    /* === COULEUR === */
    public Vector3f crosshairColor;
    public float crosshairRefHeight;

    /* === GÉOMÉTRIE === */
    public Vector3f crosshairGeom;
    public Vector2f crosshairMult;

    /* === PHYSIQUE (lag orbital) === */
    public float crosshairStiffness;
    public float crosshairLagDamping;
    public float crosshairLagMaxSpeed;

    private static ConfigCrosshair instance;

    public static ConfigCrosshair get() {
        if (instance == null) {
            List<ConfigCrosshair> list = GetDonnee.readJson("config_crosshair.json");
            instance = (list != null && !list.isEmpty()) ? list.getFirst() : new ConfigCrosshair();
            if (instance != null) instance.valider();
        }
        return instance;
    }

    public void valider() {
        ConfigJeu limites = ConfigJeu.get();
        if (limites == null) return;

        if (crosshairGeom != null && limites.crosshairGeom != null && limites.crosshairGeom.length >= 2
            && limites.crosshairGeom[0] != null && limites.crosshairGeom[1] != null) {
            crosshairGeom.x = clamp(crosshairGeom.x, limites.crosshairGeom[0].x, limites.crosshairGeom[1].x);
            crosshairGeom.y = clamp(crosshairGeom.y, limites.crosshairGeom[0].y, limites.crosshairGeom[1].y);
            crosshairGeom.z = clamp(crosshairGeom.z, limites.crosshairGeom[0].z, limites.crosshairGeom[1].z);
        }
        if (crosshairMult != null && limites.crosshairMult != null && limites.crosshairMult.length >= 2
            && limites.crosshairMult[0] != null && limites.crosshairMult[1] != null) {
            crosshairMult.x = clamp(crosshairMult.x, limites.crosshairMult[0].x, limites.crosshairMult[1].x);
            crosshairMult.y = clamp(crosshairMult.y, limites.crosshairMult[0].y, limites.crosshairMult[1].y);
        }

        crosshairStiffness = clamp(crosshairStiffness, limites.crosshairStiffness[0], limites.crosshairStiffness[1]);
        crosshairLagDamping = clamp(crosshairLagDamping, limites.crosshairLagDamping[0], limites.crosshairLagDamping[1]);
        crosshairLagMaxSpeed = clamp(crosshairLagMaxSpeed, limites.crosshairLagMaxSpeed[0], limites.crosshairLagMaxSpeed[1]);
    }

    private static int clamp(int value, int min, int max) { return Math.clamp(value, min, max); }
    private static float clamp(float value, float min, float max) { return Math.clamp(value, min, max); }
    private static double clamp(double value, double min, double max) { return Math.clamp(value, min, max); }
}
