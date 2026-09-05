# Intervalles — Référence actuelle

> **Version stable : V0.7**
>
> **Prochaine cible : V1.0**
>
> Ce document décrit uniquement le comportement actuellement implémenté.
> Les évolutions prévues sont décrites dans `ROADMAP.md`.

## Positionnement

Intervalles est une application Android gratuite, open source et entièrement hors ligne destinée à aider à interrompre la sédentarité au travail.

Ce n’est pas une application de sport.

Principes produit :

- simplicité ;
- faible friction ;
- autonomie de l’utilisateur ;
- absence de culpabilisation ;
- crédibilité scientifique ;
- moteur de décision explicable.

## Planification

L’utilisateur choisit notamment :

- les jours où il souhaite recevoir des propositions ;
- les créneaux horaires ;
- une pause éventuelle dans la journée ;
- les intensités autorisées ;
- les types d’activité autorisés.

Une seule proposition automatique peut être active à la fois.

Une proposition trop ancienne n’est pas présentée au-delà de la tolérance technique prévue par la policy.

L’écart minimal entre deux opportunités distinctes est de 10 minutes.

## Pause

Suspendre les propositions met en pause les propositions automatiques sans empêcher de modifier les jours, créneaux et préférences.

L’état de pause est visible dans l’interface.

Le libellé principal utilise :

- `Suspendre les propositions d’activité` ;
- `Reprendre les propositions d’activité`.

L’indication compacte `En pause` peut être affichée avec la prochaine proposition planifiée.

## Notifications

La notification actuelle propose :

- le report `+ X min` lorsqu’il est disponible ;
- `Commencer`.

Le swipe ferme la proposition sans créer de `Dernière activité`.

Une seule notification/proposition est active à la fois.

La notification d’une proposition reportée ne propose pas de second report.

## Report

Une proposition peut être reportée une seule fois.

Réglages disponibles :

- 15 min ;
- 30 min ;
- 60 min ;
- `Autre…`, de 5 à 60 min par pas de 5 min.

Valeur par défaut : 30 min.

Le report est calculé depuis le moment où l’utilisateur appuie sur l’action.

Il reste soumis à la règle d’espacement minimal avec les autres opportunités :

- écart inférieur à 10 min : report impossible ;
- écart supérieur ou égal à 10 min : report autorisé.

La proposition reportée reste la même opportunité et conserve la même activité.

## Sélection des activités

Le moteur filtre d’abord les activités incompatibles avec les préférences et contraintes de l’utilisateur.

Pour améliorer la diversité en V0.7 :

> si au moins une activité compatible n’apparaît pas parmi les 4 dernières activités de l’historique récent, la sélection est limitée aux activités non récentes.

Si toutes les activités compatibles font déjà partie des 4 dernières, le moteur revient à l’ensemble des activités compatibles.

La sélection pondérée s’applique ensuite normalement.

Cette règle reste explicable :

> Intervalles évite les 4 dernières activités lorsqu’une autre activité compatible est disponible.

## Alternatives

Dans une même interaction, une activité déjà affichée n’est pas reproposée.

La navigation suit donc une logique du type :

A → B → C

Cette exclusion est temporaire :

- elle ne devient pas une préférence utilisateur ;
- elle ne modifie pas durablement les poids ;
- elle est réinitialisée avec une nouvelle opportunité.

## Catalogue manuel

Le catalogue est accessible depuis l’accueil et permet de choisir explicitement une activité.

Il est indépendant du planning automatique :

- il reste disponible lorsque les propositions sont suspendues ;
- il reste disponible même sans jour actif ;
- ouvrir ou réaliser une activité depuis le catalogue ne consomme pas et ne décale pas une proposition planifiée.

Le catalogue affiche les activités publiées indépendamment des préférences utilisées par le moteur automatique.

Une activité réalisée manuellement peut :

- alimenter l’historique récent utilisé pour la diversité ;
- devenir `Dernière activité`.

Elle n’est pas assimilée à une opportunité planifiée.

## Bouger maintenant

`Bouger maintenant` permet de lancer une activité en dehors du planning.

Cette action ne consomme pas et ne décale pas une proposition planifiée.

Une activité effectivement réalisée peut alimenter l’historique récent et mettre à jour `Dernière activité`.

## Fiche activité

Le catalogue distingue techniquement :

- les activités avec chrono ;
- les activités ayant une fin naturelle et ne nécessitant pas de chrono.

Pour une activité chronométrée :

- `Commencer` lance le chrono ;
- `Pause` permet de l’interrompre temporairement ;
- `Terminer` permet de la terminer avant la fin ;
- à `0:00`, le titre, l’illustration et la consigne restent visibles ;
- l’écran affiche `C’est fini !`.

Un retour pendant un chrono en cours ou en pause abandonne la session :

- l’activité n’est pas considérée comme réalisée ;
- elle ne devient pas `Dernière activité` ;
- une notification déjà prise en charge n’est pas restaurée.

`Déjà fait` est disponible lorsqu’une fiche est ouverte depuis une proposition compatible avec cette action.

Il n’est pas affiché pour une activité ouverte depuis le catalogue ou `Bouger maintenant`.

## Catalogue d’activités

La V0.7 contient 23 activités publiées.

Les 23 activités disposent d’une illustration statique.

Les illustrations sont fonctionnelles : elles doivent aider à comprendre le geste sans devenir décoratives ou sportives.

La direction graphique actuelle repose notamment sur :

- un dessin au trait simple ;
- une silhouette peu genrée ;
- peu d’éléments ;
- des flèches uniquement lorsqu’elles facilitent réellement la compréhension.

## Historique récent

L’historique actuellement utilisé par le moteur est un historique court destiné à la diversité des propositions.

Il ne constitue pas un historique statistique complet de l’utilisation.

Il ne doit pas être détourné pour produire de futures statistiques personnelles.

## Policy active

Policy :

`decision-policy-v1.0.5`

Sélecteur :

`weighted-selector-v1.1.0`

Paramètres principaux :

- créneaux par défaut : 10:00, 11:30, 15:00, 16:30 ;
- retard technique maximal : 10 min ;
- espacement minimal entre opportunités : 10 min ;
- report par défaut : 30 min ;
- bornes de report : 5–60 min ;
- historique récent : 4 activités ;
- même activité immédiatement : ×0,15 ;
- même activité dans l’historique récent : ×0,55 ;
- absence de répétition : ×1,0.

Les coefficients restent disponibles pour la sélection pondérée, notamment lorsque toutes les activités compatibles sont déjà récentes.

## Données et confidentialité

Intervalles fonctionne entièrement hors ligne.

Aucun compte utilisateur n’est nécessaire.

Les données nécessaires au fonctionnement sont conservées localement sur l’appareil.

Android Auto Backup est désactivé pour la version actuelle.

## Sources de vérité

En cas de contradiction sur le comportement actuellement implémenté, l’ordre de vérification est :

1. le code réellement intégré, compilé et testé ;
2. les assets actifs de policy et de catalogue ;
3. les spécifications techniques actives ;
4. le présent document pour la synthèse produit.

`ROADMAP.md` décrit le futur et ne constitue pas une description du comportement actuellement disponible.

L’historique Git et les tags servent d’archive des versions précédentes.
