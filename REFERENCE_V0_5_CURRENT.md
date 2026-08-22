# Intervalles — REFERENCE_V0_5_CURRENT

**Référentiel courant : 23 août 2026**  
**Portée : V0 stabilisée + V0.5 actuellement testée**

Ce document est le **point d’entrée produit / UX / comportemental courant** pour Intervalles.  
En cas de contradiction avec un document plus ancien, **ce fichier prime**.

Ordre de priorité recommandé :
1. `REFERENCE_V0_5_CURRENT.md`
2. `VALIDATION_STATUS.md`
3. catalogue et fichiers de politique réellement embarqués dans l’application
4. spécifications spécialisées plus anciennes uniquement lorsqu’elles ne contredisent pas ce référentiel

Les anciennes baselines V0 contiennent plusieurs décisions désormais obsolètes : ne pas les réintroduire automatiquement.

---

# 1. Produit

**Intervalles** est une application Android :
- gratuite ;
- open source ;
- entièrement hors ligne ;
- sans compte ;
- destinée à aider à interrompre la sédentarité au travail ;
- avec de petites activités d’environ 30 secondes à 5 minutes.

Ce n’est **pas** une application de sport.

Principes :
- simplicité ;
- faible friction ;
- autonomie ;
- absence de culpabilisation ;
- sobriété visuelle ;
- crédibilité scientifique ;
- moteur de décision explicable.

L’application ne cherche pas à mesurer une performance ni à prouver objectivement qu’une activité physique a été réalisée.

---

# 2. Frontière V0 / V0.5 / V1

## V0 — baseline stabilisée

Comprend :
- onboarding ;
- paramètres ;
- jours et créneaux ;
- notifications ;
- report `+ 30 min` ;
- accueil ;
- fiche activité ;
- alternatives ;
- activités simples et activités avec chrono ;
- dernière activité ;
- pause/réactivation ;
- light/dark ;
- accessibilité de base ;
- fonctionnement hors ligne.

## V0.5 — actuellement en test

Deux ajouts fonctionnels issus du RETEX :
1. `Bouger maintenant`
2. modification ponctuelle de la `Prochaine proposition d’activité`

Plus une passe UX ciblée issue des premiers tests.

## V1 — ne pas introduire maintenant

À conserver hors V0.5 sauf nouveau RETEX explicite :
- catalogue consultable complet ;
- exclusions durables `Ne plus voir` ;
- statistiques ;
- gamification ;
- compte/cloud ;
- personnalisation complexe ;
- navigation inférieure ;
- système d’absences/calendrier avancé ;
- recommandations opaques ou IA ;
- illustrations d’activités tant qu’un besoin utilisateur n’est pas démontré.

---

# 3. Navigation

Architecture actuelle :

```text
Premier lancement
Bienvenue
  → Configuration
    → Notifications / autorisations
      → Accueil

Usage
Accueil
  ├── Prochaine activité → Fiche activité
  ├── Bouger maintenant → Fiche activité manuelle
  ├── Dernière activité → Fiche
  └── Paramètres
        ├── planning et préférences
        ├── Prochaine proposition d’activité
        ├── Notifications
        ├── Pourquoi ces pauses ?
        └── À propos

Fiche activité
  └── Chrono facultatif
```

Pas de :
- bottom navigation ;
- drawer ;
- FAB ;
- écran catalogue dans la V0.5 actuelle.

---

# 4. Accueil

## 4.1 Prochaine activité

Affiche :
- titre ;
- durée ;
- date / heure.

Format temporel :
- aujourd’hui : horaire approprié ;
- lendemain : `Demain · HH:mm` ;
- éviter un affichage ambigu avec la dernière activité.

Toucher la carte ouvre la fiche de **cette opportunité planifiée**.

Si l’utilisateur réalise volontairement cette activité en avance, l’opportunité correspondante est prise en charge et ne doit pas réapparaître ensuite à son horaire.

Il n’existe **aucun seuil caché de 15 / 30 / 60 minutes** pour décider si l’activité anticipée consomme ou non son créneau.

## 4.2 Dernière activité

`Dernière activité` signifie :

> la dernière activité que l’utilisateur considère avoir faite selon le flux de l’application.

Elle n’est jamais alimentée par :
- une simple ouverture ;
- un swipe de notification ;
- un Retour avant validation ;
- une activité timer abandonnée.

Affichage :
- titre ;
- heure ;
- pas de métrique de réussite.

## 4.3 Bouger maintenant — V0.5

But :

> obtenir volontairement une activité maintenant, indépendamment du planning automatique.

Règles :
- disponible même avec zéro jour sélectionné ;
- disponible même lorsque les propositions automatiques sont en pause ;
- respecte les réglages Intensité / Sur place / En mouvement ;
- utilise le même moteur explicable que les propositions automatiques ;
- ne consomme, ne décale et ne remplace jamais un créneau automatique futur ;
- permet `Autre activité`, `Précédent` et `Suivant` dans l’interaction manuelle ;
- une activité manuelle abandonnée ne devient pas `Dernière activité` ;
- une activité manuelle réellement validée entre dans `Dernière activité` ;
- une activité manuelle réalisée participe à l’historique récent de diversité afin d’éviter une répétition immédiate avec la prochaine proposition automatique.

Cette inclusion dans l’historique est un **choix produit d’anti-répétition**, pas une recommandation scientifique.

### Invariant UX actuel

Conserver :
- l’icône actuelle ;
- le placement actuel ;
- le fond actuel ;
- la forme actuelle.

Le seul ajustement récent demandé est :
- libellé `Bouger maintenant` avec le **même TextStyle que les titres d’activités** (`titleLarge`).

Aucun autre redesign de ce bouton sans validation préalable.

---

# 5. Paramètres

## 5.1 Jours

Affichage horizontal obligatoire :

```text
L M M J V S D
```

Invariant :
- un jour sélectionné **conserve toujours sa lettre** ;
- ne jamais remplacer la lettre par une coche ;
- aucune coche n’est nécessaire sur les jours ;
- la sélection reste indiquée visuellement par l’état sélectionné ;
- TalkBack doit conserver le nom complet du jour.

**Zéro jour sélectionné est autorisé.**

## 5.2 Créneaux

- 1 à 6 créneaux configurables ;
- valeurs habituelles : `10:00 · 11:30 · 15:00 · 16:30` ;
- minimum **10 minutes** entre deux créneaux configurés ;
- mêmes créneaux pour tous les jours actifs ;
- boutons `−` conservés pour supprimer ;
- cible tactile des `−` : **au moins 48 × 48 dp** ;
- `+ Ajouter un créneau` reste simple.

Les anciens champs techniques `workStart`, `workEnd`, `lunchEnabled`, `lunchStart`, `lunchEnd` sont des reliquats inertes pour la V0/V0.5 et ne doivent pas réapparaître dans l’UX.

## 5.3 Intensité

Options :
- Douce ;
- Modérée ;
- Dynamique.

Tout est sélectionné par défaut.

Choix UX :
- libellés visibles : `Douce · Modérée · Dynamique` ;
- la clé interne historique `active` reste inchangée pour `Modérée` : il s’agit uniquement d’un changement de libellé UX ;
- centrer le contenu des chips Intensité / Type de mouvement ;
- centrer les lettres `L M M J V S D` dans leurs bulles ;
- ne pas ajouter de texte d’explication simplement pour expliquer la sélection ;
- état sélectionné suffisamment explicite visuellement ;
- sélection = coche + fond sauge / état sélectionné ;
- état non sélectionné = plus neutre ;
- ne pas dépendre uniquement de la couleur.

En taille de texte normale : alignement horizontal recherché pour réduire le scroll.

À **200 % de taille de texte**, le contenu doit **wrap / se réorganiser plutôt que tronquer**.

Au moins une intensité doit rester active.

## 5.4 Type de mouvement

Options :
- Sur place ;
- En mouvement.

Même logique UX que pour Intensité :
- sélection claire sans texte d’aide supplémentaire ;
- coche + état visuel sélectionné ;
- alignement horizontal en usage normal ;
- wrap/réorganisation plutôt que troncature à 200 %.

Au moins un type doit rester actif.

---

# 6. Prochaine proposition d’activité — V0.5

Dans Paramètres, une section :

**Prochaine proposition d’activité**

affiche la prochaine date/heure et permet `Modifier`.

Objectif :
- permettre une exception ponctuelle ;
- exemple : vendredi, l’utilisateur sait qu’il ne travaille pas lundi et fixe sa prochaine proposition à mardi.

## Règle métier

Cette modification porte sur **une seule prochaine occurrence**.

Elle ne modifie jamais :
- les jours récurrents ;
- les créneaux hebdomadaires ;
- les horaires des créneaux suivants.

Exemple :

```text
Planning habituel
Lundi 10:00 · 11:30 · 15:00 · 16:30

Exception
Prochaine proposition → Mardi 10:00
```

Résultat :
- l’exception devient la prochaine proposition automatique ;
- les créneaux suivants conservent leurs horaires normaux ;
- aucune cascade de décalage n’est créée ;
- après l’exception, le planning hebdomadaire reprend au prochain créneau normal encore à venir.

L’exception est donc une **occurrence ponctuellement déplacée**, pas une translation de toute la série.

L’utilisateur peut :
- `Modifier` ;
- `Revenir au planning habituel`.

## Invariant UX du dialogue

Le dialogue d’édition doit indiquer clairement qu’il s’agit uniquement de cette prochaine occurrence.

Libellés actuellement prévus :
- date : `Modifier uniquement la prochaine proposition`
- heure : `Heure de cette prochaine proposition`

---

# 7. Notifications

Écran :
- titre `Notifications` ;
- interface épurée ;
- pas de premier bloc explicatif superflu ;
- pas de phrase inutile au-dessus de Son/Vibration ;
- bouton `Configurer les autorisations`.

Réglages :
- Son ON/OFF ;
- Vibration ON/OFF ;
- toutes les combinaisons sont autorisées.

## Notification d’activité

Actions :
- `+ 30 min`
- `Commencer`

Toucher le corps de la notification :
- ouvre seulement la fiche ;
- ne signifie pas que l’activité a été faite.

Swipe :
- abandon de cette proposition ;
- ne devient pas `Dernière activité` ;
- ne crée ni dette punitive ni préférence implicite.

## Report +30

- un seul `+30` maximum par opportunité ;
- valeur réelle V0/V0.5 : **30 minutes** ;
- les valeurs de 2 minutes ne sont que des réglages temporaires de test et ne doivent pas être livrées ;
- l’activité est conservée par défaut ;
- pendant le report, l’utilisateur peut changer d’alternative ;
- la notification reportée doit utiliser l’activité actuellement sélectionnée ;
- pas de second `+30` sur la notification reportée.

La livraison vise l’heure choisie.  
Tolérance de retard système : **10 minutes maximum** ; au-delà, ne pas créer de rattrapage tardif.

---

# 8. Permissions Android

## Notifications refusées

Intervalles reste utilisable :
- accueil fonctionnel ;
- prochaine activité future visible ;
- aucune dette ou rattrapage des créneaux passés.

## Alarmes et rappels non autorisés

Intervalles reste utilisable :
- la prochaine activité reste consultable ;
- les notifications précises ne sont pas programmées.

Wording d’onboarding :
- rester précis ;
- ne pas prétendre que l’application devient inutilisable.

---

# 9. Fiche activité et action `Commencer`

Le verbe d’action courant est :

**`Commencer`**

Choix :
- infinitif ;
- même verbe pour éviter `Je le fais` / `C’est parti`.

## Activité simple

`Commencer` :
- clôt l’opportunité ;
- alimente immédiatement `Dernière activité`.

Point UX à garder sous observation :
- le mot `Commencer` décrit un début alors que, techniquement, l’activité simple est immédiatement considérée comme la dernière activité réalisée dans le flux.

Ne pas complexifier ce comportement sans RETEX concret.

## Activité avec chrono

`Commencer` :
- démarre directement le chrono ;
- prend en charge l’opportunité ;
- empêche sa notification planifiée de revenir.

Puis :
- `Terminer` ou arrivée naturelle à `0:00` → devient `Dernière activité` ;
- Retour pendant RUNNING/PAUSED → abandon ;
- en cas d’abandon, ne devient pas `Dernière activité` ;
- la notification n’est pas restaurée.

---

# 10. Alternatives

Dans une même interaction :

```text
A → B → C
```

Une activité déjà affichée est exclue des nouveaux tirages de cette interaction.

Navigation :
- `Précédent`
- `Suivant`
- `Autre activité`

L’historique local doit survivre à un aller-retour vers l’accueil tant qu’il s’agit de la même opportunité :

```text
A → B → C → Accueil → C → Précédent B → Précédent A
```

Changer d’alternative :
- ne crée aucune préférence durable ;
- ne constitue pas une preuve de réalisation.

---

# 11. Chrono

États :
- READY ;
- RUNNING ;
- PAUSED ;
- TIME_ELAPSED.

Actions :
- Pause ;
- Reprendre ;
- Terminer.

Fin :
- `0:00`
- `C’est fini !`

Aucun :
- Bravo ;
- score ;
- série ;
- calorie ;
- niveau de réussite.

Le chrono utilise une horloge monotone afin de rester cohérent lors du verrouillage / passage en arrière-plan.

---

# 12. Catalogue V0.5

Catalogue actuel : **23 activités**.

Deux modes explicites :
- `simple`
- `timer`

Le mode n’est jamais inféré implicitement.

Principes éditoriaux récents :
- titres courts, simples, naturels ;
- éviter la sur-précaution répétitive (`tranquillement`, `doucement`, `sans forcer`) lorsqu’elle n’apporte aucune information ;
- conserver les informations réellement nécessaires à l’exécution ou à la sécurité ;
- repères de répétition concrets pour les activités de mobilité ;
- les formulations doivent rester adultes et non infantilisantes.

Exemples de direction validée :
- `Un verre d’eau ?`
- `Quelques squats ?`
- `Une marche un peu plus soutenue ?`
- consigne de marche : `Marche quelques minutes pour faire une pause et te dégourdir les jambes.`

Les anciennes activités `START01` / `START02` ne sont plus réservées au début de journée :
- elles sont proposées comme des activités standard ;
- leurs titres doivent rester courts, simples et non sportifs.

Pas d’option spécifique « start of day » en V0.5.

---

# 13. Direction visuelle

Nom :
**Intervalles**

Direction :
- Material 3 sobre ;
- calme ;
- adulte ;
- non médical ;
- non fitness ;
- espace respirant ;
- faible friction.

## Light

- `primary` : `#4F756D`
- `onPrimary` : `#FFFFFF`
- `primaryContainer` : `#DCE8E3`
- `onPrimaryContainer` : `#202522`
- `background` : `#F7F8F6`
- `surface` : `#FFFFFF`
- `surfaceVariant` : `#EEF2EF`
- `onSurface` : `#202522`
- `onSurfaceVariant` : `#626A66`
- `outline` : `#AAB5B0`
- `outlineVariant` : `#D8E0DC`

## Dark

- `primary` : `#9BC8BD`
- `onPrimary` : `#11201D`
- `primaryContainer` : `#27433D`
- `onPrimaryContainer` : `#DCEFEA`
- `background` : `#111715`
- `surface` : `#18201D`
- `surfaceVariant` : `#1E2925`
- `onSurface` : `#E7ECE9`
- `onSurfaceVariant` : `#B6C0BC`
- `outline` : `#7E8A85`
- `outlineVariant` : `#3A4742`

Pas de Dynamic Color.

Dark mode déjà observé comme fonctionnel sur téléphone.

---

# 14. Accessibilité — invariants / vérifications

À maintenir :
- TalkBack ;
- noms complets des jours via sémantique ;
- cible tactile ≥48dp pour les boutons `−` ;
- la couleur seule ne doit pas indiquer une sélection pour Intensité / Type ;
- grande taille de texte sans troncature importante ;
- boutons et cartes avec zones tactiles suffisantes.

À vérifier sur la baseline UX la plus récente :
1. light system bars : icônes système sombres sur fond clair ;
2. Intensité / Type à 200 % : wrap/réorganisation, aucune troncature ;
3. cible tactile réelle des `−` ;
4. dialogue `Prochaine proposition d’activité` : compréhension immédiate du caractère ponctuel.

---

# 15. Données / confidentialité

- aucun compte ;
- aucune collecte serveur ;
- pas de permission Internet requise pour le fonctionnement ;
- paramètres stockés localement ;
- Android cloud backup désactivé en V0/V0.5 (`allowBackup=false`).

---

# 16. Science vs choix produit

## Ce que le référentiel scientifique peut soutenir

La littérature permet de soutenir de façon générale :
- la réduction du temps sédentaire ;
- le remplacement d’une partie du temps assis par du mouvement, y compris léger ;
- l’intérêt pratique de ruptures de sédentarité.

## Ce qui reste un choix de conception

Ne pas présenter comme scientifiquement optimal :
- quatre créneaux ;
- les horaires par défaut ;
- le minimum de 10 minutes entre créneaux ;
- `+30 min` ;
- la tolérance de retard de 10 minutes ;
- les couleurs ;
- le wording ;
- `Bouger maintenant` ;
- l’anti-répétition ;
- le nombre de répétitions indiqué dans une consigne.

Le niveau de preuve scientifique reste documentaire ; il ne devient pas un multiplicateur caché du moteur.

---

# 17. Règle de gouvernance UX

**Ne pas modifier silencieusement un invariant UX déjà validé.**

En particulier, informer explicitement le propriétaire produit **avant** toute modification de :
- lettres des jours ;
- icône / placement / fond / forme de `Bouger maintenant` ;
- structure générale de l’accueil ;
- libellés d’actions ;
- architecture de navigation ;
- fonctionnement de `Prochaine proposition d’activité`.

Pas d’autre redesign en V0.5 sans :
- bug ;
- problème d’accessibilité ;
- ambiguïté démontrée ;
- ou RETEX utilisateur.

---

# 18. Objectif de la phase actuelle

La V0.5 n’est pas une phase d’ajout continu de fonctionnalités.

Objectif immédiat :
1. stabiliser `Bouger maintenant` ;
2. stabiliser `Prochaine proposition d’activité` ;
3. appliquer uniquement les correctifs UX issus du RETEX ;
4. poursuivre les tests sur de vrais téléphones ;
5. corriger les bugs / incompréhensions réellement observés.

La priorité reste la **simplicité et la stabilité**, pas l’enrichissement fonctionnel.
