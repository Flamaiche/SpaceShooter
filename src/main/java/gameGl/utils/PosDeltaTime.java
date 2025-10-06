package gameGl.utils;

public class PosDeltaTime {
    public static double[] circle(double time, double radius, double centerX, double centerY,
                                  double toursPerSecond, int index, int total) {
        // rotation + décalage angulaire par index
        double angleDeg = (time * toursPerSecond * 360.0) + (360.0 / total) * index;
        double angleRad = Math.toRadians(angleDeg);

        double x = centerX + radius * Math.cos(angleRad);
        double y = centerY + radius * Math.sin(angleRad);

        return new double[]{x, y};
    }

    public static double[] wave(double time, double amplitude, double centerX, double centerY,
                                double wavelength, double speed, int index, int total) {
        // Décalage de phase par index pour que chaque point/liste de lettres ne soit pas synchronisée
        double phaseShift = (2 * Math.PI / total) * index;

        // Position horizontale : centrée
        double x = centerX + (index - total / 2.0) * wavelength;

        // Position verticale : sinus pour créer l'effet de vague
        double y = centerY + amplitude * Math.sin(time * speed + phaseShift);

        return new double[]{x, y};
    }
}
