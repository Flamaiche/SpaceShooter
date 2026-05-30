package gamegl.utils;

import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ConfigJeu {

    /* Identifiant de cette config */
    public String name;

    /* === CAMERA INTRINSÈQUE (fixes) === */
    /* Champ de vision en degrés */
    public float fov;
    /* Plan de coupe proche */
    public float nearPlane;
    /* Distance d'affichage max */
    public float renderDistance;
    /* Distance de simulation max (au-delà, les entités despawn) */
    public float renderSimulation;

    /* === FENÊTRE === */
    /* Largeur de la fenêtre en pixels */
    public int windowWidth;
    /* Hauteur de la fenêtre en pixels */
    public int windowHeight;

    /* === COULEURS FIXES === */
    /* Couleur des balles (R, G, B) */
    public Vector3f ballColor;
    /* Couleur du crosshair (R, G, B) */
    public Vector3f crosshairColor;
    /* Hauteur de référence (pixels) pour le dimensionnement du crosshair */
    public float crosshairRefHeight;
    /* Couleur du texte HUD gauche (R, G, B) */
    public Vector3f hudLeftColor;
    /* Couleur du texte HUD droite (R, G, B) */
    public Vector3f hudRightColor;

    /* === TEMPS === */
    /* Framerate cible pour les calculs de deltaTime */
    public float targetFramerate;

    /* ===== BORNES [min, max] ===== */

    /* === VIES === */
    /* Nombre de vies du joueur au début */
    public int[] initialLives;

    /* === CAMERA PHYSICS === */
    /* Vitesse max (x), temps accélération (y), facteur freinage (z) */
    public Vector3f[] cameraPhysics;

    /* === CAMERA ROTATION === */
    /* Sensibilité souris */
    public float[] mouseSensitivity;
    /* Vitesse rotation touches fléchées (degrés/s) */
    public float[] vitesseRotation;
    /* Vitesse roulis touches A/E (degrés/appui) */
    public float[] rollSpeed;

    /* === TIR / BALLES === */
    /* Temps de rechargement (secondes) */
    public double[] shootCooldown;
    /* Nombre max de balles actives */
    public int[] ballsMax;
    /* Vitesse des balles (unités/s) */
    public float[] ballSpeed;
    /* Distance max de vol */
    public float[] ballDistanceMax;
    /* Taille (échelle) de la balle */
    public float[] ballSize;
    /* Multiplicateur de rotation visuelle */
    public float[] ballRotationMultiplier;
    /* Vitesse rotation max aléatoire au spawn (degrés/s) */
    public float[] ballRotationSpeedMax;
    /* Pas de détection de collision subdivisée */
    public float[] ballCollisionStep;

    /* === OFFSETS === */
    /* Décalage caméra vaisseau (x=avant, y=vertical) */
    public Vector2f[] shipOffset;
    /* Décalage X,Y,Z caméra 3e personne */
    public Vector3f[] thirdPersonOffset;

    /* === CROSSHAIR === */
    /* Géométrie (x=longueurSegment, y=espaceCentral, z=epaisseurLigne) */
    public Vector3f[] crosshairGeom;
    /* Multiplicateurs (x=dynamicGap, y=obliqueThickness) */
    public Vector2f[] crosshairMult;

    /* === SPAWN CAMÉRA === */
    /* Position initiale caméra (X, Y, Z) */
    public Vector3f[] cameraSpawn;

    /* === CAMERA INIT === */
    /* Angles de spawn (x=yaw, y=pitch) */
    public Vector2f[] spawnAngles;
    /* Limites mode orbit (x=rayonMin, y=pitchMax) */
    public Vector2f[] orbitLimits;

    /* === MANIABILITÉ === */
    /* Taux rotation (x=horizontal, y=vertical) */
    public Vector2f[] rotationRate;
    /* Inclinaison max du vaisseau (degrés) */
    public float[] biasMax;
    /* Facteur de roulis */
    public float[] bankFactor;
    /* Facteur de lissage slerp */
    public float[] slerpFactor;
    /* Échelle du modèle 3D du vaisseau joueur */
    public float[] playerShipScale;

    /* === HUD / TEXTE === */
    /* Taille de référence pour le calcul d'échelle HUD (x=largeur, y=hauteur) */
    public Vector2f textBaseSize;
    /* Marge par défaut pour l'alignement du texte */
    public float textMargin;
    /* Échelle uniforme par défaut du texte */
    public float textUniformScale;

    /* === COULEURS D'ARRIÈRE-PLAN === */
    /* Couleur de fond en jeu (R, G, B, A) */
    public Vector4f bgColorGameplay;
    /* Couleur de fond en pause (R, G, B, A) */
    public Vector4f bgColorPause;
    /* Couleur de fond du menu principal (R, G, B, A) */
    public Vector4f bgColorMenu;

    /* === ANIMATION MENU === */
    /* Rayon de l'animation du texte du menu */
    public float menuTextRadius;
    /* Espacement entre les lettres du menu animé */
    public float menuTextLetterSpacing;
    /* Fréquence de l'oscillation sinusoïdale du menu */
    public float menuTextFrequency;
    /* Tours par seconde de rotation des étoiles/texte */
    public float menuToursPerSecond;
    /* Rayon de l'animation des étoiles */
    public float menuStarRadius;
    /* Échelle du texte animé du menu (multiplicateur de uniformTextScale) */
    public float menuAnimatedTextScale;
    /* Échelle des items du menu (multiplicateur de uniformTextScale) */
    public float menuItemScale;
    /* Échelle de l'item sélectionné (multiplicateur de uniformTextScale) */
    public float menuSelectedScale;
    /* Couleur du texte animé du menu (R, G, B) */
    public Vector3f menuTextColor;
    /* Couleur des étoiles du menu (R, G, B) */
    public Vector3f menuStarColor;
    /* Couleur de l'item sélectionné (R, G, B) */
    public Vector3f menuSelectedColor;
    /* Couleur de l'item non sélectionné (R, G, B) */
    public Vector3f menuUnselectedColor;
    /* Nombre de faux ennemis décoratifs en pause */
    public int menuFakeEnnemisCount;

    /* === OFFSET VISUEL FAKE CAMERA === */
    /* Hauteur Y des FAKE cameras (bornes [min, max]) */
    public float[] offsetVisuelY;

    /* === CROSSHAIR LAG === */
    /* Raideur du ressort du crosshair orbital (bornes [min, max]) */
    public float[] crosshairStiffness;
    /* Amortissement du suivi du crosshair orbital (bornes [min, max]) */
    public float[] crosshairLagDamping;
    /* Vitesse angulaire max du crosshair orbital (bornes [min, max]) */
    public float[] crosshairLagMaxSpeed;

    /* === ENNEMIS - stats de base === */
    /* Nombre d'ennemis générés */
    public int[] nbEnnemis;
    /* Vitesse de base des ennemis */
    public float[] enemyBaseSpeed;
    /* Taille de base du modèle 3D */
    public float[] enemyBaseSize;
    /* Points de vie max */
    public int[] enemyMaxVie;
    /* Score par kill */
    public int[] enemyScore;
    /* Couleur aléatoire corps (x=base, y=range) */
    public Vector2f[] enemyColorConfig;

    /* === ENNEMIS - spawn === */
    /* Zone de spawn (x=demi-côté spawn, y=demi-côté exclusion) */
    public Vector2f[] spawnZone;
    /* Temps respawn (x=minimum, y=maximum) */
    public Vector2f[] respawnTime;

    /* === ENNEMIS - vitesse dynamique === */
    /* Partie fixe du random de vitesse */
    public float[] enemySpeedRandomBase;
    /* Plage de variation aléatoire de la vitesse */
    public float[] enemySpeedRandomRange;
    /* Multiplicateur de vitesse par palier de puissance */
    public float[] enemySpeedPowerMultiplier;
    /* Ennemis tués avant activation du power multiplier */
    public int[] enemySpeedPowerThreshold;
    /* Taille du groupe par palier */
    public int[] enemySpeedPowerGroupSize;

    /* === ENNEMIS - highlight === */
    /* Paramètres outline (x=échelle, y=épaisseur) */
    public Vector2f[] enemyOutline;
    /* Couleur highlight (R, G, B) */
    public Vector3f[] enemyHighlightColor;

    /* === ENNEMIS - respawn === */
    /* Multiplicateur de distance pour respawn */
    public float[] respawnDistanceMultiplier;

    /* === ENNEMIS - mutation === */
    /* Intervalle entre mutations (secondes) */
    public int[] mutationDeltaTimeInterval;
    /* Probabilité mutation vitesse (0-100) */
    public float[] mutationVitesseProb;
    /* Probabilité mutation taille (0-100) */
    public float[] mutationTailleProb;
    /* Probabilité mutation respawn (0-100) */
    public float[] mutationSleepProb;
    /* Probabilité mutation forme (0-100, réservé) */
    public float[] mutationShapeProb;
    /* Amplitude mutation vitesse (x=up, y=down) */
    public Vector2f[] mutationSpeedRange;
    /* Amplitude mutation taille (x=up, y=down) */
    public Vector2f[] mutationSizeRange;
    /* Amplitude mutation respawn (x=up, y=down) */
    public Vector2f[] mutationSleepRange;
    /* Clamp minimum du temps de respawn muté */
    public float[] mutationSleepClampMin;

    private static ConfigJeu instance;

    public static ConfigJeu get() {
        if (instance == null) {
            List<ConfigJeu> list = GetDonnee.readJson("config_jeu.json");
            instance = (list != null && !list.isEmpty()) ? list.getFirst() : new ConfigJeu();
        }
        return instance;
    }

}
