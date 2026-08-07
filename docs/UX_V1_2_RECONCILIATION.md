# UX_V1_2_RECONCILIATION

Ce document explique comment le retour UX N-of-1 reçu le 7 août 2026 a été intégré au référentiel déjà plus récent.

## Décisions nouvelles appliquées

- suppression de l'action de confirmation `C'est parti` ;
- écran activité = consultation de consigne ;
- pas de bouton fermer spécifique ;
- Retour Android neutre ;
- ajout de `Dernière activité proposée` ;
- swipe / lock / background / process death sans inférence comportementale ;
- `Pas maintenant` efface la mise en avant de l'opportunité concernée ;
- report conserve la même activité ;
- aucune métrique de réalisation déduite des interactions ;
- nouvelle révision UX V1.2 ;
- tests UX séparés du moteur pur.

## Éléments du texte reçu volontairement NON réintroduits

Le texte UX contenait plusieurs hypothèses correspondant à une ancienne version du projet :

1. **Règle 90/60** — non appliquée. La source courante conserve le minimum simple de 20 minutes.
2. **Quatre créneaux obligatoires** — non appliqué. Quatre horaires sont préremplis, mais 1 à 4 peuvent être actifs.
3. **Room + WorkManager** — non appliqués. L'architecture courante reste DataStore + AlarmManager inexact, sans Room.
4. **Ancien `DecisionEngineSpecification_v1_1.md`** — reste LEGACY ; la source moteur courante est `DecisionEngine.md`.

## Point contradictoire conservé selon le référentiel courant

Le texte reçu affirme que `Autre activité` exclut toutes les activités déjà montrées dans l'interaction, tout en appelant cela le « comportement actuel ».

Avant l'arbitrage final, le référentiel disait l'inverse : seule l'activité actuellement affichée était exclue et `A → B → A` restait possible.

Cette mise à jour **conserve donc la règle courante** afin de ne pas modifier silencieusement le moteur de sélection.

Si le test UX avait réellement pour but de changer cette règle, une décision Produit explicite est encore nécessaire.


## Arbitrage final — `Autre activité`

Décision Produit : **A → B → C**.

Une activité déjà affichée pendant l’interaction n’est pas reproposée dans cette même interaction. L’exclusion est temporaire et ne devient pas une préférence utilisateur.

Cette décision modifie réellement le comportement du moteur : la policy passe à `decision-policy-v1.0.5`.
