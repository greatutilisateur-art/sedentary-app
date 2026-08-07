# Revalidation du moteur V0

**Politique :** `decision-policy-v1.0.5`  
**Sélecteur :** `weighted-selector-v1.0.0`  
**Catalogue :** `1.0.2`  
**Catalogue utilisé :** 23 activités, dont 21 standards et 2 `start_of_day`.

## Résultat synthétique

| Simulation | Propositions | Activités distinctes | Répétitions immédiates |
|---|---:|---:|---:|
| Standard — 30 jours | 120 | 21/21 | 0 |
| Assis uniquement — 30 jours | 120 | 11/11 | 3 |
| Standard — 365 jours | 1460 | 21/21 | 15 |
| Assis uniquement — 365 jours | 1460 | 11/11 | 16 |

Les répétitions immédiates ne sont pas des violations : la politique les pénalise avec `0.15` mais ne les interdit pas.

## Effet catalogue sur la sédentarité — profil standard, 365 jours

- `certain` : 822 (56.3 %)
- `probable` : 358 (24.5 %)
- `optional` : 116 (7.9 %)
- `adjacent_only` : 164 (11.2 %)

Cette distribution décrit les fiches sélectionnées. Elle ne prouve pas que l'utilisateur exécute l'activité ni qu'une rupture de sédentarité a réellement eu lieu.

## Profil `can_stand=false`, `can_walk=false`

Le filtre conserve exactement 11 activités standards compatibles.

Dans ce profil, aucune activité `certain` n'est disponible. C'est cohérent avec les contraintes : le moteur ne peut pas garantir une rupture de l'assise lorsqu'on lui indique que la personne ne peut pas se lever.

L'UX ne devra donc jamais présenter ces propositions comme des ruptures certaines de sédentarité.

## Scoring V0 simplifié

`internal_family` est conservé comme métadonnée éditoriale mais n'est plus utilisé par le scoring.

La diversité runtime repose uniquement sur :

- même activité que la précédente : `0.15` ;
- même activité dans les quatre dernières propositions : `0.55` ;
- sinon : `1.00`.

Cette règle est plus simple et correspond au comportement réellement utile du catalogue actuel.

## Ce que cette simulation valide

- la logique des créneaux fixes avec seuil de 20 minutes ;
- l'absence de règle 90/60 ;
- les reports +10/+30 ;
- le passage possible pendant le déjeuner ou après la fin du travail ;
- la priorité des créneaux fixes lorsqu'un report créerait un conflit ;
- le filtrage capacité/catégorie/intensité ;
- l'alternative qui exclut seulement l'activité courante ;
- la sélection déterministe SHA-256 ;
- la diversité probabiliste et l'absence de starvation observée sur les longues simulations.

## Ce qu'elle ne valide pas

- l'adhésion réelle ;
- l'acceptabilité des quatre créneaux ;
- la fatigue notificationnelle ;
- l'efficacité médicale ;
- les contraintes réelles d'Android ;
- la pertinence scientifique des coefficients.

## Conclusion

La nouvelle logique de planning est techniquement cohérente et nettement plus simple que la règle 90/60.

Le nettoyage des coefficients de famille est terminé. Une dernière reproduction locale des tests reste requise avant gel pour Kotlin.
