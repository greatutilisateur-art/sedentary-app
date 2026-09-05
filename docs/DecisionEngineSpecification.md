# Decision Engine Specification — Intervalles

**Statut :** spécification technique active.

## 1. Sources de vérité

- catalogue : `catalog/activities_v1.json` ;
- paramètres numériques du moteur/planificateur : `app/src/main/assets/decision_policy_v1.json` ;
- préférences utilisateur : DataStore local ;
- invariants structurels : code + tests.

Ne pas dupliquer une valeur métier numérique en constante Kotlin si elle existe dans la policy.

## 2. Politique active

Policy :

`decision-policy-v1.0.5`

Sélecteur :

`weighted-selector-v1.1.0`

### Planification

- `technical_lateness_tolerance_minutes = 10`
- `minimum_gap_between_distinct_opportunities_minutes = 10`

Deux opportunités distinctes sont incompatibles si leur écart absolu est strictement inférieur à 10 minutes.

Un écart exactement égal à 10 minutes est valide.

### Report

Policy :

- durée par défaut : 30 min ;
- minimum : 5 min ;
- maximum : 60 min.

Préférence utilisateur : une valeur entière persistée dans cette plage.

L’UI actuelle propose 15, 30, 60 et `Autre` par pas de 5 minutes.

Un report :

1. conserve l’activité de la proposition ;
2. est possible une seule fois ;
3. calcule sa cible depuis l’heure réelle du clic ;
4. n’est proposé que si cette cible respecte le gap de 10 min avec les autres opportunités pertinentes ;
5. est revalidé au clic ;
6. s’il devient invalide, n’est pas créé et le planning normal reprend ;
7. ne modifie pas le scoring.

La proposition courante est exclue du contrôle de conflit car le report la remplace.

Les opportunités consommées en avance sont exclues.

Une modification ponctuelle de la prochaine proposition est prise en compte comme opportunité et les créneaux fixes qu’elle remplace ne doivent pas créer de faux conflits.

## 3. Retard

Une alarme reçue avec plus de 10 minutes de retard est périmée.

Aucune dette et aucun rattrapage tardif ne sont créés.

## 4. Filtrage et diversité

Le moteur commence par filtrer les activités incompatibles.

La diversité V0.7 applique ensuite une règle d’exclusion récente :

1. prendre les `history_window_proposals` dernières activités de l’historique ;
2. identifier les candidats compatibles absents de cette fenêtre ;
3. s’il existe au moins un candidat non récent, limiter la sélection à ces candidats ;
4. sinon, conserver l’ensemble des candidats compatibles.

Avec la policy actuelle :

`history_window_proposals = 4`

La règle peut être expliquée simplement :

> Intervalles évite les 4 dernières activités lorsqu’une autre activité compatible est disponible.

Cette exclusion ne doit jamais produire artificiellement une absence d’activité lorsque tous les candidats compatibles sont récents.

## 5. Scoring

Après application de la règle de diversité, le scoring pondéré reste actif.

Fenêtre récente : 4 activités.

- même activité que la précédente : ×0,15 ;
- même activité dans la fenêtre récente : ×0,55 ;
- sinon : ×1,0.

Clamp final : `[0,05 ; 20,0]`.

Aucun coefficient de famille n’est actif.

`internal_family` reste une métadonnée éditoriale sans effet sur le scoring.

Les coefficients de répétition restent notamment utiles lorsque toutes les activités compatibles font déjà partie de la fenêtre récente.

## 6. Sélection

La sélection finale reste pondérée et déterministe pour une seed donnée.

La version du sélecteur est :

`weighted-selector-v1.1.0`

Le changement de version correspond à l’ajout de la règle d’évitement des activités récentes avant scoring.

## 7. Historique de diversité

L’historique utilisé par le moteur reste un historique court.

Il sert à :
- limiter les répétitions ;
- alimenter le scoring de diversité.

Il ne constitue pas un historique statistique exhaustif.

Un futur stockage d’événements destiné aux statistiques doit rester séparé de cet historique moteur.

## 8. Alternatives

Dans une même interaction, une activité déjà affichée peut être temporairement exclue des alternatives suivantes.

Cette exclusion :
- reste limitée à l’interaction en cours ;
- ne devient pas une préférence durable ;
- est réinitialisée lors d’une nouvelle opportunité.

## 9. Performance

`decision_policy_v1.json` est lu et parsé une fois par processus, puis mis en cache en mémoire.

Les validations runtime se limitent aux invariants de parsing essentiels.

Les scénarios exhaustifs vivent dans les tests, pas au démarrage de l’application.

## 10. Versionnement

`PolicyRepository` valide les versions de policy et de sélecteur supportées.

Lorsqu’une version déclarée dans `decision_policy_v1.json` change, la version acceptée par le code doit être mise à jour dans la même évolution.

Une incohérence entre les deux peut provoquer un échec au chargement runtime même si la compilation réussit.
