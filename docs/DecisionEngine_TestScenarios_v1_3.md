# Decision Engine — scénarios de test v1.3

## A. Policy

| ID | Scénario | Attendu |
|---|---|---|
| P-01 | version `decision-policy-v1.0.5` | chargement OK |
| P-02 | 4 slots par défaut | 10:00 / 11:30 / 15:00 / 16:30 |
| P-03 | min gap = 10 | valeur runtime = 10 |
| P-04 | retard = 10 | valeur runtime = 10 |
| P-05 | report défaut/min/max | 30 / 5 / 60 |
| P-06 | coefficients famille absents | aucun champ runtime mort |

## B. Espacement

| ID | Scénario | Attendu |
|---|---|---|
| G-01 | 10:00 / 10:09 | invalide |
| G-02 | 10:00 / 10:10 | valide |
| G-03 | report 10:30 / autre opportunité 10:39 | report interdit |
| G-04 | report 10:30 / autre opportunité 10:40 | report autorisé |

## C. Report

| ID | Scénario | Attendu |
|---|---|---|
| R-01 | préférence par défaut | notification affiche `+ 30 min` |
| R-02 | préférence 15 | notification affiche `+ 15 min` |
| R-03 | préférence 60 | notification affiche `+ 60 min` |
| R-04 | Autre = 25 | notification affiche `+ 25 min` |
| R-05 | Autre <5 ou >60 via repository | refus de validation |
| R-06 | notification affichée à +15, réglage changé ensuite à 30, clic sur notification | report réel = +15 depuis le clic |
| R-07 | cible à 9 min d’un créneau | bouton report absent |
| R-08 | cible à exactement 10 min | bouton report présent |
| R-09 | conflit avec un créneau ultérieur non immédiatement prochain | bouton report absent |
| R-10 | planning modifié après affichage et conflit créé | clic revalidé, aucun report créé, reprise au prochain créneau |
| R-11 | notification issue d’un report | aucun second bouton de report |
| R-12 | report accepté | même activité conservée |
| R-13 | opportunité déjà consommée en avance proche de la cible | elle ne bloque pas le report |

## D. Retard technique

| ID | Scénario | Attendu |
|---|---|---|
| L-01 | retard 10 min | encore livrable |
| L-02 | retard >10 min | proposition périmée, pas de rattrapage |

## E. Tests UI Paramètres

- Report affiche 15 / 30 / 60 / Autre ;
- 30 sélectionné au premier lancement ;
- Autre ouvre un dialogue sans clavier ;
- `−` et `+` évoluent par pas de 5 ;
- impossible de descendre sous 5 ou monter au-dessus de 60 ;
- valeur custom persistée après fermeture/réouverture ;
- grandes polices : les choix se réorganisent sans troncature ;
- info-bulle explique la règle des 10 min et le retour au prochain créneau.
