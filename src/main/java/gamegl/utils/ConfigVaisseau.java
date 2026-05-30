package gamegl.utils;

import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ConfigVaisseau {

    /* Identifiant de cette config (pour différents profils de vaisseau) */
    public String name;

    /* === VIES === */
    /* Nombre de vies du joueur au début de la partie */
    public int initialLives;

    /* === CAMERA PHYSICS === */
    /* Physique caméra : vitesse max (x), temps accélération (y), facteur freinage (z) */
    public Vector3f cameraPhysics;

    /* === CAMERA ROTATION === */
    /* Sensibilité de la souris pour le pitch/yaw (multiplicateur) */
    public float mouseSensitivity;
    /* Vitesse de rotation via les touches fléchées (degrés/seconde) */
    public float vitesseRotation;
    /* Vitesse du roulis via les touches A/E (degrés/appui) */
    public float rollSpeed;

    /* === TIR / BALLES === */
    /* Temps de rechargement entre deux tirs (secondes) */
    public double shootCooldown;
    /* Nombre maximum de balles actives simultanément */
    public int ballsMax;
    /* Vitesse de déplacement des balles (unités/seconde) */
    public float ballSpeed;
    /* Distance maximale de vol d'une balle avant désactivation */
    public float ballDistanceMax;
    /* Taille (échelle) de la balle pour le rendu */
    public float ballSize;
    /* Multiplicateur de vitesse de rotation visuelle des balles */
    public float ballRotationMultiplier;
    /* Vitesse de rotation max aléatoire appliquée au spawn (degrés/seconde) */
    public float ballRotationSpeedMax;
    /* Pas de la détection de collision subdivisée (petite valeur = plus précis, plus lent) */
    public float ballCollisionStep;

    /* === OFFSETS VISUELS === */
    /* Décalage de la caméra par rapport au vaisseau en vue 3e personne (x=avant, y=vertical) */
    public Vector2f shipOffset;
    /* Décalage de l'origine du tir par rapport à la caméra (x=avant, y=vertical) */
    public Vector2f bulletOffset;
    /* Décalage X,Y,Z supplémentaire de la caméra en vue 3e personne */
    public Vector3f thirdPersonOffset;

    /* === CROSSHAIR === */
    /* Géométrie du crosshair : longueurSegment (x), espaceCentral (y), epaisseurLigne (z) */
    public Vector3f crosshairGeom;
    /* Multiplicateurs du crosshair : dynamicGap (x), obliqueThickness (y) */
    public Vector2f crosshairMult;

    /* === SPAWN CAMÉRA === */
    /* Position X,Y,Z initiale de la caméra dans le monde */
    public Vector3f cameraSpawn;

    /* === CAMERA INIT === */
    /* Angles de la caméra au spawn (x=lacet/yaw, y=tangage/pitch) */
    public Vector2f spawnAngles;
    /* Limites du mode orbit (x=rayon minimum, y=pitch maximum en degrés) */
    public Vector2f orbitLimits;

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

        if (cameraPhysics != null && limites.cameraPhysics != null && limites.cameraPhysics.length >= 2
            && limites.cameraPhysics[0] != null && limites.cameraPhysics[1] != null) {
            cameraPhysics.x = clamp(cameraPhysics.x, limites.cameraPhysics[0].x, limites.cameraPhysics[1].x);
            cameraPhysics.y = clamp(cameraPhysics.y, limites.cameraPhysics[0].y, limites.cameraPhysics[1].y);
            cameraPhysics.z = clamp(cameraPhysics.z, limites.cameraPhysics[0].z, limites.cameraPhysics[1].z);
        }

        mouseSensitivity = clamp(mouseSensitivity, limites.mouseSensitivity[0], limites.mouseSensitivity[1]);
        vitesseRotation = clamp(vitesseRotation, limites.vitesseRotation[0], limites.vitesseRotation[1]);
        rollSpeed = clamp(rollSpeed, limites.rollSpeed[0], limites.rollSpeed[1]);
        shootCooldown = clamp(shootCooldown, limites.shootCooldown[0], limites.shootCooldown[1]);
        ballsMax = clamp(ballsMax, limites.ballsMax[0], limites.ballsMax[1]);
        ballSpeed = clamp(ballSpeed, limites.ballSpeed[0], limites.ballSpeed[1]);
        ballDistanceMax = clamp(ballDistanceMax, limites.ballDistanceMax[0], limites.ballDistanceMax[1]);
        ballSize = clamp(ballSize, limites.ballSize[0], limites.ballSize[1]);
        ballRotationMultiplier = clamp(ballRotationMultiplier, limites.ballRotationMultiplier[0], limites.ballRotationMultiplier[1]);
        ballRotationSpeedMax = clamp(ballRotationSpeedMax, limites.ballRotationSpeedMax[0], limites.ballRotationSpeedMax[1]);
        ballCollisionStep = clamp(ballCollisionStep, limites.ballCollisionStep[0], limites.ballCollisionStep[1]);

        if (shipOffset != null && limites.shipOffset != null && limites.shipOffset.length >= 2
            && limites.shipOffset[0] != null && limites.shipOffset[1] != null) {
            shipOffset.x = clamp(shipOffset.x, limites.shipOffset[0].x, limites.shipOffset[1].x);
            shipOffset.y = clamp(shipOffset.y, limites.shipOffset[0].y, limites.shipOffset[1].y);
        }
        if (bulletOffset != null && limites.bulletOffset != null && limites.bulletOffset.length >= 2
            && limites.bulletOffset[0] != null && limites.bulletOffset[1] != null) {
            bulletOffset.x = clamp(bulletOffset.x, limites.bulletOffset[0].x, limites.bulletOffset[1].x);
            bulletOffset.y = clamp(bulletOffset.y, limites.bulletOffset[0].y, limites.bulletOffset[1].y);
        }
        if (thirdPersonOffset != null && limites.thirdPersonOffset != null && limites.thirdPersonOffset.length >= 2
            && limites.thirdPersonOffset[0] != null && limites.thirdPersonOffset[1] != null) {
            thirdPersonOffset.x = clamp(thirdPersonOffset.x, limites.thirdPersonOffset[0].x, limites.thirdPersonOffset[1].x);
            thirdPersonOffset.y = clamp(thirdPersonOffset.y, limites.thirdPersonOffset[0].y, limites.thirdPersonOffset[1].y);
            thirdPersonOffset.z = clamp(thirdPersonOffset.z, limites.thirdPersonOffset[0].z, limites.thirdPersonOffset[1].z);
        }

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
