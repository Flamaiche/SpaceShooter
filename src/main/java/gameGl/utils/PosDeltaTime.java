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

}
