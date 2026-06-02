package markershape.io;

import com.google.gson.*;
import markershape.model.*;

import java.io.*;
import java.util.*;

/**
 * Handles loading, saving, and listing shapes as JSON files.
 * Shapes are stored in {@code data/markershape/shapes/}.
 */
public class ShapeLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String SHAPES_DIR = System.getProperty("user.dir")
        + File.separator + "data" + File.separator + "markershape" + File.separator + "shapes" + File.separator;

    /**
     * Loads a shape from a JSON file.
     *
     * @param filename the shape file name (must end with .json)
     * @return the loaded shape data, or null on failure
     */
    public static ShapeData load(String filename) {
        File file = new File(SHAPES_DIR + filename);
        if (!file.exists()) return null;
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            ShapeData data = new ShapeData();
            data.name = root.get("name").getAsString();
            data.shader = root.has("shader") ? root.get("shader").getAsString() : "default";

            JsonArray verts = root.getAsJsonArray("vertices");
            for (JsonElement e : verts) {
                JsonObject o = e.getAsJsonObject();
                Vertex v = new Vertex();
                v.id = o.get("id").getAsInt();
                v.x = o.get("x").getAsFloat();
                v.y = o.get("y").getAsFloat();
                v.z = o.get("z").getAsFloat();
                if (o.has("color")) {
                    JsonArray c = o.getAsJsonArray("color");
                    v.r = c.get(0).getAsFloat();
                    v.g = c.get(1).getAsFloat();
                    v.b = c.get(2).getAsFloat();
                }
                data.addVertex(v);
            }

            JsonArray edges = root.getAsJsonArray("edges");
            if (edges != null) {
                for (JsonElement e : edges) {
                    JsonObject o = e.getAsJsonObject();
                    Edge edge = new Edge();
                    edge.id = o.get("id").getAsInt();
                    edge.a = o.get("a").getAsInt();
                    edge.b = o.get("b").getAsInt();
                    edge.mode = o.has("mode") ? o.get("mode").getAsString() : "stun";
                    edge.thickness = o.has("thickness") ? o.get("thickness").getAsFloat() : 0.02f;
                    data.addEdge(edge);
                }
            }

            JsonArray faces = root.getAsJsonArray("faces");
            if (faces != null) {
                for (JsonElement e : faces) {
                    JsonObject o = e.getAsJsonObject();
                    JsonArray idx = o.getAsJsonArray("indices");
                    int[] tri = new int[idx.size()];
                    for (int i = 0; i < idx.size(); i++) {
                        tri[i] = idx.get(i).getAsInt();
                    }
                    data.faces.add(tri);
                }
            }
            return data;
        } catch (IOException e) {
            System.err.println("[ShapeLoader] load error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves shape data to a JSON file.
     *
     * @param data     the shape data to save
     * @param filename the target file name
     * @return true if the save succeeded
     */
    public static boolean save(ShapeData data, String filename) {
        JsonObject root = new JsonObject();
        root.addProperty("name", data.name);
        root.addProperty("shader", data.shader);

        JsonArray verts = new JsonArray();
        for (Vertex v : data.vertices.values()) {
            JsonObject o = new JsonObject();
            o.addProperty("id", v.id);
            o.addProperty("x", v.x);
            o.addProperty("y", v.y);
            o.addProperty("z", v.z);
            JsonArray c = new JsonArray();
            c.add(v.r);
            c.add(v.g);
            c.add(v.b);
            o.add("color", c);
            verts.add(o);
        }
        root.add("vertices", verts);

        JsonArray edges = new JsonArray();
        for (Edge e : data.edges.values()) {
            JsonObject o = new JsonObject();
            o.addProperty("id", e.id);
            o.addProperty("a", e.a);
            o.addProperty("b", e.b);
            o.addProperty("mode", e.mode);
            o.addProperty("thickness", e.thickness);
            edges.add(o);
        }
        root.add("edges", edges);

        JsonArray faces = new JsonArray();
        for (int[] tri : data.faces) {
            JsonObject o = new JsonObject();
            JsonArray idx = new JsonArray();
            for (int i : tri) idx.add(i);
            o.add("indices", idx);
            faces.add(o);
        }
        root.add("faces", faces);

        File dir = new File(SHAPES_DIR);
        if (!dir.exists()) dir.mkdirs();

        try (FileWriter w = new FileWriter(SHAPES_DIR + filename)) {
            gson.toJson(root, w);
            return true;
        } catch (IOException e) {
            System.err.println("[ShapeLoader] save error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lists all available shape files in the shapes directory.
     *
     * @return an array of .json file names
     */
    public static String[] listShapes() {
        File dir = new File(SHAPES_DIR);
        if (!dir.exists()) return new String[0];
        return dir.list((d, name) -> name.endsWith(".json"));
    }
}
