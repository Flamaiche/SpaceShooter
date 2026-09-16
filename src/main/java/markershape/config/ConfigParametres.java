package markershape.config;

import com.google.gson.JsonObject;
import gamegl.utils.GetDonnee;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Configuration de l'application chargée depuis un fichier JSON, avec des
 * catégories de paramètres destinées à l'interface.
 */
public class ConfigParametres {
    public String name;
    public JsonObject valeurs;
    public List<Categorie> categories;

    private static ConfigParametres instance;
    private boolean dirty;

    /** Retourne l'unique instance, en la chargeant depuis le fichier JSON ou en la créant par défaut. */
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

    /** Ajoute les valeurs manquantes par rapport à la configuration par défaut. */
    private void ensureDefaults() {
        JsonObject defaults = defaultConfig().valeurs;
        for (String key : defaults.keySet()) {
            if (!valeurs.has(key)) {
                valeurs.add(key, defaults.get(key));
            }
        }
    }

    /** Recharge la configuration depuis le disque en réinitialisant l'instance. */
    public static void recharger() {
        instance = null;
        get();
        if (instance != null) instance.dirty = false;
    }

    /** Sauvegarde l'instance courante dans le fichier JSON. */
    public static void sauvegarder() {
        if (instance == null) return;
        ArrayList<ConfigParametres> list = new ArrayList<>();
        list.add(instance);
        GetDonnee.writeJson("markershape/config/parametres.json", list);
        instance.dirty = false;
    }

    /** Remet à faux l'indicateur de modification de la configuration. */
    public static void resetDirty() {
        if (instance != null) instance.dirty = false;
    }

    /** Retourne la valeur flottante de la clé donnée (0 si absente). */
    public float getFloat(String key) {
        if (valeurs == null || !valeurs.has(key)) return 0;
        return valeurs.get(key).getAsFloat();
    }

    /** Retourne la valeur booléenne de la clé donnée (false si absente). */
    public boolean getBool(String key) {
        if (valeurs == null || !valeurs.has(key)) return false;
        return valeurs.get(key).getAsBoolean();
    }

    /** Enregistre une valeur flottante et marque la configuration comme modifiée. */
    public void setFloat(String key, float val) {
        if (valeurs == null) valeurs = new JsonObject();
        valeurs.addProperty(key, val);
        dirty = true;
    }

    /** Enregistre une valeur booléenne et marque la configuration comme modifiée. */
    public void setBool(String key, boolean val) {
        if (valeurs == null) valeurs = new JsonObject();
        valeurs.addProperty(key, val);
        dirty = true;
    }

    /** Indique si la configuration a été modifiée depuis la dernière sauvegarde. */
    public boolean hasChanges() { return dirty; }

    /** Compare deux configurations par leur nom. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigParametres that)) return false;
        return Objects.equals(name, that.name);
    }

    /** Code de hachage basé sur le nom de la configuration. */
    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    /** Construit la configuration par défaut avec toutes les valeurs initiales. */
    private static ConfigParametres defaultConfig() {
        ConfigParametres cp = new ConfigParametres();
        cp.name = "default";
        cp.valeurs = new JsonObject();
        cp.valeurs.addProperty("pointSize", 7.0);
        cp.valeurs.addProperty("lineWidth", 4.0);
        cp.valeurs.addProperty("faceAlpha", 1.0);
        cp.valeurs.addProperty("transparentUI", true);
        cp.valeurs.addProperty("menuR", 1);
        cp.valeurs.addProperty("menuG", 228);
        cp.valeurs.addProperty("menuB", 186);
        cp.valeurs.addProperty("bgR", 0);
        cp.valeurs.addProperty("bgG", 100);
        cp.valeurs.addProperty("bgB", 255);
        cp.valeurs.addProperty("textR", 255);
        cp.valeurs.addProperty("textG", 255);
        cp.valeurs.addProperty("textB", 0);
        cp.valeurs.addProperty("refBgR", 255);
        cp.valeurs.addProperty("refBgG", 255);
        cp.valeurs.addProperty("refBgB", 0);
        cp.valeurs.addProperty("gridVisible", true);
        cp.valeurs.addProperty("snapEnabled", false);
        cp.valeurs.addProperty("snapStep", 1.0);
        cp.valeurs.addProperty("magnetEnabled", false);
        cp.valeurs.addProperty("magnetRadius", 14f);
        cp.valeurs.addProperty("createColorR", 1f);
        cp.valeurs.addProperty("createColorG", 1f);
        cp.valeurs.addProperty("createColorB", 1f);
        cp.valeurs.addProperty("axisX", true);
        cp.valeurs.addProperty("axisY", true);
        cp.valeurs.addProperty("axisZ", true);
        cp.valeurs.addProperty("zoomSpeed", 0.1);
        cp.valeurs.addProperty("orbitSpeed", 2.0);
        cp.valeurs.addProperty("frontYaw", 0f);
        cp.valeurs.addProperty("frontPitch", 0f);
        cp.categories = defaultCategories();
        return cp;
    }

    /** Construit la liste des catégories de paramètres affichées dans l'interface. */
    private static List<Categorie> defaultCategories() {
        List<Categorie> cats = new ArrayList<>();

        Categorie aff = new Categorie();
        aff.id = "affichage"; aff.label = "Affichage";
        aff.params = new ArrayList<>();
        aff.params.add(param("pointSize", "Taille des points", "float", 1, 20, 1));
        aff.params.add(param("lineWidth", "Epaisseur des lignes", "float", 1, 10, 1));
        aff.params.add(param("faceAlpha", "Opacite des faces", "float", 0, 1, 0.05f));
        cats.add(aff);

        Categorie menuCat = new Categorie();
        menuCat.id = "menu"; menuCat.label = "Interface";
        menuCat.params = new ArrayList<>();
        menuCat.params.add(param("transparentUI", "Fond transparent", "bool", 0, 0, 0));
        Param menuR = param("menuR", "Couleur de l'interface (rouge)", "float", 0, 255, 1);
        menuR.showIf = new ShowIf("transparentUI", false);
        menuCat.params.add(menuR);
        Param menuG = param("menuG", "Couleur de l'interface (vert)", "float", 0, 255, 1);
        menuG.showIf = new ShowIf("transparentUI", false);
        menuCat.params.add(menuG);
        Param menuB = param("menuB", "Couleur de l'interface (bleu)", "float", 0, 255, 1);
        menuB.showIf = new ShowIf("transparentUI", false);
        menuCat.params.add(menuB);
        cats.add(menuCat);

        Categorie arriere = new Categorie();
        arriere.id = "arriereplan"; arriere.label = "Couleurs fond et texte";
        arriere.params = new ArrayList<>();
        arriere.params.add(param("bgR", "Fond - rouge", "float", 0, 255, 1));
        arriere.params.add(param("bgG", "Fond - vert", "float", 0, 255, 1));
        arriere.params.add(param("bgB", "Fond - bleu", "float", 0, 255, 1));
        arriere.params.add(param("textR", "Texte - rouge", "float", 0, 255, 1));
        arriere.params.add(param("textG", "Texte - vert", "float", 0, 255, 1));
        arriere.params.add(param("textB", "Texte - bleu", "float", 0, 255, 1));
        cats.add(arriere);

        Categorie grille = new Categorie();
        grille.id = "grille"; grille.label = "Grille et axes";
        grille.params = new ArrayList<>();
        grille.params.add(param("gridVisible", "Afficher la grille", "bool", 0, 0, 0));
        grille.params.add(param("snapEnabled", "Accrochage a la grille", "bool", 0, 0, 0));
        grille.params.add(param("snapStep", "Pas de l'accrochage", "float", 0.1f, 5, 0.1f));
        grille.params.add(param("magnetEnabled", "Aimantation active", "bool", 0, 0, 0));
        grille.params.add(param("magnetRadius", "Rayon d'aimantation", "float", 1f, 60f, 1f));
        grille.params.add(param("axisX", "Afficher l'axe X", "bool", 0, 0, 0));
        grille.params.add(param("axisY", "Afficher l'axe Y", "bool", 0, 0, 0));
        grille.params.add(param("axisZ", "Afficher l'axe Z", "bool", 0, 0, 0));
        cats.add(grille);

        Categorie camera = new Categorie();
        camera.id = "camera"; camera.label = "Camera";
        camera.params = new ArrayList<>();
        camera.params.add(param("zoomSpeed", "Vitesse de zoom", "float", 0.1f, 5, 0.1f));
        camera.params.add(param("orbitSpeed", "Vitesse de rotation", "float", 0.1f, 5, 0.1f));
        cats.add(camera);

        return cats;
    }

    /** Crée un paramètre d'interface avec ses métadonnées (clé, libellé, type, bornes, pas). */
    private static Param param(String key, String label, String type, float min, float max, float step) {
        Param p = new Param();
        p.key = key; p.label = label; p.type = type;
        p.min = min; p.max = max; p.step = step;
        return p;
    }

    /** Catégorie de paramètres regroupés pour l'affichage dans l'interface. */
    public static class Categorie {
        public String id, label;
        public List<Param> params;
        public ShowIf showIf;

        /** Indique si la catégorie doit être affichée selon sa condition de visibilité. */
        public boolean isVisible(ConfigParametres cfg) {
            if (showIf == null) return true;
            boolean current = cfg.getBool(showIf.key);
            return showIf.eq instanceof Boolean ? current == (Boolean) showIf.eq : current;
        }
    }

    /** Paramètre configurable avec ses métadonnées et sa condition de visibilité. */
    public static class Param {
        public String key, label, type;
        public float min, max, step;
        public ShowIf showIf;

        /** Indique si le paramètre doit être affiché selon sa condition de visibilité. */
        public boolean isVisible(ConfigParametres cfg) {
            if (showIf == null) return true;
            boolean current = cfg.getBool(showIf.key);
            return showIf.eq instanceof Boolean ? current == (Boolean) showIf.eq : current;
        }
    }

    /** Condition de visibilité (clé de paramètre et valeur attendue). */
    public static class ShowIf {
        public String key;
        public Object eq;

        /** Constructeur par défaut. */
        public ShowIf() {}
        /** Construit une condition de visibilité sur une clé et une valeur attendue. */
        public ShowIf(String key, Object eq) { this.key = key; this.eq = eq; }
    }
}
