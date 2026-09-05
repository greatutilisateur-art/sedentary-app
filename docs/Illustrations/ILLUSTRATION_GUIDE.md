# Intervalles — Charte des illustrations

## Statut

Direction visuelle actuelle des illustrations d’Intervalles.

Les 23 activités actuellement publiées disposent d’une illustration intégrée.

Le catalogue actif définit l’association entre une activité et sa ressource via `illustration_resource`.

## Finalité

Les illustrations sont fonctionnelles : elles doivent aider à comprendre ou évoquer l’activité en un coup d’œil, sans remplacer la consigne textuelle.

## Direction graphique

- line art continu ou visuellement continu ;
- style épuré, calme, adulte, légèrement poétique ;
- aucune esthétique fitness, médicale ou enfantine ;
- personnages peu genrés, ou cadrage partiel sur la zone utile ;
- peu ou pas de visage ;
- pas de remplissage lourd ;
- objets seulement lorsqu’ils contribuent à la compréhension ;
- pas de texte dans l’image.

## Palette

- trait principal : `#4F756D`
- trait secondaire / flèche : `#8EAAA2`
- halo léger : `#EEF2EF`
- crème éventuel : `#F7F8F6`
- fond final : transparent

## Trait

- fin à moyen ;
- extrémités arrondies ;
- courbes fluides et organiques ;
- épaisseur globalement constante ;
- pas de hachures, ombres ou contours noirs.

## Cadrage

Le cadrage est choisi selon l’activité, sans règle mécanique :

- corps entier si la présence globale apporte quelque chose ;
- fragment corporel si une zone suffit ou améliore la lisibilité ;
- objet/contexte seul ou presque si l’action est évidente et le corps n’apporte rien.

Le dessin doit s’arrêter naturellement plutôt que sembler coupé brutalement.

## Mouvement

- aucune flèche si le geste est évident ;
- une seule flèche courte et discrète si nécessaire ;
- éviter les doubles poses ou séquences multiples lorsque cela surcharge la compréhension.

## Halo

- léger ;
- sans contour ;
- intégré à l’asset ;
- sert à ancrer le dessin et à maintenir la lisibilité en light/dark.

## Android

- format final : PNG avec transparence ;
- ratio recommandé : 1:1 ;
- cible de travail : 1080 × 1080 px ;
- nommage : `ill_<activity_id_en_minuscules>.png` ;
- emplacement : `app/src/main/res/drawable/` ;
- référence catalogue : `illustration_resource = "ill_<activity_id_en_minuscules>"`.

## Critères de validation

1. L’image apporte quelque chose en environ une seconde.
2. Le geste est plausible et non ambigu.
3. Le dessin reste lisible à la taille réelle dans la fiche Android.
4. La consigne textuelle reste suffisante seule.
5. Aucun détail décoratif inutile.
6. Cohérence avec le reste de la série.
7. Light et dark restent lisibles.
