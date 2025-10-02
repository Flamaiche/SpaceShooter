package gameGl.gestion.texte;

@FunctionalInterface
public interface TextTransformFunction {
    /**
     * Transforme une lettre en coordonnées (x,y) en fonction du temps et de paramètres.
     *
     * @param time            temps écoulé (deltaTime cumulé)
     * @param radius          rayon ou amplitude de la transformation
     * @param centerX         centre X
     * @param centerY         centre Y
     * @param toursPerSecond  vitesse de cycle
     * @param index           index de la lettre dans le texte
     * @return tableau {x,y}
     */
    double[] apply(double time, double radius, double centerX, double centerY, double toursPerSecond, int index);
}
