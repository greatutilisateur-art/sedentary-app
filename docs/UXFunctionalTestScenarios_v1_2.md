# UXFunctionalTestScenarios_v1.2

**Portée :** état de présentation et navigation V0.  
**Ne remplace pas** les tests du moteur pur.

| ID | Scénario | Résultat attendu |
|---|---|---|
| UX-ACT-01 | Ouvrir une activité | aucune action de confirmation n'est nécessaire |
| UX-ACT-02 | Ouvrir → lire → Retour → accueil → dernière activité | exactement la même activité peut être rouverte |
| UX-ACT-03 | verrouillage/recréation | même activité restaurée, aucune nouvelle sélection |
| UX-ACT-04 | `Autre activité` | alternative affichée et devient la dernière activité proposée |
| UX-ACT-05 | `Pas maintenant` | opportunité abandonnée et sa mise en avant est retirée |
| UX-ACT-06 | swipe notification | aucune préférence ni pénalité implicite |
| UX-ACT-07 | swipe notification | dernière activité reste consultable |
| UX-ACT-08 | nouvelle proposition distincte | remplace la dernière activité précédente |
| UX-ACT-09 | report +10/+30 | même activité et même opportunité conservées |
| UX-ACT-10 | dernière activité consultable | ne bloque pas le prochain créneau |
| UX-ACT-11 | événement passif | aucune réalisation, acceptation ou préférence n'est dérivée |
| UX-ACT-12 | `Pas maintenant` depuis un écran ancien après nouvelle proposition | ne supprime pas la proposition plus récente de l'accueil |

## Alternative V0

La règle actuelle du moteur reste : seule l'activité **actuellement affichée** est exclue lors d'un nouveau tirage.

`A → B → C` : une activité déjà montrée pendant l’interaction ne revient pas.


| UX-ACT-13 | A puis `Autre activité` → B puis `Autre activité` | la sélection suivante ne peut être ni A ni B |
| UX-ACT-14 | toutes les activités compatibles ont été montrées | aucune alternative supplémentaire |
| UX-ACT-15 | nouvelle opportunité distincte | les exclusions temporaires de l’interaction précédente sont réinitialisées |
