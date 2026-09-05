# Intervalles — Roadmap

## Statut

Version stable actuelle : **V0.7**

Prochaine version cible : **V1.0**

La V1.0 succède directement à la V0.7.

Aucune V0.8 ou V0.9 n’est prévue à ce jour.

La V0.7 ne reçoit plus de nouvelle fonctionnalité, sauf correction nécessaire à sa stabilité.

---

# V1.0

La V1.0 doit apporter des améliorations issues des retours d’usage réels sans transformer Intervalles en application de sport ou de performance.

## 1. Statistiques personnelles

Ajouter un écran :

`Mes statistiques`

accessible depuis les Paramètres.

Périodes prévues :

- 7 jours ;
- 30 jours ;
- 3 mois.

Informations principales :

- `X / Y propositions réalisées` ;
- `X activités spontanées` ;
- `X activités réalisées au total` ;
- détail des propositions réalisées par créneau.

Exemple :

- 9 / 18 propositions réalisées ;
- 5 activités spontanées ;
- 14 activités réalisées au total.

### Par créneau

Présenter pour chaque créneau :

`nombre réalisé / nombre proposé`

Exemple :

- 09:00 — 3 / 5
- 10:30 — 2 / 4
- 15:00 — 3 / 4
- 16:30 — 1 / 5

### Information utilisateur

Un seul bouton `ⓘ` associé à `Mes statistiques` explique :

- ce qu’est une proposition réalisée ;
- ce qu’est une activité spontanée ;
- comment est calculé le total ;
- comment lire les résultats par créneau ;
- que les données restent uniquement sur l’appareil.

### Principes

Ne pas introduire :

- score ;
- streak ;
- badge ;
- objectif de performance ;
- classement ;
- code rouge/vert culpabilisant.

Les statistiques décrivent l’utilisation d’Intervalles.

Elles ne doivent pas être présentées comme une mesure directe :

- de l’état de santé ;
- de la condition physique ;
- du temps réellement passé assis ;
- d’un progrès physiologique.

## 2. Historique local des événements

Les statistiques nécessitent un historique distinct de l’historique court du moteur de diversité.

Les événements devront permettre de distinguer au minimum :

- proposition effectivement présentée ;
- report ;
- démarrage ;
- réalisation ;
- abandon ;
- activité spontanée.

Une proposition reportée reste une seule opportunité et ne doit jamais être comptée deux fois.

Les définitions exactes devront être figées avant l’implémentation du stockage.

Le stockage reste entièrement local.

## 3. `Déjà fait` dans la notification

La notification V1 doit proposer au maximum trois actions :

- `+ X min` ;
- `Commencer` ;
- `Déjà fait`.

`Déjà fait` permet de confirmer qu’une activité proposée a été réalisée sans ouvrir sa fiche ni lancer son chrono.

Cette action :

- clôt l’opportunité ;
- enregistre la proposition comme réalisée ;
- met à jour `Dernière activité` si applicable ;
- alimente l’historique de diversité ;
- alimente le futur historique statistique.

Aucune quatrième action n’est prévue.

## 4. Compréhension des Paramètres

Les tests utilisateurs montrent que certains réglages ne sont pas suffisamment explicites.

Ajouter une aide `ⓘ` notamment pour :

### Jours

Expliquer que les jours sélectionnés déterminent les jours pendant lesquels Intervalles peut programmer des propositions.

### Créneaux

Expliquer qu’à chaque créneau, Intervalles choisit une activité compatible avec les réglages et envoie une notification.

Le contenu exact devra rester court et cohérent avec les autres aides déjà présentes dans les Paramètres.

## 5. Simplification des catégories visibles des activités

Les termes utilisateurs `Guidée` et `Ponctuelle` sont mal compris et n’apportent pas suffisamment de valeur.

Objectif V1 :

- supprimer ces libellés de la description visible des activités ;
- conserver si nécessaire la distinction technique dans le modèle interne.

Pour une action ayant une fin naturelle, ne pas afficher artificiellement une durée.

Une activité ne doit pas recevoir un chrono uniquement pour uniformiser l’interface.

Le choix chrono / fin naturelle doit rester lié à la nature de l’activité.

## 6. Harmonisation des titres et consignes

Faire une revue complète de toutes les activités.

Objectifs :

- choisir une structure cohérente pour les titres ;
- harmoniser le ton ;
- rendre chaque consigne immédiatement compréhensible ;
- réduire les ambiguïtés ;
- éviter les formulations inutilement sportives ou médicales ;
- rester adapté au bureau et au télétravail.

Les formats de titres à comparer avant décision sont notamment :

- infinitif ;
- impératif ;
- question.

Une convention sera choisie puis appliquée à l’ensemble du catalogue.

Aucun changement massif de wording ne doit être intégré avant validation de cette convention.

## 7. Répétitions et rythme individuel

Éviter par défaut les objectifs exprimés en nombre de répétitions.

Les utilisateurs peuvent exécuter un même mouvement à des vitesses très différentes.

Pour les activités chronométrées, préférer une consigne de mouvement continu à son propre rythme.

Exemple de principe :

`Penche le buste vers l’avant, redresse-toi, puis recommence tranquillement jusqu’à la fin du chrono.`

Le nombre de répétitions ne doit être conservé que lorsqu’il apporte un bénéfice réel et démontré à la compréhension ou à la sécurité.

## 8. Enrichissement du catalogue

Le catalogue V1 doit être enrichi, mais sans objectif artificiel de nombre.

Un catalogue plus grand n’est utile que si les activités supplémentaires apportent une expérience réellement différente.

### Méthode

1. cartographier les activités existantes ;
2. identifier les zones, mouvements et contextes sous-représentés ;
3. rechercher des activités dans des sources scientifiques et professionnelles ;
4. étudier les recommandations de santé au travail, ergonomie, kinésithérapie et autres professionnels pertinents ;
5. utiliser les sources ostéopathiques comme source d’idées pratiques avec un niveau de preuve clairement distingué ;
6. chercher des actions naturelles de bureau et de télétravail ;
7. explorer des activités combinant deux gestes simples lorsqu’elles apportent une vraie valeur ;
8. comparer chaque candidate aux activités existantes ;
9. comparer les candidates entre elles ;
10. éliminer les doublons et quasi-doublons ;
11. soumettre une shortlist à validation avant intégration.

### Types recherchés

Priorité à :

1. ruptures directes de la sédentarité ;
2. prétextes naturels ;
3. activités combinées simples ;
4. activités adjacentes lorsque leur intérêt est suffisant.

### Prétextes naturels

Une tâche quotidienne peut constituer une activité si elle provoque réellement une rupture de la posture assise.

Exemples de territoires à explorer :

- eau ;
- objets à ranger ou aller chercher ;
- petites tâches nécessitant un déplacement ;
- fenêtre / changement d’environnement ;
- tâches réelles de bureau ou de télétravail.

Des variantes d’une même séquence comportementale ne doivent pas être ajoutées comme activités différentes.

Par exemple, `boire un verre d’eau` et `aller remplir sa gourde` peuvent correspondre dans la pratique à la même rupture de sédentarité et ne justifient pas automatiquement deux activités.

### Activités combinées

Une activité peut combiner :

- un geste principal qui garantit la rupture de sédentarité ;
- un second geste simple apportant variété ou utilité.

Exemple de territoire :

mobilité des poignets + transferts de poids debout.

Règles :

- deux actions maximum ;
- consigne immédiatement compréhensible ;
- pas de combinaison inutilement complexe ;
- pas de double tâche créant un risque d’équilibre ;
- la coordination peut être un bénéfice de conception mais n’est pas une justification scientifique automatique.

Les combinaisons sont conçues et validées manuellement.

Le moteur ne génère pas automatiquement des combinaisons d’exercices.

### Déduplication

Pour chaque nouvelle candidate, comparer notamment :

- mécanisme principal de rupture de sédentarité ;
- déplacement réel ;
- posture ;
- mouvement corporel ;
- zone sollicitée ;
- contexte nécessaire ;
- durée naturelle ;
- expérience ressentie.

Identifier :

- redondance d’action ;
- redondance d’expérience ;
- redondance de fonction.

Une nouvelle activité doit apporter au moins une différence fonctionnelle importante par rapport au catalogue existant.

La cible n’est pas un nombre fixe d’activités.

Mieux vaut un catalogue plus petit et réellement diversifié qu’un catalogue artificiellement gonflé de variantes proches.

---

# V1.x — Après validation de V1.0

Candidats à étudier selon les retours :

- durées davantage adaptées à chaque activité ;
- mini-séquences ;
- sessions guidées volontaires ;
- respiration guidée plus longue ;
- possibilité de masquer certaines activités ;
- contenus scientifiques courts et sourcés ;
- adaptations spécifiques bureau / télétravail si elles apportent une vraie valeur.

Ces éléments ne font pas partie du périmètre garanti de V1.0.

---

# V2 — Exploration

À envisager uniquement si l’usage réel le justifie :

- statistiques et tendances plus avancées ;
- bibliothèque plus large de sessions guidées ;
- prise en compte contextuelle plus poussée ;
- personnalisation supplémentaire.

Ne pas introduire :

- personnalisation opaque ;
- apprentissage automatique non explicable ;
- complexité contextuelle sans bénéfice démontré.

Le moteur de décision doit rester compréhensible et explicable.

---

# Règles produit durables

Intervalles n’a pas vocation à devenir :

- une application de sport ;
- une application de suivi de performance ;
- un système de culpabilisation ;
- une plateforme sociale ;
- un système de gamification ;
- un service nécessitant un compte ou une connexion Internet.

Les recommandations scientifiques doivent rester distinguées des choix de conception produit.

---

# Règle documentaire

Les documents présents dans le dépôt décrivent l’état actif du projet.

Lorsqu’un document devient obsolète :

- il est remplacé par la source active ;
- il n’est pas conservé à côté sous forme de document `legacy` ;
- Git et les tags servent d’archive.

Les noms des documents actifs évitent les numéros de version lorsqu’ils ne sont pas nécessaires au fonctionnement du logiciel.
