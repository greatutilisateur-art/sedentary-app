# Intervalles — V0.5

## Bouger maintenant

- Activité volontaire indépendante du planning automatique.
- Disponible même avec 0 jour sélectionné ou lorsque les propositions automatiques sont en pause.
- Respecte Intensité / Sur place / En mouvement.
- Ne consomme ni ne déplace le prochain créneau automatique.
- Une activité manuelle réalisée participe à l'historique récent de diversité.

## Prochaine proposition d’activité

Les paramètres permettent de modifier ponctuellement la date et l'heure de la prochaine proposition automatique.

Exemple :
- planning : lundi 10:00, 11:30, 15:00, 16:30 ;
- l'utilisateur fixe la prochaine proposition à mardi 10:00.

Comportement :
- aucune proposition automatique avant mardi 10:00 ;
- proposition ponctuelle mardi 10:00 ;
- ensuite reprise du planning hebdomadaire normal à partir de l'heure courante ;
- aucun créneau suivant n'est décalé ;
- les créneaux sautés ne sont pas rejoués et ne créent pas de dette.

C'est une exception unique, pas un déplacement de toute la série.

L'utilisateur peut `Revenir au planning habituel` à tout moment avant l'échéance.

La date/heure ponctuelle est stockée comme heure locale afin de suivre la même logique que les créneaux habituels.
