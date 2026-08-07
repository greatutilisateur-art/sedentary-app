# Décision V0 — séquence `Autre activité`

**Date :** 7 août 2026

Décision Produit :

```text
A → B → C
```

Dans une même interaction, une activité déjà affichée n’est plus éligible pour les tirages `Autre activité` suivants.

Cette exclusion est temporaire. Elle ne signifie pas que l’utilisateur rejette durablement l’activité et n’influence pas les poids des opportunités futures.

À la fin de l’interaction, la liste d’exclusion est réinitialisée.

Cette décision modifie le comportement du moteur et justifie le passage de `decision-policy-v1.0.4` à `decision-policy-v1.0.5`.
