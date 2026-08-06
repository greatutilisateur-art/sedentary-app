# SedentaryApp — référentiel et simulateur V1

Application Android gratuite, open source et entièrement hors ligne destinée à aider les personnes à interrompre leur sédentarité au travail par des activités de 30 secondes à 5 minutes.

## État actuel

- vision et principes UX ;
- catalogue V1 de 23 activités ;
- schéma JSON et vocabulaires contrôlés ;
- spécification V1 du moteur ;
- politique V1 ;
- simulateur Python sans dépendance ;
- 14 tests d’acceptation automatisés ;
- simulations de 30 journées pour deux profils.

## Sources de vérité

1. `data/activities_v1.json` — catalogue officiel ;
2. `data/activity_catalog_schema_v1.json` — contrat de données ;
3. `data/activity_vocabularies_v1.json` — valeurs contrôlées ;
4. `data/decision_policy_v1.json` — paramètres du moteur ;
5. `docs/DecisionEngineSpecification_v1.md` — règles métier.

Les fichiers marqués `legacy` servent seulement d’historique et ne priment jamais sur les fichiers V1.

## Tester sous Windows

1. Installer Python 3.11 ou plus récent.
2. Double-cliquer sur `run_tests_windows.bat`.
3. Double-cliquer sur `run_demo_windows.bat`.
4. Lire `outputs/demo_accept_all/report.md`.

## Architecture future

Le moteur sera traduit en Kotlin pur avant l’intégration Android. Aucune règle métier ne devra dépendre de Compose, Room, DataStore ou WorkManager.
