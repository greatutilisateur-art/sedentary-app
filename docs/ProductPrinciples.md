# Principes produit — Intervalles

**Produit :** application Android hors ligne de lutte contre la sédentarité au travail
**Rôle :** filtre de décision produit, UX et technique
**Statut :** principes durables — le comportement actuellement implémenté est décrit dans `CURRENT_REFERENCE.md`

---

## 1. Objectif

Intervalles cherche à aider une personne à interrompre plus facilement sa sédentarité au travail grâce à de petites activités simples et réalisables.

Ce n’est pas une application de sport.

Le produit privilégie :

* la simplicité ;
* la faible friction ;
* la crédibilité scientifique ;
* l’autonomie ;
* l’absence de culpabilisation ;
* la fiabilité ;
* l’explicabilité.

Une fonctionnalité n’est pas justifiée parce qu’elle est techniquement possible. Elle doit répondre à un problème utilisateur identifié.

---

## 2. Proposer plutôt qu’imposer

Intervalles propose une activité. L’utilisateur reste libre de :

* la réaliser ;
* la reporter lorsque le report est disponible ;
* demander une alternative ;
* l’abandonner ;
* suspendre les propositions automatiques ;
* utiliser volontairement `Bouger maintenant` ou le catalogue.

Un refus, un swipe ou une absence d’interaction ne doit pas produire de pénalité cachée, de dette ou de culpabilisation.

Une interaction ponctuelle ne doit pas devenir automatiquement une préférence durable.

---

## 3. Aucune gamification culpabilisante

Intervalles n’a pas vocation à créer une obligation de performance.

Le produit évite notamment :

* les séries quotidiennes ;
* les objectifs ratés ;
* les scores de santé ;
* les classements ;
* les récompenses conditionnées à une performance ;
* les messages culpabilisants.

Un futur écran de statistiques peut décrire l’utilisation d’Intervalles, mais ne doit pas noter l’utilisateur ni transformer les données d’usage en jugement.

---

## 4. Pertinence avant fréquence

Davantage de notifications ne signifie pas davantage d’utilité.

Les propositions doivent rester :

* prévisibles ;
* suffisamment espacées ;
* peu intrusives ;
* faciles à ignorer ou reporter ;
* compatibles avec le rythme de travail réel.

Les horaires, fréquences, durées et règles de report sont des **choix de conception à tester**, et non des prescriptions scientifiques universelles.

Leurs valeurs actuelles sont documentées dans `CURRENT_REFERENCE.md` et dans la policy active.

---

## 5. Simplicité à l’écran

Une activité doit être comprise rapidement.

L’interface peut montrer ce qui est utile à l’action :

* le titre ;
* la durée lorsqu’elle apporte une information utile ;
* la consigne ;
* l’illustration lorsqu’elle aide à comprendre ;
* les actions disponibles.

Elle n’a pas besoin d’exposer :

* les coefficients du moteur ;
* les poids ;
* les catégories techniques ;
* la trace algorithmique ;
* un niveau de preuve scientifique détaillé pendant l’activité.

Les catégories internes ne doivent pas devenir des libellés visibles si elles ne sont pas comprises ou utiles aux personnes.

La science doit guider le produit sans médicaliser l’expérience.

---

## 6. Explicabilité du moteur

Le moteur peut utiliser de la pondération et de l’aléatoire, mais son fonctionnement doit rester compréhensible et auditable.

Une proposition doit pouvoir être expliquée à partir de facteurs identifiables, notamment :

* l’opportunité temporelle ;
* les préférences actives ;
* les contraintes de l’activité ;
* les exclusions temporaires ;
* l’historique récent ;
* les règles de diversité ;
* la policy versionnée.

Pas d’adaptation opaque ni de profil caché.

La V0.7 évite les 4 dernières activités lorsqu’une autre activité compatible est disponible.

Les coefficients de répétition restent utilisés par la sélection pondérée, notamment lorsque toutes les activités compatibles sont déjà récentes.

---

## 7. Contraintes et préférences simples

Les réglages ne doivent exister que s’ils correspondent à un choix réellement proposé à l’utilisateur ou à une contrainte réellement utilisée par le produit.

Intervalles évite les capacités, filtres ou paramètres backend conservés uniquement « au cas où ».

Une fausse configurabilité augmente :

* le risque de bugs ;
* la difficulté des tests ;
* l’ambiguïté documentaire ;
* la complexité du moteur.

Lorsqu’un concept n’est pas utilisé, il vaut mieux le supprimer que le maintenir artificiellement.

---

## 8. Science et conception restent séparées

Le cadre scientifique soutient le principe général consistant à réduire le temps sédentaire et à remplacer une partie du temps assis par du mouvement, y compris léger.

Il ne définit pas automatiquement :

* les horaires des propositions ;
* leur fréquence exacte ;
* leur durée exacte ;
* les coefficients du moteur ;
* les textes de l’interface ;
* les couleurs ;
* la navigation ;
* l’architecture Android.

Chaque décision importante doit pouvoir être identifiée comme :

* connaissance scientifique ;
* interprétation prudente ;
* décision produit ;
* décision UX ;
* décision technique.

Le détail et les limites des connaissances utilisées par le projet sont documentés dans `Scientific_framework.md`.

---

## 9. Toutes les activités ne sont pas équivalentes

Une marche ou une activité qui impose de quitter l’assise n’a pas le même effet sur la sédentarité qu’une respiration ou une pause visuelle pouvant être réalisée assis.

Intervalles peut proposer des activités apportant un bénéfice adjacent, mais ne doit pas les présenter automatiquement comme des ruptures certaines de sédentarité.

Lors de l’enrichissement du catalogue, les activités directes, les prétextes naturels et les activités combinées doivent être distingués des activités seulement adjacentes.

Le produit ne doit pas fabriquer une mesure trompeuse en additionnant toutes les activités comme si elles avaient le même effet physiologique.

---

## 10. Confidentialité par conception

Intervalles fonctionne entièrement sur l’appareil.

Le fonctionnement de base ne nécessite :

* aucun compte ;
* aucun serveur ;
* aucune authentification ;
* aucune analytics distante ;
* aucune synchronisation cloud ;
* aucune collecte médicale.

Une dépendance réseau ne doit pas être ajoutée sans bénéfice produit démontré.

Les futures statistiques personnelles restent locales.

---

## 11. Accessibilité dès la version de base

L’accessibilité n’est pas une fonctionnalité à repousser automatiquement à une version ultérieure.

L’application doit notamment rester utilisable avec :

* TalkBack ;
* grandes tailles de texte ;
* cibles tactiles suffisantes ;
* contraste adapté ;
* information qui ne dépend pas uniquement de la couleur.

Une évolution UX doit être évaluée aussi dans ces conditions.

---

## 12. Fiabilité avant sophistication

Une fonctionnalité simple mais fiable est préférable à une fonctionnalité plus intelligente qui introduit des états incohérents.

Le produit doit notamment privilégier :

* l’absence de notifications dupliquées ;
* la cohérence après modification des réglages ;
* le comportement prévisible des reports ;
* la reprise correcte après redémarrage ;
* l’absence de rattrapage inattendu ;
* la cohérence entre l’état affiché et l’état réellement planifié.

Le système de notifications et de planification ne doit pas être complexifié sans problème observé.

---

## 13. Une source de vérité par domaine

En cas de contradiction, privilégier les sources actives plutôt que les anciennes spécifications disponibles dans l’historique Git.

Sources principales :

* direction produit : `docs/Vision.md` ;
* principes produit : `docs/ProductPrinciples.md` ;
* cadre scientifique : `docs/Scientific_framework.md` ;
* comportement actuellement implémenté : `docs/CURRENT_REFERENCE.md` ;
* évolutions prévues : `docs/ROADMAP.md` ;
* contrat technique du moteur : `docs/DecisionEngineSpecification.md` ;
* scénarios moteur : `docs/DecisionEngine_TestScenarios.md` ;
* catalogue : `catalog/activities_v1.json` ;
* schéma : `catalog/activity_catalog_schema_v1.json` ;
* vocabulaire : `catalog/activity_vocabularies_v1.json` ;
* policy active : `app/src/main/assets/decision_policy_v1.json` ;
* structure du dépôt : `PROJECT_STRUCTURE.md`.

Le code réellement intégré et testé reste prioritaire lorsqu’un document ne correspond plus à son comportement.

Git conserve l’historique : les anciennes spécifications n’ont pas besoin de rester dans l’arborescence active.

---

## 14. Pas d’abstraction sans bénéfice réel

Une nouvelle couche, abstraction ou dépendance doit au moins :

* isoler une dépendance réelle ;
* supprimer une duplication existante ;
* faciliter un test important ;
* clarifier une responsabilité ;
* répondre à un besoin déjà identifié.

Intervalles évite les architectures conçues pour des fonctionnalités hypothétiques.

Le moteur métier reste séparé de l’interface Android sans multiplier artificiellement les modules.

---

## 15. Classement des évolutions

### V0.7 — version stable actuelle

La V0.7 stabilise l’expérience issue des premiers tests terrain.

Elle comprend notamment :

* amélioration de l’état final du chrono ;
* wording homogénéisé autour de Suspendre / Reprendre ;
* révision de certaines consignes ;
* renforcement de la diversité en évitant les 4 dernières activités lorsqu’une alternative compatible existe.

La V0.7 ne reçoit plus de nouvelle fonctionnalité, sauf correction nécessaire à sa stabilité.

### V1.0 — enrichissement issu des usages

Le périmètre prévu est documenté dans `ROADMAP.md`.

Il comprend notamment :

* statistiques personnelles locales ;
* historique local d’événements nécessaire aux statistiques ;
* action `Déjà fait` dans la notification ;
* meilleure compréhension des jours et créneaux dans les Paramètres ;
* simplification des catégories visibles des activités ;
* harmonisation des titres et consignes ;
* réduction des objectifs en nombre de répétitions ;
* enrichissement méthodique du catalogue.

### V1.x — candidats après validation de V1.0

Exemples :

* durées davantage adaptées aux activités ;
* mini-séquences ;
* sessions guidées volontaires ;
* respiration guidée plus longue ;
* possibilité de masquer certaines activités ;
* contenus scientifiques courts et sourcés.

### V2 — expérimental

Exemples :

* adaptation automatique avancée ;
* personnalisation plus complexe ;
* détection locale de contexte ;
* statistiques avancées ;
* fonctions nécessitant une architecture significativement plus riche.

Une idée V1.x ou V2 ne doit pas complexifier la version actuelle sans bénéfice démontré.

---

## 16. Grille de décision

Avant d’accepter une nouvelle proposition :

### Problème

* Quel problème utilisateur concret résout-elle ?
* A-t-il été observé ?
* Existe-t-il une solution plus simple ?

### UX et comportement

* Ajoute-t-elle de la friction ?
* Peut-elle devenir intrusive ?
* Peut-elle culpabiliser ?
* Respecte-t-elle l’autonomie ?
* Reste-t-elle accessible ?

### Science

* Est-ce une recommandation scientifique ou un choix de conception ?
* La formulation promet-elle davantage que les preuves disponibles ?

### Architecture

* Crée-t-elle une nouvelle source de vérité ?
* Nécessite-t-elle réellement une nouvelle abstraction ou dépendance ?
* La décision reste-t-elle explicable et testable ?
* Augmente-t-elle les risques d’état incohérent ?

### Périmètre

* Est-elle nécessaire maintenant ?
* Peut-elle attendre davantage de retours terrain ?
* Son bénéfice justifie-t-il sa maintenance ?

---

## 17. Gouvernance

Une évolution importante doit :

1. partir d’un problème ou d’un objectif explicite ;
2. distinguer recommandation scientifique et décision de conception ;
3. examiner les risques UX, comportementaux et techniques ;
4. être classée V0.7, V1.0, V1.x ou V2 lorsque pertinent ;
5. privilégier la solution la plus simple suffisante ;
6. mettre à jour les sources actives concernées ;
7. être versionnée dans Git.

La documentation ne doit pas être modifiée après coup pour justifier une complexité déjà introduite.
