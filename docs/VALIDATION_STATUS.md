# État de validation

**Mise à jour : 7 août 2026**

## Versions

- UX : `1.2`
- catalogue : `1.0.2` — inchangé
- politique : `decision-policy-v1.0.5`

## Arbitrage final intégré

`Autre activité` suit désormais une séquence de type :

```text
A → B → C
```

Toutes les activités déjà montrées pendant l’interaction courante sont exclues des tirages suivants.

Cette exclusion :

- est limitée à l’interaction courante ;
- ne devient pas une préférence persistante ;
- ne modifie pas les poids futurs ;
- est réinitialisée lors d’une nouvelle opportunité.

Quand aucune alternative compatible ne reste, aucune nouvelle activité n’est sélectionnée.

## Validation sandbox

- tests Python automatisés : **92/92 OK** ;
- simulation batch moteur : **OK** ;
- JSON Schema catalogue : **OK** ;
- compilation du port Kotlin pur : **OK** ;
- smoke-test Kotlin `A/B exclus → C seul candidat` : **OK** ;
- documents UX V1.1 : marqués **LEGACY** ;
- UX V1.2 : source de vérité courante.

## Statut

```text
UX V1.2 : COHÉRENTE
Catalogue : INCHANGÉ 1.0.2
Policy : 1.0.5
Alternative A→B→C : VALIDÉE
Tests Python : 92/92
Simulation : OK
Port Kotlin : COMPILE
Rerun dépôt local : À FAIRE
```
