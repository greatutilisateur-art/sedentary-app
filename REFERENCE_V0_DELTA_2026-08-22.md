# Intervalles — delta V0 après premiers retours testeurs

Date : 2026-08-22

## V0 confirmé

- Action principale : `Commencer` dans la notification et sur la fiche activité.
- L'action garde sa sémantique existante :
  - activité simple : `Commencer` clôt l'opportunité et alimente immédiatement `Dernière activité`;
  - activité avec chrono : `Commencer` démarre le guidage; `Terminer` ou `0:00` alimente `Dernière activité`.
- Les choix d'intensité et de type de mouvement restent tous activés par défaut.
- Pas de texte explicatif supplémentaire : l'état sélectionné doit être compris visuellement.
- Les choix sélectionnés affichent une coche et un fond plus marqué.
- `Recevoir les propositions` devient `Notifications`.
- L'écran Notifications est épuré; Son et Vibration restent deux réglages ON/OFF indépendants.
- `Debout conseillé` devient `Si tu peux, lève-toi`.
- START01 et START02 ne sont plus réservées au début de journée; elles deviennent des activités standard avec des titres courts.
- Aucun seuil caché (15/30/60 min) n'est ajouté pour déterminer si une activité future est consommée.
- `Ne plus voir` n'est pas ajouté en V0.

## Catalogue

- Réduction des formulations inutilement précautionneuses.
- Consignes de mobilité avec repères de répétition.
- Consigne mollets rendue opératoire avec appui et position du pied.
- VIS02 reformulée concrètement.
- MOUV02 reste à 2–5 minutes : l'objectif inclut une vraie petite coupure, pas seulement le trajet vers l'eau.

## V0.5 à démarrer

Priorité fonctionnelle : `Bouger maintenant`.

But :
- proposer une activité à la demande;
- respecter les réglages Intensité / Sur place / En mouvement;
- ne jamais consommer ni déplacer une opportunité automatique future;
- permettre `Autre activité` et l'historique local Précédent/Suivant;
- rester utilisable même si aucun jour de travail n'est sélectionné.
