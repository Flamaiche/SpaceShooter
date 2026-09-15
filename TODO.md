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
- [x] Suppression point (cascade arêtes + faces, avec confirmation popup)
- [ ] Couleur par point (obsolète → voir P6)
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
- [x] Orbite autour du point sous la souris : au début du geste, le pivot est
      capté au curseur (géométrie si possible, sinon profondeur du centre) et
      `camera.setOrbitPivot` y ancre l'orbite (yaw/pitch conservés). Un "+"
      orange billboard (OrbitPivotRenderer) marque le pivot pendant le geste
      et se cache au relâchement — le pan garde la priorité sur l'orbite
- [x] Pan caméra : MAJ + clic droit maintenu (ou molette appuyée) → déplacer
      le target le long du plan de vue (perpendiculaire au regard) via
      `right`/`up` calculés par `AxesCalculator`
- [x] Zoom vers le curseur : ajuster target + radius pour garder le point sous
      la souris à l'écran pendant le zoom molette
- [x] Recentrage auto sur la shape au chargement : target = centre de la bounding
      box, radius = taille max de la shape → la forme apparaît cadrée
- [x] Touche `R` = reset de vue → revient devant l'« avant » de la shape

### P1b. Direction fixe « avant » de la shape + reset (RFC 13/09)
- [x] Le front n'est plus capturé par une touche : c'est un VECTEUR FIXE dans
      parametres.json (frontYaw/frontPitch) utilisé comme placement caméra par défaut
- [x] `R` cadre la caméra selon ce vecteur de base (défini en P5) — non modifiable
- [x] Sauvegarder le vecteur dans `data/markershape/config/parametres.json`
      (frontYaw/frontPitch) via `ConfigParametres`
- [x] Afficher la flèche/repère du vecteur de base dans la vue 3D
      (FrontArrowRenderer en triangles, réutilisant `Cam`/`TriBuilder`/`TriShape`)
- [x] Au chargement d'une shape, la caméra part de ce vecteur (frameToShape → front)

### P2. Bouger les éléments (drag + axes + saisie)
- [x] Câbler `DragAction` (déjà implémenté, jamais utilisé) : press sur un sommet
      visible → drag (depth verrouillée `dragNdcZ`), move → update, release → end.
      Snap reste actif pendant le drag (`ctx.snapIfEnabled`).
      Seuil de distance (px) pour distinguer clic (sélection) vs drag (déplacement)
- [x] Contrainte d'axe pendant le drag : panneau flottant (boutons X/Y/Z/Libre)
      survolé pendant le drag → verrouille le déplacement le long de l'axe
      (le delta limité à l'axe, appliqué dans `DragAction.update`)
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

### P4. Création & édition facilitée (RFC 13/09)
Objectif : placer et modifier les éléments sans friction — la création de vertex et
d'arêtes doit devenir gestuelle, et toute modification rester accessible en 1-2 gestes.

#### P4a. Sommets (création gestuelle)
- [ ] Créer un sommet en cliquant-glissant (drag depuis le vide → posé au release,
      avec ghost + snap + aimantation déjà en place)
- [x] Ajouter un sommet PARMI un point déjà mis : en mode Vertex, MAJ+clic sur un
      sommet existant → pose un sommet frère co-localisé à la même position
- [x] Subdivision d'arête : sélectionner une arête → touche S → nouveau
      sommet au milieu, l'arête est remplacée par deux arêtes (faces subdivisées)
- [x] Choisir la couleur au moment de la création (`defaultVertexColorR/G/B`
      dans parametres.json, appliquée aux sommets posés au clic)
      NOTE: à supprimer dans P6 (la couleur passera aux arêtes/faces uniquement)
- [x] Ctrl+D : dupliquer le(s) sommet(s) sélectionné(s) offsetés (copie + sélection
      de la copie)
- [x] Weld/fusion : sélection de 2+ sommets → touche M → fusion en position
      moyenne (couleur moyennée) avec réunion des arêtes / retrait des loops
- [ ] Snap/déplacement en chaque point du drag : afficher les coordonnées live dans
      l'overlay pendant le drag (pas seulement au release)
- [x] Coordonnées live dans l'overlay PENDANT le drag (rendu temps réel)
- [x] Le grab direct (G) est supprimé : le drag de sommet + `Tab` (pendant le
      drag) verrouille la sélection au curseur (relâcher le clic = marcher,
      re-clic = valider, ESC = annuler) — rend le grab autonome inutile

#### P4b. Arêtes & Faces (tracé multidés)
- [x] Création d'arêtes à la volée pendant le placement de points : garder le mode
      Vertex actif et relier les points posés (chaîne existante) avec GHOST visible
- [x] Création de face à partir de points que l'on place : cliquer N points (mode
      "Tracé") → la face se ferme au clic sur le premier point (ou touche Entrée) →
      triangulation automatique (fan/ear-clip) + détection faces existantes
      (touche T / palette Outils → Tracé, contour orange prévisualisé)
- [x] Prévisualiser la face potentielle pendant le tracé (contour apparaît avant
      validation : polyline du loop + fermeture vers le curseur)
- [x] Création d'arêtes par rubber-band : MAJ + clic-gauche maintenu sur un sommet,
      survoler le sommet cible → prévisualisation de l'arête (ligne cyan), relâcher =
      création (le drag simple continue de déplacer)
- [x] Extrude : sélectionner une arête → touche E → les 2 sommets sont dupliqués
      et décalés (normal de face si possible), arêtes + faces créées automatiquement
- [x] Fill : sélectionner 3+ arêtes formant un contour fermé → touche F → crée les
      faces internes (triangulation fan)

#### P4c. Modification plus facile de TOUT le reste
- [x] Sélection multiple : Ctrl+clic pour ajouter/retirer un élément, clic sur un
      membre = le rend primaire, Ctrl+clic vide / clic vide = tout désélectionner,
      Ctrl+A = tout sélectionner
- [x] Marquee/boîte de sélection (cadre 2D → prend les sommets inclus à la vue)
- [x] Déplacement groupé : drag d'un sommet sélectionné → tout le groupe suit
      (tous les sommets de la sélection, axes X/Y/Z toujours valables)
- [x] Suppression groupée (Suppr/Backspace sur la sélection multiple, avec
      confirmation popup [Oui]/[Non])
- [ ] Panel de sélection multi (liste en bas à gauche) : liste toutes les entités
      sélectionnées avec cocheboks, choix couleurs/modes en masse
- [x] Copy/paste : Ctrl+C copie la sélection, Ctrl+V recolle offseté
      (clone via presse-papier interne de l'éditeur)
- [x] Pipette couleur : clic droit (tap) sur un sommet coloré → sa couleur devient la
      couleur courante (application aux prochaines créations)
- [x] Raccourcis clavier centralisés et documentés (voir `CommandesEditeur.txt`),
      touche H = aide en jeu (HelpOverlay)

#### P4d. Outil Clean (nettoyage automatique)
- [x] Touche `K` : supprime tout ce qui ne sert à rien
      - [x] Sommets orphelins (aucune arête ne les référence)
      - [x] Arêtes isolées (les 2 extrémités ne portent que cette arête)
      - [x] Faces invalides (indices inexistants, arêtes manquantes → cleanupFaces)
      - [ ] Sommets frères en double à la même position (fusion, optionnel — rapporté)
- [x] Compte-rendu après suppression ("N sommets, M arêtes, X faces" + doublons)
      avec undo/redo (snapshot avant nettoyage)

#### P4e. Nouveaux outils & meilleur choix de touches
- [x] Palette d'outils dans l'UI (bouton "Outils" + ToolPalette) : Sélection,
      Vertex, Arête, Subdiviser, Extruder, Remplir, Fusionner, Dupliquer,
      Copier, Coller, Nettoyer, Aide — mode actif mis en évidence (> en marge),
      ouverture/fermeture croisées New/Outils/Filtre + Échap
- [x] Mapping des touches révisé et documenté :
      - [x] `Ctrl+C` presse-papier (coleur Ctrl+D distinct) — `C` réservé clone MT
      - [x] `S` subdivise / `E` extrude / `F` fill (aretes selec) sinon front /
            `M` weld — conflits résolus (F double rôle + Ctrl+F front forcé)
      - [x] Grab via `Tab` pendant le drag (la sélection suit le curseur même
            sans clic, LMB=valider, ESC=annuler) — le grab `G` autonome est retiré
      - [x] `T` mode Tracé (face)
      - [x] `H` aide + tous les raccourcis affichables en jeu (HelpOverlay)
- [x] Undo/redo par outil (chaque action d'outil fait un snapshot atomique)

### P6. Simplification de la création des couleurs (RFC 14/09)
Objectif : la couleur n'est plus portée par les points (auto-gérés), mais par les
arêtes et les faces, choisies par l'utilisateur. Les points sont invisibles côté
couleur : ils servent uniquement à structurer la géométrie.

- [x] Couleur des points supprimée : plus de `defaultVertexColorR/G/B` pour le
      placement ; l'édition de la couleur d'un point (overlay / pipette) est retirée
- [x] Points auto-gérés : un point qui ne participe à aucune arête est supprimé
      automatiquement (réutilise le nettoyage `K` déclenché en continu/à la volée)
- [x] Couleur des arêtes : choisir la couleur d'une arête (overlay d'édition,
      pipette clic-droit, échantillon de couleurs dans la palette Outils)
- [x] Couleur des faces : une face créée (T tracé / F fill / E extrude / selection)
      reçoit une couleur choisie par l'utilisateur (couleur de création courante)
- [x] Créer une face par sélection : sélectionner N sommets et/ou arêtes → commande
      "Créer face" → triangulation automatique (fan/ear-clip existant), contour
      prévisualisé avant validation
- [x] Migration du format JSON : `Vertex` sans couleur (ou couleur ignorée),
      `Edge`/`Face` avec couleur persistée (`parametres.json`, `ShapeData`)

### P5. Vecteur de camera de base = placement caméra par défaut (RFC 13/09, simplifie 15/09)
Vision : le front (frontYaw/frontPitch) est un VECTEUR CAMERA FIXE, le même partout
dans tout l'éditeur. Il indique quelle est la base camera de la shape — le point d'où
l'éditeur place la caméra par défaut au chargement (et R reset). C'est aussi là que se
placera la camera du jeu (purement indicatif : le jeu gère lui-même ses formes, rien
n'est exporté). Il n'est PAS modifiable dans l'éditeur (aucune capture F, aucun mode
édition) — seule la flèche le rend visible en 3D.

- [x] Vecteur unique : `frontYaw`/`frontPitch` dans parametres.json, appliqués à la
      caméra au chargement (frameToShape et reset R depuis le front)
- [x] Flèche visuelle 3D du vecteur de base (FrontArrowRenderer, triangles) visible
      dans la vue éditeur — purement indicatif
- [x] Non éditable : capture F retirée (F = Remplir), aucun mode de placement/édition
- [x] Aucun export vers le jeu (le jeu gère ses formes, toutes dans le même sens)
- [x] Visibilité filtrée : la flèche est gérée par Filtre/affichage (masquée avec les
      overlays transitoires quand le menu est ouvert)

(remplace la vision P5 initiale : défaut par shape, placement 2 points, export jeu,
mode édition — ces sous-item sont abandonnés car le vecteur est un simple repère
visuel fixe, et les formes partagent toutes la même orientation)

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
