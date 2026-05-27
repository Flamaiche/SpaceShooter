package gamegl.utils;

/**
 * Fonctions de transformation positionnelle pour l'animation de texte.
 */
public class PosDeltaTime {

    /**
     * Calcule une position circulaire avec décalage angulaire par index.
     *
     * @param time           temps écoulé
     * @param radius         rayon du cercle
     * @param centerX        centre X
     * @param centerY        centre Y
     * @param toursPerSecond nombre de tours par seconde
     * @param index          index de l'élément
     * @param total          nombre total d'éléments
     * @return tableau {x, y}
     */
    public static double[] circle(double time, double radius, double centerX, double centerY,
                                  double toursPerSecond, int index, int total) {
        double angleDeg = (time * toursPerSecond * 360.0) + (360.0 / total) * index;
        double angleRad = Math.toRadians(angleDeg);

        double x = centerX + radius * Math.cos(angleRad);
        double y = centerY + radius * Math.sin(angleRad);

        return new double[]{x, y};
    }

    /**
     * Calcule une position sinusoïdale en forme de vague avec décalage de phase par index.
     *
     * @param time        temps écoulé
     * @param amplitude   amplitude de la vague
     * @param centerX     centre X
     * @param centerY     centre Y
     * @param wavelength  espacement horizontal entre les éléments
     * @param speed       vitesse de la vague
     * @param index       index de l'élément
     * @param total       nombre total d'éléments
     * @return tableau {x, y}
     */
    public static double[] wave(double time, double amplitude, double centerX, double centerY,
                                double wavelength, double speed, int index, int total) {
        double phaseShift = (2 * Math.PI / total) * index;

        double x = centerX + (index - total / 2.0) * wavelength;

        double y = centerY + amplitude * Math.sin(time * speed + phaseShift);

        return new double[]{x, y};
    }
}
