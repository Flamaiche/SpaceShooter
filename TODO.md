# MarkerShape — Todo

## Projet
- [x] `README.md` — spécification complète
- [x] `src/main/java/markershape/` — package dédié
- [x] `data/markershape/shapes/` — dossier des fichiers shape
- [x] `data/markershape/shaders/` — dossier des shaders
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

## Éditeur — PRIORITÉ : navigation & placement (2026-09-13)
Objectif : l'éditeur doit être « malléable » — bouger (caméra) et placer (éléments)
doit devenir fluide à la souris. Réutiliser le moteur `learngl` :
`OrbitController` (orbite + target mobile), `AxesCalculator`, `Camera`/`CameraPhysics`/
`GestionnaireVue` (vue FPS), `Shader`, `Text`.

### P1. Naviguer à la souris (orbite + pan + zoom curseur)
- [x] Câbler `EditorCamera` sur `learngl.camera.OrbitController` (au lieu des calculs
      maison yaw/pitch/radius) → orbite, zoom, + target déplaçable (pan) fournis
      par le moteur. Conserver yaw/pitch/radius exposés pour l'existant.
- [x] Orbite souris : clic droit maintenu + déplacement horizontal/vertical
      → `camera.rotate(dyaw, dpitch)` (le clic gauche conserve son rôle
      sélection/placement ; les flèches clavier restent actives)
- [x] Pan caméra : MAJ + clic droit maintenu (ou molette appuyée) → déplacer
      le target le long du plan de vue (perpendiculaire au regard) via
      `right`/`up` calculés par `AxesCalculator`
- [x] Zoom vers le curseur : ajuster target + radius pour garder le point sous
      la souris à l'écran pendant le zoom molette
- [x] Recentrage auto sur la shape au chargement : target = centre de la bounding
      box, radius = taille max de la shape → la forme apparaît cadrée
- [x] Touche `R` = reset de vue → revient devant l'« avant » de la shape

### P1b. Direction fixe « avant » de la shape + reset (RFC 13/09)
- [x] Définir une direction d'« avant » : touche `F` capture la direction de la
      caméra actuelle → devient « l'avant » de la shape (il se trouve dans ce sens)
- [x] `R` cadre la caméra face à l'avant (la vue par défaut du reset pointe là)
- [x] Sauvegarder l'avant dans `data/markershape/config/parametres.json`
      (frontYaw/frontPitch ou vecteur avant) via `ConfigParametres`
- [x] Afficher une flèche/repère du sens de l'avant dans la vue 3D
      (renderer lignes réutilisant le pattern `CrosshairRenderer`/`Shader`)

### P2. Bouger les éléments (drag + axes + saisie)
- [x] Câbler `DragAction` (déjà implémenté, jamais utilisé) : press sur un sommet
      visible → drag (depth verrouillée `dragNdcZ`), move → update, release → end.
      Snap reste actif pendant le drag (`ctx.snapIfEnabled`).
      Seuil de distance (px) pour distinguer clic (sélection) vs drag (déplacement)
- [x] Contrainte d'axe pendant le drag : maintenir `X`/`Y`/`Z` pendant le drag
      → verrouille le déplacement le long de l'axe correspondant
      (le delta χρlimited à l'axe, appliqué dans `DragAction.update`)
- [x] Saisie numérique dans les overlays : clic sur une valeur (X/Y/Z, R/G/B,
      épaisseur) → mode frappe (buffer + curseur clignotant) → Entrée = valider,
      Échap = annuler. Implémenté nativement dans `VertexOverlay`/`EdgeOverlay`
      (pattern proche de `EditableTextField`, validé sur gammes) → Entrée = valider,
      Échap = annuler.

### P3. Placer proprement (chaîne + aimantation + ghost)
- [x] Mode chaîne/polyline : en mode Vertex, chaque clic pose un sommet ;
      clic sur un sommet existant = crée l'arête avec le dernier posé ;
      en mode Edge, l'enchaînement continue (reste actif après chaque paire) ;
      Échap = arrêter la chaîne et sortir des modes
- [x] Aimantation magnétique : quand le snap est actif et la souris passe près
      d'un sommet existant (rayon configurable en px), le ghost de placement
      s'y accroche (position exacte du sommet, pas juste la grille)
- [x] Ghost de placement : faux sommet semi-transparent à la taille réelle
      des points (et non juste le crosshair) avant de valider le clic

### Affichages & diagnostics (long terme, réutiliser le moteur)
- [ ] Afficher la taille de la shape (largeur/hauteur/profondeur de la `ShapeData`)
- [ ] Afficher le centre (marqueur au centre de la bounding box)
- [ ] Afficher les vecteurs (triad position, normales des faces, bbox wireframe)
- [ ] Vue « première personne » : prévisualiser la shape comme vue du joueur en
      réutilisant `learngl.camera.Camera` + `CameraPhysics` + `GestionnaireVue`
      (mêmes patterns que `PlayingState` : déplacement ZQSD + souris libre)
- [ ] Raccourcis touches documentés (touche H = aide) + `touches.txt` à jour

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
- [x] Panneau debug listant tous les éléments (EntityListPanel)
