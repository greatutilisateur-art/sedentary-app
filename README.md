# SedentaryApp — V0

Application Android gratuite, open source et entièrement hors ligne destinée à aider les personnes à interrompre leur sédentarité au travail grâce à des activités courtes.

## État actuel

- vision produit consolidée ;
- principes produit ;
- cadre scientifique ;
- catalogue V0 de 23 activités ;
- schéma JSON et vocabulaires contrôlés ;
- moteur de décision V0 simplifié ;
- politique `decision-policy-v1.0.1` ;
- scénarios de test mis à jour ;
- UX spécifiée.

### Validation actuelle

Le moteur avait été simulé avec une ancienne politique utilisant une règle d'espacement 90/60.

Cette règle a été supprimée avant l'implémentation Android.

Les anciens résultats automatisés ne doivent donc plus être considérés comme une validation de la politique actuelle. Le simulateur devra être mis à jour et les tests relancés avant le passage au Kotlin.

## Sources de vérité

1. `catalog/activities_v1.json` — catalogue officiel ;
2. `catalog/activity_catalog_schema_v1.json` — contrat de données ;
3. `catalog/activity_vocabularies_v1.json` — valeurs contrôlées ;
4. `policy/decision_policy_v1.json` — paramètres versionnés du moteur ;
5. `docs/DecisionEngine.md` — règles métier ;
6. `docs/UXSpecification_v1_1.md` — comportement UX ;
7. `docs/Scientific_framework.md` — cadre scientifique ;
8. `docs/Vision.md` et `docs/ProductPrinciples.md` — direction et arbitrages produit.

Les documents explicitement marqués `LEGACY` sont conservés uniquement pour l'historique et ne doivent pas guider l'implémentation.

## Politique de planning V0

- quatre créneaux par défaut, entièrement modifiables ;
- créneaux fixes dans les plages de travail configurées ;
- minimum de 20 minutes entre deux opportunités distinctes ;
- aucune règle 90/60 ;
- aucun rattrapage ;
- reports +10/+30 conservant la même activité ;
- reports explicitement demandés autorisés pendant la pause déjeuner ou après la fin habituelle du travail ;
- un report ne déplace jamais un créneau fixe.

## Architecture future

Le moteur sera implémenté en Kotlin pur avant son intégration Android.

Aucune règle métier ne doit dépendre de Compose, Room, DataStore, WorkManager ou du gestionnaire de notifications.
