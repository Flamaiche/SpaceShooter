# MarkerShape — Todo

## Projet
- [x] `README.md` — spécification complète
- [ ] `src/main/java/markershape/` — package dédié
- [x] `data/markershape/shapes/` — dossier des fichiers shape
- [x] `data/markershape/shaders/` — dossier des shaders
- [ ] `data/markershape/config.json` — config fond d'écran

## Data model
- [ ] `Vertex` : id, x, y, z, color, edgeIds (cascade suppression)
- [ ] `Edge` : id, a, b, mode (stun/move), thickness
- [ ] `Face` : indices
- [ ] `ShapeData` : name, shader, HashMap<id, Vertex>, HashMap<id, Edge>, List<Face>
- [ ] Lookup O(1), suppression point O(k), suppression arête O(1)

## ShapeLoader
- [ ] `ShapeLoader` — chargement JSON → ShapeData
- [ ] Gestion erreurs (fichier invalide, manquant)
- [ ] `ShapeSaver` — ShapeData → JSON (bouton Save)
- [ ] Sauvegarde uniquement les données géométriques (pas le shader)

## ShapeRenderer
- [ ] `ShapeRenderer` — conversion ShapeData → Shape existant
- [ ] Chargement shader à l'ouverture
- [ ] Libération shader au retour (sauf option keep shader future)
- [ ] Rendu temps réel triangles 3D

## Camera
- [ ] OrbitalCamera — yaw/pitch autour de la shape
- [ ] Zoom avant/arrière
- [ ] Souris libre (pas de lock)

## UI (fenêtres flottantes)
- [ ] Overlay 2D OpenGL maison
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

## Future
- [ ] Symétrie axe X/Y/Z et point central
- [ ] Option "keep shader" entre deux shapes
- [ ] Panneau debug listant tous les éléments
- [ ] Fond écran configurable (markershape.json)
