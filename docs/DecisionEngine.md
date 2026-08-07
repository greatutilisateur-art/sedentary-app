# Moteur de décision — V0

**Produit :** application Android hors ligne de lutte contre la sédentarité au travail  
**Portée :** V0  
**Statut :** spécification consolidée  
**Source des paramètres :** `policy/decision_policy_v1.json`  
**Catalogue :** `catalog/activities_v1.json`

---

## 1. Finalité

Le moteur répond à deux questions :

1. une proposition doit-elle être présentée pour l’opportunité demandée ?
2. si oui, quelle activité éligible proposer ?

Le moteur reste volontairement simple.

Il ne cherche pas à recalculer les horaires de l’utilisateur, à optimiser médicalement sa journée ou à compenser les notifications ignorées.

Principe V0 :

> Les créneaux choisis par l’utilisateur constituent le planning. Le moteur respecte ce planning et choisit seulement une activité compatible.

---

## 2. Principes

Le moteur est :

- explicite ;
- déterministe à entrées identiques ;
- testable hors Android ;
- indépendant de l’interface ;
- indépendant de WorkManager, DataStore, Room ou toute autre API Android.

Il ne contient :

- aucun machine learning ;
- aucune adaptation cachée ;
- aucune dette de notifications ;
- aucun rattrapage automatique ;
- aucune fréquence scientifique prétendument optimale.

---

# A. CRÉNEAUX ET OPPORTUNITÉS

## 3. Créneaux standards

La configuration initiale propose :

```text
10:10
11:50
15:10
17:00
```

Ces valeurs sont préremplies mais modifiables.

L’utilisateur choisit ses créneaux.

Le moteur ne les déplace pas automatiquement.

### Validité d’un créneau

Un créneau standard doit :

- appartenir à un jour travaillé ;
- être activé ;
- se trouver dans une plage de travail configurée ;
- ne pas tomber dans la pause déjeuner configurée ;
- respecter l’écart minimal entre créneaux standards.

Aucune règle d’espacement `90/60` n’existe.

---

## 4. Écart minimal

La seule règle d’espacement fonctionnelle entre deux **opportunités distinctes** est :

```text
20 minutes minimum
```

Cette règle ne signifie pas qu’une activité doit durer 20 minutes.

Elle évite seulement deux propositions différentes presque simultanées.

### Important

Un report de la même opportunité n’est pas une nouvelle activité.

Ainsi :

```text
notification à 10:10
→ Dans 10 min
→ nouvelle présentation à 10:20
```

reste autorisée.

Le seuil de 20 minutes s’applique entre cette opportunité reportée et les autres créneaux distincts.

---

## 5. Configuration des créneaux

Deux créneaux standards configurés doivent être espacés d’au moins 20 minutes.

Exemple :

```text
10:10
10:30
→ valide
```

```text
10:10
10:25
→ invalide
```

L’application explique l’erreur dans la configuration plutôt que de déplacer silencieusement un horaire.

---

## 6. Pas de recalcul dynamique des créneaux

L’affichage ou le report d’une proposition ne décale pas automatiquement les autres créneaux.

Exemple :

```text
10:10
11:50
15:10
17:00
```

reste le planning du jour même si la proposition de 10:10 est reportée.

Le moteur ne crée pas de cascade de nouveaux horaires.

---

## 7. Retard technique Android

Un déclenchement Android peut arriver légèrement après l’heure cible.

La tolérance technique V0 reste :

```text
15 minutes
```

Au-delà, le créneau est considéré comme manqué.

Il n’est pas rattrapé.

### Protection du prochain créneau

Si une notification arrivée tardivement se présenterait à moins de 20 minutes du prochain créneau standard, la notification tardive est abandonnée.

Le prochain créneau fixe est conservé.

Cette règle protège le planning choisi par l’utilisateur.

---

# B. REPORT

## 8. Actions de report

La notification peut proposer :

```text
Dans 10 min
Dans 30 min
Pas maintenant
```

Le report conserve exactement la même activité.

Le moteur ne sélectionne pas une nouvelle activité.

---

## 9. Signification du report

Un report signifie :

> ce n’est pas le bon moment.

Il ne signifie pas :

> cette activité ne me plaît pas.

Par conséquent, un report :

- ne change aucun poids ;
- ne crée aucune préférence ;
- ne pénalise pas l’activité ;
- ne relance pas le moteur de sélection.

---

## 10. Déjeuner et fin de journée

Un report demandé explicitement par l’utilisateur peut tomber :

- pendant sa pause déjeuner ;
- après son horaire habituel de fin de travail.

Ces frontières ne bloquent plus le report.

### Raison produit

Le créneau initial est une suggestion configurée.

Lorsqu’une personne choisit elle-même `Dans 10 min` ou `Dans 30 min`, elle prend une décision explicite sur sa disponibilité.

L’application n’a pas à supposer qu’elle ne travaille plus ou qu’elle ne souhaite plus recevoir la proposition.

---

## 11. Conflit entre report et prochain créneau

Un report ne doit pas déplacer ou annuler un créneau fixe.

Donc, si l’heure obtenue par un report serait située à moins de 20 minutes d’un autre créneau standard, cette option de report n’est pas proposée.

### Exemple

```text
notification actuelle : 10:30
prochain créneau : 10:50
```

`Dans 10 min` donnerait :

```text
10:40
→ seulement 10 min avant 10:50
→ option masquée
```

`Dans 30 min` donnerait :

```text
11:00
```

Si aucun autre créneau n’est à moins de 20 minutes de 11:00 :

```text
→ option autorisée
```

### Priorité

```text
créneau fixe > report optionnel
```

Cela évite toute cascade de replanification.

---

## 12. Reports successifs

Une notification reportée peut à nouveau proposer un report si les règles restent satisfaites.

Le système ne crée qu’une seule occurrence active de cette opportunité.

Chaque nouveau report remplace le précédent.

---

## 13. `Pas maintenant`

`Pas maintenant` :

- abandonne l’opportunité ;
- retire la notification ;
- ne relance rien ;
- ne modifie aucun créneau ;
- ne crée aucune préférence ;
- ne génère aucune pénalité.

Le planning normal continue.

---

## Comportements passifs et événements système

Un comportement passif ou un événement système n’est pas interprété comme une préférence utilisateur.

Exemples : notification ignorée, écran verrouillé ou éteint, téléphone éteint, application arrêtée par le système, redémarrage ou appareil indisponible.

Ces événements ne créent ni pénalité, ni préférence, ni relance, ni dette, ni rattrapage. Une opportunité devenue périmée est simplement abandonnée.

Le moteur métier ne reçoit pas l’état de l’écran ou du téléphone comme signal comportemental.

## Une seule notification d’activité visible

Une seule notification d’activité peut être visible à la fois. Une nouvelle proposition valide remplace une ancienne notification encore présente et non traitée. L’ancienne opportunité est abandonnée silencieusement.


## Modification des horaires avec report actif

Toute modification des jours, horaires de travail, pause déjeuner ou créneaux annule le report temporaire éventuellement actif.

Le nouveau planning devient immédiatement la seule référence.

Aucun report créé sous l’ancienne configuration n’est recalculé ou migré.

---

## Limite de jour pour les reports

Un report explicitement demandé peut dépasser la pause déjeuner et l’heure habituelle de fin de travail.

En revanche, il ne peut pas changer de jour civil.

```text
23:50 + 30 min
→ option non proposée
```

Cette limite évite de mélanger une opportunité de la veille avec le planning du lendemain.

---

# C. SUSPENSION

## 14. États

États V0 :

```text
ACTIVE
SUSPENDED
```

### Suspension manuelle

`Suspendre` met les propositions en pause jusqu'à une action explicite `Reprendre`.

La suspension :

- conserve les jours, horaires, créneaux et préférences ;
- ne se termine jamais automatiquement au prochain jour travaillé ;
- ne crée aucun rattrapage lors de la reprise ;
- reste distincte de la permission Android.

Si les propositions sont suspendues :

```text
OpportunityPolicy → DENIED
```

---

# D. DÉBUT DE JOURNÉE

## 15. Opportunité de début de journée

Fonction V0 :

```text
supportée : oui
activée par défaut : non
```

Elle utilise uniquement les activités :

```text
eligible_opportunity_types = ["start_of_day"]
```

Elle n’appartient pas aux quatre créneaux standards.

### Écart minimal

Si cette opportunité et le premier créneau standard sont distants de moins de 20 minutes, la configuration doit être ajustée.

Aucune règle 90/60 n’est appliquée.

---

# E. FILTRAGE DES ACTIVITÉS

## 16. CandidateFilter

Le filtre applique uniquement les contraintes absolues.

Il ne choisit aucune activité.

---

## 17. Statut

Seules les activités :

```text
status = published
```

sont admissibles.

---

## 18. Catégorie

Une catégorie désactivée entraîne l’exclusion de ses activités.

---

## 19. Intensité

Une intensité désactivée entraîne l’exclusion correspondante.

Valeurs :

```text
gentle
active
dynamic
```

---

## 20. Station debout

Si :

```text
standing_required = true
can_stand = false
```

l’activité est exclue.

Une activité compatible assise reste disponible même si la station debout est préférée.

---

## 21. Marche

Si :

```text
walking_required = true
can_walk = false
```

l’activité est exclue.

---

## 22. Durée

Une activité est exclue si sa durée minimale dépasse la fenêtre disponible lorsque cette information existe.

La durée maximale est indicative.

---

## 23. Type d’opportunité

Pour un créneau normal :

```text
eligible_opportunity_types contient "standard"
```

Pour le début de journée :

```text
eligible_opportunity_types contient "start_of_day"
```

---

## 24. Aucun candidat

Si aucune activité n’est compatible :

```text
NO_ELIGIBLE_ACTIVITY
```

Aucune activité incompatible n’est proposée « faute de mieux ».

---

# F. CHOIX D’UNE AUTRE ACTIVITÉ

## 25. Principe

L’utilisateur peut demander :

```text
Autre activité
```

L’application fait une nouvelle suggestion.

Elle ne cherche pas à contrôler ce que la personne fera réellement.

L’utilisateur peut toujours :

- accepter la suggestion ;
- refaire une activité proposée plus tôt ;
- faire une autre activité de son choix ;
- ne rien faire.

---

## 26. Exclusion lors d’une alternative

Lors d’une demande `Autre activité`, toutes les activités déjà affichées pendant l’interaction courante sont exclues.

### Exemple

Historique :

```text
notification 1 : marche
notification 2 : mobilité épaules
notification actuelle : squats
```

Si l’utilisateur demande une autre activité :

```text
squats → exclus pour ce tirage
marche → éligible
mobilité épaules → éligible
```

Le fait qu’une activité ait déjà été montrée auparavant ne la rend pas indisponible.

---

## 27. Alternatives successives

Exemple :

```text
A affichée
→ Autre activité
→ B affichée
→ Autre activité
```

Pour le second changement :

```text
B est exclue
A peut redevenir éligible
```

### Risque UX accepté en V0

Une séquence :

```text
A → B → C
```

est possible.

C’est un compromis volontaire en faveur :

- de la simplicité ;
- de la liberté ;
- de l’absence de mémoire artificielle de rejet.

Si les retours d’usage montrent que ces cycles sont irritants, une règle légère pourra être envisagée en V1.

---

## 28. Aucun rejet durable

Une demande `Autre activité` ne signifie pas :

> je n’aime pas cette activité.

Elle ne crée donc :

- aucun blocage permanent ;
- aucune préférence négative ;
- aucun apprentissage.

---

# G. SCORING

## 29. Objectif

Le score sert uniquement à varier les suggestions.

Il ne mesure ni la santé, ni la qualité d’une activité.

Formule :

```text
final_weight =
  clamp(
    base_weight × repetition_coefficient,
    min_weight,
    max_weight
  )
```

---

## 30. Historique récent

Fenêtre :

```text
4 dernières propositions
```

L’historique sert uniquement à rendre temporairement moins probable une activité récemment proposée.

`internal_family` est conservé dans le catalogue comme métadonnée éditoriale, mais n’est pas utilisé par le scoring V0.

---

## 31. Coefficients

Un seul coefficient est appliqué :

```text
même activité que la précédente       × 0.15
sinon même activité dans les 4 dernières
                                      × 0.55
sinon                                 × 1.00
```

Les coefficients ne se cumulent pas.

### Pourquoi les familles ne sont pas utilisées

Le catalogue V0 possède actuellement une famille interne distincte par activité. Des pénalités de « même famille » n’auraient donc aucun effet réel.

La V0 préfère supprimer cette complexité plutôt que regrouper artificiellement les activités pour justifier une règle.

### Conséquence

Une activité proposée récemment reste toujours éligible. Elle devient seulement moins probable.

---

## 32. Bornes

```text
min_weight = 0.05
max_weight = 20.0
```

Les poids doivent être strictement positifs.

---

## 33. Niveau de preuve

`evidence_level` ne participe pas au score runtime.

Il reste documentaire.

---

# H. SÉLECTION

## 34. WeightedSelector

Le choix entre candidats éligibles est :

- pondéré ;
- pseudo-aléatoire ;
- reproductible.

Avant le tirage :

```text
trier par id
```

À mêmes entrées et même graine :

```text
→ même résultat
```

---

## 35. Alternatives

Lors d’une alternative :

- l’activité courante est retirée du tirage ;
- les autres activités éligibles restent disponibles ;
- une nouvelle graine dérivée est utilisée.

Si l’activité courante est le seul candidat :

```text
NO_MORE_ALTERNATIVES
```

---

# I. CONTRATS

## 36. DecisionRequest

La requête contient notamment :

```text
decision_id
resolved_local_datetime
opportunity_type
source_slot_id
work_schedule
lunch_break
daily_slots
active_snooze
enabled_categories
enabled_intensities
can_stand
can_walk
recent_proposals
current_activity_id
catalog_version
policy_version
seed
```

Une donnée obligatoire absente entraîne :

```text
INVALID_CONTEXT
```

---

## 37. OpportunityType

```text
NORMAL_SLOT
SNOOZE
START_OF_DAY
ALTERNATIVE
```

Une alternative reste dans l’opportunité courante.

Elle ne crée pas un nouveau créneau.

---

## 38. DecisionResult

Résultats V0 :

```text
NO_OPPORTUNITY
NO_ELIGIBLE_ACTIVITY
NO_MORE_ALTERNATIVES
ACTIVITY_SELECTED
INVALID_CONTEXT
INVALID_CATALOG
```

---

# J. TRACE

## 39. Explicabilité

La trace peut indiquer :

### Opportunité

- créneau source ;
- heure cible ;
- heure réelle ;
- retard éventuel ;
- raison d’autorisation ou refus.

### Filtrage

Pour chaque activité exclue :

- ID ;
- raison.

### Scoring

Pour chaque candidat :

- poids de base ;
- coefficient de répétition ;
- poids final.

### Tirage

- graine ;
- valeur pseudo-aléatoire ;
- somme des poids ;
- activité choisie.

Aucune donnée médicale n’est nécessaire.

---

# K. INVARIANTS

## 40. Invariants V0

1. Les créneaux configurés ne sont pas déplacés automatiquement.
2. Deux créneaux standards sont espacés d’au moins 20 minutes.
3. La règle 90/60 n’existe pas.
4. Un report conserve la même activité.
5. Un report peut traverser la pause déjeuner.
6. Un report peut dépasser la fin habituelle du travail.
7. Un report ne déplace pas un autre créneau.
8. Une option de report créant un écart inférieur à 20 minutes avec un autre créneau est masquée.
9. `Pas maintenant` ne provoque aucune relance.
10. Aucun rattrapage automatique n’existe.
11. Une activité sélectionnée était éligible.
12. Une activité incompatible ne peut pas être réintroduite.
13. `Autre activité` exclut uniquement l’activité actuellement affichée.
14. Une activité vue auparavant peut être reproposée.
15. Une demande d’alternative ne crée aucune préférence durable.
16. Les répétitions d’une même activité sont pénalisées, jamais interdites.
17. `internal_family` ne modifie pas le score V0.
18. `evidence_level` ne modifie pas le score.
18. Le moteur reste indépendant d’Android.
19. À entrées identiques, le résultat est reproductible.
20. `C’est parti` signifie accepté, pas vérifié comme réalisé.

---

21. Un comportement passif ou un événement système ne crée aucun signal comportemental.
22. Une seule notification d’activité est visible à la fois.
23. Une nouvelle notification valide remplace l’ancienne notification non traitée.
24. Modifier les horaires annule tout report actif.
25. Un report ne traverse jamais minuit.

26. Un utilisateur peut avoir de 1 à 4 créneaux standards actifs.
27. Aucun jour travaillé sélectionné est un planning valide sans opportunité automatique.
28. `can_stand=false` impose `can_walk=false`.
29. Réactiver `can_stand` ne réactive pas automatiquement `can_walk`.
30. La suspension V0 est uniquement manuelle jusqu'à `Reprendre`.
31. Une reprise, restauration de permission ou fin d'onboarding ne rattrape jamais un créneau passé.
32. Toute modification affectant l'éligibilité annule un report actif.
33. Une notification déjà affichée n'est pas réécrite après un changement de réglage.
34. Une alternative utilise les derniers réglages disponibles.
35. Les créneaux sont interprétés en heure locale.
36. Un changement de fuseau ou d'heure recalcule seulement les opportunités futures.
37. Une plage de travail traversant minuit n'est pas supportée en V0.

# L. RISQUES

## 41. Risque UX — report proche d’un créneau

Sans règle de conflit, un report pourrait produire deux notifications presque simultanées.

### Réponse V0

Le créneau fixe garde la priorité.

L’option de report conflictuelle n’est pas affichée.

---

## 42. Risque UX — cycles d’alternatives

Avec exclusion de l’activité courante uniquement :

```text
A → B → C
```

est possible.

### Décision V0

Risque accepté.

Le bénéfice est un comportement extrêmement simple et compréhensible.

V1 pourra ajouter un petit cooldown d’interaction si le problème apparaît réellement.

---

## 43. Risque comportemental — surinterpréter l’utilisateur

Le produit ne sait pas si l’utilisateur :

- a réellement fait l’activité ;
- a fait une autre activité ;
- n’a rien fait ;
- aime ou déteste une proposition.

### Réponse

Ne pas construire de préférences implicites à partir de ces interactions en V0.

---

## 44. Risque d’architecture — moteur de calendrier inutilement complexe

La règle 90/60 créait des dépendances entre créneaux, reports, frontières et affichages réels.

### Décision V0

Suppression de cette logique.

Le planning appartient à l’utilisateur.

Le moteur n’intervient que pour :

- valider les créneaux ;
- appliquer le minimum de 20 minutes ;
- gérer les reports ;
- sélectionner l’activité.

---

# M. V0 / V1 / V2

## 45. V0

Inclut :

- créneaux utilisateur ;
- minimum 20 minutes ;
- reports 10/30 ;
- reports possibles pendant déjeuner ou après travail ;
- aucune cascade de planning ;
- aucune règle 90/60 ;
- alternative excluant seulement l’activité courante ;
- scoring simple fondé uniquement sur la répétition de la même activité ;
- sélection pondérée reproductible.

---

## 46. V1

Seulement après retours :

- cooldown léger pour éviter `A → B → C` si nécessaire ;
- favoris ou masquage ;
- motifs facultatifs de changement ;
- préférences explicites ;
- ancrages « Si… alors ».

---

## 47. V2

Expérimental :

- calendrier ;
- adaptation automatique des horaires ;
- contexte local ;
- fading des notifications ;
- personnalisation avancée.

Aucun de ces éléments ne doit complexifier la V0.

---

# N. TESTS PRIORITAIRES

## 48. Créneaux

```text
10:10 puis 10:30
→ valide
```

```text
10:10 puis 10:25
→ configuration invalide
```

```text
créneau pendant déjeuner
→ invalide
```

```text
créneau hors plage de travail
→ invalide
```

---

## 49. Reports

```text
10:10 → report 10 min → 10:20
→ autorisé, même opportunité
```

```text
17:30 → report 30 min → 18:00
fin de travail 17:30
→ autorisé
```

```text
11:50 → report 30 min → 12:20
déjeuner à 12:00
→ autorisé
```

```text
notification 10:30
prochain créneau 10:50
report +10 → 10:40
→ masqué car conflit <20 min
```

---

## 50. Alternatives

```text
activité actuelle A
candidats A, B, C
→ alternative choisie parmi B, C
```

Puis :

```text
activité actuelle B
candidats A, B, C
→ A et C sont à nouveau éligibles
```

---

## 51. Répétition

Une activité montrée deux notifications auparavant :

```text
reste éligible
```

mais peut recevoir le coefficient de répétition correspondant.

---

## 52. Critères avant Kotlin

Le moteur est prêt à implémenter lorsque :

1. ce document est validé ;
2. `decision_policy_v1.json` reflète ces règles ;
3. les scénarios de test sont mis à jour ;
4. l’UX ne contient plus de référence à la règle 90/60 ;
5. les reports ne sont plus bloqués par déjeuner ou fin de travail ;
6. l’alternative n’exclut plus toutes les activités vues dans l’interaction ;
7. aucune règle métier concurrente ne subsiste dans les anciens documents.


## Historique temporaire de l’interaction

Pour `Autre activité`, le moteur reçoit l’ensemble des IDs déjà affichés pendant l’interaction courante.

```text
A → Autre activité → B → Autre activité → C
```

Au troisième tirage, A et B sont exclus.

Cet ensemble :

- est temporaire ;
- ne devient pas une préférence ;
- n’altère aucun poids futur ;
- est réinitialisé à la fin de l’interaction ;
- peut être restauré si Android recrée l’écran pendant la même interaction.

Quand tous les candidats compatibles ont été montrés, aucune alternative n’est disponible.
