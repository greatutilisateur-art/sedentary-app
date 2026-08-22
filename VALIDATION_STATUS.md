# Intervalles — VALIDATION_STATUS

**État au 23 août 2026**  
**Portée : V0 + V0.5 actuelle**

Légende :
- ✅ **VALIDÉ** : testé avec succès sur build/téléphone ou comportement confirmé par les tests utilisateur.
- 🟡 **À VÉRIFIER** : implémenté ou spécifié, mais la validation utilisateur/physique finale n’a pas encore été rapportée.
- ⏸ **HORS PÉRIMÈTRE** : ne doit pas bloquer la phase actuelle.

---

# 1. Build / intégration

| Élément | Statut | Note |
|---|---|---|
| Build Android | ✅ VALIDÉ | `Build` / `Assemble` rapportés fonctionnels sur la baseline V0.5 avant le dernier micro-patch UX |
| `Bouger maintenant` | ✅ VALIDÉ | Fonctionnement rapporté comme correct |
| `Prochaine proposition d’activité` | ✅ VALIDÉ | Fonctionnement rapporté comme correct |
| Patch UX post-audit le plus récent | 🟡 À VÉRIFIER | Build + contrôle visuel à refaire après remplacement de `HomeScreen.kt` et `ScheduleScreen.kt` |
| Fonctionnement hors ligne | ✅ VALIDÉ | Architecture locale, pas de compte |
| Android Auto Backup | ✅ VALIDÉ CÔTÉ CONFIG | `allowBackup=false` retenu pour V0/V0.5 |

---

# 2. Tests V0 déjà validés

## Notifications / opportunités

| Test | Statut |
|---|---|
| Toucher le corps de la notification ouvre la fiche sans créer `Dernière activité` | ✅ |
| Swipe d’une notification = abandon, pas `Dernière activité` | ✅ |
| Une seule notification visible / comportement de remplacement | ✅ |
| Activité simple : action principale → `Dernière activité` immédiate | ✅ |
| Activité timer : action principale → chrono | ✅ |
| Retour pendant RUNNING/PAUSED → abandon, pas `Dernière activité` | ✅ |
| Activité commencée avant son créneau → pas de notification doublon | ✅ |
| `+30` fonctionne | ✅ |
| Deuxième notification après report : pas de second `+30` | ✅ |
| Activité sélectionnée pendant report conservée pour la notification reportée | ✅ |
| Absence de permission notifications : accueil reste utilisable | ✅ |
| Tolérance métier de retard max = 10 min | ✅ CÔTÉ RÈGLE |

**Attention :** la durée de report de production doit être **30 min**, pas la valeur de test 2 min.

---

# 3. Fiche / alternatives / chrono

| Test | Statut |
|---|---|
| Historique alternatives A ⇄ B ⇄ C | ✅ |
| Historique conservé après retour à l’accueil pour la même opportunité | ✅ |
| Une activité déjà vue n’est pas reproposée comme nouveau choix dans la même interaction | ✅ |
| Activité simple sans chrono | ✅ |
| Activité avec chrono | ✅ |
| `Terminer` → `Dernière activité` | ✅ |
| Fin naturelle à `0:00` → `Dernière activité` | ✅ |
| `0:00` + `C’est fini !` | ✅ |
| Pas de gamification / félicitations / score | ✅ |

---

# 4. Accueil V0

| Élément | Statut | Note |
|---|---|---|
| Carte `Prochaine activité` | ✅ | Fonctionnelle |
| Carte `Dernière activité` | ✅ | Règles de mise à jour testées |
| `Demain · HH:mm` | ✅ | Décision appliquée/testée |
| Dark mode | ✅ | Fonctionne sur téléphone, pas de flash blanc observé |
| Permission refusée sans disparition du fonctionnement Home | ✅ | Testé |
| Pause / Réactiver | ✅ | Baseline fonctionnelle |

---

# 5. V0.5 — Bouger maintenant

| Test | Statut | Note |
|---|---|---|
| Ouvre une activité manuelle | ✅ | Rapporté fonctionnel |
| Ne modifie pas le prochain créneau automatique | ✅ | Principe implémenté et RETEX positif |
| Disponible avec zéro jour sélectionné | ✅ / À RECONFIRMER | Prévu et intégré ; à garder dans la campagne V0.5 |
| Disponible lorsque les propositions automatiques sont en pause | ✅ / À RECONFIRMER | Prévu et intégré |
| Alternatives Précédent / Suivant / Autre activité | ✅ | Fonctionnement V0 réutilisé |
| Abandon manuel → pas `Dernière activité` | ✅ | Même sémantique que le flux existant |
| Réalisation manuelle → `Dernière activité` | ✅ | Même sémantique que le flux existant |
| Participe à l’historique de diversité | ✅ CÔTÉ IMPLÉMENTATION | Choix V0.5 A |
| N’est jamais interprété comme un créneau planifié | ✅ CÔTÉ ARCHITECTURE | Clé manuelle distincte |

## UX `Bouger maintenant`

État voulu après audit :
- icône : inchangée ;
- placement : inchangé ;
- fond : inchangé ;
- forme : inchangée ;
- libellé : même `TextStyle` que les titres d’activités (`titleLarge`).

**Validation finale du micro-patch typographique : 🟡 À VÉRIFIER visuellement.**

---

# 6. V0.5 — Prochaine proposition d’activité

| Test | Statut | Note |
|---|---|---|
| Affichage de la prochaine date/heure dans Paramètres | ✅ |
| Modification ponctuelle date + heure | ✅ |
| Persistance après fermeture/réouverture | ✅ selon RETEX global `Tout marche nickel` |
| Ne décale pas les créneaux suivants | ✅ | Règle centrale confirmée |
| Reprise du planning habituel après l’exception | ✅ | Fonctionnement rapporté correct |
| `Revenir au planning habituel` | ✅ | Fonctionnement intégré |
| Peut cibler une date hors jour normalement travaillé | ✅ | Cas d’usage recherché |
| `Bouger maintenant` reste indépendant de cette exception | ✅ | Architectures séparées |

## UX du dialogue

Dernière demande :
- date : `Modifier uniquement la prochaine proposition`
- heure : `Heure de cette prochaine proposition`

**Compréhension réelle du caractère ponctuel : 🟡 À VÉRIFIER auprès d’un testeur.**

---

# 7. Paramètres — UX actuelle

## Jours

Invariant :

```text
L M M J V S D
```

| Vérification | Statut |
|---|---|
| Alignement horizontal | ✅ CÔTÉ IMPLÉMENTATION |
| Un jour sélectionné garde sa lettre | 🟡 À VÉRIFIER après dernier patch |
| Aucune coche sur les jours | 🟡 À VÉRIFIER après dernier patch |
| TalkBack = nom complet du jour | ✅ CÔTÉ IMPLÉMENTATION |
| Zéro jour autorisé | ✅ |

## Intensité / Type

| Vérification | Statut |
|---|---|
| Tout sélectionné par défaut | ✅ |
| Sélection plus visible : coche + fond | ✅ CÔTÉ UX / IMPLÉMENTATION |
| Pas de texte d’aide ajouté | ✅ |
| Alignement horizontal en taille normale | ✅ CÔTÉ IMPLÉMENTATION |
| Libellés Intensité : `Douce · Modérée · Dynamique` | 🟡 À VÉRIFIER après dernier patch |
| Contenu Intensité / Type centré | 🟡 À VÉRIFIER visuellement |
| Lettres des jours centrées dans leurs bulles | 🟡 À VÉRIFIER visuellement |
| À 200 % : pas de troncature, wrap/réorganisation | 🟡 À VÉRIFIER PHYSIQUEMENT |
| Au moins une intensité active | ✅ |
| Au moins un type actif | ✅ |

## Créneaux

| Vérification | Statut |
|---|---|
| 1 à 6 créneaux | ✅ |
| Min 10 min entre deux créneaux | ✅ |
| Boutons `−` conservés | ✅ |
| Cible tactile `−` ≥ 48 × 48 dp | ✅ CÔTÉ CODE / 🟡 À VÉRIFIER AU TOUCHER |

---

# 8. Notifications — UX / wording

| Élément | Statut |
|---|---|
| `Recevoir les propositions` → `Notifications` | ✅ intégré |
| Suppression du premier bloc explicatif | ✅ intégré |
| Suppression de la phrase superflue avant Son/Vibration | ✅ intégré |
| `Configurer les autorisations` | ✅ intégré |
| Son ON/OFF | ✅ |
| Vibration ON/OFF | ✅ |
| Action `Commencer` | ✅ décision courante |
| `+ 30 min` | ✅ décision courante |

---

# 9. Catalogue / copy

Catalogue courant : **23 activités**.

## Modifications issues du RETEX

| Élément | Statut |
|---|---|
| `Quelques squats ?` sans `tranquilles` | ✅ |
| Réduction des `tranquillement` inutiles | ✅ |
| Marche : consigne simplifiée | ✅ |
| Escaliers : `un ou plusieurs étages` | ✅ |
| Rotation du haut du corps simplifiée | ✅ |
| Mobilité vers l’avant moins infantilisante | ✅ |
| Consigne mollets rendue opératoire | ✅ |
| VIS02 réécrite concrètement | ✅ |
| Repères de répétitions pour mobilité | ✅ |
| `Si tu peux, lève-toi` pour posture debout conseillée | ✅ |
| START01 / START02 transformées en activités standard | ✅ |
| Pas de fonctionnalité `Ne plus voir` | ✅ décision |

Les identifiants START sont conservés pour compatibilité technique, mais le concept UX « start of day » n’est plus actif.

---

# 10. Accessibilité

## Déjà validé / observé

- ✅ TalkBack amélioré pour les jours ;
- ✅ dark mode ;
- ✅ contraste général de la palette déjà audité ;
- ✅ aucun flash blanc observé au lancement en dark mode.

## Vérifications finales demandées après audit UX

1. 🟡 **Light system bars**  
   Vérifier sur téléphone :
   - fond clair ;
   - icônes système sombres.

2. 🟡 **Font scale 200 %**  
   Vérifier :
   - Intensité ;
   - Type de mouvement ;
   - aucune troncature ;
   - wrap/réorganisation acceptable.

3. 🟡 **Boutons `−`**  
   Vérifier :
   - cible tactile réelle confortable ≥48dp.

4. 🟡 **Prochaine proposition d’activité**  
   Faire tester le dialogue sans explication préalable :
   - l’utilisateur doit comprendre qu’il ne modifie qu’une seule occurrence.

---

# 11. Moteur / science

## Moteur

Chaîne explicable conservée :

```text
OpportunityPolicy
→ CandidateFilter
→ CandidateScorer
→ WeightedSelector
```

avec planification temporelle en amont.

Pas d’IA opaque dans le choix runtime.

## Science

✅ Principe scientifique général documenté :
- réduire le temps sédentaire ;
- remplacer une partie du temps assis par du mouvement, y compris léger.

⚠️ Ne pas transformer en affirmations scientifiques :
- 4 propositions/jour ;
- +30 min ;
- horaires par défaut ;
- 10 min entre créneaux ;
- anti-répétition ;
- wording ;
- couleurs ;
- nombre de répétitions.

---

# 12. Confidentialité / architecture

| Élément | Statut |
|---|---|
| Hors ligne | ✅ |
| Sans compte | ✅ |
| Pas de données serveur | ✅ |
| Backup cloud Android désactivé | ✅ |
| Champs legacy work/lunch inertes | ✅ connus, non exposés |
| Exact alarms / notifications séparés du métier | ✅ architecture retenue |

---

# 13. Bugs / blocages connus

## P0

**Aucun P0 connu au moment de ce document.**

## P1

**Aucun P1 fonctionnel connu** sur la baseline V0.5 avant le dernier micro-patch UX.

## Vérifications P1/P2 avant élargissement des tests

- 🟡 system bars light ;
- 🟡 font scale 200 % ;
- 🟡 tactile `−` ;
- 🟡 compréhension du dialogue de prochaine proposition ;
- 🟡 validation visuelle du dernier patch : jours sans coche + typographie `Bouger maintenant`.

---

# 14. Hors périmètre actuel

Ne doit pas bloquer la V0.5 :
- ⏸ illustrations ;
- ⏸ catalogue complet ;
- ⏸ `Ne plus voir` ;
- ⏸ statistiques ;
- ⏸ gamification ;
- ⏸ recommandations avancées ;
- ⏸ calendrier d’absences complexe ;
- ⏸ nettoyage des champs legacy non exposés ;
- ⏸ refonte générale UX.

---

# 15. Verdict actuel pour le chat UX Expert

**La V0.5 est fonctionnellement suffisamment stable pour continuer les tests utilisateurs sans redesign général.**

L’audit UX doit maintenant se concentrer uniquement sur :
- compréhension ;
- friction ;
- accessibilité ;
- cohérence visuelle ;
- problèmes réellement observables.

Ne pas proposer de nouvelle architecture ou de nouvelles fonctions sans démontrer qu’un problème utilisateur actuel le justifie.
