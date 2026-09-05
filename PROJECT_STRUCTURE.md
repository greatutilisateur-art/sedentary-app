# Structure du projet — Intervalles

Ce document décrit uniquement la structure **active** du dépôt.

Il ne constitue pas un inventaire exhaustif de tous les fichiers. Git conserve l’historique des anciennes architectures et spécifications.

## Modules Gradle

```text
:app
:engine
```

### `:app`

Application Android.

Responsabilités principales :

* interface Jetpack Compose ;
* état et préférences locales ;
* chargement du catalogue et de la policy ;
* notifications ;
* alarmes et planification Android ;
* orchestration entre Android et le moteur métier.

### `:engine`

Moteur métier Kotlin indépendant de l’interface Android.

Responsabilités principales :

* modèles métier ;
* sélection des activités ;
* filtrage ;
* scoring ;
* diversité ;
* planification logique des opportunités ;
* validation des règles temporelles indépendantes d’Android.

---

## Arborescence principale

```text
sedentary-app/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/
│       │   │   └── decision_policy_v1.json
│       │   ├── java/org/sedentaryapp/
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   ├── notification/
│       │   │   ├── scheduling/
│       │   │   └── ui/
│       │   └── res/
│       │       ├── drawable/
│       │       ├── mipmap-*/
│       │       ├── values/
│       │       └── xml/
│       └── test/
│
├── engine/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/org/sedentaryapp/engine/
│       │   ├── decision/
│       │   ├── model/
│       │   └── schedule/
│       └── test/kotlin/org/sedentaryapp/engine/
│
├── catalog/
│   ├── activities_v1.json
│   ├── activity_catalog_schema_v1.json
│   └── activity_vocabularies_v1.json
│
├── docs/
│   ├── Vision.md
│   ├── ProductPrinciples.md
│   ├── Scientific_framework.md
│   ├── CURRENT_REFERENCE.md
│   ├── ROADMAP.md
│   ├── DecisionEngineSpecification.md
│   ├── DecisionEngine_TestScenarios.md
│   └── Illustrations/
│       └── ILLUSTRATION_GUIDE.md
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md
├── PROJECT_STRUCTURE.md
└── LICENSE
```

---

## Sources de vérité

### Comportement réellement exécuté

Le code intégré et testé est prioritaire lorsqu’un document est devenu incohérent avec l’implémentation.

### Référence fonctionnelle actuelle

```text
docs/CURRENT_REFERENCE.md
```

Décrit le comportement actuellement implémenté.

### Roadmap

```text
docs/ROADMAP.md
```

Décrit les évolutions prévues. Elle ne constitue pas une description du comportement déjà disponible.

### Policy moteur

```text
app/src/main/assets/decision_policy_v1.json
```

Source canonique des paramètres numériques runtime du moteur.

Les coefficients détaillés ne doivent pas être recopiés inutilement dans plusieurs documents.

### Catalogue

```text
catalog/activities_v1.json
```

Source canonique des activités publiées.

### Schéma du catalogue

```text
catalog/activity_catalog_schema_v1.json
```

Source canonique du contrat de données du catalogue.

### Vocabulaires

```text
catalog/activity_vocabularies_v1.json
```

Source canonique des valeurs contrôlées du catalogue.

### Modèles du moteur

```text
engine/src/main/java/org/sedentaryapp/engine/model/Models.kt
```

Il n’existe pas de seconde copie active des modèles dans `:app`.

### Spécification moteur

```text
docs/DecisionEngineSpecification.md
```

Décrit le contrat technique et les invariants importants du moteur actuel.

### Scénarios moteur

```text
docs/DecisionEngine_TestScenarios.md
```

Décrit les scénarios de référence à maintenir pour le moteur et la policy.

### Principes produit et science

```text
docs/ProductPrinciples.md
docs/Scientific_framework.md
docs/Vision.md
```

Ces documents guident les arbitrages mais ne remplacent pas la policy ou le comportement réellement intégré.

---

## Composants Android principaux

### Données

```text
app/src/main/java/org/sedentaryapp/data/
```

Contient notamment :

* état local de l’application ;
* accès au catalogue ;
* chargement de la policy.

### Planification et notifications

```text
app/src/main/java/org/sedentaryapp/scheduling/
app/src/main/java/org/sedentaryapp/notification/
```

Ces composants adaptent les règles métier aux mécanismes Android.

La logique Android ne doit pas devenir une seconde implémentation indépendante du moteur métier.

### Interface

```text
app/src/main/java/org/sedentaryapp/ui/
```

Principales zones :

* `activity/` — fiche activité et chrono ;
* `catalog/` — catalogue manuel ;
* `home/` — accueil ;
* `info/` — écrans d’information ;
* `onboarding/` — configuration initiale ;
* `schedule/` — planning et paramètres associés ;
* `theme/` — thème Compose.

---

## Moteur Kotlin

```text
engine/src/main/java/org/sedentaryapp/engine/
```

### `decision/`

Sélection des activités compatibles, règles de diversité, scoring et choix pondéré.

### `model/`

Modèles métier canoniques.

### `schedule/`

Planification logique et validation des opportunités.

L’ancien `ScheduleRules.kt` n’est plus une source active.

---

## Illustrations

Les ressources fonctionnelles des activités sont dans :

```text
app/src/main/res/drawable/ill_*.png
```

L’association activité → illustration est définie par le catalogue via `illustration_resource`.

La charte graphique active est documentée dans :

```text
docs/Illustrations/ILLUSTRATION_GUIDE.md
```

Il ne faut pas maintenir manuellement un second mapping Markdown des illustrations.

---

## Fichiers générés et locaux

Ne doivent pas être considérés comme des sources du projet :

```text
.gradle/
.kotlin/
**/build/
local.properties
```

Ils sont exclus du suivi Git.

---

## Règle de maintenance

Lorsqu’une architecture ou une règle est remplacée :

1. mettre à jour la source active concernée ;
2. mettre à jour ce document uniquement si la structure du dépôt change réellement ;
3. supprimer les documents obsolètes de l’arborescence active ;
4. utiliser l’historique Git et les tags pour retrouver les anciennes versions.

`PROJECT_STRUCTURE.md` doit rester court. Il ne doit pas devenir un dump automatique de tous les fichiers ou de leurs dates de modification.
