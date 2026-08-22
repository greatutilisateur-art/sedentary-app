# Intervalles — décisions UX V0.5 après audit

## Invariants confirmés

### Jours

L'affichage des jours est toujours :

`L M M J V S D`

Un jour sélectionné conserve toujours sa lettre.
Aucune coche ne remplace ou n'accompagne la lettre dans ce composant.

### Bouger maintenant

Conserver :
- icône;
- placement;
- fond;
- forme.

Seule la typographie du libellé utilise le même TextStyle que les titres d'activités (`titleLarge`).

### Prochaine proposition d'activité

Le dialogue d'édition doit expliciter qu'il s'agit d'une exception portant uniquement sur la prochaine occurrence.
La modification ne décale jamais les créneaux récurrents suivants.

### Accessibilité

- Intensité / Type : pas de troncature à 200 %; retour à la ligne autorisé.
- Jours : libellé court visuel + nom complet TalkBack.
- Boutons `−` : cible tactile ≥ 48 × 48 dp.

## Règle de gouvernance UX

Ne pas modifier ces choix visuels sans les présenter explicitement au propriétaire produit avant implémentation.
Pas d'autre redesign en V0.5 sans bug, problème d'accessibilité ou RETEX utilisateur.
