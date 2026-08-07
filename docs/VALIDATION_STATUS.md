# État de validation

**Mise à jour : 7 août 2026**

## Catalogue

- 23 activités publiées.
- Schéma V1 conservé.
- Aucun changement du catalogue requis par la simplification du planning.
- `policy_compatibility = 1.0.x` reste compatible avec `decision-policy-v1.0.1`.

## Moteur

Décisions V0 désormais consolidées :

- suppression complète de la règle 90/60 ;
- créneaux fixes choisis par l'utilisateur ;
- minimum de 20 minutes entre deux opportunités distinctes ;
- aucun déplacement automatique des créneaux ;
- aucun rattrapage ;
- report +10/+30 conservant la même activité ;
- report explicitement demandé autorisé pendant la pause déjeuner et après la fin habituelle du travail ;
- créneau fixe prioritaire si un report créerait un conflit <20 min ;
- `Autre activité` exclut uniquement l'activité actuellement affichée ;
- une activité montrée auparavant peut être reproposée.

## Tests

Les anciens résultats :

```text
14/14 tests réussis
120/120 opportunités simulées
```

correspondaient à l'ancienne politique 90/60.

Ils sont **historiques et non valides pour la politique actuelle**.

Statut actuel :

```text
Spécification : mise à jour
Politique JSON : mise à jour
Scénarios de test : mis à jour
Simulateur Python : À METTRE À JOUR
Tests automatisés : À RELANCER
Validation avant Kotlin : NON ENCORE ACQUISE
```

## Prochaine étape technique

Adapter le simulateur à `decision-policy-v1.0.1`, exécuter les nouveaux scénarios, corriger les écarts éventuels puis seulement déclarer le moteur prêt pour l'implémentation Kotlin.
