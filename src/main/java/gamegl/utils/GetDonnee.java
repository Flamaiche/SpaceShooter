package gamegl.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire de lecture et écriture de données JSON et de fichiers bruts.
 */
public class GetDonnee {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_DIR = System.getProperty("user.dir") + File.separator + "data" + File.separator;

    /**
     * Écrit une liste d'objets dans un fichier JSON, en mettant à jour les doublons existants.
     *
     * @param nomFichier      nom du fichier de destination
     * @param nouvellesDonnees liste des objets à écrire
     */
    public static void writeJson(String nomFichier, ArrayList<?> nouvellesDonnees) {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) dir.mkdirs();

        List<Object> donneesExistantes = readJson(nomFichier);
        if (donneesExistantes == null) donneesExistantes = new ArrayList<>();

        for (Object nouvelleDonnee : nouvellesDonnees) {
            int index = donneesExistantes.indexOf(nouvelleDonnee);
            if (index != -1) donneesExistantes.set(index, nouvelleDonnee);
            else donneesExistantes.add(nouvelleDonnee);
        }

        String typeName = nouvellesDonnees.isEmpty() ? "Unknown" : nouvellesDonnees.get(0).getClass().getName();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", typeName);
        jsonObject.add("data", gson.toJsonTree(donneesExistantes));

        try (FileWriter writer = new FileWriter(DATA_DIR + nomFichier)) {
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lit une liste d'objets depuis un fichier JSON.
     *
     * @param nomFichier nom du fichier à lire
     * @return liste d'objets, ou null si le fichier n'existe pas
     * @param <T> type des objets
     */
    public static <T> List<T> readJson(String nomFichier) {
        File file = new File(DATA_DIR + nomFichier);
        if (!file.exists()) return null;

        try (FileReader reader = new FileReader(file)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            String typeName = jsonObject.get("type").getAsString();
            JsonArray dataArray = jsonObject.getAsJsonArray("data");

            Class<?> clazz = Class.forName(typeName);
            Type type = TypeToken.getParameterized(List.class, clazz).getType();

            return gson.fromJson(dataArray, type);
        } catch (IOException | ClassNotFoundException | IllegalStateException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lit le contenu brut d'un fichier texte.
     *
     * @param filename nom du fichier à lire
     * @return contenu du fichier, ou un message d'erreur
     */
    public static String readFile(String filename) {
        File file = new File(DATA_DIR + filename);
        if (!file.exists()) return "Fichier non trouvé : " + filename;

        try (FileReader reader = new FileReader(file)) {
            char[] buffer = new char[(int) file.length()];
            reader.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            return "Erreur lors de la lecture du fichier : " + e.getMessage();
        }
    }
}
