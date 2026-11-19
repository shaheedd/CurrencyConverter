# TP4 Android — Currency Converter

Application Android Kotlin qui convertit un montant d'une devise vers une autre.
Le projet est réalisé avec Android Studio, Kotlin et des layouts XML.

## Fonctionnalités

- Interface en `ConstraintLayout` avec une zone d'affichage turquoise et un clavier numérique `TableLayout` 4 × 3.
- Deux `Spinner` alimentés depuis le `string-array` `R.array.devises` (USD, EUR, TND, GBP, JPY, CAD).
- Récupération des vues avec **View Binding** (`ActivityMainBinding`).
- Saisie uniquement via le clavier personnalisé : un seul point décimal, 10 caractères maximum, touche `C` pour effacer.
- Conversion avec l'euro comme devise pivot, résultat formaté avec deux décimales (`Locale.US`).
- `Toast` si l'utilisateur appuie sur **CONVERTIR** sans saisir de montant.
- Bouton d'inversion des deux devises.
- Écran verrouillé en portrait dans le `AndroidManifest.xml`.
- Toutes les chaînes et couleurs sont définies dans `res/values/strings.xml` et `res/values/colors.xml`.

## Bonus implémentés

1. **Conversion automatique** : le résultat est recalculé à chaque chiffre saisi et à chaque changement de devise.
2. **Taux réels via API** : l'API gratuite `open.er-api.com` est utilisée via Retrofit et des coroutines. Les taux fixes du TP restent actifs par défaut ; toucher le libellé en bas de l'écran charge les taux réels, puis permet de revenir aux taux fixes. En cas d'absence de réseau, l'application replie automatiquement sur les taux fixes.
3. **ViewModel** : la saisie, les devises, le résultat et la source des taux sont conservés après une rotation ou une recréation de l'activité.

## Taux fixes utilisés (pour 1 EUR)

| Devise | Taux |
|---|---:|
| EUR | 1.00 |
| USD | 1.17 |
| TND | 3.40 |
| GBP | 0.87 |
| JPY | 172.50 |
| CAD | 1.62 |

## Tests effectués

| Entrée | Résultat attendu |
|---|---:|
| 120 USD → EUR | 102.56 |
| 100 EUR → TND | 340.00 |
| 50 GBP → USD | 67.24 |
| 10 EUR → EUR | 10.00 |
| 100 USD → EUR puis inversion | 85.47 puis 117.00 |

La saisie, la limite de 10 caractères, le point décimal unique, la touche `C`, le `Toast`, l'inversion, la conversion automatique, le chargement des taux réels et le repli hors ligne ont également été vérifiés sur l'émulateur.

## Captures d'écran

Les captures sont disponibles dans le dossier [`screenshots/`](screenshots/) :

- Démarrage : `00-demarrage.png`
- 120 USD → EUR : `01-120-usd-eur.png`
- 100 EUR → TND : `02-100-eur-tnd.png`
- 50 GBP → USD : `03-50-gbp-usd.png`
- 10 EUR → EUR : `04-10-eur-eur.png`

## Compte rendu

Le compte rendu PDF se trouve dans [`compte-rendu/compte-rendu.pdf`](compte-rendu/compte-rendu.pdf). Il est généré par `compte-rendu/generer_pdf.py` (ReportLab).

## Construction et exécution

1. Ouvrir le dossier du projet dans Android Studio.
2. Ajuster `local.properties` (non versionné) avec le chemin du SDK Android si nécessaire.
3. Synchroniser Gradle puis lancer l'application sur un émulateur ou un appareil.

En ligne de commande :

```bash
./gradlew assembleDebug
```

L'APK de debug est généré dans `app/build/outputs/apk/debug/app-debug.apk`.

## Structure principale

```text
app/src/main/java/com/example/currencyconverter/
├── MainActivity.kt        # interface, clavier, conversion, API
├── CurrencyViewModel.kt   # conservation de l'état
└── RatesApi.kt            # service Retrofit

app/src/main/res/
├── layout/activity_main.xml
├── values/strings.xml
├── values/colors.xml
└── values/themes.xml
```

Auteur : Shahed Attari
