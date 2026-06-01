# MarkerShape — Éditeur de formes 3D

Application standalone de visualisation et d'édition de formes 3D (maillages/polygones).

## Fonctionnalités

### Chargement
- Sélection d'un fichier shape depuis un dossier de l'application
- Chargement et affichage au centre de l'écran
- Face avant de la shape orientée vers la caméra

### Visualisation 3D
- Déplacement orbital autour de la shape (yaw/pitch)
- Zoom avant/arrière
- Souris libre (pas de lock)

### Interaction avec les points
- **Survol** : le point/l'axe s'illumine (glow)
- **Clic sur un point** : fenêtre d'édition avec ses coordonnées et ses liaisons
- **Bouton +** (haut) : ajouter un nouveau point avec ses coordonnées

### Interaction avec les arêtes (liaisons)
- **Survol** : l'arête s'illumine
- **Clic sur une arête** : fenêtre avec les deux points reliés et les détails
- **Mode** : `stun` (fixe) ou `move` (déformable selon les autres points)
- **Taille** : épaisseur/longueur configurable

### Édition
- Modification des coordonnées d'un point
- Ajout de liaison entre deux points (sélection depuis une liste)
- Couleur par point
- Rendu en temps réel

### Sauvegarde
- Bouton **Save** : sauvegarde la forme modifiée dans son fichier

### Debug (plus tard)
- Panneau latéral listant tous les éléments de la shape

### UI
- Fenêtres flottantes (rectangles avec croix de fermeture)
- Boutons + et Save

## Architecture

- Java + LWJGL (OpenGL)
- Réutilise les systèmes existants : Shape, Shader, Camera orbit, PreVerticesTable
- Package dédié : `markershape` (au même niveau que `gamegl` et `learngl`)

### Règle stricte : réutilisation sans modification

L'app `markershape` **ne modifie aucun fichier existant** de `gamegl/` ou `learngl/`.
Elle peut :
- Instancier et appeler les classes existantes (`Shape`, `Shader`, `Camera`, `PreVerticesTable`, etc.)
- Créer ses propres classes dans `markershape/` qui **enveloppent ou étendent** le comportement

Elle ne doit **jamais** :
- Modifier un fichier source de `gamegl/` ou `learngl/`
- Changer une signature ou un comportement existant pour ses besoins

Après finalisation, une refactorisation mutualisée sera envisagée si pertinent.

## Format de fichier (data/shapes/*.json)

```json
{
  "name": "playerShip",
  "shader": "default",
  "vertices": [
    { "id": 0, "x": -0.385, "y": 0.0, "z": 0.0, "color": [0.75, 0.08, 0.08] }
  ],
  "edges": [
    { "id": 0, "a": 0, "b": 1, "mode": "stun", "thickness": 0.02 }
  ],
  "faces": [
    { "indices": [0, 1, 2] }
  ]
}
```

## Stockage (en mémoire)

- `HashMap<Integer, Vertex>` — lookup O(1) par ID
- `HashMap<Integer, Edge>` — lookup O(1) par ID
- Chaque `Vertex` stocke ses `edgeIds` (pour suppression en cascade)
- Suppression point O(k) avec k = nombre d'arêtes liées
- Suppression arête O(1)

## Futures fonctionnalités

- Symétrie sur axe X/Y/Z et sur un point central
- Option "keep shader" en mémoire entre deux visualisations
- Fond d'écran configurable (fichier `data/markershape.json`)
- Panneau debug listant tous les éléments
