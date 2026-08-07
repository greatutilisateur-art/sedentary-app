# SedentaryApp — V0

Application Android gratuite, open source et entièrement hors ligne destinée à aider les personnes à interrompre leur sédentarité au travail grâce à des activités courtes.

## État actuel

- vision produit consolidée ;
- principes produit ;
- cadre scientifique ;
- catalogue V0 de 23 activités, version `1.0.2` ;
- schéma JSON et vocabulaires contrôlés ;
- moteur de décision V0 simplifié ;
- politique `decision-policy-v1.0.5` ;
- scénarios de test mis à jour ;
- UX V1.2 spécifiée et validée avant implémentation Android.

### Validation actuelle

Le moteur a été resimulé après simplification du planning puis du scoring. `internal_family` n’influence plus la sélection runtime.

Cette règle a été supprimée avant l'implémentation Android.

La validation actuelle est documentée dans `docs/VALIDATION_STATUS.md`. Une dernière exécution doit être reproduite sur le dépôt local synchronisé avant le passage au Kotlin.

## Sources de vérité

1. `catalog/activities_v1.json` — catalogue officiel ;
2. `catalog/activity_catalog_schema_v1.json` — contrat de données ;
3. `catalog/activity_vocabularies_v1.json` — valeurs contrôlées ;
4. `policy/decision_policy_v1.json` — paramètres versionnés du moteur ;
5. `docs/DecisionEngine.md` — règles métier ;
6. `docs/UXSpecification_v1_2.md` — comportement UX courant ;
7. `docs/AndroidArchitecture_V0_2.md` — architecture Android V0 ;
8. `docs/Scientific_framework.md` — cadre scientifique ;
9. `docs/Vision.md` et `docs/ProductPrinciples.md` — direction et arbitrages produit.

Les documents explicitement marqués `LEGACY` sont conservés uniquement pour l'historique et ne doivent pas guider l'implémentation.

## Politique de planning V0

- quatre créneaux proposés par défaut, avec 1 à 4 créneaux actifs au choix ;
- créneaux fixes dans les plages de travail configurées ;
- minimum de 20 minutes entre deux opportunités distinctes ;
- aucune règle 90/60 ;
- aucun rattrapage ;
- reports +10/+30 conservant la même activité ;
- reports explicitement demandés autorisés pendant la pause déjeuner ou après la fin habituelle du travail ;
- un report ne déplace jamais un créneau fixe ;
- une seule notification d’activité visible à la fois ;
- les comportements passifs ne créent ni préférence, ni relance, ni rattrapage ;
- modifier le planning ou un réglage d’éligibilité annule un report actif ;
- un report ne traverse jamais minuit.

- zéro jour travaillé sélectionné est valide et produit zéro proposition ;
- suspension uniquement manuelle jusqu’à `Reprendre` ;
- reprise/permission/onboarding : opportunités futures uniquement ;
- horaires interprétés en heure locale ;
- travail traversant minuit non supporté en V0.

- accueil réduit à l’état + prochaine proposition ;
- posture portée par un badge contextuel ;
- permission Android différée si aucune proposition automatique n’est prévue ;
- réglages pouvant produire zéro activité éligible sans blocage.


## Flux activité UX V1.2

- aucun bouton de confirmation de démarrage ;
- l’écran activité sert à consulter la consigne ;
- Retour Android est neutre ;
- `Dernière activité proposée` permet de rouvrir la consigne depuis l’accueil ;
- swipe, verrouillage et autres comportements passifs n’ont aucune signification comportementale ;
- `Pas maintenant` reste l’abandon explicite ;
- un report conserve la même activité ;
- aucune métrique V0 ne prétend mesurer les activités réellement effectuées ;
- `Autre activité` suit une séquence `A → B → C` : toutes les activités déjà montrées dans l’interaction sont temporairement exclues.

## Architecture Android V0

Le port Kotlin pur du moteur est préparé pour `:engine` mais n’est pas encore intégré au projet Android.

Le module `:app` utilise Compose, DataStore et AlarmManager inexact.

Pas de Room, pas de Hilt et pas de WorkManager comme scheduler principal en V0.

Aucune règle métier ne dépend de Compose, DataStore, AlarmManager ou du gestionnaire de notifications.
