# ActivityCatalogSchema V1

**Statut :** VALIDÉ POUR SIMULATION  
**Version du contrat :** 1.0.1  
**Nombre d'activités migrées :** 23

## 1. Finalité

Ce contrat définit la structure officielle du catalogue V0 de SedentaryApp. Le fichier `data/activities_v1.json` est la **source de vérité**. Les vues Markdown sont générées depuis ce fichier et ne doivent pas être modifiées manuellement.

Le schéma sépare :

- le contenu affiché (`title`, `instruction`, `user_category`) ;
- les facettes utilisées par le moteur (`internal_family`, contraintes fonctionnelles, poids) ;
- les métadonnées scientifiques documentaires (`evidence_level`).

Les catégories visibles ne pilotent pas directement la diversité. `internal_family` est conservé comme métadonnée éditoriale ; le moteur V0 ne l’utilise pas pour le scoring.

## 2. Décisions V0 intégrées

- 23 activités publiées ;
- `movement_required` est remplacé par `walking_required` ;
- les contextes ambigus `workday`, `engaged_window`, `stress_window`, `screen_work` et `start_of_day` sont supprimés ;
- les routines `START01` et `START02` sont explicitement limitées à `eligible_opportunity_types = ["start_of_day"]` ;
- les autres activités utilisent `["standard"]` ;
- la posture est séparée en `compatible_postures`, `preferred_posture` et `standing_required` ;
- `seated_to_standing` n'est plus une posture ;
- `safety_flags` et `internal_family` utilisent un vocabulaire contrôlé ;
- `variant_of` signifie uniquement : « variante reconnaissable de la même activité fondamentale » ;
- aucune relation seulement thématique n'est stockée ;
- `evidence_level` ne modifie pas le score du moteur ;
- `internal_family` ne modifie pas le score du moteur V0 ;
- la durée d’une activité publiée reste comprise entre 30 secondes et 5 minutes.

## 3. Champs d'une activité

| Champ | Type | Rôle | Lu par le moteur V0 |
|---|---|---|---|
| `id` | chaîne stable | Identifiant immuable | Oui |
| `revision` | entier ≥ 1 | Révision éditoriale de la fiche | Trace seulement |
| `status` | enum | Cycle de vie | Oui |
| `title` | chaîne | Titre affiché | Non |
| `instruction` | chaîne | Consigne affichée | Non |
| `user_category` | enum | Navigation et réglage explicite | Oui, comme filtre |
| `internal_family` | enum contrôlé | Métadonnée éditoriale / diagnostic | Non en V0 |
| `intensity` | enum | Compatibilité avec les réglages | Oui |
| `duration_seconds_min/max` | entiers | Fenêtre de durée | Oui |
| `compatible_postures` | liste | Postures dans lesquelles l'activité est faisable | Oui |
| `preferred_posture` | enum ou null | Recommandation d'affichage non bloquante | Non en V0 |
| `standing_required` | booléen | Nécessité fonctionnelle de pouvoir se lever | Oui |
| `walking_required` | booléen | Nécessité fonctionnelle de pouvoir marcher | Oui |
| `eligible_opportunity_types` | liste | `standard` ou `start_of_day` | Oui |
| `sedentary_break_effect` | enum | Effet attendu sur l'interruption de l'assise | Politique/trace |
| `space_required` | enum | Espace minimal | Oui si le contexte est renseigné |
| `discretion` | enum | Visibilité sociale du geste | Non en V0 |
| `body_focus` | liste contrôlée | Indexation corporelle | Non en V0 |
| `safety_flags` | liste contrôlée | Consignes et garde-fous | Affichage/validation |
| `evidence_level` | enum | Niveau de preuve documentaire | Non |
| `base_weight` | nombre > 0 | Fréquence relative de base | Oui |
| `variant_of` | ID ou null | Variante du même comportement fondamental | Trace/diagnostic |
| `search_synonyms` | liste | Recherche éditoriale et dédoublonnage | Non |

## 4. Invariants

1. `activity_count` correspond exactement au nombre de fiches.
2. Les identifiants sont uniques.
3. `30 ≤ duration_seconds_min ≤ duration_seconds_max ≤ 300`.
4. Une posture préférée appartient aux postures compatibles.
5. `walking_required = true` implique `standing_required = true`.
6. Une cible `variant_of` existe et n'est pas la fiche elle-même.
7. Les activités `START` sont réservées à `start_of_day`.
8. Les autres activités ne sont pas proposées au démarrage dans la V0.
9. Les valeurs de vocabulaire non enregistrées sont refusées.
10. Une métadonnée invalide n'est jamais réparée silencieusement par le moteur.

## 5. Frontière mobilité / étirement

- **Mobilité :** mouvement répété, progressif ou exploratoire, sans maintien prolongé d'une position finale.
- **Étirement :** mise en tension ciblée, tenue ou répétée autour d'une position identifiable.

`MOB04` et `ETIR02` sont conservées comme activités distinctes : la première est une mobilisation globale en flexion du tronc ; la seconde est un étirement unilatéral ciblé des ischio-jambiers. Cette distinction devra rester visible dans les consignes et les tests UX.

## 6. `variant_of`

Le champ est volontairement étroit. Une activité peut référencer une autre fiche seulement lorsqu'un utilisateur reconnaîtrait immédiatement le même comportement principal avec une variation de contexte, d'intensité ou de séquence.

Relations conservées :

- `MOUV04 → MOUV01` ;
- `DYN04 → MOUV01` ;
- `VIS02 → VIS01`.

Les anciennes relations `MOUV02 → MOUV01` et `BIEN02 → MOB01` sont retirées car elles décrivaient surtout une proximité thématique.

## 7. Gouvernance

- Toute nouvelle valeur de vocabulaire doit être ajoutée à `activity_vocabularies_v1.json` avec justification.
- Toute modification de contenu augmente `revision`.
- Toute modification structurelle augmente la version du schéma.
- Les suppressions utilisent `deprecated` ou `archived`, jamais la disparition silencieuse d'un ID publié.
- Le Markdown est régénéré automatiquement après toute modification du JSON.

## 8. V1 / V2

### V1 éventuelle

- internationalisation avant forte croissance du catalogue ;
- historique éditorial plus détaillé ;
- accessibilité structurée si des variantes fonctionnelles sont réellement ajoutées ;
- matériel requis uniquement lorsque le catalogue en contient.

### V2 éventuelle

- médias ou illustrations seulement après validation d'un besoin UX ;
- relations sémantiques plus riches uniquement si un consommateur réel les utilise.

Aucun champ spéculatif n'est ajouté à la V0.
