package gameGl.utils;

public class PosDeltaTime {
    public static double[] circle(double deltaTime, double radius, double centerX, double centerY, double tourPerSecond) {
        // calcule l'angle en degrés
        double angleDeg = deltaTime * tourPerSecond * 360;
        // convertit en radians
        double angleRad = Math.toRadians(angleDeg);

        // calcule les coordonnées
        double x = centerX + radius * Math.cos(angleRad);
        double y = centerY + radius * Math.sin(angleRad);

        return new double[]{x, y};
    }
}
