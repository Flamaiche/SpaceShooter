package markershape.config;

import com.google.gson.JsonObject;
import gamegl.utils.GetDonnee;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigParametres {
    public String name;
    public JsonObject valeurs;
    public List<Categorie> categories;

    private static ConfigParametres instance;
    private boolean dirty;

    public static ConfigParametres get() {
        if (instance == null) {
            List<ConfigParametres> list = GetDonnee.readJson("markershape/config/parametres.json");
            if (list != null && !list.isEmpty()) {
                instance = list.getFirst();
                instance.ensureDefaults();
            } else {
                instance = defaultConfig();
                sauvegarder();
            }
        }
        return instance;
    }

    private void ensureDefaults() {
        JsonObject defaults = defaultConfig().valeurs;
        for (String key : defaults.keySet()) {
            if (!valeurs.has(key)) {
                valeurs.add(key, defaults.get(key));
            }
        }
    }

    public static void recharger() {
        instance = null;
        get();
        if (instance != null) instance.dirty = false;
    }

    public static void sauvegarder() {
        if (instance == null) return;
        ArrayList<ConfigParametres> list = new ArrayList<>();
        list.add(instance);
        GetDonnee.writeJson("markershape/config/parametres.json", list);
        instance.dirty = false;
    }

    public static void resetDirty() {
        if (instance != null) instance.dirty = false;
    }

    public float getFloat(String key) {
        if (valeurs == null || !valeurs.has(key)) return 0;
        return valeurs.get(key).getAsFloat();
    }

    public boolean getBool(String key) {
        if (valeurs == null || !valeurs.has(key)) return false;
        return valeurs.get(key).getAsBoolean();
    }

    public void setFloat(String key, float val) {
        if (valeurs == null) valeurs = new JsonObject();
        valeurs.addProperty(key, val);
        dirty = true;
    }

    public void setBool(String key, boolean val) {
        if (valeurs == null) valeurs = new JsonObject();
        valeurs.addProperty(key, val);
        dirty = true;
    }

    public boolean hasChanges() { return dirty; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigParametres that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    private static ConfigParametres defaultConfig() {
        ConfigParametres cp = new ConfigParametres();
        cp.name = "default";
        cp.valeurs = new JsonObject();
        cp.valeurs.addProperty("pointSize", 5);
        cp.valeurs.addProperty("lineWidth", 3);
        cp.valeurs.addProperty("faceAlpha", 1.0);
        cp.valeurs.addProperty("transparentUI", true);
        cp.valeurs.addProperty("menuR", 13);
        cp.valeurs.addProperty("menuG", 31);
        cp.valeurs.addProperty("menuB", 46);
        cp.valeurs.addProperty("bgR", 26);
        cp.valeurs.addProperty("bgG", 26);
        cp.valeurs.addProperty("bgB", 31);
        cp.valeurs.addProperty("gridVisible", true);
        cp.valeurs.addProperty("snapEnabled", false);
        cp.valeurs.addProperty("snapStep", 1.0);
        cp.valeurs.addProperty("axisX", true);
        cp.valeurs.addProperty("axisY", true);
        cp.valeurs.addProperty("axisZ", true);
        cp.valeurs.addProperty("zoomSpeed", 0.5);
        cp.valeurs.addProperty("orbitSpeed", 2.0);
        cp.categories = defaultCategories();
        return cp;
    }

    private static List<Categorie> defaultCategories() {
        List<Categorie> cats = new ArrayList<>();

        Categorie aff = new Categorie();
        aff.id = "affichage"; aff.label = "Affichage";
        aff.params = new ArrayList<>();
        aff.params.add(param("pointSize", "Taille points", "float", 1, 20, 1));
        aff.params.add(param("lineWidth", "Epaisseur lignes", "float", 1, 10, 1));
        aff.params.add(param("faceAlpha", "Alpha faces", "float", 0, 1, 0.05f));
        cats.add(aff);

        Categorie menuCat = new Categorie();
        menuCat.id = "menu"; menuCat.label = "Barre de menu";
        menuCat.params = new ArrayList<>();
        menuCat.params.add(param("transparentUI", "Fond menu transparent", "bool", 0, 0, 0));
        Param menuR = param("menuR", "Couleur menu R", "float", 0, 255, 1);
        menuR.showIf = new ShowIf("transparentUI", false);
        menuCat.params.add(menuR);
        Param menuG = param("menuG", "Couleur menu V", "float", 0, 255, 1);
        menuG.showIf = new ShowIf("transparentUI", false);
        menuCat.params.add(menuG);
        Param menuB = param("menuB", "Couleur menu B", "float", 0, 255, 1);
        menuB.showIf = new ShowIf("transparentUI", false);
        menuCat.params.add(menuB);
        cats.add(menuCat);

        Categorie arriere = new Categorie();
        arriere.id = "arriereplan"; arriere.label = "Arriere-plan";
        arriere.params = new ArrayList<>();
        arriere.params.add(param("bgR", "Fond R", "float", 0, 255, 1));
        arriere.params.add(param("bgG", "Fond V", "float", 0, 255, 1));
        arriere.params.add(param("bgB", "Fond B", "float", 0, 255, 1));
        cats.add(arriere);

        Categorie grille = new Categorie();
        grille.id = "grille"; grille.label = "Grille";
        grille.params = new ArrayList<>();
        grille.params.add(param("gridVisible", "Visible", "bool", 0, 0, 0));
        grille.params.add(param("snapEnabled", "Snap actif", "bool", 0, 0, 0));
        grille.params.add(param("snapStep", "Pas snap", "float", 0.1f, 5, 0.1f));
        grille.params.add(param("axisX", "Axe X", "bool", 0, 0, 0));
        grille.params.add(param("axisY", "Axe Y", "bool", 0, 0, 0));
        grille.params.add(param("axisZ", "Axe Z", "bool", 0, 0, 0));
        cats.add(grille);

        Categorie camera = new Categorie();
        camera.id = "camera"; camera.label = "Camera";
        camera.params = new ArrayList<>();
        camera.params.add(param("zoomSpeed", "Zoom speed", "float", 0.1f, 5, 0.1f));
        camera.params.add(param("orbitSpeed", "Rotation speed", "float", 0.1f, 5, 0.1f));
        cats.add(camera);

        return cats;
    }

    private static Param param(String key, String label, String type, float min, float max, float step) {
        Param p = new Param();
        p.key = key; p.label = label; p.type = type;
        p.min = min; p.max = max; p.step = step;
        return p;
    }

    public static class Categorie {
        public String id, label;
        public List<Param> params;
        public ShowIf showIf;

        public boolean isVisible(ConfigParametres cfg) {
            if (showIf == null) return true;
            boolean current = cfg.getBool(showIf.key);
            return showIf.eq instanceof Boolean ? current == (Boolean) showIf.eq : current;
        }
    }

    public static class Param {
        public String key, label, type;
        public float min, max, step;
        public ShowIf showIf;

        public boolean isVisible(ConfigParametres cfg) {
            if (showIf == null) return true;
            boolean current = cfg.getBool(showIf.key);
            return showIf.eq instanceof Boolean ? current == (Boolean) showIf.eq : current;
        }
    }

    public static class ShowIf {
        public String key;
        public Object eq;

        public ShowIf() {}
        public ShowIf(String key, Object eq) { this.key = key; this.eq = eq; }
    }
}
