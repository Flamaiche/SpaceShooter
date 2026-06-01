package gamegl.utils.config;

import java.util.List;

import gamegl.utils.GetDonnee;
import org.joml.Vector3f;

public class ConfigBalles {

    public String name;

    /* === SHADER === */
    public String shaderVertex;
    public String shaderFragment;

    /* === COULEUR === */
    public Vector3f ballColor;

    /* === TIR === */
    public double shootCooldown;
    public int ballsMax;

    /* === PHYSIQUE === */
    public float ballSpeed;
    public float ballDistanceMax;
    public float ballSize;
    public float ballRotationMultiplier;
    public float ballRotationSpeedMax;
    public float ballCollisionStep;

    private static ConfigBalles instance;

    public static ConfigBalles get() {
        if (instance == null) {
            List<ConfigBalles> list = GetDonnee.readJson("config_balles.json");
            instance = (list != null && !list.isEmpty()) ? list.getFirst() : new ConfigBalles();
            if (instance != null) instance.valider();
        }
        return instance;
    }

    public void valider() {
        ConfigJeu limites = ConfigJeu.get();
        if (limites == null) return;

        shootCooldown = clamp(shootCooldown, limites.shootCooldown[0], limites.shootCooldown[1]);
        ballsMax = clamp(ballsMax, limites.ballsMax[0], limites.ballsMax[1]);
        ballSpeed = clamp(ballSpeed, limites.ballSpeed[0], limites.ballSpeed[1]);
        ballDistanceMax = clamp(ballDistanceMax, limites.ballDistanceMax[0], limites.ballDistanceMax[1]);
        ballSize = clamp(ballSize, limites.ballSize[0], limites.ballSize[1]);
        ballRotationMultiplier = clamp(ballRotationMultiplier, limites.ballRotationMultiplier[0], limites.ballRotationMultiplier[1]);
        ballRotationSpeedMax = clamp(ballRotationSpeedMax, limites.ballRotationSpeedMax[0], limites.ballRotationSpeedMax[1]);
        ballCollisionStep = clamp(ballCollisionStep, limites.ballCollisionStep[0], limites.ballCollisionStep[1]);
    }

    private static int clamp(int value, int min, int max) { return Math.clamp(value, min, max); }
    private static float clamp(float value, float min, float max) { return Math.clamp(value, min, max); }
    private static double clamp(double value, double min, double max) { return Math.clamp(value, min, max); }
}
