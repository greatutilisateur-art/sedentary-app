# Decision Engine Specification v1.3 — Intervalles V0.6

## 1. Sources de vérité

- catalogue : `activities_v1.json` ;
- paramètres numériques du moteur/planificateur : `decision_policy_v1.json` ;
- préférences utilisateur : DataStore local ;
- invariants structurels : code + tests.

Ne pas dupliquer une valeur métier numérique en constante Kotlin si elle existe dans la policy.

## 2. Politique active

Version : `decision-policy-v1.0.5`.

### Planification

- `technical_lateness_tolerance_minutes = 10`
- `minimum_gap_between_distinct_opportunities_minutes = 10`

Deux opportunités distinctes sont incompatibles si leur écart absolu est strictement inférieur à 10 minutes. Un écart exactement égal à 10 minutes est valide.

### Report

Policy :

- durée par défaut : 30 min ;
- minimum : 5 min ;
- maximum : 60 min.

Préférence utilisateur : une valeur entière persistée dans cette plage. L’UI V0.6 propose 15, 30, 60 et `Autre` par pas de 5 minutes.

Un report :

1. conserve l’activité de la proposition ;
2. est possible une seule fois ;
3. calcule sa cible depuis l’heure réelle du clic ;
4. n’est proposé que si cette cible respecte le gap de 10 min avec les autres opportunités pertinentes ;
5. est revalidé au clic ;
6. s’il devient invalide, n’est pas créé et le planning normal reprend ;
7. ne modifie pas le scoring.

La proposition courante est exclue du contrôle de conflit car le report la remplace.

Les opportunités consommées en avance sont exclues. Une modification ponctuelle de la prochaine proposition est prise en compte comme opportunité et les créneaux fixes qu’elle remplace ne doivent pas créer de faux conflits.

## 3. Retard

Une alarme reçue avec plus de 10 minutes de retard est périmée. Aucune dette et aucun rattrapage tardif ne sont créés.

## 4. Scoring

Fenêtre récente : 4 propositions.

- même activité que la précédente : ×0,15 ;
- même activité dans la fenêtre récente : ×0,55 ;
- sinon : ×1,0.

Clamp final : `[0,05 ; 20,0]`.

Aucun coefficient de famille n’est actif en V0.6. `internal_family` reste une métadonnée éditoriale.

## 5. Performance

`decision_policy_v1.json` est lu et parsé une fois par processus, puis mis en cache en mémoire.

Les validations runtime se limitent aux invariants de parsing essentiels. Les scénarios exhaustifs vivent dans les tests, pas au démarrage de l’application.
