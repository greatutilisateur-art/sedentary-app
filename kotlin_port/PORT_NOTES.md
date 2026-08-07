# Port Kotlin du moteur V0 — étape 1

Ce paquet contient le premier port du moteur Python vers Kotlin pur.

## Inclus

- modèles minimaux utilisés par le moteur ;
- filtrage des candidats ;
- scoring de répétition V0 ;
- sélection pondérée déterministe SHA-256 ;
- validation des créneaux ;
- décision d'opportunité ;
- options de report ;
- dépendance `canStand → canWalk` ;
- calcul du prochain jour travaillé ;
- tests JUnit de base.

## Non inclus volontairement

- Android ;
- Compose ;
- DataStore ;
- AlarmManager ;
- parsing JSON ;
- sérialisation ;
- notification ;
- NextOpportunityPlanner complet.

Le but est de conserver la logique métier testable avant l'intégration Android.


## Mise à jour UX V1.2

`Autre activité` exclut désormais tous les IDs déjà montrés dans l’interaction courante. `CandidateFilterRequest.excludedActivityIds` porte cet état temporaire. La règle est A → B → C et n’est pas persistée comme préférence.
