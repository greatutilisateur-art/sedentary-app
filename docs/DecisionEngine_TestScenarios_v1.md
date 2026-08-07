# DecisionEngine — scénarios de test V1

**Statut :** scénarios alignés sur `DecisionEngine.md` et `decision-policy-v1.0.5`  
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
| C-06 | 1, 2, 3 ou 4 créneaux actifs valides | VALID |
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
| O-08 | suspension manuelle active | DENIED |
| O-09 | `Reprendre` puis créneau passé | aucun rattrapage |

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
| S-01 | même activité que la précédente | 0.15 |
| S-02 | même activité dans l’historique récent mais pas précédente | 0.55 |
| S-03 | aucune répétition de l’activité | 1.00 |
| S-04 | deux activités synthétiques partagent `internal_family` | aucun effet |
| S-05 | plusieurs occurrences récentes de la même activité | un seul coefficient, jamais cumulé |
| S-06 | report | aucun recalcul de sélection ou de score |
| S-07 | `evidence_level` différent | aucun effet runtime |
| S-08 | poids final < min | clamp à 0.05 |
| S-09 | poids final > max | clamp à 20.0 |
| S-10 | activité montrée deux notifications auparavant | reste sélectionnable, coefficient 0.55 |

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
2. **Fin plus tôt** — fin configurée à 17:00 : le créneau fixe 17:00 est invalide dans la configuration.
3. **Déjeuner** — pause 12:00–14:00 : le créneau fixe 11:50 est valide ; un report +30 à 12:20 est autorisé.
4. **Après le travail** — créneau 17:00, fin de travail 17:00 : un report +30 à 18:00 est autorisé.
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


## Cycle de vie passif

| ID | Situation | Résultat attendu |
|---|---|---|
| L-01 | notification ignorée jusqu’au créneau suivant | ancienne notification retirée ; nouvelle affichée ; aucun signal comportemental |
| L-02 | écran éteint pendant un créneau | aucune préférence, pénalité ou relance créée |
| L-03 | téléphone éteint puis rallumé après expiration de la tolérance | opportunité manquée ; aucun rattrapage |
| L-04 | ouverture de l’activité puis bouton Retour | aucune relance |
| L-05 | nouvelle notification alors qu’une ancienne est encore visible | une seule notification ; la nouvelle remplace l’ancienne |
| L-06 | modification des horaires avec report actif | report annulé |
| L-07 | report +30 à 23:50 | option non proposée car changement de jour civil |
| L-08 | report après heure de fin mais avant minuit | autorisé si aucun conflit avec un créneau fixe |


## Configuration libre

| ID | Situation | Résultat attendu |
|---|---|---|
| CFG-01 | 1 seul créneau actif valide | configuration valide |
| CFG-02 | 2 créneaux actifs valides | configuration valide |
| CFG-03 | créneau inactif avec heure hors plage | ignoré par la validation |
| CFG-04 | 0 jour travaillé sélectionné | configuration valide, aucune opportunité |
| CFG-05 | lundi et jeudi uniquement, vendredi courant | prochain jour travaillé = lundi |
| CFG-06 | aucun jour sélectionné | aucun prochain jour calculé |

## Capacités

| ID | Situation | Résultat attendu |
|---|---|---|
| CAP-01 | `can_stand=true`, `can_walk=true`, puis désactivation de `can_stand` | `can_stand=false`, `can_walk=false` |
| CAP-02 | réactivation de `can_stand` | `can_walk` reste false |
| CAP-03 | tentative d'activer `can_walk` alors que `can_stand=false` | `can_walk` reste false |

## Réactivation / réglages

| ID | Situation | Résultat attendu |
|---|---|---|
| RE-01 | permission notifications rétablie après un créneau | aucun rattrapage |
| RE-02 | `Reprendre` après un créneau passé | aucune notification du passé |
| RE-03 | onboarding terminé après un créneau | prochaine opportunité future uniquement |
| RE-04 | changement horaire avec report actif | report annulé |
| RE-05 | désactivation catégorie avec report actif | report annulé |
| RE-06 | désactivation marche avec activité marchée reportée | report annulé |
| RE-07 | notification déjà visible puis catégorie désactivée | notification inchangée |
| RE-08 | après RE-07, `Autre activité` | nouvelle sélection respecte les nouveaux réglages |

## Temps local

| ID | Situation | Résultat attendu |
|---|---|---|
| TIME-01 | changement France → Londres | créneau reste 10:10 heure locale |
| TIME-02 | passage heure été/hiver | recalcul des opportunités futures |
| TIME-03 | créneau déjà traité avant changement de fuseau | jamais rejoué |
| TIME-04 | horaires 22:00 → 06:00 | configuration non supportée en V0 |


## Onboarding et affichage UX

| ID | Situation | Résultat attendu |
|---|---|---|
| UX-01 | un seul créneau actif | accueil affiche seulement la prochaine proposition, pas la liste des créneaux |
| UX-02 | aucun jour travaillé sélectionné | permission Android non demandée |
| UX-03 | premier jour activé ultérieurement avec créneau valide | permission demandée à ce moment-là |
| UX-04 | `preferred_posture=standing`, `can_stand=true`, non obligatoire | badge `Debout conseillé` |
| UX-05 | même activité, `can_stand=false`, compatible assise | badge `Assis possible` |
| UX-06 | `standing_required=true` | badge `Debout nécessaire` |
| UX-07 | toutes catégories désactivées | sauvegarde autorisée, 0 candidat |
| UX-08 | toutes intensités désactivées | sauvegarde autorisée, 0 candidat |
| UX-09 | 0 candidat à cause d'une combinaison de réglages | texte exact `Aucune activité ne correspond actuellement à ces réglages.` |


## Alternative — exclusion temporaire d’interaction

| ID | Situation | Résultat attendu |
|---|---|---|
| ALT-01 | A affichée, demande autre | A exclue |
| ALT-02 | A puis B affichées, demande autre | A et B exclues |
| ALT-03 | A puis B puis C | aucun retour à A/B dans la même interaction |
| ALT-04 | tous les candidats compatibles déjà vus | aucune alternative |
| ALT-05 | nouvelle opportunité | exclusions de l’interaction précédente réinitialisées |
