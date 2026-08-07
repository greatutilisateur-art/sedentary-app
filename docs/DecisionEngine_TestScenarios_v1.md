# DecisionEngine — scénarios de test V1

**Statut :** scénarios alignés sur `DecisionEngine.md` et `decision-policy-v1.0.1`  
**Important :** ces scénarios remplacent les anciens tests fondés sur la règle 90/60.

---

## Configuration des créneaux

| ID | Situation | Résultat attendu |
|---|---|---|
| C-01 | créneaux 10:10 et 10:30 | VALID, écart = 20 min |
| C-02 | créneaux 10:10 et 10:25 | INVALID, écart < 20 min |
| C-03 | cible pendant pause déjeuner configurée | INVALID |
| C-04 | cible hors plage de travail | INVALID |
| C-05 | cible exactement égale à l'heure de fin de travail | VALID |
| C-06 | quatre créneaux par défaut avec horaires compatibles | VALID |
| C-07 | début de journée 09:50 et premier créneau 10:10 | VALID, écart = 20 min |
| C-08 | début de journée 09:55 et premier créneau 10:10 | INVALID, écart < 20 min |

## OpportunityPolicy

| ID | Situation | Résultat attendu |
|---|---|---|
| O-01 | 10:10, premier affichage du créneau | ALLOWED |
| O-02 | créneau déjà traité | DENIED |
| O-03 | retard Android de 12 min, aucun conflit avec un autre créneau | ALLOWED |
| O-04 | retard Android de 20 min | DENIED, missed slot |
| O-05 | créneau manqué | aucun rattrapage |
| O-06 | double exécution même `decision_id` et même état | même résultat, une seule notification |
| O-07 | notification tardive désormais à <20 min du prochain créneau fixe | DENIED pour l'opportunité tardive ; prochain créneau conservé |
| O-08 | propositions suspendues jusqu'au prochain jour travaillé | DENIED |
| O-09 | propositions suspendues indéfiniment | DENIED |

## Reports

| ID | Situation | Résultat attendu |
|---|---|---|
| R-01 | report +10 | même activité, même opportunité replanifiée |
| R-02 | report +30 | même activité, même opportunité replanifiée |
| R-03 | report traverse le début de pause déjeuner | ALLOWED |
| R-04 | report tombe pendant toute la pause déjeuner | ALLOWED |
| R-05 | report dépasse l'heure de fin de travail | ALLOWED |
| R-06 | créneau à 10:50 ; report demandé depuis 10:30 pour 10:40 | option +10 masquée car <20 min du créneau fixe |
| R-07 | créneau à 10:50 ; report demandé depuis 10:20 pour 10:30 | option +10 autorisée, écart = 20 min |
| R-08 | un report valide existe ; le prochain créneau fixe reste inchangé | créneau non déplacé |
| R-09 | report successif | l'occurrence précédente est remplacée, même activité conservée |
| R-10 | `Pas maintenant` | abandon sans relance, pénalité ni préférence |
| R-11 | report demandé après l'heure de fin habituelle | évalué normalement ; fin de travail non bloquante |

## CandidateFilter

| ID | Situation | Résultat attendu |
|---|---|---|
| F-01 | catégorie désactivée | activités exclues |
| F-02 | intensité désactivée | activités exclues |
| F-03 | `can_stand=false` | `standing_required` exclues |
| F-04 | `can_stand=false`, activité assise-compatible mais debout préféré | conservée |
| F-05 | `can_walk=false` | `walking_required` exclues |
| F-06 | durée disponible < durée minimale | exclue |
| F-07 | `START_OF_DAY` | seulement activités `start_of_day` |
| F-08 | `NORMAL_SLOT` | activités `start_of_day` exclues |
| F-09 | alternative demandée avec activité courante A | A exclue du nouveau tirage |
| F-10 | activité A montrée plus tôt mais activité courante B | A reste éligible |
| F-11 | aucun candidat | `NO_ELIGIBLE_ACTIVITY`, aucune activité incompatible de secours |

## CandidateScorer

| ID | Situation | Coefficient attendu |
|---|---|---|
| S-01 | même activité que précédente | 0.15 |
| S-02 | activité différente, même famille précédente | 0.45 |
| S-03 | activité récente, famille précédente différente | 0.55 |
| S-04 | famille vue au moins 2 fois sur 4 | 0.75 |
| S-05 | aucune répétition | 1.00 |
| S-06 | plusieurs conditions vraies | un seul coefficient hiérarchique |
| S-07 | report | aucun recalcul de sélection ou de score |
| S-08 | `evidence_level` différent | aucun effet runtime |
| S-09 | poids final < min | clamp à 0.05 |
| S-10 | poids final > max | clamp à 20.0 |
| S-11 | activité montrée deux notifications auparavant | reste sélectionnable, seulement pénalisée si règle applicable |

## WeightedSelector

| ID | Situation | Résultat attendu |
|---|---|---|
| W-01 | mêmes entrées et graine | même activité |
| W-02 | ordre des candidats modifié | même activité |
| W-03 | un seul candidat | candidat sélectionné |
| W-04 | poids nul | candidat non sélectionnable / catalogue invalide selon contrat |
| W-05 | poids négatif | `INVALID_CATALOG` |
| W-06 | identifiants dupliqués | `INVALID_CATALOG` |
| W-07 | alternative : activité courante A, candidats A/B/C | sélection parmi B/C |
| W-08 | A → autre → B → autre | A peut redevenir candidate |
| W-09 | activité courante = seul candidat éligible | `NO_MORE_ALTERNATIVES` |

## Scénarios journaliers

1. **Journée standard** — 08:30–12:30 / 13:30–17:45 : les quatre créneaux par défaut sont valides.
2. **Fin plus tôt** — fin configurée à 17:00 : le créneau fixe 17:30 est invalide dans la configuration.
3. **Déjeuner** — pause 12:00–14:00 : le créneau fixe 11:50 est valide ; un report +30 à 12:20 est autorisé.
4. **Après le travail** — créneau 17:30, fin de travail 17:30 : un report +30 à 18:00 est autorisé.
5. **Conflit report/créneau** — notification à 10:30, prochain créneau 10:50 : `Dans 10 min` est masqué ; le créneau 10:50 n'est pas déplacé.
6. **Pas de cascade** — une notification reportée n'entraîne aucun recalcul des autres horaires fixes.
7. **Début de journée** — 09:50 puis créneau 10:10 : valide ; 09:55 puis 10:10 : configuration invalide.
8. **Capacités limitées** — `can_stand=false`, `can_walk=false` : seules les activités compatibles assises restent candidates, ou résultat sans candidat.
9. **Alternative libre** — A affichée, puis B après `Autre activité`, puis nouvelle demande : A peut être reproposée.
10. **Répétition non interdite** — une activité récente reste sélectionnable avec un poids réduit.
11. **Retard technique** — une occurrence tardive qui créerait moins de 20 min avant le prochain créneau fixe est abandonnée ; le créneau fixe est protégé.

---

## Critère de revalidation

Les anciens résultats automatisés `14/14 réussis` ne sont plus valables comme preuve de conformité après la suppression de la règle 90/60.

Le simulateur et ses tests doivent être adaptés à ces scénarios puis exécutés à nouveau avant de déclarer le moteur validé pour Kotlin.
