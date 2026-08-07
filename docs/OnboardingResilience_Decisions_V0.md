# Décisions onboarding et résilience — V0

**Date :** 7 août 2026

1. L'utilisateur peut garder 1 à 4 créneaux standards actifs.
2. Seuls les créneaux actifs sont validés.
3. Aucun jour travaillé sélectionné est autorisé ; l'application ne produit alors aucune proposition.
4. Le futur catalogue consultable manuellement relève de V1.
5. Désactiver `Je peux me lever` désactive également `Je peux marcher`.
6. Réactiver `Je peux me lever` ne réactive pas automatiquement la marche.
7. Il n'existe qu'une suspension manuelle : `Suspendre` puis `Reprendre`.
8. Le prochain jour travaillé est calculé uniquement depuis les jours sélectionnés ; les congés/jours fériés ne sont pas devinés.
9. Permission restaurée, reprise manuelle, fin d'onboarding ou retour après indisponibilité : aucune opportunité passée n'est rattrapée.
10. Toute modification pouvant invalider un report (planning, catégories, intensités, lever, marcher) annule ce report.
11. Une notification déjà affichée n'est pas modifiée rétroactivement ; `Autre activité` utilise les réglages courants.
12. Les horaires sont des heures locales ; fuseau et changement d'heure recalculent uniquement le futur.
13. Le travail de nuit traversant minuit n'est pas supporté en V0.

Toutes ces règles sont des choix de conception, non des recommandations scientifiques.
