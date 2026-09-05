# Decision Engine — Scénarios de test

**Statut :** scénarios de test actifs du moteur.

## A. Policy

| ID | Scénario | Attendu |
|---|---|---|
| P-01 | version `decision-policy-v1.0.5` | chargement OK |
| P-02 | version sélecteur `weighted-selector-v1.1.0` | chargement OK |
| P-03 | 4 slots par défaut | 10:00 / 11:30 / 15:00 / 16:30 |
| P-04 | min gap = 10 | valeur runtime = 10 |
| P-05 | retard = 10 | valeur runtime = 10 |
| P-06 | report défaut/min/max | 30 / 5 / 60 |
| P-07 | coefficients famille absents | aucun champ runtime mort |
| P-08 | version policy non supportée | refus au chargement |
| P-09 | version sélecteur non supportée | refus au chargement |

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

## E. Diversité

| ID | Scénario | Attendu |
|---|---|---|
| D-01 | activité A récente, activité B compatible non récente disponible | A exclue de la sélection, B reste disponible |
| D-02 | toutes les activités compatibles sont dans la fenêtre récente | aucun candidat n’est supprimé définitivement ; sélection possible |
| D-03 | historique récent vide | sélection normale sur les candidats compatibles |
| D-04 | activité présente hors des 4 dernières entrées seulement | elle n’est pas considérée récente par la règle V0.7 |
| D-05 | `internal_family` identique ou différente | aucun effet de scoring |
| D-06 | même seed et mêmes entrées | sélection déterministe |

Les tests automatisés doivent notamment couvrir :
- `recentActivityIsAvoidedWhenAlternativeExists()` ;
- `recentActivitiesRemainAvailableWhenNoAlternativeExists()`.

## F. Tests UI Paramètres

- Report affiche 15 / 30 / 60 / Autre ;
- 30 sélectionné au premier lancement ;
- Autre ouvre un dialogue sans clavier ;
- `−` et `+` évoluent par pas de 5 ;
- impossible de descendre sous 5 ou monter au-dessus de 60 ;
- valeur custom persistée après fermeture/réouverture ;
- grandes polices : les choix se réorganisent sans troncature ;
- info-bulle explique la règle des 10 min et le retour au prochain créneau.

Les aides supplémentaires prévues pour `Jours` et `Créneaux` appartiennent à la V1.0 et ne sont pas des invariants V0.7.
