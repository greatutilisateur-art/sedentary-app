# UXSpecification_v1.2

**Produit :** application Android hors ligne de lutte contre la sédentarité au travail  
**Portée :** V0  
**Statut :** validée produit — révision UX majeure avant implémentation Android  
**Dépendances métier :** `DecisionEngine.md`, `ActivityCatalogSchema_v1.md`  
**Remplace :** `UXSpecification_v1_1.md`

---

## 1. Finalité UX

L'application doit permettre de comprendre une proposition en quelques secondes, de consulter sa consigne, de la reporter, de demander une alternative ou de l'abandonner explicitement sans culpabilité.

L'UX ne cherche pas à maximiser le nombre d'interactions.

### Règle centrale

> Une activité doit être comprise en 2 à 3 secondes.

### V0 ne cherche pas à savoir si l'activité a été réalisée

Une activité peut être faite directement après lecture de la notification, sans ouvrir l'application.

Par conséquent :

- notification ouverte ≠ activité réalisée ;
- notification ignorée ≠ activité non réalisée ;
- écran activité consulté ≠ réussite ;
- aucun bouton de confirmation de démarrage ou de fin n'est nécessaire.

---

## 2. Contraintes V0

- aucune gamification ;
- aucun score ;
- aucune série quotidienne ;
- aucun chronomètre ;
- aucune félicitation automatique ;
- aucune promesse médicale ;
- aucune obligation de déclarer une activité terminée ;
- aucune collecte serveur ;
- aucune navigation complexe ;
- aucune inférence comportementale issue d'une non-action.

---

## 3. Choix de conception V0

Ces choix sont des heuristiques produit, pas des recommandations scientifiques :

- quatre créneaux préremplis : `10:10`, `11:50`, `15:10`, `17:00` ;
- liberté d'en conserver 1 à 4 actifs ;
- zéro jour travaillé sélectionné est autorisé ;
- minimum de 20 minutes entre deux opportunités distinctes ;
- reports `Dans 10 min` et `Dans 30 min` depuis la notification uniquement ;
- un report conserve exactement la même activité ;
- report possible après la pause déjeuner ou après l'heure habituelle de fin, mais jamais au-delà de minuit ;
- suspension manuelle jusqu'à `Reprendre` ;
- écran activité sans action de confirmation ;
- mémorisation de présentation `Dernière activité proposée` sur l'accueil.

---

## 4. Navigation V0

```text
Premier lancement
  └── Onboarding
       └── Accueil

Usage quotidien
  ├── Accueil
  │    └── Dernière activité proposée → Écran activité
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

Pas de catalogue manuel en V0.

---

# A. ONBOARDING

## 5. Présentation

Titre de travail :

> Des pauses courtes pour bouger un peu au travail

Action :

- `Configurer mes pauses`

Aucun carrousel marketing.

---

## 6. Jours et horaires

Saisie :

- jours travaillés ;
- début de journée ;
- fin de journée ;
- pause déjeuner facultative.

Règles :

- aucun jour travaillé sélectionné est valide ;
- la fin doit être postérieure au début dans le même jour civil ;
- le travail traversant minuit n'est pas supporté en V0 ;
- aucun congé ou jour férié n'est deviné.

Si aucun jour n'est sélectionné :

> Aucune proposition automatique n'est prévue.

---

## 7. Créneaux

Préremplis :

- 10 h 10 ;
- 11 h 50 ;
- 15 h 10 ;
- 17 h 00.

Chaque créneau possède :

- actif/inactif ;
- heure ;
- validation inline lorsqu'il est actif.

L'utilisateur garde 1 à 4 créneaux actifs.

Un créneau actif doit :

- appartenir à la plage de travail ;
- ne pas tomber pendant la pause déjeuner ;
- être séparé d'au moins 20 minutes des autres créneaux standards actifs.

Les créneaux inactifs sont ignorés.

Aucun créneau n'est déplacé automatiquement.

---

## 8. Possibilités habituelles

Options :

- `Je peux généralement me lever quelques instants`
- `Je peux généralement marcher quelques minutes`

Si la possibilité de se lever est désactivée :

- la marche est désactivée ;
- la marche reste indisponible tant que se lever reste désactivé.

Réactiver la possibilité de se lever ne réactive pas automatiquement la marche.

Ces options décrivent des possibilités habituelles, pas un état médical.

---

## 9. Permission notifications

Demander `POST_NOTIFICATIONS` uniquement lorsqu'au moins une proposition automatique peut réellement être planifiée.

Si aucun jour travaillé n'est sélectionné :

- ne pas demander la permission ;
- terminer normalement l'onboarding.

Si un premier jour est activé plus tard, demander la permission à ce moment-là.

Aucun rattrapage d'un créneau passé après obtention de la permission.

---

# B. ACCUEIL

## 10. Objectif

L'accueil répond à trois questions au maximum :

1. les propositions sont-elles actives ?
2. quand est la prochaine proposition ?
3. quelle était la dernière activité proposée, si elle doit encore être consultable ?

Il n'affiche pas la liste complète des créneaux.

---

## 11. États principaux

### Actif

> Prochaine proposition vers 15 h 10

### Report actif

> Prochaine proposition vers 10 h 40  
> Reportée de 30 min

### Prochain jour travaillé

> Prochaine proposition lundi vers 10 h 10

### Aucun jour travaillé

> Aucun jour de travail sélectionné.  
> Aucune proposition prévue.

### Suspendu

> Propositions en pause.

### Notifications refusées

> Les notifications sont désactivées sur cet appareil.

### Aucun candidat

> Aucune activité ne correspond actuellement à ces réglages.

Aucun conseil supplémentaire n'est ajouté à ce dernier message.

---

## 12. Dernière activité proposée

Libellé :

> Dernière activité proposée

Cette zone est une **mémoire de présentation**, pas une todo et pas une opportunité encore active.

Elle ne doit jamais être formulée comme :

- activité à faire ;
- activité en attente ;
- activité non terminée ;
- dette ;
- tâche.

Affichage minimal recommandé :

```text
Dernière activité proposée
Une petite marche ?
```

Un appui ouvre exactement la consigne de cette activité.

### Cycle de vie

La dernière activité reste consultable après :

- ouverture de la notification ;
- Retour Android ;
- passage de l'application en arrière-plan ;
- verrouillage/déverrouillage ;
- swipe de la notification ;
- notification ignorée ;
- report +10/+30, avec la même activité ;
- destruction/recréation du processus si l'état persistant a été restauré.

Elle est remplacée quand :

- une nouvelle proposition distincte est générée ;
- l'utilisateur obtient une alternative via `Autre activité`.

Elle n'est plus mise en avant quand :

- l'utilisateur choisit explicitement `Pas maintenant` pour cette opportunité.

### Invariant

La présence de `Dernière activité proposée` :

- ne bloque aucun prochain créneau ;
- ne maintient pas une notification ;
- ne signifie pas que l'opportunité est encore active ;
- ne modifie pas le scoring.

### Garde-fou de concurrence

Si un écran plus ancien reste ouvert alors qu'une nouvelle proposition a depuis été générée, `Pas maintenant` sur l'ancien écran ne doit pas effacer la mise en avant de la proposition plus récente.

L'implémentation compare donc l'identifiant d'opportunité avant d'effacer la mémoire de présentation.

---

## 13. Actions accueil

Actif :

- `Suspendre`
- `Modifier mes réglages`

Suspendu :

- `Reprendre`
- `Modifier mes réglages`

La suspension est uniquement manuelle jusqu'à `Reprendre`.

---

# C. NOTIFICATION

## 14. Contenu

Titre :

```text
{activity_title}
```

Corps :

```text
{duration_label} · Touche pour voir la consigne
```

Ne pas afficher :

- niveau de preuve ;
- score ;
- catégorie ;
- historique ;
- bénéfice médical ;
- reproche.

---

## 15. Actions rapides

Ordre :

1. `Dans 10 min`
2. `Dans 30 min`
3. `Pas maintenant`

Les reports invalides temporellement sont masqués.

`Pas maintenant` reste disponible.

Les actions +10/+30 restent propres à la notification V0 et ne sont pas ajoutées à l'écran activité.

---

## 16. Report

Un report :

- conserve exactement la même activité ;
- conserve la même opportunité ;
- ne recalcule pas le score ;
- ne déplace aucun créneau fixe ;
- peut traverser la pause déjeuner ou la fin du travail ;
- ne traverse jamais minuit ;
- conserve `Dernière activité proposée`.

Le report signifie `mauvais moment`, pas `mauvais contenu`.

---

## 17. Swipe / ignorance passive

Swiper ou ignorer une notification :

- retire seulement la notification de l'interface système ;
- ne vaut pas `Pas maintenant` ;
- ne crée aucune préférence ;
- ne crée aucune pénalité ;
- ne crée aucune dette ;
- ne supprime pas `Dernière activité proposée`.

Aucun événement passif n'est transformé en réussite ou refus.

---

## 18. Une seule notification visible

Une seule notification d'activité peut être visible à la fois.

Une nouvelle proposition valide remplace l'ancienne notification non traitée.

Cette opération remplace également `Dernière activité proposée` par la nouvelle activité.

---

# D. ÉCRAN ACTIVITÉ

## 19. Fonction

L'écran activité est avant tout **un écran de consultation de la consigne**.

Il ne demande pas à l'utilisateur de confirmer qu'il commence ou qu'il termine.

---

## 20. Structure

Ordre :

1. durée ;
2. éventuel badge de posture ;
3. titre ;
4. instruction ;
5. `Autre activité` ;
6. `Pas maintenant`.

Il n'y a pas de bouton `✕` spécifique en V0.

Il n'y a pas de bouton :

- `Commencer` ;
- `OK` ;
- `J'ai compris` ;
- `Terminé`.

---

## 21. Comportement

L'écran :

- reste visible tant que l'utilisateur n'en sort pas ;
- ne se ferme pas automatiquement ;
- ne lance aucun chronomètre ;
- peut rester visible pendant que l'utilisateur réalise l'activité.

Une recomposition ou recréation Compose ne doit **jamais** déclencher une nouvelle sélection.

L'activité affichée est restaurée à partir de son identifiant persistant / argument de navigation, pas en rappelant le moteur.

Après verrouillage puis déverrouillage, la même activité doit être retrouvée sous réserve du cycle de vie Android normal.

---

## 22. Retour Android

Retour :

- revient à l'accueil ;
- est une navigation uniquement ;
- ne signifie ni réussite, ni refus, ni abandon ;
- ne pénalise pas l'activité ;
- ne modifie aucune préférence ;
- conserve `Dernière activité proposée`.

Aucun bouton fermer spécifique n'est ajouté.

---

## 23. Badge de posture

Priorité :

1. `standing_required=true` → `Debout nécessaire` ;
2. posture debout préférée + `can_stand=true` → `Debout conseillé` ;
3. posture debout préférée + `can_stand=false` + activité assise compatible → `Assis possible` ;
4. sinon aucun badge.

La consigne reste identique quel que soit le badge.

---

## 24. Autre activité

V0 applique la règle métier suivante :

- même opportunité ;
- toutes les activités déjà affichées pendant l'interaction courante sont exclues du nouveau tirage ;
- une activité affichée plus tôt dans cette interaction ne peut pas revenir ;
- `A → B → C` est la règle V0 tant que des candidats compatibles restent disponibles ;
- aucune préférence durable n'est créée ;
- la nouvelle activité devient immédiatement `Dernière activité proposée`.

La nouvelle sélection utilise les réglages les plus récents.

Si aucune autre activité n'est disponible :

> Aucune autre activité disponible

Aucune activité incompatible n'est proposée en secours.

---

## 25. Pas maintenant

`Pas maintenant` est une action explicite.

Elle :

- abandonne l'opportunité courante ;
- ne relance pas ;
- ne pénalise pas ;
- ne crée aucune préférence durable ;
- retire la notification éventuelle ;
- retire de l'accueil la mise en avant de **cette opportunité** dans `Dernière activité proposée` ;
- revient à l'accueil.

Un swipe ou Retour n'est jamais assimilé à `Pas maintenant`.

---

# E. COMPORTEMENTS PASSIFS

## 26. Invariant central

> Un comportement passif ne permet aucune inférence sur le comportement de l'utilisateur.

Passifs :

- notification ignorée ;
- notification swipée ;
- application mise en arrière-plan ;
- Retour Android ;
- écran verrouillé ;
- téléphone éteint ;
- processus Android tué ;
- appareil indisponible.

Ils ne signifient pas :

- activité réalisée ;
- activité refusée ;
- satisfaction ;
- insatisfaction ;
- préférence implicite.

Ils ne produisent :

- aucune dette ;
- aucune relance punitive ;
- aucune adaptation des poids ;
- aucune préférence implicite ;
- aucun rattrapage.

Les logs techniques locaux restent possibles en debug, sans sémantique comportementale.

---

# F. RÉGLAGES ET PLANIFICATION

## 27. Catégories et intensités

Toutes peuvent être désactivées.

Si aucun candidat :

> Aucune activité ne correspond actuellement à ces réglages.

Aucune correction automatique.

---

## 28. Modification des réglages

Un report actif est annulé si une modification affecte :

- jours ;
- horaires ;
- pause déjeuner ;
- créneaux ;
- catégories ;
- intensités ;
- possibilité de se lever ;
- possibilité de marcher.

Une notification déjà affichée n'est pas réécrite rétroactivement.

`Autre activité` utilise immédiatement les nouveaux réglages.

---

## 29. Réactivation

Après :

- `Reprendre` ;
- restauration de la permission Android ;
- fin d'onboarding ;
- retour après indisponibilité de l'appareil ;

seules les opportunités futures sont prises en compte.

Aucun rattrapage.

---

## 30. Heure locale

Les créneaux sont des heures locales.

Changement de fuseau / heure d'été-hiver :

- recalcul du futur uniquement ;
- aucun doublon ;
- aucun rattrapage.

Un report actif est annulé lors d'un changement significatif de fuseau ou d'heure système.

---

# G. ÉVÉNEMENTS ET MÉTRIQUES

## 31. Pas de faux KPI

La V0 ne maintient aucun événement d'acceptation explicite.

Il n'existe aucune action d'acceptation explicite ayant une fonction métier.

`ACTIVITY_OPENED`, si un diagnostic local existe un jour, ne doit jamais être présenté comme un succès.

La V0 n'affiche pas de statistiques de réalisation.

---

# H. ACCESSIBILITÉ

## 32. Exigences

- TalkBack pour les contrôles ;
- cibles tactiles suffisantes ;
- grandes tailles de texte ;
- aucune information uniquement par couleur ;
- écran activité lisible sans animation ;
- ordre de focus cohérent avec durée → posture → titre → instruction → actions.

---

# I. V0 / V1 / V2

## 33. V0

- flux décrit dans cette spécification ;
- pas de confirmation de démarrage ;
- dernière activité consultable ;
- comportement passif neutre ;
- alternative simple ;
- reports notification uniquement.

## 34. V1

Seulement après retours :

- catalogue consultable manuellement ;
- contexte télétravail avec catalogue élargi ;
- éventuel masquage individuel ;
- aucun cooldown supplémentaire tant que les retours réels ne le justifient.

## 35. V2

Aucune sophistication anticipée sans bénéfice démontré.
