package gamegl.utils;

import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ConfigEnnemis {

    /* Identifiant de cette config (pour différents profils d'ennemis) */
    public String name;

    /* === STATS DE BASE === */
    /* Nombre d'ennemis générés au début de la partie */
    public int nbEnnemis;
    /* Taille de base du modèle 3D des ennemis */
    public float enemyBaseSize;

    /* Couleur aléatoire du corps (x=valeur de base, y=plage de variation) */
    public Vector2f enemyColorConfig;

    /* Zone de spawn autour du joueur (x=demi-côté du cube, y=demi-côté de l'exclusion centrale) */
    public Vector2f spawnZone;

    /* Points de vie maximum d'un ennemi */
    public int enemyMaxVie;
    /* Score gagné quand un ennemi est tué */
    public int enemyScore;

    /* Temps avant respawn après la mort (x=minimum, y=maximum en secondes) */
    public Vector2f respawnTime;

    /* === VITESSE DYNAMIQUE === */
    /* Vitesse de déplacement de base des ennemis */
    public float enemyBaseSpeed;
    /* Partie fixe de la vitesse aléatoire (base du random) */
    public float enemySpeedRandomBase;
    /* Plage de variation aléatoire de la vitesse */
    public float enemySpeedRandomRange;
    /* Multiplicateur de vitesse appliqué par palier de puissance */
    public float enemySpeedPowerMultiplier;
    /* Nombre d'ennemis tués avant d'activer le power multiplier */
    public int enemySpeedPowerThreshold;
    /* Taille du groupe d'ennemis pour chaque palier de puissance */
    public int enemySpeedPowerGroupSize;

    /* === HIGHLIGHT === */
    /* Paramètres de l'outline de highlight (x=échelle du modèle, y=épaisseur de la ligne) */
    public Vector2f enemyOutline;
    /* Couleur de l'outline de highlight (R, G, B) */
    public Vector3f enemyHighlightColor;

    /* === RESPAWN === */
    /* Multiplicateur de distance pour le respawn (appliqué à renderSimulation) */
    public float respawnDistanceMultiplier;

    /* === MUTATION === */
    /* Intervalle de temps entre deux mutations (secondes) */
    public int mutationDeltaTimeInterval;
    /* Probabilité de mutation de la vitesse (0-100) */
    public float mutationVitesseProb;
    /* Probabilité de mutation de la taille (0-100) */
    public float mutationTailleProb;
    /* Probabilité de mutation du temps de respawn (0-100) */
    public float mutationSleepProb;
    /* Probabilité de mutation de la forme (0-100, réservé) */
    public float mutationShapeProb;

    /* Amplitude d'augmentation (x) et de diminution (y) de la vitesse par mutation */
    public Vector2f mutationSpeedRange;
    /* Amplitude d'augmentation (x) et de diminution (y) de la taille par mutation */
    public Vector2f mutationSizeRange;
    /* Amplitude d'augmentation (x) et de diminution (y) du temps de respawn par mutation */
    public Vector2f mutationSleepRange;

    /* Valeur minimale de clamp pour le temps de respawn muté (évite respawn instantané) */
    public float mutationSleepClampMin;

    private static ConfigEnnemis instance;

    public static ConfigEnnemis get() {
        if (instance == null) {
            List<ConfigEnnemis> list = GetDonnee.readJson("config_ennemis.json");
            instance = (list != null && !list.isEmpty()) ? list.getFirst() : new ConfigEnnemis();
            if (instance != null) instance.valider();
        }
        return instance;
    }

    public void valider() {
        ConfigJeu limites = ConfigJeu.get();
        if (limites == null) return;

        nbEnnemis = clamp(nbEnnemis, limites.nbEnnemis[0], limites.nbEnnemis[1]);
        enemyBaseSize = clamp(enemyBaseSize, limites.enemyBaseSize[0], limites.enemyBaseSize[1]);

        if (enemyColorConfig != null && limites.enemyColorConfig != null && limites.enemyColorConfig.length >= 2
            && limites.enemyColorConfig[0] != null && limites.enemyColorConfig[1] != null) {
            enemyColorConfig.x = clamp(enemyColorConfig.x, limites.enemyColorConfig[0].x, limites.enemyColorConfig[1].x);
            enemyColorConfig.y = clamp(enemyColorConfig.y, limites.enemyColorConfig[0].y, limites.enemyColorConfig[1].y);
        }

        if (spawnZone != null && limites.spawnZone != null && limites.spawnZone.length >= 2
            && limites.spawnZone[0] != null && limites.spawnZone[1] != null) {
            spawnZone.x = clamp(spawnZone.x, limites.spawnZone[0].x, limites.spawnZone[1].x);
            spawnZone.y = clamp(spawnZone.y, limites.spawnZone[0].y, limites.spawnZone[1].y);
        }

        enemyMaxVie = clamp(enemyMaxVie, limites.enemyMaxVie[0], limites.enemyMaxVie[1]);
        enemyScore = clamp(enemyScore, limites.enemyScore[0], limites.enemyScore[1]);

        if (respawnTime != null && limites.respawnTime != null && limites.respawnTime.length >= 2
            && limites.respawnTime[0] != null && limites.respawnTime[1] != null) {
            respawnTime.x = clamp(respawnTime.x, limites.respawnTime[0].x, limites.respawnTime[1].x);
            respawnTime.y = clamp(respawnTime.y, limites.respawnTime[0].y, limites.respawnTime[1].y);
        }

        enemyBaseSpeed = clamp(enemyBaseSpeed, limites.enemyBaseSpeed[0], limites.enemyBaseSpeed[1]);
        enemySpeedRandomBase = clamp(enemySpeedRandomBase, limites.enemySpeedRandomBase[0], limites.enemySpeedRandomBase[1]);
        enemySpeedRandomRange = clamp(enemySpeedRandomRange, limites.enemySpeedRandomRange[0], limites.enemySpeedRandomRange[1]);
        enemySpeedPowerMultiplier = clamp(enemySpeedPowerMultiplier, limites.enemySpeedPowerMultiplier[0], limites.enemySpeedPowerMultiplier[1]);
        enemySpeedPowerThreshold = clamp(enemySpeedPowerThreshold, limites.enemySpeedPowerThreshold[0], limites.enemySpeedPowerThreshold[1]);
        enemySpeedPowerGroupSize = clamp(enemySpeedPowerGroupSize, limites.enemySpeedPowerGroupSize[0], limites.enemySpeedPowerGroupSize[1]);

        if (enemyOutline != null && limites.enemyOutline != null && limites.enemyOutline.length >= 2
            && limites.enemyOutline[0] != null && limites.enemyOutline[1] != null) {
            enemyOutline.x = clamp(enemyOutline.x, limites.enemyOutline[0].x, limites.enemyOutline[1].x);
            enemyOutline.y = clamp(enemyOutline.y, limites.enemyOutline[0].y, limites.enemyOutline[1].y);
        }

        if (enemyHighlightColor != null && limites.enemyHighlightColor != null && limites.enemyHighlightColor.length >= 2
            && limites.enemyHighlightColor[0] != null && limites.enemyHighlightColor[1] != null) {
            enemyHighlightColor.x = clamp(enemyHighlightColor.x, limites.enemyHighlightColor[0].x, limites.enemyHighlightColor[1].x);
            enemyHighlightColor.y = clamp(enemyHighlightColor.y, limites.enemyHighlightColor[0].y, limites.enemyHighlightColor[1].y);
            enemyHighlightColor.z = clamp(enemyHighlightColor.z, limites.enemyHighlightColor[0].z, limites.enemyHighlightColor[1].z);
        }

        respawnDistanceMultiplier = clamp(respawnDistanceMultiplier, limites.respawnDistanceMultiplier[0], limites.respawnDistanceMultiplier[1]);
        mutationDeltaTimeInterval = clamp(mutationDeltaTimeInterval, limites.mutationDeltaTimeInterval[0], limites.mutationDeltaTimeInterval[1]);
        mutationVitesseProb = clamp(mutationVitesseProb, limites.mutationVitesseProb[0], limites.mutationVitesseProb[1]);
        mutationTailleProb = clamp(mutationTailleProb, limites.mutationTailleProb[0], limites.mutationTailleProb[1]);
        mutationSleepProb = clamp(mutationSleepProb, limites.mutationSleepProb[0], limites.mutationSleepProb[1]);
        mutationShapeProb = clamp(mutationShapeProb, limites.mutationShapeProb[0], limites.mutationShapeProb[1]);

        if (mutationSpeedRange != null && limites.mutationSpeedRange != null && limites.mutationSpeedRange.length >= 2
            && limites.mutationSpeedRange[0] != null && limites.mutationSpeedRange[1] != null) {
            mutationSpeedRange.x = clamp(mutationSpeedRange.x, limites.mutationSpeedRange[0].x, limites.mutationSpeedRange[1].x);
            mutationSpeedRange.y = clamp(mutationSpeedRange.y, limites.mutationSpeedRange[0].y, limites.mutationSpeedRange[1].y);
        }
        if (mutationSizeRange != null && limites.mutationSizeRange != null && limites.mutationSizeRange.length >= 2
            && limites.mutationSizeRange[0] != null && limites.mutationSizeRange[1] != null) {
            mutationSizeRange.x = clamp(mutationSizeRange.x, limites.mutationSizeRange[0].x, limites.mutationSizeRange[1].x);
            mutationSizeRange.y = clamp(mutationSizeRange.y, limites.mutationSizeRange[0].y, limites.mutationSizeRange[1].y);
        }
        if (mutationSleepRange != null && limites.mutationSleepRange != null && limites.mutationSleepRange.length >= 2
            && limites.mutationSleepRange[0] != null && limites.mutationSleepRange[1] != null) {
            mutationSleepRange.x = clamp(mutationSleepRange.x, limites.mutationSleepRange[0].x, limites.mutationSleepRange[1].x);
            mutationSleepRange.y = clamp(mutationSleepRange.y, limites.mutationSleepRange[0].y, limites.mutationSleepRange[1].y);
        }

        mutationSleepClampMin = clamp(mutationSleepClampMin, limites.mutationSleepClampMin[0], limites.mutationSleepClampMin[1]);
    }

    private static int clamp(int value, int min, int max) {
        return Math.clamp(value, min, max);
    }

    private static float clamp(float value, float min, float max) {
        return Math.clamp(value, min, max);
    }

}
