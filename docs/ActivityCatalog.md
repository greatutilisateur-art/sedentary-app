# Catalogue d’activités — Référentiel produit V0

**Produit :** application Android hors ligne de lutte contre la sédentarité au travail  
**Portée :** V0  
**Statut :** référentiel produit consolidé  
**Source de vérité :** `catalog/activities_v1.json`  
**Contrat de données :** `catalog/activity_catalog_schema_v1.json`  
**Vocabulaire contrôlé :** `catalog/activity_vocabularies_v1.json`

---

## 1. Rôle de ce document

Ce document explique comment le catalogue d’activités est structuré, gouverné et utilisé.

Il ne remplace pas les fichiers JSON.

En cas de divergence :

1. `catalog/activities_v1.json` est la source de vérité du contenu publié ;
2. `catalog/activity_catalog_schema_v1.json` définit le contrat de données ;
3. `catalog/activity_vocabularies_v1.json` définit les valeurs autorisées ;
4. le Markdown généré depuis le JSON est une vue de lecture, pas une source à éditer ;
5. le présent document décrit les règles produit et la gouvernance.

---

## 2. État V0

Le catalogue V0 contient **23 activités publiées**.

Répartition actuelle :

- mouvement : 4 ;
- mobilité : 8, dont 2 activités de début de journée ;
- étirements : 3 ;
- activation/dynamique : 4 ;
- bien-être : 2 ;
- repos visuel : 2.

Le catalogue est suffisamment diversifié pour la V0 sans chercher l’exhaustivité.

### Décision produit

La V0 ne cherche pas à proposer des dizaines d’exercices supplémentaires.

Un catalogue plus grand augmenterait :

- la charge éditoriale ;
- le risque de doublons ;
- les besoins de validation scientifique ;
- les risques de métadonnées incohérentes ;
- la difficulté de tester le moteur.

L’ajout d’activités doit répondre à un manque observé, pas à un objectif de volume.

---

## 3. Catégories visibles

Les catégories visibles sont :

- `mouvement` → Marche et déplacement ;
- `mobilite` → Mobilité ;
- `etirement` → Étirements ;
- `dynamique` → Activation ;
- `bien_etre` → Bien-être ;
- `visuel` → Repos visuel.

### Important

Les catégories visibles servent principalement :

- aux réglages utilisateur ;
- au classement éditorial ;
- à la lisibilité du catalogue.

Elles ne pilotent pas directement la diversité du moteur.

`internal_family` reste une métadonnée éditoriale utile pour décrire le catalogue, mais le moteur V0 ne l’utilise pas pour son scoring.

---

## 4. Familles internes

Chaque activité possède une `internal_family`.

Exemples :

- `walking` ;
- `brisk_walk` ;
- `squat` ;
- `calf_raise` ;
- `thoracic_rotation` ;
- `hamstring_stretch` ;
- `visual_rest`.

Ces familles permettent de détecter des répétitions fonctionnelles plus fines que les catégories visibles.

### Exemple

Deux activités peuvent appartenir à la même grande catégorie utilisateur tout en sollicitant des comportements différents.

À l’inverse, deux activités de catégories différentes peuvent être proches dans l’expérience réelle.

### Décision V0

`internal_family` est conservé pour l’édition, la recherche de doublons et une éventuelle évolution future.

Le moteur V0 ne l’utilise pas pour la diversité. Celle-ci repose seulement sur une pénalité temporaire lorsque la même activité vient d’être proposée ou apparaît dans l’historique récent.

Cette simplification évite des règles sans effet réel avec le catalogue actuel.

---

## 5. Intensités

Les intensités internes sont :

- `gentle` ;
- `active` ;
- `dynamic`.

Les libellés utilisateur sont :

- Douce ;
- Active ;
- Dynamique.

### Règle V0

L’intensité est un filtre explicite.

Si l’utilisateur désactive une intensité, les activités correspondantes sont exclues.

Il n’existe pas en V0 de niveau sportif ou de score de condition physique.

---

## 6. Postures et capacités

Le schéma distingue :

- `compatible_postures` ;
- `preferred_posture` ;
- `standing_required` ;
- `walking_required`.

### Pourquoi cette séparation

Une posture conseillée n’est pas une contrainte absolue.

Exemple :

- une rotation du haut du corps peut être meilleure debout ;
- mais elle peut rester faisable assis.

Le moteur ne doit donc pas exclure l’activité seulement parce que la posture préférée n’est pas disponible.

La formulation de la consigne reste neutre vis-à-vis de la posture lorsque l'activité est faisable assise ou debout.

L'UX traduit les métadonnées en badge contextuel :

- `Debout nécessaire` si la station debout est obligatoire ;
- `Debout conseillé` si elle est préférée et disponible ;
- `Assis possible` si elle est préférée mais que l'utilisateur a indiqué ne pas pouvoir généralement se lever.

### Invariants

- `walking_required = true` implique `standing_required = true` ;
- une posture préférée doit appartenir aux postures compatibles ;
- une activité nécessitant la station debout est exclue si `can_stand = false` ;
- une activité nécessitant la marche est exclue si `can_walk = false`.

---

## 7. Opportunités temporelles

Les activités sont compatibles avec :

- `standard` ;
- `start_of_day`.

### V0

Les activités standards sont proposées pendant les créneaux standards actifs de l'utilisateur (1 à 4 en V0).

Les activités `START01` et `START02` sont réservées au début de journée.

Elles ne doivent pas apparaître dans une opportunité normale.

### Raison

Le début de journée est un contexte différent :

- l’utilisateur vient potentiellement d’arriver ;
- la marche n’est pas forcément pertinente ;
- une courte mobilisation globale peut être plus cohérente.

---

## 8. Rupture de sédentarité

Chaque activité possède `sedentary_break_effect`.

Valeurs :

- `certain` ;
- `probable` ;
- `optional` ;
- `adjacent_only`.

### `certain`

L’activité impose de quitter l’assise.

Exemples :

- marche ;
- squats ;
- levers de chaise.

### `probable`

L’activité peut parfois être réalisée assis, mais la consigne recommande explicitement de se lever ou la station debout est la manière privilégiée de la réaliser.

La rupture de l’assise est donc attendue si cette recommandation est suivie, mais elle n’est pas garantie.

### `optional`

L’activité peut être réalisée assis ou debout sans recommandation forte de quitter l’assise.

### `adjacent_only`

L’activité peut être utile mais ne garantit pas une rupture de l’assise.

Exemples :

- respiration ;
- pause visuelle.

### Nature de la classification

Cette classification est une métadonnée de conception décrivant l’effet attendu sur l’assise. Elle ne constitue ni une mesure de comportement réel ni un niveau de preuve scientifique.

---

## 9. Niveau de preuve

Le champ `evidence_level` utilise :

- `high` ;
- `moderate` ;
- `limited` ;
- `indirect`.

### Rôle

Ce champ est documentaire.

Il indique la proximité entre l’activité proposée et la littérature disponible.

### Règle V0

Le moteur ne lit pas `evidence_level` pour calculer le score.

Il ne doit donc exister aucun multiplicateur caché du type :

```text
high = x1.5
limited = x0.5
```

### Pourquoi

Le niveau de preuve ne mesure pas directement :

- l’acceptabilité ;
- la pertinence contextuelle ;
- la faisabilité ;
- la préférence utilisateur.

L’utiliser directement dans le score rendrait le moteur moins explicable et mélangerait science et conception.

---

## 10. Poids de base

Chaque activité possède un `base_weight` strictement positif.

Le poids de base influence la fréquence relative de sélection entre activités déjà éligibles.

### Règle V0

Le poids de base est une décision éditoriale.

Il peut avoir été informé par :

- la pertinence avec la mission principale ;
- la variété ;
- la faisabilité au bureau ;
- la proximité avec les comportements soutenus scientifiquement.

Il n’est pas présenté comme une recommandation médicale.

---

## 11. Variantes

Le champ `variant_of` est volontairement strict.

Il signifie :

> variante reconnaissable de la même activité fondamentale.

Relations actuelles :

- `MOUV04 → MOUV01` ;
- `DYN04 → MOUV01` ;
- `VIS02 → VIS01`.

### Ce que `variant_of` ne signifie pas

Il ne décrit pas une simple proximité thématique.

Deux activités peuvent cibler la même zone corporelle sans être variantes.

### Raison

Un graphe de relations trop riche n’a aucun consommateur réel en V0 et compliquerait inutilement le schéma.

---

## 12. Durées

Chaque activité possède :

- `duration_seconds_min` ;
- `duration_seconds_max`.

### Règle moteur

La durée minimale doit être compatible avec la fenêtre disponible.

La durée maximale reste indicative.

### Règle UX

L’utilisateur voit une durée simple et lisible, par exemple :

- `30 s` ;
- `30 à 60 s` ;
- `1 min` ;
- `2 à 5 min`.

### Limite scientifique

Les durées du catalogue sont des choix de conception.

Elles ne doivent pas être présentées comme des durées optimales scientifiquement démontrées.

---

## 13. Espace et discrétion

Le catalogue peut décrire :

### `space_required`

- `desk` ;
- `small` ;
- `outside_desk` ;
- `building` ;
- `outside`.

### `discretion`

- `high` ;
- `medium` ;
- `low`.

### V0

Ces métadonnées sont présentes mais ne doivent pas déclencher une complexité produit excessive.

L’onboarding ne demande pas si l’utilisateur dispose :

- d’escaliers ;
- d’un espace extérieur ;
- d’un bureau fermé ;
- d’un espace privé.

Les activités concernées restent formulées de manière conditionnelle.

Exemple :

> Si tu peux sortir quelques minutes…

---

## 14. Zones corporelles

Le champ `body_focus` sert à l’indexation et au diagnostic éditorial.

Exemples :

- épaules ;
- hanches ;
- mollets ;
- chaîne postérieure ;
- colonne thoracique ;
- corps entier.

### V0

`body_focus` n’est pas un facteur de score runtime.

Il peut être utilisé pour :

- repérer des déséquilibres du catalogue ;
- vérifier les doublons ;
- préparer une évolution future.

---

## 15. Sécurité

Le catalogue utilise des `safety_flags` contrôlés.

Exemples :

- `stop_if_pain` ;
- `no_forced_range` ;
- `use_support_if_needed` ;
- `stable_chair` ;
- `stop_if_dizziness`.

### Règle V0

Les garde-fous doivent rester courts et proportionnés.

L’application n’est pas un outil clinique.

Elle ne connaît pas :

- les blessures ;
- les diagnostics ;
- les limitations individuelles ;
- la qualité d’exécution.

### Risque

Multiplier les avertissements sur chaque écran pourrait médicaliser l’expérience et augmenter la friction.

La sécurité doit donc être structurée dans les données et affichée seulement lorsque nécessaire.

---

## 16. Frontière mobilité / étirement

Le catalogue utilise la distinction suivante.

### Mobilité

Mouvement répété, progressif ou exploratoire sans maintien prolongé d’une position finale.

### Étirement

Mise en tension ciblée, tenue ou répétée autour d’une position identifiable.

### Cas particulier

`MOB04` et `ETIR02` restent distinctes :

- `MOB04` : mobilisation globale en flexion ;
- `ETIR02` : étirement unilatéral ciblé des ischio-jambiers.

Cette distinction doit rester visible dans les consignes.

---

## 17. Activités V0 par catégorie

### Mouvement — 4

- `MOUV01` — Une petite marche ?
- `MOUV02` — Un verre d’eau ?
- `MOUV03` — Un petit tour par les escaliers ?
- `MOUV04` — Prendre un peu l’air ?

### Mobilité — 6 standards

- `MOB01` — Quelques cercles d’épaules ?
- `MOB02` — Tourne doucement le haut du corps
- `MOB03` — Fais bouger tes chevilles
- `MOB04` — Mobilise doucement l’arrière des jambes
- `MOB05` — Ouvre doucement le haut du corps
- `MOB06` — Fais bouger doucement tes hanches

### Début de journée — 2

- `START01` — Pour commencer : le haut du corps
- `START02` — Pour commencer : jambes et hanches

### Étirements — 3

- `ETIR01` — Un petit étirement des mollets ?
- `ETIR02` — Un peu d’étirement pour l’arrière des jambes ?
- `ETIR03` — Grandis-toi quelques secondes

### Activation — 4

- `DYN01` — Quelques squats tranquilles ?
- `DYN02` — Quelques montées sur la pointe des pieds ?
- `DYN03` — Quelques levers de chaise ?
- `DYN04` — Une marche un peu plus soutenue ?

### Bien-être — 2

- `BIEN01` — Une minute pour souffler ?
- `BIEN02` — Relâche un peu les épaules

### Repos visuel — 2

- `VIS01` — Regarde au loin
- `VIS02` — Une petite pause pour les yeux

---

## 18. Source de vérité et génération

Le catalogue officiel est :

```text
catalog/activities_v1.json
```

Le Markdown détaillé du catalogue doit être généré automatiquement depuis ce fichier.

### Interdit

Ne pas :

- modifier manuellement une fiche uniquement dans le Markdown ;
- modifier un ID publié sans migration ;
- supprimer silencieusement une activité ;
- introduire une valeur de vocabulaire non déclarée ;
- corriger automatiquement une donnée invalide au runtime.

---

## 19. Gouvernance des modifications

### Modification éditoriale

Une modification de titre ou de consigne augmente `revision`.

### Modification structurelle

Une modification du contrat de données augmente la version du schéma.

### Nouvelle valeur contrôlée

Elle doit être ajoutée au vocabulaire avec justification.

### Suppression

Une activité publiée ne disparaît pas silencieusement.

Elle passe par :

- `deprecated` ;
- ou `archived`.

### Nouvelle activité

Avant ajout, vérifier :

1. répond-elle à un manque réel ?
2. est-elle suffisamment différente d’une activité existante ?
3. est-elle faisable au bureau ?
4. sa catégorie est-elle claire ?
5. sa famille interne est-elle pertinente ?
6. ses contraintes sont-elles cohérentes ?
7. son niveau de preuve est-il correctement qualifié ?
8. sa consigne reste-t-elle compréhensible rapidement ?
9. sa présence améliore-t-elle réellement le catalogue ?

---

## 20. Risques V0

### Doublons éditoriaux

Deux activités trop proches peuvent donner l’impression que le moteur se répète malgré des IDs différents.

### Mauvaises métadonnées

Une activité mal marquée `standing_required` ou `walking_required` peut être proposée dans un contexte impossible.

### Surclassement scientifique

Attribuer `high` à une activité seulement parce qu’elle semble saine serait incorrect.

### Trop de sécurité visible

Afficher tous les drapeaux à chaque fois nuirait à la simplicité.

### Catalogue trop grand

Un enrichissement prématuré augmenterait la maintenance sans bénéfice démontré.

### Source de vérité concurrente

Modifier le Markdown sans modifier le JSON créerait une incohérence dangereuse.

---

## 21. V0 / V1 / V2

### V0

Conserver :

- 23 activités ;
- 6 catégories visibles ;
- familles internes ;
- intensités ;
- postures ;
- capacités debout/marche ;
- effet sur la sédentarité ;
- sécurité structurée ;
- poids de base ;
- niveau de preuve documentaire.

### V1

Uniquement après retours d’usage :

- favoris ;
- masquage d’une activité ;
- éventuelles nouvelles activités pour combler un manque observé ;
- meilleure internationalisation ;
- outils éditoriaux de validation ;
- éventuels contextes de travail supplémentaires.

### V2

Seulement si un besoin réel apparaît :

- relations sémantiques plus riches ;
- médias ou illustrations structurées ;
- adaptation contextuelle avancée ;
- métadonnées supplémentaires consommées par un moteur plus complexe.

Aucun champ V2 ne doit être ajouté au schéma V0 sans consommateur réel.

---

## 22. Critères d’acceptation du catalogue

Le catalogue V0 est acceptable si :

1. les 23 IDs sont uniques ;
2. toutes les activités sont conformes au schéma ;
3. les valeurs appartiennent au vocabulaire contrôlé ;
4. toutes les durées sont comprises entre 30 et 300 secondes et le minimum ne dépasse pas le maximum ;
5. `walking_required` implique `standing_required` ;
6. chaque posture préférée est compatible ;
7. les activités de début de journée sont isolées ;
8. aucune activité standard n’est réservée par erreur au démarrage ;
9. les `variant_of` pointent vers une activité existante ;
10. les niveaux de preuve restent documentaires ;
11. les poids sont strictement positifs ;
12. aucune activité incompatible n’est proposée comme secours ;
13. les consignes restent courtes et compréhensibles ;
14. la vue Markdown peut être régénérée depuis le JSON.

---

## 23. Documents associés

- `docs/Scientific_framework.md`
- `docs/DecisionEngineSpecification_v1_1.md`
- `docs/UXSpecification_v1_1.md`
- `docs/ActivityCatalogSchema_v1.md`
- `catalog/activities_v1.json`
- `catalog/activity_catalog_schema_v1.json`
- `catalog/activity_vocabularies_v1.json`

Le catalogue décrit **ce que l’application peut proposer**.

Le moteur décrit **quand et comment une activité est choisie**.

L’UX décrit **comment cette activité est présentée**.

Le cadre scientifique décrit **ce que le produit peut raisonnablement soutenir à son sujet**.
