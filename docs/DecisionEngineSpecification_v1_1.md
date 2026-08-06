# DecisionEngineSpecification_v1.1

**Produit :** application Android hors ligne de lutte contre la sédentarité au travail  
**Portée :** V0  
**Statut :** spécification fonctionnelle consolidée, prête à implémenter et simuler  
**Version de politique :** `decision-policy-v1.0.0`

---

## 1. Finalité

Le moteur répond à deux questions distinctes :

1. **Une notification doit-elle être affichée maintenant ?**
2. **Si oui, quelle activité éligible doit être proposée ?**

Objectif produit :

> Proposer, au plus quatre fois par journée de travail par défaut, une activité courte et acceptable susceptible d'interrompre la sédentarité, sans créer de dette de notifications, de culpabilisation ni de charge mentale excessive.

Le moteur n'optimise pas une mesure de santé opaque. Il applique des règles explicites et versionnées.

---

## 2. Décisions V0 déjà validées

### 2.1 Créneaux par défaut

À l'installation :

- `10:10`
- `11:50`
- `15:10`
- `17:30`

Ils sont modifiables. Un créneau situé hors des horaires de travail ou pendant la pause déjeuner est invalide et ne produit aucune notification.

Ces heures sont des **horaires cibles**, pas des alarmes exactes.

### 2.2 Espacement 90/60

L'espacement est calculé entre les **heures réelles d'affichage** des notifications.

- règle normale : au moins `90 min` ;
- exception : l'intervalle peut être réduit si attendre 90 min atteindrait ou dépasserait la prochaine frontière de demi-journée — début de pause déjeuner ou fin du travail ;
- plancher absolu de l'exception : `60 min` ;
- si l'écart disponible est inférieur à 60 min : créneau annulé ;
- aucun rattrapage ultérieur.

Cette règle concerne l'espacement des notifications. Elle ne définit ni une fréquence scientifiquement optimale ni une limite de report.

### 2.3 Actions rapides

La notification propose :

- `Dans 10 min`
- `Dans 30 min`
- `Pas maintenant`

`Pas maintenant` :

- abandonne l'occasion courante ;
- n'entraîne ni rattrapage, ni dette, ni pénalité ;
- laisse le prochain créneau normal inchangé, sous réserve de la règle 90/60.

### 2.4 Autre activité

Après ouverture :

- l'utilisateur peut demander `Autre activité` ;
- il s'agit d'une nouvelle sélection dans la même occasion ;
- les activités déjà montrées pendant cette interaction sont exclues ;
- aucune préférence durable n'est inférée en V0.

### 2.5 Démarrage

Le bouton `C'est parti` :

- enregistre `ACTIVITY_ACCEPTED` ;
- ferme l'écran ;
- ne lance pas de chronomètre ;
- ne prouve pas que l'activité a réellement été effectuée.

### 2.6 Onboarding minimal

Il demande seulement :

- jours travaillés ;
- horaires de travail ;
- pause déjeuner ;
- capacité habituelle à se lever ;
- capacité habituelle à marcher quelques minutes.

Toutes les catégories et intensités sont actives par défaut. Elles sont modifiables plus tard.

### 2.7 Activité de début de journée

Option V0 facultative, désactivée par défaut :

- déclencheur distinct `START_OF_DAY` ;
- activité de 2–3 minutes ;
- compte comme notification affichée pour la règle 90/60 ;
- n'est pas incluse dans les quatre créneaux normaux.

---

## 3. Hors périmètre V0

- mode « recommandations » automatisé ;
- apprentissage des horaires ;
- détection d'appel, réunion, document ou charge cognitive ;
- calendrier ;
- capteurs ;
- machine learning ;
- adaptation cachée des poids ;
- gamification ;
- statistiques visibles ;
- profil médical ;
- validation objective de l'activité ;
- objectif hebdomadaire ;
- notifications « Si… alors » configurables.

---

## 4. Architecture fonctionnelle

```text
DecisionRequest
      ↓
OpportunityPolicy
      ↓
ALLOWED / DENIED
      ↓
CandidateFilter
      ↓
EligibleCandidates
      ↓
CandidateScorer
      ↓
ScoredCandidates
      ↓
WeightedSelector
      ↓
DecisionResult
```

L'orchestrateur appelle les composants dans cet ordre. Il ne contient aucune règle cachée.

---

## 5. Objets métier

### 5.1 DecisionRequest

Contient :

- `decision_id` ;
- date/heure locale résolue ;
- `opportunity_type` : `NORMAL_SLOT`, `SNOOZE`, `START_OF_DAY`, `ALTERNATIVE` ;
- identifiant du créneau source ;
- état du créneau ;
- horaires de travail ;
- pause déjeuner ;
- créneaux du jour ;
- dernière notification réellement affichée ;
- éventuel report actif ;
- catégories/intensités actives ;
- capacités `can_stand`, `can_walk` ;
- historique borné des propositions ;
- activités déjà montrées dans l'interaction ;
- version du catalogue ;
- version de la politique ;
- graine pseudo-aléatoire explicite.

Le moteur ne lit jamais directement l'horloge Android, DataStore, Room ou WorkManager.

### 5.2 ActivityDefinition

Champs nécessaires au moteur :

- `id`, `revision`, `status` ;
- `user_category` ;
- `internal_family` ;
- `intensity` ;
- durée minimale/maximale ;
- `posture_requirement` ;
- `walking_required` ;
- contexte temporel : `normal_slot`, `start_of_day` ;
- espace/discrétion ;
- `base_weight` ;
- métadonnées de sécurité ;
- version du catalogue.

Le niveau de preuve est documentaire. **Le moteur V0 ne lit pas `evidence_level` pour calculer le score.** La science peut avoir informé l'inclusion et le poids éditorial de base en amont.

### 5.3 DecisionResult

Résultats possibles :

- `NO_OPPORTUNITY`
- `NO_ELIGIBLE_ACTIVITY`
- `ACTIVITY_SELECTED`
- `INVALID_CONTEXT`
- `INVALID_CATALOG`

En cas de sélection : activité, opportunité source, tentative, graine, version et trace.

---

## 6. OpportunityPolicy

### 6.1 Responsabilité

Décider si l'occasion temporelle peut produire une notification. Elle ne consulte pas le catalogue et ne choisit aucune activité.

### 6.2 Règles

#### OPP-01 — Notifications et propositions actives

Si la permission nécessaire est indisponible, si les notifications fonctionnelles sont désactivées, si la journée est suspendue jusqu'au prochain jour travaillé, ou si les propositions sont suspendues jusqu'à nouvel ordre : `DENIED`.

États fonctionnels V0 :

- `ACTIVE` ;
- `SUSPENDED_UNTIL_NEXT_WORKDAY` ;
- `SUSPENDED_INDEFINITELY`.

La suspension persistante ne modifie ni les horaires ni les préférences. Seule une action explicite de reprise la termine.

#### OPP-02 — Créneau valide

Un créneau normal doit :

- appartenir au jour courant ;
- être actif ;
- se trouver dans un intervalle de travail ;
- ne pas se trouver pendant la pause déjeuner ;
- être antérieur ou égal à l'heure de fin de travail ;
- ne pas avoir déjà été traité.

Un créneau cible exactement égal à l'heure de fin de travail est valide. La tolérance technique OPP-04 reste applicable à son affichage réel.

#### OPP-03 — Une seule notification active

Une seule notification comportementale peut être affichée ou planifiée à la fois.

#### OPP-04 — Tolérance technique

Une exécution Android légèrement tardive reste valide jusqu'à `15 min` après l'heure cible. Au-delà : créneau manqué, sans rattrapage.

Ce seuil est une heuristique produit V0, configurable et à tester.

#### OPP-05 — Espacement 90/60

Soit :

- `last_displayed_at` = dernière notification réellement affichée ;
- `target_at` = affichage envisagé ;
- `boundary_at` = prochaine pause déjeuner ou fin de journée ;
- `gap = target_at - last_displayed_at`.

Algorithme :

1. s'il n'existe aucune notification antérieure ce jour : autoriser ;
2. si `gap >= 90 min` : autoriser ;
3. sinon, calculer `last_displayed_at + 90 min` ;
4. si ce moment est strictement antérieur à `boundary_at` : refuser le créneau ;
5. si ce moment atteint ou dépasse `boundary_at`, appliquer l'exception ;
6. si `gap >= 60 min` : autoriser ;
7. sinon : annuler.

La notification annulée n'est pas décalée.

#### OPP-06 — Report de 10/30 min

Un report :

- remplace la notification active ;
- reste rattaché au même créneau ;
- est calculé depuis l'heure réelle d'action ;
- n'est valide que s'il reste hors pause déjeuner et avant la fin du travail ;
- annule toute autre occurrence planifiée du même créneau ;
- devient la seule notification active ou planifiée.

Une fois le report réellement affiché, son heure devient la nouvelle référence pour l'espacement 90/60 des créneaux suivants.

#### OPP-07 — Pas maintenant

Marquer l'occasion `ABANDONED`. Aucun report et aucune pénalité.

#### OPP-08 — Conflit report/créneau normal

Si un report et un créneau normal entrent en conflit :

- le report déjà demandé par l'utilisateur garde la priorité ;
- le créneau normal est ensuite évalué avec la règle 90/60 depuis l'heure réelle d'affichage du report ;
- il peut donc être annulé.

#### OPP-09 — Idempotence

Même `decision_id` + même état = même résultat. Une double exécution Android ne doit pas afficher deux notifications.

#### OPP-10 — Donnée indispensable absente

Refus sûr : `INVALID_CONTEXT`.

### 6.3 Ce qu'elle ne fait pas

- pas de sélection d'activité ;
- pas de scoring ;
- pas de programmation Android ;
- pas d'apprentissage ;
- pas d'inférence de disponibilité.

---

## 7. CandidateFilter

### 7.1 Responsabilité

Appliquer uniquement les contraintes absolues.

### 7.2 Règles

#### FIL-01 — Statut

Seules les activités `published/active` sont admissibles.

#### FIL-02 — Catégorie

Catégorie désactivée = exclusion.

#### FIL-03 — Intensité

Intensité désactivée = exclusion.

#### FIL-04 — Capacité à se lever

- `standing_required` + `can_stand=false` = exclusion ;
- `standing_preferred` + `can_stand=false` = activité conservée si réellement faisable assis ;
- une préférence éditoriale ne devient pas une contrainte.

#### FIL-05 — Marche

`walking_required=true` + `can_walk=false` = exclusion.

#### FIL-06 — Durée

La durée minimale doit tenir dans la fenêtre disponible. La durée maximale est indicative.

#### FIL-07 — Déclencheur

- `START_OF_DAY` : seulement activités compatibles début de journée ;
- `NORMAL_SLOT/SNOOZE` : exclure les activités exclusivement début de journée.

#### FIL-08 — Activités déjà montrées dans l'interaction

Pour `ALTERNATIVE`, exclure toutes les activités déjà présentées dans cette interaction.

#### FIL-09 — Sécurité et schéma

Une activité avec métadonnées invalides ou contradictions fortes est exclue avec motif technique.

#### FIL-10 — Extérieur et escaliers

En V0, l'onboarding ne demande pas ces ressources. Elles ne sont donc pas des contraintes globales du profil.

Les activités correspondantes doivent être rédigées comme opportunités conditionnelles (« si tu peux… ») et recevoir un poids raisonnable. Elles ne sont exclues que si l'utilisateur les a explicitement désactivées ultérieurement.

### 7.3 Aucun candidat

Retourner une liste vide. Le moteur n'impose aucune activité « universelle » contre les contraintes explicites de l'utilisateur.

Résultat final : `NO_ELIGIBLE_ACTIVITY`, sans notification.

---

## 8. CandidateScorer

### 8.1 Responsabilité

Attribuer un poids relatif aux candidats éligibles. Il ne peut ni exclure un candidat éligible ni déclencher une notification.

### 8.2 Modèle

```text
final_weight = clamp(base_weight × repetition_coefficient,
                     min_weight,
                     max_weight)
```

V0 n'utilise que peu de facteurs pour rester explicable.

### 8.3 Répétition hiérarchique

Un seul coefficient est choisi afin de ne pas pénaliser plusieurs fois le même phénomène :

1. même activité que la proposition précédente : `× 0.15` ;
2. sinon même famille que la proposition précédente : `× 0.45` ;
3. sinon même activité dans les 4 dernières propositions : `× 0.55` ;
4. sinon famille apparue au moins 2 fois dans les 4 dernières : `× 0.75` ;
5. sinon : `× 1.00`.

Aucun cumul entre ces cinq lignes.

### 8.4 Poids et bornes

- `min_weight = 0.05` ;
- `max_weight = 20.0` ;
- `base_weight` doit être strictement positif ;
- valeurs invalides = erreur de catalogue.

### 8.5 Préférences

V0 distingue uniquement :

- activé : éligible ;
- désactivé : filtré.

Il n'existe pas encore de catégorie « préférée » avec bonus automatique.

### 8.6 Report

Un report ne modifie pas le score de l'activité : il signale un mauvais moment, pas un rejet du contenu.

### 8.7 Autre activité

La proposition précédente est exclue par le filtre de l'interaction ; aucune pénalité persistante n'est créée.

### 8.8 Science

`evidence_level` n'entre pas dans la formule. Toute influence scientifique est explicitée lors de la validation éditoriale et dans `base_weight`.

---

## 9. WeightedSelector

### 9.1 Responsabilité

Choisir une activité parmi les candidats scorés avec un tirage pondéré reproductible.

### 9.2 Règles

1. trier les candidats par `id` ;
2. vérifier identifiants uniques et poids positifs ;
3. calculer la somme des poids sans normalisation ;
4. utiliser la graine fournie ;
5. produire une valeur pseudo-aléatoire dans `[0, total[` ;
6. sélectionner l'intervalle cumulatif correspondant ;
7. retourner activité, valeur, poids, total et version du sélecteur.

Avec mêmes candidats, poids, ordre, graine et version : même activité.

### 9.3 Alternative

Chaque tentative utilise :

- `attempt_index` incrémenté ;
- une graine différente fournie par l'orchestrateur ;
- les activités déjà montrées exclues.

Si toutes les alternatives sont épuisées : `NO_MORE_ALTERNATIVES`. Aucun bouclage.

---

## 10. Orchestrateur

Séquence :

1. valider la requête ;
2. appeler `OpportunityPolicy` ;
3. arrêter si refus ;
4. charger le catalogue versionné ;
5. filtrer ;
6. arrêter sans notification si aucun candidat ;
7. scorer ;
8. sélectionner ;
9. produire la décision et la trace ;
10. laisser les adaptateurs Android persister et afficher.

L'orchestrateur ne modifie pas directement l'état persistant.

---

## 11. Événements V0

Événements persistés localement :

- `OPPORTUNITY_DENIED`
- `DECISION_MADE`
- `NOTIFICATION_SHOWN`
- `REMINDER_POSTPONED`
- `OPPORTUNITY_ABANDONED`
- `ACTIVITY_OPENED`
- `ALTERNATIVE_REQUESTED`
- `ACTIVITY_ACCEPTED`
- `NOTIFICATION_DISMISSED`

Le moteur utilise surtout les événements du jour et les quatre dernières propositions.

La trace complète est retournée par le moteur. En production V0, la persistance complète est optionnelle ; les builds de test peuvent la conserver pour le diagnostic. Aucune statistique n'est affichée.

---

## 12. Trace explicable

La trace comprend :

- décision d'opportunité et raison ;
- activités exclues et raisons ;
- activité/poids de base ;
- coefficient de répétition choisi ;
- poids final ;
- graine, valeur tirée, somme et version ;
- version du catalogue et de la politique.

Elle ne contient pas de données médicales ni de texte libre utilisateur.

---

## 13. Invariants

1. Une activité sélectionnée était éligible.
2. Une activité exclue ne peut pas être réintroduite.
3. Une préférence ne rend jamais possible une activité incompatible.
4. Une seule notification est active ou planifiée.
5. Un report ne crée pas de dette.
6. `Pas maintenant` n'entraîne aucune relance de l'occasion.
7. L'espacement se calcule depuis l'affichage réel.
8. Aucune notification normale n'est affichée à moins de 60 min de la précédente.
9. L'exception 60–89 min n'existe qu'à cause d'une frontière de demi-journée.
10. À entrées identiques, résultat identique.
11. Le résultat ne dépend pas de l'ordre initial du catalogue.
12. Le niveau de preuve scientifique n'est pas un facteur caché du score.
13. `C'est parti` signifie accepté, pas vérifié comme réalisé.
14. L'absence de candidat est un résultat métier valide.
15. Le moteur ne connaît aucune API Android.
16. Une suspension jusqu'à nouvel ordre persiste jusqu'à une reprise explicite.
17. Un créneau cible exactement égal à l'heure de fin de travail est valide.

---

## 14. Tests d'acceptation prioritaires

### A — Espacement normal

- affichage à 10:10 ; cible 11:50 ; écart 100 min → autorisée.

### B — Créneau trop proche

- affichage réel à 11:00 ; cible 11:50 ; écart 50 min → annulée.

### C — Exception fin de journée

- affichage réel à 16:20 ; cible 17:30 ; écart 70 min ; 90 min dépasserait 17:30 → autorisée.

### D — 59 minutes malgré frontière

- écart 59 min et 90 min dépasserait la frontière → annulée.

### E — Report et créneau suivant

- 10:10 report 30 min ; affichage réel 10:40 ; cible suivante 11:50 ; écart 70 min ; 12:10 reste avant déjeuner 12:30 → cible annulée.

### F — Pas maintenant

- notification affichée ; action `Pas maintenant` ; aucune relance ; prochain créneau évalué normalement depuis l'affichage réel.

### G — Capacité limitée

- `can_stand=false`, `can_walk=false` → seules activités assises compatibles éligibles.

### H — Alternative

- première activité A ; demande autre ; A exclue ; même graine de base + tentative différente → activité B reproductible.

### I — Catalogue vide ou incompatible

- aucun candidat → aucune notification affichée et résultat `NO_ELIGIBLE_ACTIVITY`.

### J — Déterminisme

- même requête, même catalogue, même graine → même trace et même activité.

### K — Ordre du catalogue

- catalogue mélangé → même résultat.

### L — Début de journée

- option activée à 8:30 ; notification affichée ; premier créneau 10:10 : 100 min → autorisé.

- option activée à 9:30 ; premier créneau 10:10 : 40 min → créneau 10:10 annulé.

---

## 15. V1 / V2

### V1 — uniquement après retours

- option explicite `Pas aujourd'hui` si `Pas maintenant` ne suffit pas ;
- ancrages « Si… alors » choisis par l'utilisateur ;
- distinction de motifs de rejet ;
- préférences pondérées ;
- contextes bureau/télétravail ;
- outils locaux de diagnostic ;
- génération automatique de la documentation catalogue ;
- adaptation bornée et réinitialisable.

### V2 — expérimental

- intégration calendrier volontaire ;
- détection locale de contexte ;
- fading des notifications ;
- bandit contextuel interprétable ;
- micro-randomisation dans un protocole de recherche ;
- exercices visuels/attentionnels poétiques contextualisés.

---

## 16. Décisions de conception versus science

Les éléments suivants sont des **heuristiques produit V0**, à tester :

- quatre créneaux ;
- horaires par défaut ;
- espacement 90/60 ;
- tolérance technique de 15 min ;
- coefficients de répétition ;
- bornes de poids.

Ils ne doivent pas être présentés comme des prescriptions scientifiques.

---

## 17. Critères de sortie du sprint moteur

Le moteur est prêt pour Kotlin lorsque :

- la spécification est validée ;
- la politique JSON reflète exactement ses paramètres ;
- le catalogue respecte son schéma ;
- les scénarios prioritaires passent dans le simulateur ;
- aucune règle métier n'est nécessaire dans l'UI ou dans la planification Android ;
- une décision complète peut être reproduite à partir de sa trace.


## Révision 1.1 — décisions produit intégrées

### Suspension persistante

La suspension jusqu'à nouvel ordre est une capacité V0. Elle agit comme une barrière d'opportunité, sans modifier la configuration et sans supprimer l'historique.

### Créneau de fin de journée

Une heure cible égale à l'heure de fin configurée est valide. La notification peut être affichée dans la tolérance technique habituelle ; au-delà, elle est annulée sans rattrapage.

### Disponibilité des actions de report

La couche de présentation ne propose un report de 10 ou 30 minutes que si `OpportunityPolicy` confirme que l'échéance resterait valide. `Pas maintenant` reste toujours disponible. Pour un créneau cible exactement égal à la fin de journée, aucun report n'est proposé.
