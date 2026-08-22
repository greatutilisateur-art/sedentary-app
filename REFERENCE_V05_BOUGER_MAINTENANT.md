# Intervalles — V0.5 : Bouger maintenant

## Décision

`Bouger maintenant` propose une activité volontaire, indépendante du planning automatique.

### Règles

- Disponible même si aucun jour de travail n'est sélectionné.
- Disponible même lorsque les propositions automatiques sont en pause.
- Respecte les réglages d'intensité et `Sur place / En mouvement`.
- Utilise le même moteur explicable que les propositions automatiques.
- N'avance, ne consomme et ne décale jamais le prochain créneau automatique.
- `Autre activité`, `Précédent` et `Suivant` fonctionnent dans l'interaction manuelle.
- Une activité manuelle abandonnée ne devient pas `Dernière activité`.
- Une activité manuelle réellement validée selon le flux existant devient `Dernière activité`.
- Une activité manuelle réalisée entre dans l'historique récent de diversité.
- L'historique manuel utilise une clé technique distincte (`manual-...`) et n'est jamais interprété comme un créneau planifié.

## Choix de conception

L'historique de diversité inclut les activités manuelles afin d'éviter, autant que possible, une répétition immédiate entre `Bouger maintenant` et la prochaine proposition automatique.

Ce comportement est un choix produit d'anti-répétition, pas une recommandation scientifique.
