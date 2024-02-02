# tc-sir
Projet de SIR (INSA Lyon - Télécommunications) : application Android pour l'analyse de l'association au réseau cellulaire.
ATTENTION : Version 1.0.0 telle que rendue pour le projet.
Github n'avait pas été utilisé pendant la majeure partie du développement.

## Présentation

L'application récupère périodiquement les données de l'API TelephonyManager en générant du trafic.
Les mesures sont associées à une position GPS. 
Puis une analyse des Serving Cells est effectuée à partir des données collectées.
Les points GPS sont affichés sur une carte avec une couleur correspondant à l'ID de la Serving Cell associée.
La trace de l'enregistrement ainsi que l'analyse en JSON sont sauvegardables dans le dosssier "Téléchargements".

## Installation

Nécessite Android 12. 
Ne fonctionne pas sur les versions antérieures.
Fonctionne sur les versions ultérieures mais sans sauvegarde sur le stockage externe.
Activer la localisation sur le téléphone.

**Téléchargement de l'APK : voir Release 1.0.0**

## Utilisation

* Session d'enregistrement : 
  * Bouton "Lancer l'affaire"
  * Choix de la technologie (2G, 3G ou 4G)
  * Bouton "Lancer la recherche"
  * (Il est possible d'afficher l'état du réseau en temps réel avec "Activer scan". Cela ne lance pas l'enregistrement.)
  * (Il est possible de générer du traffic HTTP pendant quelques secondes avec le bouton "Connexion". Cela ne lance pas l'enregistrement.)
  * Lancer l'enregistrement avec le bouton "Commencer enregistrement"
  * Entrer les paramètres souhaités. (La case "Forcer GPS" indique de ne pas récupérer les données GPS du cache)
  * L'enregistrement est en cours. Les données enregistrées sont affichées en italique gris
  * A tout moment il est possible de terminer l'enregistrement avec le bouton "Arrêter enregistrement"
  * A la fin de l'enregistrement, un bouton "Visualiser dernier enregistrement" apparaît et mène à la partie d'analyse.

* Analyse :
  * La carte affiche les points GPS collectés. La couleur du point correspond à l'ID de la Serving Cell associée
  * Un rappel des infos générales de l'enregistrement est affiché
  * Des données comme le nombre de cellules et les zones de locations traversées sont affichées
  * Le pourcentage de mesures où chaque cellule était Serving Cell est affiché, accompagné d'un diagramme circulaire
  * Deux boutons permettent de sauvegarder la trace de l'enregistrement (.csv) et l'analyse (.json) dans le dossier "Téléchargements"

* Charger ancien enregistrement :
  * Le bouton "Générer rapport" au début de l'application montre une liste des enregistrements précédents
  * En choisissant un enregistrement, l'application ammène à sa page d'analyse