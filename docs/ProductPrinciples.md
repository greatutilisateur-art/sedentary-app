# Principes produit — V0

**Produit :** application Android hors ligne de lutte contre la sédentarité au travail  
**Portée :** V0  
**Statut :** consolidé  
**Rôle :** filtre de décision produit, UX et technique

---

## 1. Rôle de ce document

Ce document ne remplace ni la vision, ni les spécifications UX, ni la spécification du moteur.

Il sert à évaluer une nouvelle idée avant de la transformer en écran, règle métier ou code.

En cas de conflit :

1. `Vision.md` définit la direction ;
2. les spécifications définissent le comportement attendu ;
3. les fichiers JSON versionnés définissent les données et paramètres ;
4. le code applique ces décisions sans en inventer de nouvelles.

---

## 2. Simplicité avant richesse fonctionnelle

La V0 doit résoudre un problème simple :

> aider une personne à interrompre plus facilement sa sédentarité au travail.

Une fonctionnalité n’est justifiée que si elle réduit une friction réelle, par exemple :

- oublier de bouger ;
- ne pas savoir quoi faire ;
- recevoir une proposition au mauvais moment ;
- ne pas pouvoir réaliser l’activité ;
- trouver l’interaction trop longue.

Une possibilité technique ou une idée séduisante ne suffit pas.

### Risque à éviter

Ajouter trop tôt capteurs, personnalisation avancée, statistiques ou navigation secondaire peut rendre l’application plus lourde sans améliorer l’usage principal.

---

## 3. Proposer plutôt qu’imposer

L’application propose une activité. L’utilisateur garde toujours la possibilité de :

- l’accepter ;
- la reporter ;
- demander une autre activité ;
- l’abandonner ;
- suspendre les propositions ;
- modifier ses réglages.

Aucune de ces actions ne doit créer de pénalité cachée.

### Règle V0

`Pas maintenant` ne produit ni rattrapage, ni dette, ni message culpabilisant, ni préférence durable inférée.

### Risque comportemental

Interpréter un refus ponctuel comme un rejet durable serait une fausse personnalisation.

---

## 4. Aucune culpabilisation, même indirecte

La V0 exclut :

- score ;
- série quotidienne ;
- badge ;
- objectif raté ;
- comparaison ;
- félicitation automatique ;
- obligation de confirmer la fin d’une activité.

`C’est parti` signifie seulement que la proposition a été acceptée. Cela ne prouve pas que l’activité a été réalisée.

### Risque UX

Une mécanique présentée comme motivante peut devenir une pression et favoriser la désactivation de l’application.

---

## 5. Pertinence avant fréquence

Plus de notifications ne signifie pas plus d’utilité.

La V0 privilégie :

- quatre créneaux préremplis et modifiables ;
- une seule notification active ou planifiée ;
- aucun rattrapage automatique ;
- des reports limités ;
- une suspension simple.

Ces paramètres sont des choix de conception à tester, pas une fréquence scientifiquement optimale.

### Risque comportemental

Des interruptions répétées ou mal placées peuvent provoquer fatigue notificationnelle, évitement et désinstallation.

---

## 6. Simplicité à l’écran

Une activité doit être comprise en 2 à 3 secondes.

L’utilisateur a besoin de connaître :

- la durée ;
- la posture utile ;
- le titre ;
- la consigne ;
- les actions possibles.

Il n’a pas besoin de voir :

- le score ;
- le poids ;
- le niveau de preuve ;
- la trace du moteur ;
- la catégorie interne ;
- les coefficients de diversité.

### Risque UX

Afficher la logique scientifique ou algorithmique au moment de l’action augmenterait la charge mentale et pourrait médicaliser l’expérience.

---

## 7. Explicabilité du moteur

Le moteur peut être structuré, mais il doit rester reproductible et auditable.

Une décision doit pouvoir être expliquée par :

- l’opportunité temporelle ;
- les réglages ;
- les capacités déclarées ;
- les contraintes du catalogue ;
- l’historique récent ;
- les règles de diversité ;
- la graine pseudo-aléatoire.

### Règle d’architecture V0

Le moteur métier :

- est en Kotlin pur ;
- ne dépend pas d’Android ;
- ne lit pas directement l’horloge, DataStore, Room ou WorkManager ;
- ne contient aucune adaptation cachée ;
- retourne une trace explicable.

### Risque d’architecture

Une règle placée dans Compose, WorkManager ou le gestionnaire de notification deviendrait difficile à tester et à reproduire.

---

## 8. Contraintes avant préférences

Une contrainte détermine si une activité est possible.

Exemples :

- catégorie ou intensité désactivée ;
- impossibilité de se lever ;
- impossibilité de marcher ;
- durée minimale incompatible ;
- opportunité temporelle incompatible.

Une préférence influence seulement le choix entre activités déjà admissibles.

### Règle V0

Une préférence ne rend jamais possible une activité incompatible.

### Risque UX

Une activité annoncée comme personnalisée mais impossible à réaliser détériore rapidement la confiance.

---

## 9. Science et conception restent séparées

Le cadre scientifique peut soutenir :

- la réduction du temps sédentaire ;
- le remplacement d’une partie de l’assise par du mouvement ;
- l’intérêt d’activités légères ;
- l’existence de bénéfices adjacents pour certaines pauses.

Il ne valide pas automatiquement :

- les quatre créneaux ;
- les horaires par défaut ;
- le minimum produit de 20 minutes entre deux opportunités distinctes ;
- la tolérance de 15 minutes ;
- les coefficients de répétition ;
- le wording ;
- la durée exacte de chaque activité.

### Règle V0

Toute affirmation doit être identifiable comme :

- connaissance scientifique ;
- interprétation prudente ;
- décision produit ;
- décision UX ;
- décision technique.

### Risque produit

Présenter une heuristique comme prescription scientifique fragiliserait la crédibilité du projet.

---

## 10. Rupture de sédentarité et bénéfice adjacent

Le catalogue distingue :

- `certain` : l’activité impose de quitter l’assise ;
- `probable` : elle se réalise normalement debout ;
- `optional` : elle peut être réalisée assis ou debout ;
- `adjacent_only` : elle ne garantit aucune rupture de l’assise.

Une pause visuelle ou respiratoire n’est donc pas automatiquement équivalente à une marche ou à un lever de chaise.

### Risque scientifique

Additionner toutes les propositions comme si elles interrompaient l’assise produirait une interprétation trompeuse.

---

## 11. Confidentialité par conception

La V0 fonctionne entièrement sur l’appareil.

Elle ne nécessite :

- aucun compte ;
- aucun serveur ;
- aucune authentification ;
- aucune analytics distante ;
- aucune synchronisation cloud ;
- aucune collecte médicale ;
- aucune publicité ciblée.

### Risque d’architecture

Ajouter une dépendance réseau anticipée créerait maintenance, sécurité et complexité sans bénéfice V0 démontré.

---

## 12. Accessibilité obligatoire en V0

La première version doit inclure :

- TalkBack ;
- ordre de lecture logique ;
- zones tactiles d’au moins 48 dp ;
- grandes tailles de texte ;
- contraste adapté ;
- aucune information portée seulement par la couleur ;
- aucune animation indispensable ;
- durée exprimée en texte.

L’accessibilité n’est pas classée V1.

### Risque UX

Un écran correct à taille standard peut devenir inutilisable lorsque le texte est fortement agrandi.

---

## 13. Fiabilité avant sophistication

La V0 doit prioriser :

- l’absence de doublon ;
- l’idempotence ;
- la reprise après redémarrage ;
- la cohérence après modification des horaires ;
- la conservation de la même activité après report ;
- la distinction entre suspension fonctionnelle et permission Android ;
- l’annulation des occasions périmées.

### Risque d’architecture

Multiplier les planifications indépendantes augmente les risques de notifications dupliquées et d’états incohérents.

---

## 14. Une source de vérité par domaine

Les principales sources sont :

- vision : `docs/Vision.md` ;
- science : `docs/Scientific_framework.md` ;
- UX : `docs/UXSpecification_v1_1.md` ;
- moteur : `docs/DecisionEngineSpecification_v1_1.md` ;
- catalogue : `catalog/activities_v1.json` ;
- schéma : `catalog/activity_catalog_schema_v1.json` ;
- vocabulaire : `catalog/activity_vocabularies_v1.json` ;
- politique : `policy/decision_policy_v1.json`.

Les vues Markdown générées depuis le catalogue ne sont pas modifiées manuellement.

### Risque documentaire

Deux sources décrivant différemment la même règle créent une ambiguïté qui finit dans le code.

---

## 15. Pas d’abstraction sans bénéfice réel

Une couche, une interface ou une bibliothèque supplémentaire doit :

- isoler une dépendance réelle ;
- réduire une duplication existante ;
- faciliter un test important ;
- clarifier une responsabilité ;
- répondre à un besoin déjà identifié.

La V0 évite :

- les architectures à trop de modules ;
- les couches artificielles ;
- les frameworks ajoutés par habitude ;
- les modèles prévus uniquement pour une hypothétique V2.

### Risque technique

Une architecture surdimensionnée masque les règles simples et ralentit les corrections.

---

## 16. Classement des évolutions

### V0 — nécessaire

Une fonctionnalité relève de la V0 si son absence empêche :

- de configurer les propositions ;
- de recevoir une notification ;
- de comprendre l’activité ;
- de reporter ou abandonner ;
- de demander une alternative ;
- de suspendre et reprendre ;
- de garantir un fonctionnement local cohérent.

### V1 — après retours d’usage

Exemples :

- activité manuelle ;
- favoris ou masquage ;
- aperçu hebdomadaire non culpabilisant ;
- ancrages « Si… alors » ;
- plusieurs contextes de travail ;
- préférences pondérées explicites.

### V2 — expérimental

Exemples :

- calendrier ;
- détection locale de contexte ;
- adaptation automatique des horaires ;
- personnalisation avancée ;
- fading des notifications ;
- protocole de recherche.

### Règle

Une idée V1 ou V2 ne doit pas complexifier l’architecture V0 sans nécessité structurelle démontrée.

---

## 17. Grille de décision

Avant d’accepter une proposition, vérifier :

### Problème

- Quel problème utilisateur concret résout-elle ?
- Ce problème a-t-il été observé ?
- Existe-t-il une solution plus simple ?

### UX et comportement

- Ajoute-t-elle une étape ?
- Peut-elle culpabiliser ?
- Peut-elle devenir intrusive ?
- Respecte-t-elle l’autonomie ?
- Fonctionne-t-elle avec TalkBack et grande taille de texte ?

### Science

- Est-ce une recommandation scientifique ou un choix de conception ?
- La formulation surpromet-elle un effet ?

### Architecture

- La règle reste-t-elle dans le moteur pur ?
- Crée-t-elle une nouvelle source de vérité ?
- Nécessite-t-elle réellement une dépendance ?
- Peut-elle être testée hors Android ?
- Augmente-t-elle les risques de doublon ou d’état incohérent ?

### Périmètre

- Est-elle indispensable en V0 ?
- Peut-elle attendre des retours réels ?
- Son bénéfice justifie-t-il sa maintenance ?

---

## 18. Anti-modèles refusés en V0

- notifications répétées jusqu’à réponse ;
- rattrapage des occasions manquées ;
- activité incompatible proposée faute de mieux ;
- score de santé opaque ;
- préférence inférée après un seul refus ;
- changement silencieux des horaires ;
- chronomètre automatique ;
- écran obligatoire de fin ;
- historique présenté comme performance ;
- règle métier placée dans l’interface ;
- dépendance serveur inutile ;
- abstraction destinée seulement à une fonction V2.

---

## 19. Critères d’acceptation

Une décision produit est acceptable lorsqu’elle :

1. répond à un problème défini ;
2. respecte l’autonomie ;
3. n’ajoute pas de culpabilisation ;
4. reste compatible avec le hors-ligne ;
5. distingue science et conception ;
6. reste explicable ;
7. conserve le métier hors d’Android ;
8. ne crée pas de source de vérité concurrente ;
9. respecte l’accessibilité V0 ;
10. est classée V0, V1 ou V2 ;
11. retient la solution la plus simple suffisante ;
12. peut être testée avec des critères observables.

---

## 20. Gouvernance

Ce document ne doit pas être modifié pour justifier après coup une fonctionnalité déjà développée.

Toute évolution importante doit :

1. être discutée avant le code ;
2. identifier les risques UX, comportementaux et techniques ;
3. être classée V0, V1 ou V2 ;
4. mettre à jour les spécifications concernées ;
5. être versionnée dans Git.
