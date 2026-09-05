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

Version stable actuelle : **V0.7**.

Prochaine cible : **V1.0**.

L’application est fonctionnelle et testée en conditions réelles.

La V0.7 comprend notamment :

* propositions automatiques à des horaires configurables ;
* notifications Android programmées ;
* report configurable : 15, 30, 60 minutes ou durée personnalisée ;
* une seule possibilité de report par proposition ;
* préférences d’intensité et de type de mouvement ;
* alternatives d’activités ;
* historique récent utilisé pour favoriser la diversité ;
* évitement des 4 dernières activités lorsqu’une autre activité compatible est disponible ;
* chrono pour les activités pratiquées pendant une durée ;
* contenu de l’activité conservé à la fin naturelle du chrono ;
* `Bouger maintenant` ;
* catalogue manuel ;
* mise en pause et reprise des propositions ;
* fonctionnement entièrement hors ligne ;
* 23 activités ;
* 23 illustrations fonctionnelles.

Le comportement actuellement implémenté est documenté dans [`docs/CURRENT_REFERENCE.md`](docs/CURRENT_REFERENCE.md).

Les évolutions prévues sont décrites dans [`docs/ROADMAP.md`](docs/ROADMAP.md).

## Architecture

Le projet comporte deux modules Gradle actifs :

* `:app` — application Android, interface Compose, persistance locale, notifications et planification Android ;
* `:engine` — logique métier Kotlin indépendante de l’interface Android.

Le moteur de décision reste volontairement simple et auditable.

Il repose sur :

* filtrage des activités compatibles ;
* historique récent ;
* règles explicites de diversité ;
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

Le catalogue actuel contient 23 activités.

Les illustrations Android associées sont placées dans :

```text
app/src/main/res/drawable/
```

La charte visuelle active des illustrations est documentée dans :

```text
docs/Illustrations/ILLUSTRATION_GUIDE.md
```

## Moteur et policy

La policy runtime active est :

```text
app/src/main/assets/decision_policy_v1.json
```

La spécification technique actuelle du moteur est :

```text
docs/DecisionEngineSpecification.md
```

Les scénarios de référence sont :

```text
docs/DecisionEngine_TestScenarios.md
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
* [`docs/CURRENT_REFERENCE.md`](docs/CURRENT_REFERENCE.md) — comportement actuellement implémenté ;
* [`docs/ROADMAP.md`](docs/ROADMAP.md) — évolutions prévues ;
* [`docs/DecisionEngineSpecification.md`](docs/DecisionEngineSpecification.md) — contrat technique du moteur ;
* [`docs/DecisionEngine_TestScenarios.md`](docs/DecisionEngine_TestScenarios.md) — scénarios moteur ;
* [`docs/Illustrations/ILLUSTRATION_GUIDE.md`](docs/Illustrations/ILLUSTRATION_GUIDE.md) — charte des illustrations.

Les anciennes spécifications restent disponibles dans l’historique Git mais ne constituent pas des sources de vérité actives.

## Sources de vérité

En cas de contradiction sur le comportement actuellement implémenté, privilégier dans cet ordre :

1. le code réellement intégré et testé ;
2. les fichiers actifs de policy et de catalogue ;
3. les spécifications techniques actives ;
4. `docs/CURRENT_REFERENCE.md` pour la synthèse produit.

`docs/ROADMAP.md` décrit le futur et ne constitue pas une description du comportement déjà disponible.

Les principes produit, la vision et le cadre scientifique guident les arbitrages mais ne remplacent pas le comportement réellement intégré.

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

Intervalles est distribué sous licence Apache License 2.0.

Voir [`LICENSE`](LICENSE).
