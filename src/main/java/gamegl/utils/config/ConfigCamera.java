package gamegl.utils.config;

import java.util.List;

import gamegl.utils.GetDonnee;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ConfigCamera {

    public String name;

    /* === CAMERA PHYSICS === */
    public Vector3f cameraPhysics;

    /* === CAMERA ROTATION === */
    public float mouseSensitivity;
    public float vitesseRotation;
    public float rollSpeed;

    /* === SPAWN CAMÉRA === */
    public Vector3f cameraSpawn;

    /* === CAMERA INIT === */
    public Vector2f spawnAngles;
    public Vector2f orbitLimits;

    private static ConfigCamera instance;

    public static ConfigCamera get() {
        if (instance == null) {
            List<ConfigCamera> list = GetDonnee.readJson("config_camera.json");
            instance = (list != null && !list.isEmpty()) ? list.getFirst() : new ConfigCamera();
            if (instance != null) instance.valider();
        }
        return instance;
    }

    public void valider() {
        ConfigJeu limites = ConfigJeu.get();
        if (limites == null) return;

        if (cameraPhysics != null && limites.cameraPhysics != null && limites.cameraPhysics.length >= 2
            && limites.cameraPhysics[0] != null && limites.cameraPhysics[1] != null) {
            cameraPhysics.x = clamp(cameraPhysics.x, limites.cameraPhysics[0].x, limites.cameraPhysics[1].x);
            cameraPhysics.y = clamp(cameraPhysics.y, limites.cameraPhysics[0].y, limites.cameraPhysics[1].y);
            cameraPhysics.z = clamp(cameraPhysics.z, limites.cameraPhysics[0].z, limites.cameraPhysics[1].z);
        }

        mouseSensitivity = clamp(mouseSensitivity, limites.mouseSensitivity[0], limites.mouseSensitivity[1]);
        vitesseRotation = clamp(vitesseRotation, limites.vitesseRotation[0], limites.vitesseRotation[1]);
        rollSpeed = clamp(rollSpeed, limites.rollSpeed[0], limites.rollSpeed[1]);

        if (cameraSpawn != null && limites.cameraSpawn != null && limites.cameraSpawn.length >= 2
            && limites.cameraSpawn[0] != null && limites.cameraSpawn[1] != null) {
            cameraSpawn.x = clamp(cameraSpawn.x, limites.cameraSpawn[0].x, limites.cameraSpawn[1].x);
            cameraSpawn.y = clamp(cameraSpawn.y, limites.cameraSpawn[0].y, limites.cameraSpawn[1].y);
            cameraSpawn.z = clamp(cameraSpawn.z, limites.cameraSpawn[0].z, limites.cameraSpawn[1].z);
        }

        if (spawnAngles != null && limites.spawnAngles != null && limites.spawnAngles.length >= 2
            && limites.spawnAngles[0] != null && limites.spawnAngles[1] != null) {
            spawnAngles.x = clamp(spawnAngles.x, limites.spawnAngles[0].x, limites.spawnAngles[1].x);
            spawnAngles.y = clamp(spawnAngles.y, limites.spawnAngles[0].y, limites.spawnAngles[1].y);
        }
        if (orbitLimits != null && limites.orbitLimits != null && limites.orbitLimits.length >= 2
            && limites.orbitLimits[0] != null && limites.orbitLimits[1] != null) {
            orbitLimits.x = clamp(orbitLimits.x, limites.orbitLimits[0].x, limites.orbitLimits[1].x);
            orbitLimits.y = clamp(orbitLimits.y, limites.orbitLimits[0].y, limites.orbitLimits[1].y);
        }
    }

    private static int clamp(int value, int min, int max) { return Math.clamp(value, min, max); }
    private static float clamp(float value, float min, float max) { return Math.clamp(value, min, max); }
    private static double clamp(double value, double min, double max) { return Math.clamp(value, min, max); }
}
