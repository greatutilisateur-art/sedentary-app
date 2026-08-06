# DecisionEngine — scénarios de test V1

## OpportunityPolicy

| ID | Situation | Résultat attendu |
|---|---|---|
| O-01 | 10:10, premier affichage du jour | ALLOWED |
| O-02 | affichage 10:10, cible 11:50 | ALLOWED (100 min) |
| O-03 | affichage réel 11:00, cible 11:50 | DENIED, gap < 60 |
| O-04 | affichage 16:20, cible 17:30, fin 17:30 | ALLOWED, exception frontière |
| O-05 | gap 59 min avant fin de journée | DENIED |
| O-06 | cible pendant pause déjeuner | DENIED |
| O-07 | créneau hors horaires | DENIED |
| O-08 | créneau déjà traité | DENIED |
| O-09 | retard Android de 12 min | ALLOWED |
| O-10 | retard Android de 20 min | DENIED, missed slot |
| O-11 | double exécution même decision_id | même résultat, une seule notification |

## Reports

| ID | Situation | Résultat attendu |
|---|---|---|
| R-01 | report +10 valide | même occasion replanifiée |
| R-02 | report +30 valide | même occasion replanifiée |
| R-03 | report traverse le début de pause déjeuner | occasion annulée |
| R-04 | report dépasse fin de journée | occasion annulée |
| R-05 | report affiché à 10:40, cible 11:50, déjeuner 12:30 | cible annulée : règle 90 normale encore possible avant frontière |
| R-06 | Pas maintenant | abandon sans relance ni pénalité |
| R-07 | report en conflit avec créneau normal | report prioritaire, créneau réévalué ensuite |

## CandidateFilter

| ID | Situation | Résultat attendu |
|---|---|---|
| F-01 | catégorie désactivée | activités exclues |
| F-02 | intensité désactivée | activités exclues |
| F-03 | can_stand=false | standing_required exclues |
| F-04 | can_stand=false | standing_preferred assis-compatible conservées |
| F-05 | can_walk=false | walking_required exclues |
| F-06 | durée disponible < durée minimale | exclue |
| F-07 | START_OF_DAY | seulement activités start_of_day |
| F-08 | NORMAL_SLOT | start_of_day-only exclues |
| F-09 | alternative demandée | activité déjà vue exclue |
| F-10 | aucun candidat | NO_ELIGIBLE_ACTIVITY, aucune notification |

## CandidateScorer

| ID | Situation | Coefficient attendu |
|---|---|---|
| S-01 | même activité que précédente | 0.15 |
| S-02 | activité différente, même famille précédente | 0.45 |
| S-03 | activité récente, famille précédente différente | 0.55 |
| S-04 | famille vue au moins 2 fois sur 4 | 0.75 |
| S-05 | aucune répétition | 1.00 |
| S-06 | plusieurs conditions vraies | un seul coefficient hiérarchique |
| S-07 | report | aucun changement de score |
| S-08 | evidence_level différent | aucun effet runtime |
| S-09 | poids final < min | clamp à 0.05 |
| S-10 | poids final > max | clamp à 20.0 |

## WeightedSelector

| ID | Situation | Résultat attendu |
|---|---|---|
| W-01 | mêmes entrées et graine | même activité |
| W-02 | ordre des candidats modifié | même activité |
| W-03 | un seul candidat | candidat sélectionné |
| W-04 | poids nul | candidat non sélectionnable |
| W-05 | poids négatif | erreur de catalogue |
| W-06 | identifiants dupliqués | erreur de catalogue |
| W-07 | alternative attempt_index+1 | sélection reproductible différente si possible |
| W-08 | toutes alternatives épuisées | NO_MORE_ALTERNATIVES |

## Scénarios journaliers

1. Journée standard 08:30–12:30 / 13:30–17:45 : quatre créneaux admissibles.
2. Fin de journée à 17:00 : créneau 17:30 invalide.
3. Pause déjeuner 12:00–14:00 : créneau 11:50 valide, reports au-delà de 12:00 annulés.
4. Notification de 15:10 reportée à 15:40 : vérifier l'admissibilité de 17:30 depuis 15:40.
5. Début de journée activé à 09:30 : créneau 10:10 annulé par espacement <60.
6. Utilisateur ne pouvant ni se lever ni marcher : vérifier qu'une activité assise existe ou qu'aucune notification n'est affichée.
7. Quatre dernières propositions de la même grande zone corporelle : vérifier la diversité des familles internes sans rotation forcée des catégories.
