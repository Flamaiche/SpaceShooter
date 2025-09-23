package gameGl.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GetDonnee {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_DIR = System.getProperty("user.dir") + File.separator + "data" + File.separator;

    // Écrire une liste d'objets dans un JSON (mise à jour si doublons)
    public static void writeJson(String nomFichier, ArrayList<?> nouvellesDonnees) {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) dir.mkdirs();

        List<Object> donneesExistantes = readJson(nomFichier);
        if (donneesExistantes == null) donneesExistantes = new ArrayList<>();

        for (Object nouvelleDonnee : nouvellesDonnees) {
            int index = donneesExistantes.indexOf(nouvelleDonnee);
            if (index != -1) donneesExistantes.set(index, nouvelleDonnee); // remplace si existe
            else donneesExistantes.add(nouvelleDonnee); // sinon ajoute
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

    // Lire une liste d'objets depuis un JSON
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

    // Lire un fichier texte brut
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
