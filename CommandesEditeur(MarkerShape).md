# Commandes de l'editeur MarkerShape

---

## Navigation camera

| Action | Activation | Description |
|--------|-----------|-------------|
| Orbite | **Clic droit** maintenu + bouger la souris | Tourne autour du point sous le curseur (pivot capture au debut du geste, marque par un "+" orange) |
| Pan | **Molette** maintenue + bouger la souris | Deplace le point de vue (la scene suit le curseur) |
| Pan (alt.) | **MAJ + clic droit** maintenu + bouger la souris | Meme chose que molette |
| Orbite clavier | **Fleches** ↑ ↓ ← → | Orbite par pas fixe autour du centre de la forme |
| Zoom avant-arriere | **Molette haut-bas** | Zoom avant (molette haut) / arriere (molette bas), vers le point sous le curseur |
| Zoom clavier | **Ctrl+P** / **Shift+P** | Zoom avant / arriere par pas clavier |
| Cadrer la shape | **R** | Recadre automatiquement la camera face a l'avant (direction de cadrage fixe, non redefinissable) |

> **Note :** le pan est prioritaire sur l'orbite : des qu'un geste de pan est actif, il reste en pan jusqu'a la fin du geste.

---

## Selection

| Action | Activation | Description |
|--------|-----------|-------------|
| Selectionner un sommet | **Clic gauche** sur un sommet | Selectionne le sommet (fenetre d'edition) |
| Selectionner une arete | **Clic gauche** sur une arete | Selectionne l'arete (fenetre d'edition) |
| Ajouter/retirer (multi) | **Ctrl + clic gauche** sur sommet ou arete | Ajoute ou retire de la multi-selection (cyan) |
| Tout selectionner | **Ctrl+A** | Selectionne tous les sommets |
| Selection boite (marquee) | **Clic gauche maintenu** sur zone vide + deplacement | Cadre bleu ; tous les sommets visibles a l'interieur sont selectionnes |
| Selectionner par la liste | **Clic** sur un element de la liste d'entites (bas a gauche) | Selectionne le sommet ou l'arete correspondant |
| Deselectionner | **Clic gauche** sur zone vide | Deselectionne tout |

---

## Deplacement

| Action | Activation | Description |
|--------|-----------|-------------|
| Drag sommet | **Clic gauche maintenu** sur un sommet (> 5 px) | Deplace le sommet ou la selection. Panneau flottant pour contraindre l'axe |
| Tab pendant drag | **Tab** (durant le drag) | Passe en mode grab : relache le clic, la selection suit le curseur. Clic gauche ou ESC = valider/annuler |
| Supprimer | **Suppr** ou **Backspace** | Ouvre une confirmation ([Oui] / [Non]) puis supprime la selection (cascade sommets → aretes → faces) |

---

## Creation de sommets / aretes

| Action | Activation | Description |
|--------|-----------|-------------|
| Mode Vertex | Bouton **New** → Vertex, ou bouton **Outils** → Sommet | Chaque clic pose un sommet (les points sont neutres, sans couleur) |
| Chaine Vertex | Clic sur sommet existant pendant le mode Vertex | Cree une arete avec le dernier sommet pose et enchaine |
| Sommet frere | **MAJ + clic** sur un sommet existant | Pose un sommet co-localise (meme position) |
| Mode Edge | Bouton **New** → Edge, ou bouton **Outils** → Arete | Selectionnez 2 sommets pour creer une arete ; le mode enchaine |
| Aimantation | Case **Magnet** cochée dans Filtre | Le crosshair se colle au sommet le plus proche du curseur (rayon reglable) |
| Ghost de placement | Automatique en mode Vertex/Edge | Point orange semi-transparent montrant ou le sommet va atterrir |

---

## Creation de faces (Trace)

| Action | Activation | Description |
|--------|-----------|-------------|
| Mode Trace | **T**, ou bouton **Outils** → Trace (face) | Boucle de clics → aretes auto + faces a la fermeture |
| Fermer la boucle | Clic sur le **premier sommet** pose, ou **Entree** | Cree l'arete de cloture + triangulation eventail des faces internes |
| Annuler le trace | **ESC** | Abandonne sans fermer |
| Rubber-band (arete rapide) | **MAJ + clic maintenu** sur sommet → survoler cible → relacher | Cree une arete tiree entre les deux sommets |

---

## Modification de la geometrie

| Action | Activation | Description |
|--------|-----------|-------------|
| Subdiviser | **S** (1 arete selectionnee) | Insere un sommet au milieu, remplace l'arete par deux, subdivise les faces |
| Extruder | **E** (1 arete selectionnee) | Duplique et decale les 2 sommets, cree 3 aretes + 2 faces de raccord |
| Remplir | **F** (3+ aretes en boucle fermee) | Cree les faces internes (triangulation eventail) |
| Creer face (selection) | **N** (3+ sommets ou une boucle d'aretes selectionnes) | Previsualise un contour puis cree les faces (triangulation eventail) : **Entree** valide, **Echap** annule. Les aretes selectionnees sont remplacees par les aretes de la face (pas de doublon) |
| Fusionner (weld) | **M** (2+ sommets selectionnes) | Regroupe sur position moyenne, reunie les aretes, supprime les doublons |

---

## Deplacement de l'origine

| Action | Activation | Description |
|--------|-----------|-------------|
| Deplacer l'origine | Bouton **Origine** de la barre, puis **[-]** / **[+]** sur X, Y ou Z | Deplace TOUS les points du modele d'un pas de 0.1 dans la direction choisie (annulable avec Ctrl+Z, sauvegarde avec Ctrl+S) |

---

## Copie et duplication

| Action | Activation | Description |
|--------|-----------|-------------|
| Dupliquer | **Ctrl+D** | Copie la selection avec un decalage ; les copies restent selectionnees |
| Copier | **Ctrl+C** | Copie dans le presse-papier interne (sommets + aretes entre eux) |
| Coller | **Ctrl+V** | Colle le presse-papier (decale) ; les copies restent selectionnees |

---

## Nettoyage

| Action | Activation | Description |
|--------|-----------|-------------|
| Nettoyage auto | **K** | Supprime : sommets orphelins, aretes isolees, faces invalides. Compte-rendu en console. |

---

## Edition des valeurs

| Action | Activation | Description |
|--------|-----------|-------------|
| Modifier par boutons | **[-]** / **[+]** dans l'overlay sommet ou arete | Incremente / decremente la valeur |
| Saisie directe | **Clic** sur un champ (X, Y, Z, R, G, B, epaisseur) | Curseur clignotant ; tapez la valeur puis **Entree** = valider, **ESC** = annuler |
| Backspace | **Backspace** pendant la frappe | Efface le dernier caractere |

---

## Pipette couleur

| Action | Activation | Description |
|--------|-----------|-------------|
| Pipette | **Clic droit rapide** (tap) sur une arete | Sa couleur devient la couleur courante de creation (appliquee aux nouvelles aretes et faces). Se regle aussi dans l'overlay Arete (champs RGB) |

---

## Annuler / refaire / sauvegarder

| Action | Activation | Description |
|--------|-----------|-------------|
| Annuler | **Ctrl+Z** (ou **Ctrl+W** sur AZERTY) | Revient a l'etape precedente |
| Refaire | **Ctrl+MAJ+Z** (ou **Ctrl+Y**) | Avance d'une etape |
| Sauvegarder | **Ctrl+S**, ou bouton **Sauvegarder** | Enregistre la shape dans le fichier courant |
| Quitter | Bouton **Quitter** | Popup de confirmation → sauvegarde → retour au menu |

---

## Aide

| Action | Activation | Description |
|--------|-----------|-------------|
| Aide | **H** | Affiche / masque la liste des raccourcis |
| Fermer aide | **ESC** | Ferme le panneau d'aide |

---

## Interface

| Element | Description |
|---------|-------------|
| **Barre du haut** | Titre de la shape + boutons New / Outils / Filtre / Sauvegarder / Quitter |
| **New** | Menu deroulant : Vertex, Edge |
| **Outils** | Palette de 13 outils — voir section dediee ci-dessous |
| **Filtre** | Cases a cocher : Faces / Aretes / Points / Axes X/Y/Z / Grille / Magnet. Curseurs : taille points, largeur lignes, opacite faces, pas de grille |
| **Liste d'entites** | Bas a gauche : toutes les sommets/aretes. Clic = selection, molette = page |
| **ESC** | Ferme progressivement : aide → outils → new → filtre → modes → selection |

### Menu Outils (palette)

Le bouton **Outils** ouvre une palette de 13 outils, tombe sous le bouton.
Un seul menu reste ouvert a la fois : ouvrir "Outils" ferme "New" et "Filtre" (et inversement). **ESC** la ferme. Chaque ligne affiche son nom et, a droite, son raccourci clavier ; le mode actif est marque par "> " et un fond plus clair.

Les 4 **modes** reglent le meme etat que le menu "New" (les deux affichent le meme mode actif) :

| Outil | Rac. | Description |
|-------|------|-------------|
| Selection | — | Sort du mode de creation, retour a la selection |
| Sommet | — | Mode Vertex : chaque clic pose un sommet, clic sur l'existant enchaina une arete, MAJ+clic = frere |
| Arete | — | Mode Edge : relie 2 sommets existants |
| Tracé (face) | **T** | Mode Trace : boucle de sommets fermee par une face |

Les outils de **modification** agissent sur la selection en cours :

| Outil | Rac. | Description |
|-------|------|-------------|
| Subdiviser | **S** | Insere un sommet au milieu de l'arete selectionnee |
| Extruder | **E** | Duplique et decale les 2 sommets de l'arete + cree les faces |
| Remplir | **F** | Cree les faces internes d'un contour de 3+ aretes |
| Creer face (selection) | **N** | Triangule un contour previsualise a partir de la selection (Entree valide, Echap annule) |
| Fusionner | **M** | Fusionne 2+ sommets selectionnes (position moyenne) |

Les outils de **copie & nettoyage** :

| Outil | Rac. | Description |
|-------|------|-------------|
| Nettoyer | **K** | Supprime orphelins / aretes isolees / faces invalides |
| Dupliquer | **Ctrl+D** | Copie la selection avec un decalage |
| Copier | **Ctrl+C** | Selection dans le presse-papier interne |
| Coller | **Ctrl+V** | Recolle le presse-papier (decale) |

Et l'**aide** :

| Outil | Rac. | Description |
|-------|------|-------------|
| Aide | **H** | Affiche / masque le panneau des raccourcis |

Comportement : les outils de modification ne font rien sans selection adaptee — un message l'explique dans la console ("Subdiviser demande une arete selectionnee", "fusionner demande au moins 2 sommets selectionnes", etc.). Toutes ces actions passent par l'annulation **Ctrl+Z**.

---

## Configuration (parametres.json)

Fichier : `data/markershape/config/parametres.json`

| Cle | Description |
|-----|-------------|
| `frontYaw` / `frontPitch` | Vecteur de camera de base : angles du placement caméra par defaut (non modifiables dans l'editeur) |
| `defaultVertexColorR` / `G` / `B` | Couleur des sommets crees au clic |
| `zoomSpeed` | Sensibilite du zoom molette |
| `orbitSpeed` | Sensibilite de l'orbite |
| `snapEnabled` / `snapStep` | Magnetisme de grille pendant le drag |
| `magnetEnabled` / `magnetRadius` | Aimantation aux sommets (px) |
| `pointSize` / `lineWidth` / `faceAlpha` | Rendu global |
| `gridVisible` / `axisX` / `axisY` / `axisZ` | Affichage du plan de travail |
