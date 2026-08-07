# UXSpecification_v1.1

**Produit :** application Android hors ligne de lutte contre la sédentarité au travail  
**Portée :** V0  
**Statut :** validée produit — révision 1.1.1 (planning simplifié)  
**Dépendances métier :** `DecisionEngine.md`, `ActivityCatalogSchema_v1`

---

## 1. Finalité UX

L'application doit permettre à une personne de comprendre une proposition en quelques secondes, de l'accepter, de la reporter ou de l'abandonner sans culpabilité.

L'UX ne cherche pas à maximiser le nombre d'interactions. Elle cherche à réduire la friction autour d'une interruption volontaire de la sédentarité.

### Règle centrale

> Une activité doit être comprise en 2 à 3 secondes.

### Contraintes V0

- aucune gamification ;
- aucun score ;
- aucune série quotidienne ;
- aucun chronomètre ;
- aucune félicitation automatique ;
- aucune promesse médicale ;
- aucune obligation de déclarer l'activité terminée ;
- aucune collecte serveur ;
- aucune navigation complexe.

---

## 2. Distinction entre science et conception

### Appuyé par le cadre scientifique

- limiter les périodes sédentaires prolongées ;
- remplacer l'assise par du mouvement, même léger ;
- proposer des actions réalisables ;
- éviter de présenter une fréquence produit comme un optimum scientifique.

### Choix de conception V0

- quatre créneaux préremplis et modifiables ;
- minimum de 20 minutes entre deux opportunités distinctes ;
- boutons `Dans 10 min`, `Dans 30 min`, `Pas maintenant` ;
- report autorisé pendant la pause déjeuner ou après l'heure de fin habituelle lorsqu'il est demandé explicitement ;
- écran sans chronomètre ;
- bouton `C'est parti` qui ferme l'écran ;
- même activité conservée après un report ;
- affichage du prochain créneau avec le mot `vers` ;
- suspension jusqu'au prochain jour travaillé ou jusqu'à réactivation manuelle ;
- possibilité de suspendre les propositions jusqu'au lendemain.

Ces choix sont des heuristiques produit, pas des recommandations médicales.

---

## 3. Architecture de navigation

La V0 utilise une structure courte, sans barre de navigation inférieure.

```text
Premier lancement
  └── Onboarding
       └── Accueil

Usage quotidien
  ├── Accueil
  ├── Notification
  │    └── Écran activité
  │         └── Autre activité
  └── Réglages
       ├── Jours et horaires
       ├── Créneaux
       ├── Types d'activités
       ├── Intensités
       ├── Possibilités habituelles
       ├── Début de journée
       ├── Notifications
       └── Données et à propos
```

Aucun catalogue complet n'est exposé comme écran principal en V0. Parcourir manuellement 23 activités n'aide pas directement la mission principale et ajouterait une navigation secondaire.

---

## 4. Onboarding

### 4.1 Objectif

Obtenir uniquement les informations nécessaires au moteur et à la planification, sans demander de profil sportif, médical ou comportemental.

### 4.2 Parcours

#### ONB-01 — Présentation

Contenu :

- promesse courte ;
- mention `hors ligne` ;
- mention `modifiable à tout moment`.

Action principale :

- `Configurer mes pauses`

Aucun carrousel marketing.

#### ONB-02 — Jours et horaires

Saisie :

- jours travaillés ;
- heure de début ;
- heure de fin ;
- début et fin de pause déjeuner.

Règles UX :

- valeurs préremplies modifiables ;
- validation immédiate ;
- message sous le champ, pas de boîte de dialogue ;
- la fin doit être postérieure au début ;
- la pause déjeuner doit être comprise dans la journée ou désactivée.

#### ONB-03 — Créneaux

Créneaux préremplis :

- 10 h 10 ;
- 11 h 50 ;
- 15 h 10 ;
- 17 h 30.

Chaque créneau affiche :

- son heure ;
- son état valide ou invalide ;
- une action de modification.

Un créneau invalide n'est pas déplacé silencieusement.

Deux créneaux standards actifs doivent être séparés d'au moins 20 minutes. Ce minimum est une règle de conception destinée à éviter deux propositions distinctes presque simultanées ; ce n'est pas une fréquence scientifique recommandée.

Un créneau cible exactement égal à l'heure de fin de travail est valide. La tolérance technique habituelle peut donc conduire à un affichage quelques minutes après cette heure, sauf si cet affichage tardif créerait moins de 20 minutes avant le prochain créneau fixe.

Messages possibles :

> Cet horaire est en dehors de tes heures de travail.

> Cet horaire tombe pendant ta pause déjeuner.

> Laisse au moins 20 min entre deux créneaux.

La poursuite est autorisée seulement si au moins un créneau est valide. Un créneau invalide reste signalé jusqu'à correction ; il n'est jamais déplacé automatiquement.

#### ONB-04 — Possibilités habituelles

Deux questions uniquement :

- `Je peux généralement me lever quelques instants`
- `Je peux généralement marcher quelques minutes`

Ces réponses décrivent les possibilités habituelles, pas un état médical.

Toutes les catégories et intensités restent actives par défaut.

#### ONB-05 — Notifications

La permission Android n'est demandée qu'après explication.

Texte :

> Pour afficher tes propositions aux horaires choisis, l'application a besoin de pouvoir envoyer des notifications.

Action :

- `Autoriser les notifications`

Si la permission est refusée :

- l'onboarding se termine ;
- l'accueil affiche un état clair ;
- aucune boucle insistante ;
- un bouton ouvre les réglages Android lorsque l'utilisateur le décide.

---

## 5. Accueil

### 5.1 Objectif

Répondre immédiatement à deux questions :

1. les propositions sont-elles actives ?
2. quand aura lieu la prochaine proposition approximative ?

### 5.2 Contenu

#### Barre supérieure

- titre : `Mes pauses` ;
- icône Réglages avec libellé TalkBack.

Le nom commercial de l'application reste à définir. Il ne doit pas bloquer le développement.

#### Carte d'état

États possibles :

**Actif**

> Prochaine proposition vers 15 h 10

**Report actif**

> Prochaine proposition vers 10 h 40  
> Reportée de 30 min

**Plus aucun créneau**

> Aucune autre proposition prévue aujourd'hui.

**Hors journée de travail**

> Les propositions reprendront demain vers 10 h 10.

**Suspendu jusqu'à demain**

> Les propositions sont suspendues jusqu'au prochain jour travaillé.

**Suspendu jusqu'à nouvel ordre**

> Les propositions sont en pause jusqu'à ce que tu les réactives.

**Permission refusée**

> Les notifications sont désactivées sur cet appareil.

**Configuration invalide**

> Certains créneaux doivent être ajustés.

Le mot `vers` évite de promettre une précision qu'Android ne garantit pas.

#### Actions

- `Suspendre jusqu'au prochain jour travaillé` lorsque la journée est active ;
- `Mettre en pause jusqu'à nouvel ordre` comme action secondaire ;
- `Reprendre les propositions` lorsqu'une suspension est active ;
- `Modifier mes horaires`.

La suspension jusqu'au prochain jour travaillé :

- abandonne les opportunités restantes du jour ;
- se réinitialise automatiquement au prochain jour travaillé ;
- ne modifie pas les horaires permanents ;
- ne produit aucun message culpabilisant.

La suspension jusqu'à nouvel ordre :

- persiste jusqu'à une réactivation manuelle ;
- ne modifie ni les horaires ni les préférences ;
- reste distincte de la permission Android et de l'interrupteur système des notifications ;
- convient notamment aux vacances ou à une pause volontaire d'utilisation ;
- doit pouvoir être annulée en un geste depuis l'accueil.

#### Résumé des créneaux

Afficher les quatre créneaux sous forme de lignes simples.

Exemple :

```text
10 h 10
11 h 50
15 h 10
17 h 30
```

Seul le prochain créneau est visuellement accentué. Les créneaux passés ne portent ni coche, ni score, ni notion de réussite.

---

## 6. Notification

### 6.1 Contenu

Titre :

- titre de l'activité sélectionnée.

Corps :

- durée lisible ;
- invitation à toucher pour voir la consigne.

Exemple :

```text
Une petite marche ?
2 à 5 min · Touche pour voir la consigne
```

La notification ne montre pas :

- niveau de preuve ;
- catégorie ;
- score ;
- historique ;
- bénéfice médical ;
- reproche sur le temps assis.

### 6.2 Actions rapides

Ordre lorsque les reports sont temporellement possibles :

1. `Dans 10 min`
2. `Dans 30 min`
3. `Pas maintenant`

`Pas maintenant` reste toujours disponible.

Un report peut être proposé même s'il tombe pendant la pause déjeuner ou après l'heure habituelle de fin de travail : il s'agit alors d'une décision explicite de l'utilisateur.

En revanche, un bouton de report n'est pas affiché si l'heure obtenue serait située à moins de 20 minutes d'un autre créneau fixe. Les créneaux fixes gardent la priorité et ne sont ni déplacés ni annulés à cause d'un report.

Toucher le corps de la notification ouvre l'écran activité.

### 6.3 Report

Décision V0 :

> Un report conserve exactement la même activité.

Raisons :

- le report signifie `mauvais moment`, pas `mauvais contenu` ;
- la proposition reste prévisible ;
- aucune nouvelle décision du moteur n'est nécessaire ;
- l'utilisateur peut demander `Autre activité` après ouverture.

Le report retire la notification courante et planifie la même activité. Aucune notification de confirmation supplémentaire n'est envoyée.

Un report :

- ne recalcule pas les autres créneaux ;
- peut traverser la pause déjeuner ;
- peut dépasser l'heure habituelle de fin de travail ;
- reste soumis au minimum de 20 minutes avec les autres opportunités fixes ;
- peut être reporté à nouveau si une option reste disponible.

### 6.4 Pas maintenant

- retire la notification ;
- abandonne l'occasion ;
- ne relance pas ;
- ne modifie aucune préférence ;
- ne génère aucun message de regret ou de félicitation.

### 6.5 Notification arrivée tardivement

Si la notification est encore dans la tolérance technique et ne créerait pas un conflit de moins de 20 minutes avec le prochain créneau fixe, elle s'affiche normalement.

Sinon :

- aucune notification pour cette occurrence tardive ;
- aucun rattrapage ;
- le prochain créneau fixe reste inchangé ;
- aucun message technique à l'utilisateur.

---

## 7. Écran activité

### 7.1 Structure

Ordre de lecture :

1. bouton fermer ;
2. durée ;
3. éventuel badge de posture ;
4. titre ;
5. instruction ;
6. actions.

La catégorie n'est pas affichée : elle n'aide pas à exécuter l'action.

### 7.2 Éléments

#### Durée

Formats :

- `30 s`
- `30 à 60 s`
- `1 min`
- `2 à 5 min`

#### Badge de posture

Afficher uniquement lorsqu'il apporte une information utile :

- `Debout nécessaire`
- `Debout conseillé`
- `Assis possible`

Ne pas afficher plusieurs badges simultanément.

#### Instruction

- une ou deux phrases courtes ;
- vocabulaire concret ;
- pas de paragraphe scientifique ;
- pas d'animation obligatoire ;
- l'instruction reste compréhensible sans illustration.

#### Actions

Action principale :

- `C'est parti`

Effet :

- enregistre `ACTIVITY_ACCEPTED` ;
- ferme l'écran ;
- ne démarre aucun chronomètre ;
- ne demande aucune confirmation ultérieure.

Action secondaire :

- `Autre activité`

Action tertiaire :

- `Pas maintenant`

`Pas maintenant` ferme l'écran et abandonne l'occasion sans pénalité.

### 7.3 Fermeture système

Le bouton Retour Android et le bouton fermer :

- ferment l'écran ;
- ne replanifient rien ;
- ne pénalisent pas l'activité ;
- enregistrent un événement distinct de l'acceptation, uniquement pour le diagnostic local.

L'utilisateur ne doit jamais être piégé dans l'écran.

---

## 8. Autre activité

### 8.1 Comportement

- reste dans la même opportunité ;
- exclut uniquement l'activité actuellement affichée du prochain tirage ;
- permet de reproposer une activité montrée plus tôt ;
- conserve les mêmes contraintes de capacité et de réglages ;
- ne crée aucune préférence négative durable ;
- remplace le contenu sur place ;
- utilise une transition visuelle courte et non indispensable à la compréhension.

Une séquence `A → B → A` est donc possible en V0. Ce risque de répétition est accepté pour garder une logique simple : l'application suggère, elle ne prétend pas mémoriser ce que l'utilisateur veut réellement faire.

### 8.2 Aucune alternative

Message :

> Aucune autre activité n'est disponible avec tes réglages actuels.

Actions :

- `Fermer`
- `Modifier les activités`

Aucune activité incompatible n'est proposée comme secours.

---

## 9. Réglages

### 9.1 Écran principal

Sections :

1. `Jours et horaires`
2. `Créneaux`
3. `Types d'activités`
4. `Intensités`
5. `Possibilités habituelles`
6. `Début de journée`
7. `Notifications`
8. `Données et confidentialité`
9. `À propos`

### 9.2 Types d'activités

Libellés utilisateur recommandés :

- `Marche et déplacement`
- `Mobilité`
- `Étirements`
- `Activation`
- `Bien-être`
- `Repos visuel`

Le libellé `Activation` évite la confusion entre la catégorie interne `dynamique` et l'intensité `Dynamique`.

Au moins un type doit rester actif.

Message inline :

> Garde au moins un type d'activité.

### 9.3 Intensités

Libellés :

- `Douce`
- `Active`
- `Dynamique`

Au moins une intensité doit rester active.

L'interface ne demande pas de choisir un `niveau sportif`.

### 9.4 Possibilités habituelles

- possibilité de se lever ;
- possibilité de marcher.

Les changements s'appliquent aux décisions futures. Ils ne modifient pas une notification déjà affichée.

### 9.5 Début de journée

Désactivé par défaut.

Contenu :

- interrupteur ;
- heure cible ;
- texte expliquant qu'il s'agit d'une activité de 2 minutes ;
- rappel qu'elle doit rester à au moins 20 minutes des créneaux standards.

Pas de marche proposée dans ce contexte V0.

### 9.6 Notifications et état des propositions

- état fonctionnel de l'application : actif, suspendu jusqu'au prochain jour travaillé ou suspendu jusqu'à nouvel ordre ;
- état de la permission Android ;
- bouton `Reprendre les propositions` lorsqu'elles sont suspendues ;
- bouton `Ouvrir les réglages Android` si nécessaire.

Ne pas demander d'alarme exacte.

### 9.7 Données et confidentialité

Afficher clairement :

- `Toutes les données restent sur cet appareil.`
- `Aucun compte n'est nécessaire.`
- `Aucune donnée n'est envoyée à un serveur.`

Actions :

- `Supprimer l'historique local`
- `Réinitialiser les réglages`

Une confirmation est nécessaire pour la réinitialisation complète, mais pas pour les réglages ordinaires.

### 9.8 À propos

- version de l'application ;
- lien vers le code source ;
- licences open source ;
- cadre scientifique ;
- avertissement sobre : l'application ne remplace pas un avis médical.

---

## 10. États d'erreur et cas limites

### Aucun candidat

Aucune notification n'est affichée.

Si cela apparaît dans l'accueil à cause d'une configuration trop restrictive :

> Aucune activité ne correspond actuellement à tes réglages.

Action :

- `Modifier les activités`

### Toutes les catégories désactivées

Bloquer uniquement la dernière désactivation avec un message inline. Pas de dialogue modal.

### Créneaux trop proches

Message inline dans l'édition :

> Laisse au moins 20 min entre deux créneaux.

Aucun déplacement automatique n'est effectué.

### Tous les créneaux invalides

Accueil :

> Aucun créneau valide n'est configuré.

Action :

- `Modifier mes créneaux`

### Permission Android supprimée après l'onboarding

Accueil :

> Les notifications sont désactivées sur cet appareil.

Action :

- `Ouvrir les réglages Android`

### Changement d'heure ou redémarrage

Aucun message technique. Le système recalcule les prochaines opportunités.

### Application ouverte depuis une ancienne notification

Si l'occasion est périmée :

> Cette proposition n'est plus active.

Actions :

- `Fermer`
- aucune nouvelle sélection automatique.

---

## 11. Accessibilité

Obligatoire en V0 :

- TalkBack sur toutes les actions ;
- ordre de lecture logique ;
- zones tactiles d'au moins 48 dp ;
- texte compatible avec l'agrandissement système ;
- contraste conforme à Material 3 ;
- aucune information portée seulement par la couleur ;
- aucune animation obligatoire ;
- retour haptique facultatif, jamais nécessaire ;
- durée écrite en texte, pas seulement par une icône ;
- illustrations décoratives ignorées par TalkBack ;
- illustrations informatives accompagnées d'un texte équivalent.

---

## 12. Ton éditorial

Toujours :

- tutoiement ;
- phrases courtes ;
- proposition concrète ;
- bénéfice immédiat implicite ;
- ton calme et adulte.

Jamais :

- `Tu es resté assis trop longtemps`
- `Tu dois bouger`
- `Objectif raté`
- `Bravo !`
- `Tu as brûlé X calories`
- `Protège ta santé maintenant`
- jargon biomédical dans la notification.

---

## 13. Événements UX locaux

Événements minimaux :

- `NOTIFICATION_SHOWN`
- `SNOOZED_10`
- `SNOOZED_30`
- `OPPORTUNITY_ABANDONED`
- `ACTIVITY_OPENED`
- `ALTERNATIVE_REQUESTED`
- `ACTIVITY_ACCEPTED`
- `ACTIVITY_SCREEN_DISMISSED`
- `SUSPENDED_UNTIL_NEXT_WORKDAY`
- `SUSPENDED_INDEFINITELY`
- `PROPOSALS_RESUMED`

Ces événements restent locaux. Ils servent au fonctionnement et au diagnostic, pas à une analytics distante.

---

## 14. Hors périmètre V0

- historique visible ;
- tableau de bord ;
- catalogue navigable complet ;
- recherche d'activités ;
- favoris ;
- activité manuelle `maintenant` ;
- widget Android ;
- écran de fin d'activité ;
- mesure de satisfaction après chaque activité ;
- conseils personnalisés ;
- détection de réunion ou de charge mentale ;
- photos ou vidéos obligatoires ;
- onboarding comportemental `Si… alors`.

---

## 15. V1 et V2

### V1 — uniquement après retour d'usage

- ancrages `Si… alors` choisis manuellement ;
- motif facultatif après plusieurs demandes `Autre activité` ;
- aperçu hebdomadaire non culpabilisant ;
- favoris ou masquage d'une activité ;
- activité manuelle depuis l'accueil ;
- meilleure gestion de plusieurs lieux de travail ;
- invitation facultative à remarquer un bénéfice ressenti.

### V2 — expérimental

- déclencheurs contextuels automatiques ;
- adaptation locale des horaires ;
- fading des notifications ;
- personnalisation avancée ;
- illustrations ou animations contextuelles ;
- intégration calendrier volontaire.

---

## 16. Critères d'acceptation UX

La V0 est acceptable si :

1. l'onboarding peut être terminé sans information médicale ;
2. une notification se comprend sans ouvrir l'application ;
3. une activité se comprend en 2 à 3 secondes après ouverture ;
4. reporter ou abandonner demande un seul geste ;
5. aucun écran n'exige une déclaration de réussite ;
6. aucune action ne crée de dette de notifications ;
7. l'utilisateur peut suspendre les propositions jusqu'au prochain jour travaillé ou jusqu'à réactivation manuelle, sans modifier ses réglages permanents ;
8. une configuration invalide est expliquée près du champ concerné ;
9. l'application reste utilisable avec TalkBack et grande taille de texte ;
10. aucun choix de design n'est présenté comme une recommandation scientifique.
