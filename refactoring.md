# Feuille de route Refactoring

## Classes ciblées par ordre de priorité

### 1. Camera.java (216 lignes) — Plus simple

**Problème :** Responsabilités mélangées (orbite, roll, axes, vue). Déjà améliorée (pitch ±90°, sortie d'orbite robuste).

**Proposition :**

| Nouvelle classe | Responsabilité | Effort |
|---|---|---|
| `AxesCalculator` | `updateAxes()` + `updateAxesToTarget()` (stateless, utilitaire pur) | 30min |
| `OrbitController` | `target`, `orbitTheta/Phi/Radius`, logique d'orbite | 1h |

**Résultat :** Camera passerait de ~216 à ~120 lignes, plus lisible.

---

### 2. Shape.java (509 lignes) — Le plus gros

**Problème :** 4 responsabilités mélangées dans une seule classe :
- Gestion VAO/VBO + rendu OpenGL
- Collisions (triangle-triangle, rayon-triangle, AABB)
- Conversion de coordonnées et formats de vertex
- Transformations géométriques (scale, center, applyTransform)

**Proposition :**

| Nouvelle classe | Responsabilité | Lignes extraites | Effort |
|---|---|---|---|
| `MeshCollider` | Collision triangle-triangle (Moller), ray-triangle, ray-AABB | ~150 | 1-2h |
| `VertexUtils` | `autoAddSlotColor`, `autoAddSlotTexture`, `convertLogicalToNormalized` | ~40 | 30min |
| `Shape` (allégé) | VAO/VBO, `render()`, `cleanup()`, `clone()`, `setColor()`, `setScale()`, `center()` | ~300 | — |

---

### 3. Système Text (542 lignes, 4 fichiers)

**Problèmes :**
- `Text.getTextWidth()` + `getTextHeight()` appellent STB **2 fois** par texte par frame
- `Text.drawText()` recrée la matrice ortho à chaque appel
- `TextManager.renderAlignedTexts` / `renderCenterTexts` ~70% de code dupliqué
- `TextHUD.getText(GameData)` switch de 20 cas — couplage fort à GameData
- `Text` entièrement statique — pas testable, pas injectable
- `TextHUD` a 4 constructeurs → builder pattern

**Proposition :**

| Action | Bénéfice | Effort |
|---|---|---|
| Fusionner `getTextWidth`/`getTextHeight` → `getTextExtent()` | -50% appels STB | 30min |
| Cacher matrice ortho dans `Text` (recréée seulement au resize) | -1 allocation/texte/image | 15min |
| Dédupliquer `renderAlignedTexts`/`renderCenterTexts` | ~80 lignes supprimées | 1h |
| Remplacer le switch de `TextHUD.getText()` par `Map<TextType, Function<GameData, String>>` | Découplage GameData | 1h |
| Extraire interface `TextRenderer` de `Text` (instance) | Testabilité, injectable | 30min |
| Builder pattern pour `TextHUD` (4 constructeurs) | Clarté | 30min |

---

## Branches

| Branche | Refactoring | Statut |
|---|---|---|
| `refactoring/camera` | Camera.java → AxesCalculator + OrbitController | ✅ Créée |
| `refactoring/shape` | Shape.java → MeshCollider + VertexUtils | ⏳ À faire |
| `refactoring/text` | Système Text complet | ⏳ À faire |
