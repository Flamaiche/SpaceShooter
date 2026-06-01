package gamegl.utils.config;

import java.util.List;

import gamegl.utils.GetDonnee;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ConfigVaisseau {

    /* Identifiant de cette config (pour différents profils de vaisseau) */
    public String name;

    /* === VIES === */
    /* Nombre de vies du joueur au début de la partie */
    public int initialLives;

    /* === OFFSETS VISUELS === */
    /* Décalage de la caméra par rapport au vaisseau en vue 3e personne (x=avant, y=vertical) */
    public Vector2f shipOffset;
    /* Décalage X,Y,Z supplémentaire de la caméra en vue 3e personne */
    public Vector3f thirdPersonOffset;

    /* === MANIABILITÉ === */
    /* Taux de rotation du vaisseau (x=horizontal, y=vertical) */
    public Vector2f rotationRate;
    /* Inclinaison maximale du vaisseau en degrés (bias) */
    public float biasMax;
    /* Facteur de roulis (bank) appliqué lors des virages horizontaux */
    public float bankFactor;
    /* Facteur de lissage (slerp) entre rotation brute et rotation lissée */
    public float slerpFactor;
    /* Échelle d'affichage du modèle 3D du vaisseau joueur */
    public float playerShipScale;

    /* === FAKE CAMERA === */
    /* Hauteur Y des FAKE cameras en mode 3e personne */
    public float offsetVisuelY;

    private static ConfigVaisseau instance;

    public static ConfigVaisseau get() {
        if (instance == null) {
            List<ConfigVaisseau> list = GetDonnee.readJson("config_vaisseau.json");
            instance = (list != null && !list.isEmpty()) ? list.getFirst() : new ConfigVaisseau();
            if (instance != null) instance.valider();
        }
        return instance;
    }

    public void valider() {
        ConfigJeu limites = ConfigJeu.get();
        if (limites == null) return;

        initialLives = clamp(initialLives, limites.initialLives[0], limites.initialLives[1]);

        if (shipOffset != null && limites.shipOffset != null && limites.shipOffset.length >= 2
            && limites.shipOffset[0] != null && limites.shipOffset[1] != null) {
            shipOffset.x = clamp(shipOffset.x, limites.shipOffset[0].x, limites.shipOffset[1].x);
            shipOffset.y = clamp(shipOffset.y, limites.shipOffset[0].y, limites.shipOffset[1].y);
        }
        if (thirdPersonOffset != null && limites.thirdPersonOffset != null && limites.thirdPersonOffset.length >= 2
            && limites.thirdPersonOffset[0] != null && limites.thirdPersonOffset[1] != null) {
            thirdPersonOffset.x = clamp(thirdPersonOffset.x, limites.thirdPersonOffset[0].x, limites.thirdPersonOffset[1].x);
            thirdPersonOffset.y = clamp(thirdPersonOffset.y, limites.thirdPersonOffset[0].y, limites.thirdPersonOffset[1].y);
            thirdPersonOffset.z = clamp(thirdPersonOffset.z, limites.thirdPersonOffset[0].z, limites.thirdPersonOffset[1].z);
        }

        if (rotationRate != null && limites.rotationRate != null && limites.rotationRate.length >= 2
            && limites.rotationRate[0] != null && limites.rotationRate[1] != null) {
            rotationRate.x = clamp(rotationRate.x, limites.rotationRate[0].x, limites.rotationRate[1].x);
            rotationRate.y = clamp(rotationRate.y, limites.rotationRate[0].y, limites.rotationRate[1].y);
        }
        biasMax = clamp(biasMax, limites.biasMax[0], limites.biasMax[1]);
        bankFactor = clamp(bankFactor, limites.bankFactor[0], limites.bankFactor[1]);
        slerpFactor = clamp(slerpFactor, limites.slerpFactor[0], limites.slerpFactor[1]);
        playerShipScale = clamp(playerShipScale, limites.playerShipScale[0], limites.playerShipScale[1]);
        offsetVisuelY = clamp(offsetVisuelY, limites.offsetVisuelY[0], limites.offsetVisuelY[1]);
    }

    private static int clamp(int value, int min, int max) {
        return Math.clamp(value, min, max);
    }

    private static float clamp(float value, float min, float max) {
        return Math.clamp(value, min, max);
    }

    private static double clamp(double value, double min, double max) {
        return Math.clamp(value, min, max);
    }

}
