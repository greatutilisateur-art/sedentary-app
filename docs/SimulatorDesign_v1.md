# SimulatorDesign V1

## Objet

Le simulateur est un banc de test du moteur pur. Il ne contient aucune API Android, aucune base de données et aucune interface.

## Choix simples

- Python standard uniquement ;
- entrées JSON versionnées ;
- tests `unittest` ;
- sorties CSV, JSON et Markdown ;
- tirage reproductible par SHA-256 sur une graine textuelle.

## Pourquoi SHA-256 ?

Utiliser le générateur pseudo-aléatoire natif de Python rendrait la parité future avec Kotlin moins évidente. Le simulateur transforme les 64 premiers bits de `SHA-256(seed)` en fraction dans `[0,1[`. La même règle pourra être reproduite dans le module Kotlin.

Ce choix est une décision technique, pas une règle scientifique ni comportementale.

## Séparation

- `OpportunityPolicy` : calendrier, créneaux, tolérance et espacement ;
- `CandidateFilter` : contraintes absolues ;
- `CandidateScorer` : poids et répétitions ;
- `WeightedSelector` : choix déterministe ;
- `DecisionEngine` : orchestration sans règle cachée ;
- `batch.py` : simulation de plusieurs journées et rapports.

## Ce que le simulateur ne prétend pas faire

- prédire l'adhésion ;
- modéliser la fatigue des notifications ;
- valider une efficacité médicale ;
- choisir les coefficients optimaux ;
- simuler les contraintes Android.
