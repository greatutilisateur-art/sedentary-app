# Architecture Android V0.1 — corrections avant le premier build

**Date :** 7 août 2026

Ce document complète l'audit `AndroidArchitecture_V0.md`.

## 1. SDK

Décision corrigée :

```text
minSdk = 26
compileSdk = 37
targetSdk = 36
```

La cible produit reste Android 16 / API 36.

`compileSdk` passe à 37 uniquement pour rester compatible avec les bibliothèques Compose stables actuelles. Ce changement n'augmente pas le niveau Android minimum supporté.

## 2. Toolchain de départ

```text
Android Gradle Plugin  9.3.1
Gradle                 9.5.0
Kotlin                 2.3.21
Compose BOM            2026.06.00
JDK                    17
```

Le module Android utilisera le support Kotlin intégré d'AGP 9.x.

Le module `:engine`, Kotlin/JVM pur, utilisera le plugin Kotlin JVM.

## 3. AlarmManager

La première implémentation doit privilégier une alarme **inexacte utilisable en idle** :

```text
AlarmManager.setAndAllowWhileIdle(...)
```

Motif :

- les rappels sont des actions visibles choisies par l'utilisateur ;
- le téléphone peut être verrouillé ou entrer en Doze ;
- aucune permission d'alarme exacte n'est nécessaire.

La règle métier reste :

```text
retard réel > 15 min
→ opportunité abandonnée
→ aucun rattrapage
```

Les retards réels devront être mesurés sur téléphone avant diffusion.

## 4. Ce qui ne change pas

V0 :

- deux modules : `:app` + `:engine` ;
- moteur Kotlin pur ;
- Compose ;
- DataStore ;
- pas Room ;
- pas Hilt ;
- pas de serveur ;
- pas de permission INTERNET ;
- pas d'alarme exacte ;
- une seule prochaine alarme logique ;
- une seule notification d'activité visible.

V1 :

- catalogue manuel ;
- contexte télétravail ;
- uniquement après retours réels.

V2 :

- aucune sophistication supplémentaire anticipée.
