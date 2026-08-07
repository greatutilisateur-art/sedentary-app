# Étape pratique suivante — créer le squelette Android

## Avant de commencer

Ne pousse pas encore de nouveau code sur GitHub.

L'objectif de cette étape est seulement d'obtenir un projet Android qui :

1. s'ouvre ;
2. se synchronise avec Gradle ;
3. compile ;
4. affiche un écran minimal.

Ensuite seulement on intégrera le moteur Kotlin déjà préparé.

## Petite correction technique par rapport à l'audit précédent

Configuration de départ recommandée :

```text
minSdk = 26
compileSdk = 37
targetSdk = 36
AGP = 9.3.1
Gradle = 9.5.0
Kotlin = 2.3.21
Compose BOM = 2026.06.00
```

`compileSdk` passe à 37 parce que les versions Compose stables actuelles l'exigent.
`targetSdk` reste 36.

## Sur ton PC personnel

### 1. Synchroniser GitHub

Dans GitHub Desktop :

1. sélectionner le dépôt `sedentary-app` ;
2. vérifier que la branche est `main` ;
3. cliquer sur `Fetch origin` ;
4. si `Pull origin` apparaît, cliquer dessus ;
5. vérifier qu'il n'y a pas de changement local en attente.

### 2. Créer le projet Android

Dans Android Studio :

1. `File` → `New` → `New Project` ;
2. choisir **Empty Activity** ;
3. nom : `SedentaryApp` ;
4. package : `org.sedentaryapp` ;
5. emplacement : le dossier `android` du dépôt ;
6. langage : Kotlin ;
7. Minimum SDK : API 26 ;
8. terminer la création.

Le package `org.sedentaryapp` est un identifiant technique provisoire. Il devra être figé avant une diffusion publique.

### 3. Premier test

Attendre la fin du téléchargement/synchronisation Gradle.

Puis :

1. `Build` → `Make Project` ;
2. vérifier que le build se termine sans erreur.

À ce stade, ne modifie pas encore l'écran généré.

### 4. Ce qu'il faut me renvoyer

Si le build réussit :
- dis simplement `build OK`.

S'il échoue :
- copie le texte rouge de l'erreur ou envoie une capture.

## Étape suivante immédiatement après

On ajoutera le module `:engine`, puis on copiera le port Kotlin déjà préparé.

Le port ne contient volontairement aucune dépendance Android.
