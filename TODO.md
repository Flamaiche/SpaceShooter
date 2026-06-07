# MarkerShape — Todo

## Projet
- [x] `README.md` — spécification complète
- [x] `src/main/java/markershape/` — package dédié
- [x] `data/markershape/shapes/` — dossier des fichiers shape
- [x] `data/markershape/shaders/` — dossier des shaders
- [ ] `data/markershape/config.json` — config fond d'écran
- [x] `AjoutVersion(SpaceShooter).txt` — changelog à jour

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
- [x] Sauvegarde uniquement les données géométriques (pas le shader)

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
- [x] Fenêtre survol point (glow + infos)
- [x] Fenêtre survol arête (glow)
- [x] Fenêtre édition point (coordonnées, couleurs, liaisons)
- [x] Fenêtre édition arête (mode, thickness)
- [x] Bouton New unifié (dropdown Vertex/Edge)
- [x] Bouton Save
- [x] Croix de fermeture
- [x] Bouton Delete dans les overlays
- [x] SiblingPicker (popup pour résoudre les sommets partageant la même position)

## Interaction points
- [x] Survol → illumination (glow double)
- [x] Clic → fenêtre édition (ou sélection)
- [x] Ajout point (clic 3D en mode Vertex)
- [x] Modification coordonnées ([-]/[+])
- [x] Suppression point (cascade arêtes + faces)
- [x] Couleur par point ([-]/[+])
- [x] Drag de sommet (écran → profondeur verrouillée)

## Interaction arêtes
- [x] Survol → illumination (ligne blanche)
- [x] Clic → fenêtre détails
- [x] Ajout liaison entre deux sommets (mode Edge)
- [x] Suppression arête (seule, cascade faces)
- [x] Mode stun/move
- [x] Épaisseur configurable ([-]/[+])
- [x] Connected-edge highlighting (triple couleur : hover/selected/common)

## Face
- [x] Création automatique après ajout d'arête
- [x] Suppression automatique (cleanupFaces)

## Crosshair
- [x] Axes X/Y/Z (rouge/vert/bleu) en 3D
- [x] Suit la souris en mode Vertex
- [x] Suit le sommet sélectionné
- [x] Suit le sommet en cours de drag
- [x] Rendu par-dessus (glDepthMask false)

## Save
- [x] Bouton Save → ShapeData → JSON (Ctrl+S)
- [x] Écraser le fichier existant

## Filtres
- [x] Bouton filtre avec checkboxes :
- [x] Afficher toutes les arêtes
- [x] Afficher tous les points
- [x] Afficher les faces
- [x] Sliders (taille points, épaisseur lignes, alpha faces)
- [x] Click-and-drag sur les sliders

## Court terme
- [x] **Undo/Redo** — pile d'états ShapeData (Ctrl+Z, Ctrl+Shift+Z), snapshot avant mutation, max 50, clear au changement de shape
- [x] **Snap‑to‑grid** — accrochage placement/drag (checkbox + slider pas 0.1–5 dans le panneau filtre)
- [x] **Grille 3D** — quadrillage XZ (Y=0), 21×21 lignes de -10 à +10, axes XYZ au centre (rouge/vert/bleu)
- [x] **Filtres** — positionné sous le bouton, boutons [-] [+] pour chaque slider, axes XYZ toggleables, grille + axes liés

## Moyen terme
- [ ] **Menu symétrie**
  - Afficher/masquer le centre de symétrie
  - Afficher/masquer l'axe central
  - Sélectionner un axe (X/Y/Z ou axe central) → symétrie miroir des éléments sélectionnés
  - Sélectionner le centre → symétrie centrale (point)
- [ ] **BluePrint (clone)**
  - Changement de curseur en main
  - Clic sur sommet/arête + touche `C` → entre en mode blueprint
  - Le clone suit la souris (les sommets et arêtes sélectionnés)
  - Le clone peut lui-même subir les symétries
  - Si une arête est sélectionnée, ses deux sommets sont inclus dans le clone
  - La source reste en place (copie, pas déplacement)

## Plus tard
- [ ] Option "keep shader" entre deux shapes
- [ ] Panneau debug listant tous les éléments
- [ ] Fond écran configurable (markershape.json)
