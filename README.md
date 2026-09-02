# Intervalles

Intervalles est une application Android gratuite, open source et entièrement hors ligne destinée à aider à interrompre la sédentarité au travail.

Elle propose de petites activités simples, généralement de quelques minutes : marche, mobilité, étirements, mouvements dynamiques, respiration ou pauses visuelles.

**Intervalles n’est pas une application de sport.**

Le produit privilégie :

* la simplicité ;
* la faible friction ;
* la crédibilité scientifique ;
* l’autonomie de l’utilisateur ;
* l’absence de culpabilisation ;
* la fiabilité ;
* un moteur de décision explicable.

## État du projet

Version de travail actuelle : **V0.6**.

L’application est fonctionnelle et testée en conditions réelles.

La V0.6 comprend notamment :

* propositions automatiques à des horaires configurables ;
* notifications Android programmées ;
* report configurable : 15, 30, 60 minutes ou durée personnalisée ;
* une seule possibilité de report par proposition ;
* préférences d’intensité et de type de mouvement ;
* alternatives d’activités ;
* historique récent utilisé pour favoriser la diversité ;
* chrono pour les activités guidées dans le temps ;
* `Bouger maintenant` ;
* catalogue manuel ;
* mise en pause des propositions ;
* fonctionnement entièrement hors ligne ;
* 23 activités ;
* 23 illustrations fonctionnelles.

Les règles fonctionnelles détaillées de la version actuelle sont documentées dans [`docs/REFERENCE_V0_6_CURRENT.md`](docs/REFERENCE_V0_6_CURRENT.md).

## Architecture

Le projet comporte deux modules Gradle actifs :

* `:app` — application Android, interface Compose, persistance locale, notifications et planification Android ;
* `:engine` — logique métier Kotlin indépendante de l’interface Android.

Le moteur de décision reste volontairement simple et auditable.

Il repose sur :

* filtrage des activités compatibles ;
* historique récent ;
* pondération explicite ;
* sélection aléatoire pondérée ;
* règles temporelles versionnées.

Il n’utilise pas d’IA opaque pour choisir les activités.

Voir [`PROJECT_STRUCTURE.md`](PROJECT_STRUCTURE.md) pour les chemins et sources de vérité du dépôt.

## Données et confidentialité

Intervalles fonctionne localement sur l’appareil.

Le fonctionnement de base ne nécessite :

* aucun compte ;
* aucun serveur ;
* aucune authentification ;
* aucune synchronisation cloud ;
* aucune analytics distante ;
* aucune collecte de données médicales.

Le backup cloud Android est désactivé pour la version actuelle.

## Catalogue

Le catalogue actif se trouve dans :

```text
catalog/activities_v1.json
```

Son contrat et ses valeurs contrôlées sont définis dans :

```text
catalog/activity_catalog_schema_v1.json
catalog/activity_vocabularies_v1.json
```

Le catalogue V0.6 contient 23 activités.

Les illustrations Android associées sont placées dans :

```text
app/src/main/res/drawable/
```

La charte visuelle des illustrations est documentée dans :

```text
docs/Illustrations/V0_6_ILLUSTRATION_GUIDE.md
```

## Moteur et policy

La policy runtime active est :

```text
app/src/main/assets/decision_policy_v1.json
```

La spécification technique actuelle du moteur est :

```text
docs/DecisionEngineSpecification_v1_3.md
```

Les scénarios de référence sont :

```text
docs/DecisionEngine_TestScenarios_v1_3.md
```

La logique métier Kotlin se trouve principalement dans :

```text
engine/src/main/java/org/sedentaryapp/engine/
```

## Documentation

Les documents actifs sont volontairement peu nombreux :

* [`docs/Vision.md`](docs/Vision.md) — vision et positionnement ;
* [`docs/ProductPrinciples.md`](docs/ProductPrinciples.md) — principes de décision produit ;
* [`docs/Scientific_framework.md`](docs/Scientific_framework.md) — cadre scientifique et limites d’interprétation ;
* [`docs/REFERENCE_V0_6_CURRENT.md`](docs/REFERENCE_V0_6_CURRENT.md) — baseline fonctionnelle actuelle ;
* [`docs/DecisionEngineSpecification_v1_3.md`](docs/DecisionEngineSpecification_v1_3.md) — contrat technique du moteur ;
* [`docs/DecisionEngine_TestScenarios_v1_3.md`](docs/DecisionEngine_TestScenarios_v1_3.md) — scénarios moteur ;
* [`docs/Illustrations/V0_6_ILLUSTRATION_GUIDE.md`](docs/Illustrations/V0_6_ILLUSTRATION_GUIDE.md) — charte des illustrations.

Les anciennes spécifications restent disponibles dans l’historique Git mais ne constituent pas des sources de vérité actives.

## Sources de vérité

En cas de contradiction, privilégier dans cet ordre :

1. le code réellement intégré et testé ;
2. les fichiers actifs de policy et de catalogue ;
3. `docs/REFERENCE_V0_6_CURRENT.md` ;
4. les spécifications techniques actives ;
5. les principes et documents de contexte.

Une ancienne spécification ne doit pas être utilisée pour reconstruire un comportement qui n’existe plus dans le code actuel.

## Construire et tester

Le projet peut être ouvert directement dans Android Studio.

Sous Windows, depuis la racine du dépôt :

```powershell
.\gradlew.bat :engine:test :app:testDebugUnitTest :app:assembleDebug
```

Cette commande exécute les tests Kotlin concernés et construit l’APK debug.

## Principes d’évolution

Une nouvelle fonctionnalité doit répondre à un problème utilisateur identifiable.

Le projet évite notamment :

* les abstractions ajoutées « au cas où » ;
* les réglages sans utilité réelle ;
* les mécanismes de gamification culpabilisants ;
* les profils comportementaux opaques ;
* les dépendances réseau non nécessaires ;
* la duplication des sources de vérité.

Les recommandations scientifiques sont distinguées des choix de conception produit, UX et technique.

## Licence

Le projet a vocation à être distribué sous une licence open source.

La licence définitive est en cours de formalisation dans le fichier [`LICENSE`](LICENSE).
