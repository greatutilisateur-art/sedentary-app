# Cadre scientifique — V0.2

**Produit :** application Android hors ligne de lutte contre la sédentarité au travail  
**Portée :** V0  
**Statut :** référentiel scientifique consolidé  
**Dernière revue :** 6 août 2026  
**Rôle :** définir ce que le produit peut soutenir, ce qu’il doit présenter avec prudence et ce qu’il ne doit pas affirmer

---

## 1. Finalité

Ce document représente l’état des connaissances utile au produit.

Il ne sert pas à justifier après coup une fonctionnalité ou un choix UX. Il doit au contraire empêcher le produit de transformer une hypothèse, une heuristique ou un résultat expérimental limité en recommandation médicale.

Il distingue :

- les constats scientifiques relativement robustes ;
- les résultats prometteurs mais encore incertains ;
- les bénéfices adjacents à la rupture de sédentarité ;
- les choix de conception du produit ;
- les limites et controverses.

---

## 2. Périmètre

Le cadre porte principalement sur :

- le comportement sédentaire au travail de bureau ;
- l’interruption de périodes assises ;
- les activités physiques légères et brèves ;
- les interventions numériques et les rappels ;
- les micro-pauses ;
- les mouvements simples de mobilité ou de renforcement ;
- les étirements ;
- la récupération visuelle ;
- les mécanismes comportementaux compatibles avec une application volontaire.

Il ne constitue pas :

- une recommandation clinique individuelle ;
- un protocole de rééducation ;
- un programme de prévention des troubles musculosquelettiques ;
- un traitement de la douleur ;
- une prescription d’activité physique ;
- une garantie d’amélioration de santé.

---

## 3. Définitions

### 3.1 Comportement sédentaire

Le comportement sédentaire correspond à une activité éveillée caractérisée par une faible dépense énergétique, réalisée en position assise, inclinée ou allongée.

Le fait d’être physiquement actif en dehors du travail n’empêche pas nécessairement d’accumuler de longues périodes sédentaires.

### 3.2 Inactivité physique

L’inactivité physique désigne le fait de ne pas atteindre les recommandations générales d’activité physique.

Elle ne doit pas être confondue avec le comportement sédentaire.

Une personne peut :

- atteindre les recommandations d’activité physique ;
- et rester néanmoins assise une grande partie de sa journée.

### 3.3 Rupture de sédentarité

Dans le produit, une rupture de sédentarité signifie une interruption de la période assise par un changement de posture ou du mouvement.

Toutes les pauses ne garantissent pas cette rupture.

### 3.4 Micro-pause

Une micro-pause est une interruption courte d’une tâche de travail.

Elle peut impliquer du mouvement, mais peut aussi rester entièrement assise. Les résultats concernant le bien-être au travail ne doivent donc pas être confondus avec ceux concernant la réduction de l’assise.

---

## 4. Échelle de confiance utilisée

### Confiance élevée

Résultat cohérent avec des recommandations institutionnelles ou plusieurs synthèses convergentes. Des incertitudes peuvent subsister sur l’ampleur exacte de l’effet.

### Confiance modérée

Résultat soutenu par des essais ou des revues, mais avec hétérogénéité, limites de transposition, petits échantillons ou durée de suivi insuffisante.

### Confiance faible

Résultat plausible, mais littérature limitée, indirecte, contradictoire ou principalement fondée sur des études aiguës.

### Donnée insuffisante

Aucune conclusion fiable ne peut être tirée pour le contexte précis du produit.

Cette échelle qualifie les affirmations du présent document. Elle ne remplace pas le champ documentaire `evidence_level` du catalogue.

---

## 5. Consensus principal

### 5.1 Limiter le temps sédentaire est cohérent avec les recommandations de santé publique

Les recommandations de l’Organisation mondiale de la santé encouragent les adultes à limiter le temps sédentaire et à remplacer une partie de ce temps par de l’activité physique, y compris d’intensité légère.

**Confiance : élevée.**

### Ce que le produit peut en déduire

Le produit peut légitimement encourager une personne à :

- se lever ;
- marcher ;
- réaliser une activité physique légère ;
- interrompre une période assise.

### Ce qu’il ne peut pas en déduire

Les recommandations générales ne valident pas automatiquement :

- quatre rappels par jour ;
- les horaires `10:10`, `11:50`, `15:10`, `17:30` ;
- l’espacement `90/60` ;
- une durée universelle de pause ;
- un optimum identique pour tous.

Ces paramètres restent des heuristiques produit.

---

## 6. Fréquence et durée des interruptions

Il n’existe pas de protocole universel suffisamment établi permettant d’affirmer que toute personne doit interrompre son assise selon un intervalle précis.

Les études utilisent des protocoles très variés :

- interruptions toutes les 20 ou 30 minutes ;
- rappels horaires ;
- activités de quelques dizaines de secondes à plusieurs minutes ;
- marche, station debout ou mouvements de résistance ;
- interventions isolées ou multicomposantes.

Cette hétérogénéité empêche de transformer un protocole expérimental particulier en règle générale.

**Confiance dans l’absence d’un optimum universel : élevée.**

### Conséquence pour le produit

Les créneaux et l’espacement sont configurables et doivent être décrits comme des choix de conception, pas comme une prescription scientifique.

---

## 7. Efficacité des rappels numériques

### 7.1 Rappels seuls

Une revue systématique et méta-analyse publiée en 2025 a inclus 18 études et 1 164 employés de bureau. Les interventions par rappel informatique ont été associées à une réduction moyenne d’environ 12,5 minutes de temps sédentaire par journée de travail de huit heures et à environ 1 030 pas supplémentaires.

Les résultats sur d’autres indicateurs, notamment certaines transitions assis-debout, le temps debout et les marqueurs cardiométaboliques, étaient moins cohérents ou non significatifs.

Les études étaient souvent petites, de durée variable et exposées à des risques de biais.

**Confiance : modérée pour une réduction modeste de l’assise.**  
**Confiance : faible pour des bénéfices de santé durables attribuables aux rappels seuls.**

### 7.2 Interventions avec plusieurs composantes

Une méta-analyse publiée en 2024 a observé une réduction moyenne proche de 30 minutes par journée de huit heures pour des interventions de travail incorporant des éléments numériques. Les interventions étaient fréquemment multicomposantes : information, soutien organisationnel, accompagnement ou modifications du contexte.

L’hétérogénéité était importante. L’effet ne peut pas être attribué à la notification seule.

**Confiance : modérée pour la réduction de l’assise par des interventions multicomposantes.**  
**Transposition à l’application V0 : limitée.**

### 7.3 Applications mobiles

Une revue systématique et méta-analyse publiée en 2026 a inclus 38 études portant sur des interventions mobiles chez des employés de bureau. Elle suggère une réduction du temps sédentaire et une augmentation des pas, mais souligne une forte hétérogénéité, des risques de biais et une certitude faible à très faible pour plusieurs résultats.

**Confiance : faible à modérée.**

### Interprétation prudente

Une application de rappels peut contribuer à réduire modestement l’assise, mais :

- l’effet moyen ne prédit pas l’effet individuel ;
- la durabilité reste incertaine ;
- les rappels peuvent être ignorés ;
- le contexte organisationnel influence l’efficacité ;
- une notification ne garantit pas qu’une activité est effectuée ;
- une application seule ne reproduit pas une intervention multicomposante.

---

## 8. Marche légère

Des essais expérimentaux montrent que de courtes périodes de marche légère interrompant une assise prolongée peuvent améliorer certains marqueurs métaboliques aigus, notamment la glycémie postprandiale.

Les protocoles ont souvent été réalisés :

- en laboratoire ;
- sur une seule journée ;
- chez des adultes en surpoids, inactifs ou atteints de diabète de type 2 ;
- avec des interruptions répétées et contrôlées.

Ces résultats soutiennent la plausibilité de la marche comme activité prioritaire, mais ne démontrent pas l’effet clinique à long terme d’une proposition occasionnelle dans une application grand public.

**Confiance : modérée pour les effets métaboliques aigus.**  
**Confiance : faible pour les effets cliniques à long terme de la V0.**

### Conséquence pour le catalogue

Les activités de marche peuvent recevoir un niveau documentaire élevé lorsqu’elles représentent directement le comportement étudié.

Le produit ne doit toutefois pas promettre :

- une baisse de glycémie ;
- une prévention du diabète ;
- un effet cardiovasculaire mesurable ;
- un bénéfice individuel garanti.

---

## 9. Mouvements simples de résistance

Des essais contrôlés ont étudié de courtes séquences comprenant notamment :

- demi-squats ;
- élévations de mollets ;
- contractions des fessiers ;
- montées de genoux.

Chez des adultes atteints de diabète de type 2, des séquences de trois minutes répétées toutes les trente minutes ont amélioré des marqueurs glycémiques aigus par rapport à une assise ininterrompue.

Ces résultats justifient l’inclusion prudente de mouvements simples comme les squats, les élévations de mollets ou les levers de chaise.

Ils ne valident pas directement :

- le nombre exact de répétitions du catalogue ;
- la fréquence des notifications ;
- une efficacité identique chez tous les employés ;
- un bénéfice clinique durable.

**Confiance : modérée pour les effets aigus de séquences répétées.**  
**Transposition aux activités isolées de la V0 : modérée à faible.**

---

## 10. Station debout

Remplacer l’assise par la station debout modifie la posture, mais l’effet métabolique semble généralement moins marqué et moins constant que celui d’une marche légère.

Certaines études observent des bénéfices, tandis que d’autres trouvent peu de différence sur certains indicateurs.

La station debout ne doit pas être présentée comme équivalente à une activité physique légère.

**Confiance : modérée.**

### Conséquence

Le produit peut distinguer :

- une rupture certaine par marche ou mouvement ;
- une rupture probable par activité normalement réalisée debout ;
- une activité simplement compatible avec la position debout.

---

## 11. Micro-pauses, fatigue et performance

Une méta-analyse de 2022 portant sur 22 échantillons et 2 335 participants a observé de petits effets favorables des micro-pauses sur :

- la vigueur ;
- la fatigue.

L’effet global sur la performance n’était pas statistiquement significatif. Les effets variaient selon les tâches, les activités réalisées pendant la pause et leur durée.

**Confiance : modérée pour un petit effet immédiat sur le bien-être.**  
**Confiance : faible pour une amélioration générale de la performance.**

### Ce que le produit peut soutenir

Une courte pause peut aider certaines personnes à se sentir momentanément moins fatiguées ou plus disponibles.

### Ce qu’il ne doit pas affirmer

- les micro-pauses augmentent nécessairement la productivité ;
- toute pause de moins de cinq minutes restaure la concentration ;
- une fréquence déterminée optimise les performances.

Le produit n’est pas un outil de productivité.

---

## 12. Mobilité et étirements

La littérature sur les programmes d’étirements au travail montre des résultats hétérogènes.

Certaines études observent une diminution de l’inconfort ou de certains symptômes. D’autres ne permettent pas de conclure clairement. Les protocoles sont souvent plus longs, plus encadrés et plus réguliers que les activités isolées proposées par l’application.

La mobilité douce et les étirements peuvent être inclus pour :

- varier les propositions ;
- offrir une action corporelle simple ;
- procurer un confort subjectif ;
- favoriser un changement de posture.

Ils ne doivent pas être présentés comme capables, à eux seuls, de :

- prévenir les troubles musculosquelettiques ;
- corriger la posture ;
- traiter une douleur ;
- restaurer une amplitude articulaire ;
- remplacer une prise en charge ergonomique ou clinique.

**Confiance : faible à modérée selon le résultat étudié.**  
**Preuve spécifique pour une activité isolée de 30 à 60 secondes : faible.**

---

## 13. Respiration et relaxation

Une respiration calme peut constituer une courte activité de récupération ou de régulation subjective.

Dans le contexte du produit, elle ne garantit pas une interruption de l’assise.

Elle doit donc être considérée comme un bénéfice adjacent et non comme une activité équivalente à la marche ou au lever.

Le produit ne doit pas promettre :

- un traitement de l’anxiété ;
- une réduction clinique du stress ;
- une amélioration cardiovasculaire ;
- un effet thérapeutique.

**Confiance pour un effet immédiat de détente subjective : faible à modérée.**  
**Confiance comme rupture de sédentarité : nulle si elle reste assise.**

---

## 14. Pauses visuelles

Les pauses visuelles peuvent réduire temporairement certains symptômes de fatigue visuelle lors de tâches prolongées sur écran.

Une étude expérimentale publiée en 2025 a comparé plusieurs rythmes de pause pendant une tâche de lecture de quarante minutes. Certains programmes ont amélioré des symptômes ou signes, mais l’étude était courte et comprenait 24 jeunes adultes.

La littérature ne permet pas de considérer la règle « 20-20-20 » comme un protocole universel solidement confirmé pour tous les utilisateurs et toutes les situations.

**Confiance : faible à modérée pour un soulagement temporaire.**  
**Confiance : faible pour une fréquence universelle.**

### Conséquence

Les pauses visuelles restent des activités adjacentes. Elles ne sont pas comptées automatiquement comme rupture de sédentarité.

---

## 15. Escaliers et activités plus dynamiques

Prendre les escaliers ou marcher d’un bon pas augmente davantage l’intensité que rester debout.

Leur inclusion est cohérente avec l’objectif général de mouvement, mais les études sur l’interruption de l’assise ne valident pas nécessairement le wording, le contexte ou la fréquence de ces propositions particulières.

Les activités plus dynamiques doivent rester :

- facultatives ;
- compatibles avec les réglages ;
- exclues lorsque les capacités déclarées les rendent inadaptées ;
- formulées sans objectif de performance.

**Confiance : modérée pour l’activité physique elle-même.**  
**Preuve spécifique comme notification V0 : indirecte.**

---

## 16. Ancrages comportementaux

Les rappels, la planification de l’action, les intentions « Si… alors », l’autonomie et la réduction de friction sont des cadres plausibles pour faciliter un comportement.

Cependant :

- les effets varient selon les personnes et les contextes ;
- aucune théorie ne justifie à elle seule une détection intrusive ;
- l’existence d’un cadre comportemental ne prouve pas l’efficacité d’une fonctionnalité ;
- une application simple peut fonctionner sans profilage ni apprentissage automatique.

**Confiance : modérée pour leur pertinence générale.**  
**Confiance : faible pour un mécanisme précis non testé dans l’application.**

### Classement produit

- **V0 :** rappels configurables, choix, report, abandon, faible friction ;
- **V1 :** ancrages « Si… alors » choisis volontairement, seulement après retours ;
- **V2 :** déclencheurs contextuels automatiques, à considérer comme expérimentaux.

---

## 17. Hiérarchie scientifique interne du catalogue

Le catalogue utilise les niveaux suivants.

### `high`

La famille d’activité correspond directement à un comportement étudié et cohérent avec la rupture de l’assise.

Exemples possibles :

- marche légère ;
- marche plus soutenue.

Ce niveau ne signifie pas que la fiche exacte, sa durée et son texte ont été validés par un essai.

### `moderate`

Un appui expérimental existe, mais la transposition est imparfaite.

Exemples possibles :

- squats simples ;
- élévations de mollets ;
- levers de chaise ;
- escaliers ;
- regard au loin selon l’objectif visuel.

### `limited`

L’activité peut être utile pour le confort, la mobilité ou la récupération, mais la preuve spécifique est faible ou hétérogène.

Exemples possibles :

- mobilité des épaules ;
- rotations du tronc ;
- étirements ;
- respiration ;
- relâchement musculaire ;
- certaines pauses visuelles.

### `indirect`

L’activité ou sa formulation provient principalement d’un choix éditorial ou comportemental du produit.

Exemples possibles :

- aller boire un verre d’eau comme ancrage de marche ;
- marcher dehors ;
- routine de début de journée.

### Limite importante

`evidence_level` est une métadonnée documentaire.

Le moteur V0 ne doit pas l’utiliser comme multiplicateur caché de score. L’inclusion et le poids de base sont validés en amont.

---

## 18. Effet attendu sur la rupture de l’assise

Le catalogue distingue également :

### `certain`

L’activité impose de quitter l’assise.

### `probable`

L’activité se réalise normalement debout, mais une variante assise peut parfois exister.

### `optional`

L’activité peut être effectuée assis ou debout.

### `adjacent_only`

L’activité apporte un bénéfice adjacent sans garantir de rupture de l’assise.

Cette classification décrit le comportement attendu. Elle n’est pas un niveau de preuve scientifique.

---

## 19. Affirmations acceptables

Le produit peut employer des formulations prudentes telles que :

- « Bouger un peu au travail »
- « Interrompre une période assise »
- « Se lever ou marcher quelques minutes »
- « Faire une courte pause »
- « Regarder quelques instants au loin »
- « Choisir une activité adaptée à ce qui est possible »

Dans les documents explicatifs, il peut indiquer que :

- limiter le temps sédentaire est recommandé ;
- remplacer une partie de l’assise par du mouvement léger est cohérent avec les recommandations ;
- les rappels numériques peuvent réduire modestement le temps assis ;
- les effets varient selon les personnes et les contextes.

---

## 20. Affirmations à éviter

Le produit ne doit pas affirmer :

- « cette application protège votre santé » ;
- « ces quatre pauses sont scientifiquement optimales » ;
- « bouger toutes les 90 minutes est la recommandation officielle » ;
- « cette activité prévient les troubles musculosquelettiques » ;
- « cet étirement corrige votre posture » ;
- « cette pause améliore votre productivité » ;
- « cette activité réduit votre glycémie » ;
- « la règle 20-20-20 est prouvée pour tous » ;
- « vous avez interrompu votre sédentarité » lorsqu’une activité adjacente a été proposée ;
- « C’est parti » signifie que l’activité a été réalisée.

---

## 21. Sécurité et limites individuelles

Le produit s’adresse à un usage général et volontaire.

Les consignes doivent rester compatibles avec les garde-fous du catalogue :

- ne pas forcer une amplitude ;
- arrêter en cas de douleur ;
- arrêter en cas de vertige ou de malaise ;
- utiliser un support stable si nécessaire ;
- utiliser une chaise stable pour les levers ;
- revenir à une respiration habituelle en cas d’inconfort.

L’application ne peut pas connaître :

- les antécédents ;
- les limitations fonctionnelles ;
- les contre-indications ;
- l’environnement exact ;
- la qualité d’exécution.

Elle ne doit donc pas personnaliser médicalement une activité.

---

## 22. Conséquences par version

### V0

Le cadre soutient :

- une priorité éditoriale aux activités qui quittent l’assise ;
- la présence de marche et de mouvements simples ;
- une diversité incluant des bénéfices adjacents clairement identifiés ;
- des rappels configurables et non présentés comme optimaux ;
- une communication prudente ;
- l’absence de promesse médicale ;
- un moteur explicable sans usage runtime du niveau de preuve.

### V1

Après retours d’usage, la recherche pourra examiner :

- l’acceptabilité réelle des catégories ;
- la fréquence de report et d’abandon ;
- la compréhension de la distinction entre mouvement et récupération ;
- l’effet d’ancrages volontaires ;
- l’utilité d’un aperçu non culpabilisant ;
- la nécessité de revoir les poids éditoriaux.

Ces observations locales ne démontreront pas une efficacité médicale.

### V2

Restent expérimentaux :

- l’adaptation automatique des horaires ;
- le fading des notifications ;
- les déclencheurs contextuels ;
- la personnalisation avancée ;
- la micro-randomisation dans un protocole de recherche ;
- toute mesure d’efficacité clinique.

Une étude de recherche nécessiterait un protocole, des critères définis, une gestion éthique et une analyse distincte du produit courant.

---

## 23. Principales incertitudes

Les questions suivantes restent insuffisamment résolues :

- fréquence optimale des interruptions pour différents profils ;
- effet à long terme des rappels seuls ;
- maintien de l’adhésion au-delà de quelques semaines ou mois ;
- efficacité spécifique en télétravail ;
- compromis entre fréquence utile et fatigue notificationnelle ;
- effet indépendant de chaque type de contenu ;
- bénéfice clinique réel d’activités très courtes et occasionnelles ;
- capacité des interventions entièrement hors ligne à maintenir leur effet ;
- généralisation des essais de laboratoire à la vie professionnelle réelle ;
- différence d’effet entre station debout, marche et mouvements simples selon les individus.

Le produit ne doit pas masquer ces incertitudes.

---

## 24. Gouvernance scientifique

### Révision

Le cadre doit être réexaminé :

- avant une nouvelle version majeure du catalogue ;
- avant toute promesse nouvelle ;
- avant l’ajout d’un mode présenté comme « recommandations » ;
- lorsqu’une synthèse importante est publiée ;
- au minimum une fois par an pendant le développement actif.

### Traçabilité

Toute modification scientifique doit préciser :

- la source ;
- le type d’étude ;
- la population ;
- le résultat principal ;
- les limites ;
- la conséquence documentaire ;
- l’absence ou la présence d’une conséquence produit.

### Séparation des rôles

Le référentiel scientifique peut :

- modifier le niveau de confiance ;
- signaler une controverse ;
- recommander de retirer une affirmation non soutenue ;
- demander une consigne de prudence.

Il ne décide pas seul :

- des horaires ;
- du nombre de créneaux ;
- de la navigation ;
- des boutons ;
- de l’architecture Android ;
- des priorités de développement.

Ces éléments relèvent de la conception produit et doivent être identifiés comme tels.

---

## 25. Références principales

1. World Health Organization. *WHO Guidelines on Physical Activity and Sedentary Behaviour*. 2020. ISBN 978-92-4-001512-8.

2. Bull FC, et al. World Health Organization 2020 guidelines on physical activity and sedentary behaviour. *British Journal of Sports Medicine*. 2020;54:1451–1462. DOI: `10.1136/bjsports-2020-102955`.

3. Parés-Salomón I, et al. Effectiveness of workplace interventions with digital elements to reduce sedentary behaviours in office employees: a systematic review and meta-analysis. *International Journal of Behavioral Nutrition and Physical Activity*. 2024;21:41. DOI: `10.1186/s12966-024-01595-6`.

4. Leppe-Zamora J, et al. The effect of computer prompt in breaks of sedentary behaviour among office workers: a systematic review and meta-analysis. *International Journal of Behavioral Nutrition and Physical Activity*. 2025;22:75. DOI: `10.1186/s12966-025-01781-0`.

5. Chandrasekaran B, et al. Effectiveness of mHealth Interventions on Physical Activity and Sedentary Behavior Among Office Workers: A Systematic Review and Meta-analysis. *Journal of Occupational Rehabilitation*. 2026. DOI: `10.1007/s10926-026-10373-z`.

6. Albulescu P, et al. “Give me a break!” A systematic review and meta-analysis on the efficacy of micro-breaks for increasing well-being and performance. *PLOS ONE*. 2022;17:e0272460. DOI: `10.1371/journal.pone.0272460`.

7. Dempsey PC, et al. Benefits for Type 2 Diabetes of Interrupting Prolonged Sitting With Brief Bouts of Light Walking or Simple Resistance Activities. *Diabetes Care*. 2016. PMID: `27208318`.

8. Dempsey PC, et al. Interrupting prolonged sitting with brief bouts of light walking or simple resistance activities reduces resting blood pressure and plasma noradrenaline. 2016. PMID: `27512975`.

9. Bailey DP, Locke CD. Breaking up prolonged sitting with light-intensity walking improves postprandial glycemia, but breaking up sitting with standing does not. 2015. PMID: `24704421`.

10. Larsen RN, et al. Does the type of activity “break” from prolonged sitting differentially impact on postprandial blood glucose reductions? 2017. PMID: `28340302`.

11. da Costa BR, Vieira ER. Stretching to reduce work-related musculoskeletal disorders: a systematic review. 2008. PMID: `18461255`.

12. Van Eerd D, et al. Effectiveness of workplace interventions in the prevention of upper extremity musculoskeletal disorders and symptoms: an update of the evidence. 2016. PMID: `26552695`.

13. Redondo B, et al. The impact of break schedules on digital eye strain symptoms and ocular signs during sustained digital reading. 2025. PMID: `40466853`.

---

## 26. Conclusion

L’état des connaissances soutient le principe général de l’application :

> faciliter le remplacement ponctuel de l’assise par du mouvement, même léger, dans un cadre volontaire et peu intrusif.

Il soutient plus fortement :

- la marche ;
- les activités physiques légères ;
- certains mouvements simples de résistance ;
- la réduction pragmatique de longues périodes assises.

Il soutient avec davantage de prudence :

- les étirements ;
- la mobilité isolée ;
- les pauses visuelles ;
- la respiration ;
- les effets sur le confort, la fatigue ou la performance.

Il ne permet pas de présenter les horaires, la fréquence, les durées ou les coefficients du moteur comme scientifiquement optimaux.

La crédibilité du produit dépend moins de la quantité de références affichées que de sa capacité à respecter ces limites.
