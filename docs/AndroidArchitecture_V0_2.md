# AndroidArchitecture_V0.2

**Date :** 7 août 2026  
**Statut :** source d'architecture Android courante avant premier build

---

## 1. Architecture

Deux modules Gradle uniquement :

```text
:app
:engine
```

### `:engine`

Kotlin/JVM pur :

- modèles métier ;
- OpportunityPolicy ;
- CandidateFilter ;
- CandidateScorer ;
- WeightedSelector ;
- règles de créneaux/reports ;
- NextOpportunityPlanner ;
- tests JVM.

Aucune dépendance Android.

### `:app`

- Compose ;
- ViewModels / StateFlow ;
- Preferences DataStore ;
- AlarmManager ;
- notifications ;
- BroadcastReceivers ;
- repositories locaux ;
- navigation.

---

## 2. Toolchain de départ

```text
minSdk     26
compileSdk 37
targetSdk  36
JDK        17
```

Le choix exact des versions AGP/Kotlin/Compose est figé au moment de générer le projet Android et doit rester cohérent avec Android Studio installé.

---

## 3. Ce qui n'est pas utilisé en V0

- Room ;
- Hilt/Koin ;
- WorkManager comme scheduler principal ;
- foreground service ;
- exact alarms ;
- permission `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` ;
- serveur ;
- permission `INTERNET` ;
- analytics ;
- Health Connect.

---

## 4. Planification

La V0 conserve **une seule prochaine alarme logique**.

```text
NextOpportunityPlanner (pur)
        ↓
ScheduleCoordinator.reconcile()
        ↓
AlarmManager inexact
```

Première implémentation Android : alarme inexacte utilisable en idle, sans permission d'alarme exacte.

Au déclenchement :

- revalider l'opportunité ;
- abandonner si retard > 15 min ;
- ne jamais rattraper ;
- programmer ensuite la prochaine opportunité.

---

## 5. Persistance DataStore

Un seul Preferences DataStore, séparé logiquement en :

```text
SettingsRepository
RuntimeStateRepository
```

### Réglages

- onboardingCompleted ;
- jours travaillés ;
- horaires ;
- pause déjeuner ;
- 1 à 4 créneaux actifs ;
- catégories ;
- intensités ;
- canStand ;
- canWalk ;
- suspended ;
- startOfDayEnabled.

### État runtime moteur

- créneaux traités du jour ;
- quatre derniers IDs proposés pour le scoring ;
- report actif ;
- identifiant d'opportunité courant si nécessaire à l'idempotence.

### État runtime de présentation

Ajouter seulement :

```text
lastProposedActivityId: String?
lastProposedOpportunityId: String?
```

Aucune nouvelle table et aucune base Room.

Cet état sert à rouvrir une consigne depuis l'accueil après Retour, swipe, verrouillage ou recréation du processus.

Il ne bloque jamais la planification.

---

## 6. Transitions de `Dernière activité proposée`

```text
nouvelle proposition
→ lastProposed = nouvelle activité/opportunité

Autre activité
→ activityId remplacé
→ opportunityId identique

report +10/+30
→ inchangé

Retour / swipe / background / lock
→ inchangé

Pas maintenant
→ effacer seulement si opportunityId correspond encore
```

La comparaison d'opportunité évite qu'un ancien écran n'efface une proposition plus récente.

---

## 7. Écran activité et cycle de vie Compose

La sélection du moteur a lieu **avant** l'affichage.

L'écran reçoit ou recharge un `activityId` déjà choisi.

Une recomposition, rotation, restauration Compose ou recréation de ViewModel :

```text
→ recharge l'activité par ID
→ ne rappelle jamais WeightedSelector
```

Le process death est géré grâce à l'état persistant minimal.

---

## 8. Navigation

Une seule `MainActivity`.

Navigation Compose.

Notification → écran activité via identifiants d'activité/opportunité.

Retour Android → accueil, sans événement métier.

Pas de bouton fermer spécifique.

---

## 9. Actions écran activité

V0 :

```text
Autre activité
Pas maintenant
```

Aucune action d'acceptation explicite.

Aucun événement d'acceptation explicite.

Aucun timer.

Aucune preuve de réalisation.

---

## 10. `Autre activité`

L'action appelle le moteur avec :

- même opportunité ;
- activityId courant exclu ;
- réglages courants ;
- nouvelle graine/tentative reproductible.

La règle moteur actuelle reste : **seule l'activité courante est exclue**. Une activité montrée plus tôt peut revenir.

L'alternative devient `lastProposedActivityId`.

---

## 11. `Pas maintenant`

Action explicite :

- abandonner l'opportunité ;
- annuler notification/report associé ;
- effacer `lastProposed*` uniquement si l'ID d'opportunité correspond ;
- `reconcile()` ;
- revenir à l'accueil.

---

## 12. Notification swipe / Retour / téléphone verrouillé

Aucun de ces événements n'est un signal métier.

Ne pas créer de journal événementiel persistant en V0 uniquement pour les observer.

Les logs debug techniques sont suffisants si nécessaire.

---

## 13. Notification unique

Un ID Android fixe pour la notification d'activité.

Une nouvelle proposition remplace naturellement l'ancienne notification.

La nouvelle proposition remplace aussi `lastProposed*`.

---

## 14. Repositories

```text
SettingsRepository
RuntimeStateRepository
CatalogRepository
```

Pas de nouvelle couche `UseCase` pour `Dernière activité proposée`.

Le ViewModel Home lit simplement l'état runtime et le catalogue pour résoudre le titre.

---

## 15. Package structure

```text
:engine
  model/
  decision/
  schedule/

:app
  ui/onboarding/
  ui/home/
  ui/activity/
  ui/settings/
  data/
  scheduling/
  notifications/
  di/AppContainer.kt
```

---

## 16. Tests

### Engine JVM

Conserver les tests existants de sélection/planning.

### État UX pur

Tester séparément :

- persistance dernière activité ;
- Retour neutre ;
- swipe neutre ;
- alternative remplace la dernière activité ;
- `Pas maintenant` efface la bonne opportunité ;
- ancienne activité affichée ne peut pas effacer une proposition plus récente.

### Instrumentation plus tard

- notification → écran ;
- process recreation ;
- verrouillage/déverrouillage ;
- back navigation ;
- grandes tailles de texte.

---

## 17. V0 / V1 / V2

### V0

Architecture ci-dessus.

### V1

- catalogue manuel ;
- contexte télétravail ;
- éventuel masquage/cooldown seulement si retours réels.

### V2

Aucune architecture anticipée sans besoin démontré.


## État minimal pour `Autre activité`

La règle `A → B → C` nécessite un petit état temporaire :

```text
shownActivityIdsInInteraction
```

Il est conservé uniquement pendant l’interaction courante, afin de pouvoir restaurer correctement l’écran après recréation Android.

Il ne s’agit ni d’un historique durable, ni d’une préférence, ni d’une raison d’ajouter Room ou une nouvelle couche d’architecture.
