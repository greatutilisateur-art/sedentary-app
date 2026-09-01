# Intervalles — Référence V0.6 actuelle

## Pause

Mettre les propositions en pause suspend les propositions automatiques sans empêcher de modifier les jours, créneaux et préférences.

L’accueil et les paramètres doivent rendre explicitement visible l’état de pause.

La prochaine proposition peut afficher `En pause` tout en conservant l’information sur le planning prévu.

## Catalogue manuel

Le catalogue est accessible depuis l’accueil et permet à l’utilisateur de choisir explicitement une activité.

Il est indépendant du planning automatique :

- il reste disponible lorsque les propositions sont en pause ;
- il reste disponible même sans jour actif ;
- ouvrir ou réaliser une activité depuis le catalogue ne consomme pas et ne décale pas une proposition planifiée.

Le catalogue affiche toutes les activités publiées, indépendamment des préférences utilisées par le moteur pour les propositions automatiques.

En V0.6, il n’intègre ni recherche, ni favoris, ni filtres avancés, ni statistiques.

## Fiche activité

Deux modes existent :

- activité ponctuelle avec objectif de fin évident : action principale `Fait` ;
- activité guidée dans le temps : action principale `Commencer`, puis chrono.

Pour une activité guidée ouverte depuis une proposition active, `Déjà fait` permet de clôturer l’opportunité sans lancer le chrono.

`Déjà fait` est réservé aux contextes de proposition (`CURRENT_PROPOSAL`, `SNOOZED_PROPOSAL`, `FUTURE_PREVIEW`) et reste masqué pour `LAST_ACTIVITY` et `MANUAL_ACTIVITY`.

Le chrono est le mode majoritaire pour les activités pratiquées pendant une durée. Le mode ponctuel est réservé aux actions ayant une fin évidente.

Ce choix est un choix UX / produit issu des retours d’usage, pas une recommandation scientifique.

## Sources de vérité

En cas de contradiction, l’ordre de priorité est :

1. le code réellement intégré, compilé et testé ;
2. les fichiers actifs de policy et de catalogue ;
3. le présent document de référence V0.6 ;
4. les décisions plus récentes explicitement confirmées.

Une ancienne spécification ne doit jamais être utilisée pour reconstruire un comportement actuel absent du code.

Les valeurs techniques détaillées du moteur restent définies par `app/src/main/assets/decision_policy_v1.json` et par la spécification moteur active.

## Positionnement

Application Android gratuite, open source, entièrement hors ligne, destinée à interrompre la sédentarité au travail par de petites activités de 30 secondes à 5 minutes. Ce n’est pas une application de sport.

Principes : simplicité, faible friction, autonomie, absence de culpabilisation, moteur explicable.

## Notifications

- une seule notification/proposition active à la fois ;
- horaire précis via alarme exacte ;
- swipe = abandon de la proposition, sans créer `Dernière activité` ;
- action principale : `Commencer` ;
- report : libellé dynamique `+ X min` si le report est possible ;
- notification reportée : aucun second report.

## Report

Réglage dans `Paramètres > Notifications > Report` :

- `15 min`
- `30 min`
- `60 min`
- `Autre…` de 5 à 60 min par pas de 5

Valeur par défaut : 30 min.

Info-bulle :

> Une proposition peut être reportée une seule fois. Choisis ici la durée du report. Si ce report placerait la proposition à moins de 10 minutes d’un autre créneau prévu, le bouton de report n’est pas proposé. Dans ce cas, la prochaine proposition arrive au prochain créneau prévu.

Règle : conflit si écart `< 10 min`; écart `>= 10 min` autorisé.

## Policy active

`decision-policy-v1.0.5`

Paramètres métier numériques actifs :

- quatre créneaux par défaut : 10:00, 11:30, 15:00, 16:30 ;
- retard technique max : 10 min ;
- espacement minimal : 10 min ;
- report par défaut : 30 min ;
- bornes de report : 5–60 min ;
- historique de scoring : 4 propositions ;
- même activité immédiatement : ×0,15 ;
- même activité dans l’historique récent : ×0,55 ;
- pas de répétition : ×1,0.

Les raccourcis UI 15/30/60 et le pas de 5 min sont des choix UX, pas des options génériques du moteur.
