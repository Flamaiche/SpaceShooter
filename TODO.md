# MarkerShape — Todo

## Projet
- [x] `README.md` — spécification complète
- [x] `src/main/java/markershape/` — package dédié
- [x] `data/markershape/shapes/` — dossier des fichiers shape
- [x] `data/markershape/shaders/` — dossier des shaders
- [ ] `data/markershape/config.json` — config fond d'écran

## Data model
- [x] `Vertex` : id, x, y, z, color, edgeIds (cascade suppression)
- [x] `Edge` : id, a, b, mode (stun/move), thickness
- [x] `Face` : indices
- [x] `ShapeData` : name, shader, HashMap<id, Vertex>, HashMap<id, Edge>, List<Face>
- [x] Lookup O(1), suppression point O(k), suppression arête O(1)

## ShapeIO
- [x] `ShapeIO.load` — chargement JSON → ShapeData
- [x] Gestion erreurs (fichier invalide, manquant)
- [x] `ShapeIO.save` — ShapeData → JSON
- [ ] Sauvegarde uniquement les données géométriques (pas le shader) ✓ déjà le cas

## ShapeRenderer
- [x] `ShapeRenderer` — conversion ShapeData → Shape existant
- [x] Chargement shader à l'ouverture
- [x] Libération shader au cleanup
- [x] Rendu temps réel triangles 3D

## Camera
- [x] OrbitalCamera — yaw/pitch autour de la shape
- [x] Zoom avant/arrière
- [x] Souris libre (drag pour orbiter)

## UI (fenêtres flottantes)
- [x] Overlay 2D OpenGL maison (top bar)
- [ ] Fenêtre survol point (glow + infos)
- [ ] Fenêtre survol arête (glow + infos)
- [ ] Fenêtre édition point (coordonnées, liaisons)
- [ ] Fenêtre édition arête (mode, thickness)
- [ ] Bouton + (ajout point)
- [ ] Bouton Save
- [ ] Fenêtres adaptatives (taille selon contenu)
- [ ] Croix de fermeture

## Interaction points
- [ ] Survol → illumination (glow)
- [ ] Clic → fenêtre édition
- [ ] Ajout point (bouton +)
- [ ] Modification coordonnées
- [ ] Suppression point (cascade arêtes)
- [ ] Couleur par point

## Interaction arêtes
- [ ] Survol → illumination (glow)
- [ ] Clic → fenêtre détails
- [ ] Ajout liaison entre deux points
- [ ] Suppression arête (seule)
- [ ] Mode stun/move
- [ ] Épaisseur configurable

## Save
- [ ] Bouton Save → ShapeData → JSON
- [ ] Écraser le fichier existant

## Filtres
- [ ] Bouton filtre avec checkboxes :
  - [ ] Afficher toutes les arêtes (glow)
  - [ ] Afficher tous les points (glow)
  - [ ] Afficher les faces

## Future
- [ ] Symétrie axe X/Y/Z et point central
- [ ] Option "keep shader" entre deux shapes
- [ ] Panneau debug listant tous les éléments
- [ ] Fond écran configurable (markershape.json)
