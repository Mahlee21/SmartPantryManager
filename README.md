Mobile App Development 700: Smart Pantry Manager

A Java Android app that keeps track of the ingredients you have at home and suggests recipes you can cook *using only what is in your pantry*. A recipe is suggested only if *every* ingredient is available in the required quantity.


| | |
|---|---|
| **Student** | Amahle Mthembu 402307995 |
| **Language** | Java |
| **Platform** | Android  |
| **Package** | `com.example.smartpantry` |
| **Storage** | SQLite (`SQLiteOpenHelper`) and `SharedPreferences` |

Features
- **Pantry management (full CRUD):** add, view, edit and delete ingredients with a name, quantity, unit and optional expiry date.
- **Input validation:** empty names, non-numeric or non-positive quantities, and badly formatted dates are rejected with a clear on-screen error.
- **17 pre-loaded recipes**, created automatically the first time the app runs.
- **Strict recipe matching:** the Suggested Recipes screen only shows recipes you can make right now.
- **Smart matching:** handles singular/plural names (`tomato` = `tomatoes`) and unit conversion (`kg` to `g`, `tbsp` to `ml`).
- **Recipe detail screen** with the full ingredient list and method.
- **Expiry alerts:** items expiring within 3 days (or already expired) are highlighted in red. This can be switched off in Settings.
- **No maps, GPS or location features**, and no location permissions are requested.


 Screens and navigation

| Screen | Class | How you get there |
|---|---|---|
| Pantry List (launcher) | `MainActivity` | App start |
| Add / Edit Ingredient | `AddEditActivity` | "+ Add ingredient" button, or tap an item to edit it |
| Suggested Recipes | `SuggestedActivity` | Toolbar menu, "Suggested Recipes" |
| Recipe Detail | `RecipeDetailActivity` | Tap a suggested recipe |
| Settings | `SettingsActivity` | Toolbar menu, "Settings" |

Navigation uses explicit intents. Data is passed between screens with intent extras (`ITEM_ID`, `RECIPE_ID`).

## How to use

1. Open the app. The Pantry List shows your ingredients, or a message if it is empty.
2. Tap **+ Add ingredient**, enter a name, quantity and unit (and an expiry date if you want), then tap **Save**.
3. Tap an item to **edit** it, or tap **Delete** and confirm to remove it.
4. Open the toolbar menu and choose **Suggested Recipes** to see what you can cook.
5. Tap a recipe to see its ingredients and method.
6. Open **Settings** to turn the expiring-soon highlight on or off.

Validation rules

| Field | Rule |
|---|---|
| Name | Required |
| Quantity | Must be a number greater than 0 |
| Unit | Chosen from: `g`, `kg`, `ml`, `l`, `tsp`, `tbsp`, `cup`, `pcs` |
| Expiry | Optional. If entered, it must be a real date in `yyyy-MM-dd` format (strict, so `2026-02-30` is rejected) |


## The strict-matching rule

The core logic lives in `RecipeMatcher.java`. A recipe is suggested **only if every one of its ingredients** is covered by the pantry in at least the required quantity. One missing or insufficient ingredient excludes the recipe.

To make this reliable in practice:

- **Names are normalised:** converted to lower case, punctuation removed and each word made singular, so `Tomatoes`, `tomato` and `TOMATO` all match.
- **Units are converted to a base unit** before comparing:

  | Kind | Conversions |
  |---|---|
  | Mass (base `g`) | `kg` = 1000 g |
  | Volume (base `ml`) | `l` = 1000 ml, `cup` = 250 ml, `tbsp` = 15 ml, `tsp` = 5 ml |
  | Count | `pcs` (no conversion) |

- **Duplicate pantry rows are summed:** two entries of rice (100 g + 100 g) count as 200 g.
- **Different kinds of unit never match:** grams cannot be compared with pieces, so `tomato 500 g` does not satisfy a recipe that needs `tomato 3 pcs`.

### Quick test scenarios

| Add to pantry | Expected result |
|---|---|
| `egg` 3 pcs, `butter` 10 g, `salt` 1 tsp | **Scrambled Eggs** is suggested |
| `Eggs` 3 pcs (plural, capitalised) with the same items | Still suggested (name normalisation) |
| `butter` 0.01 kg instead of 10 g | Still suggested (unit conversion) |
| `rice` 100 g and `rice` 100 g | Counts as 200 g toward **Egg Fried Rice** / **Rice and Beans** |
| `olive oil` 1 l | Satisfies the `2 tbsp` olive oil needed for **Tomato Pasta** |
| `tomato` 500 g (but the recipe needs pieces) | Recipe is **not** suggested |
| Empty pantry | "No recipes match your pantry yet" message is shown |

---

## Database

SQLite, through `DatabaseHelper` (`SQLiteOpenHelper`). I chose it because it is local, needs no network or account, persists across app restarts, and is the approach covered in the module's persistent data chapter.

```
pantry              (id, name, quantity, unit, expiry)
recipe              (id, name, steps)
recipe_ingredient   (id, recipe_id -> recipe.id ON DELETE CASCADE, name, quantity, unit)
```

- Database file: `pantry.db`, version 1.
- Foreign keys are enabled in `onConfigure()`.
- The 17 recipes are seeded once, in `onCreate()`, the first time the database is created.
- The expiry-alert setting is stored separately in `SharedPreferences` (file `settings`, key `expiryAlerts`, default `true`).

### Pre-loaded recipes

Scrambled Eggs, Cheese Omelette, Tomato Pasta, Pap and Tomato Relish, Egg Fried Rice, Pancakes, Cheese Toastie, Mashed Potatoes, Chicken Stir-fry, Vegetable Soup, Garlic Bread, Tuna Salad, Banana Smoothie, Beans on Toast, Rice and Beans, French Toast, Potato Wedges.


## Project structure

```
SmartPantry/
├── AndroidManifest.xml
├── SETUP_IN_ANDROID_STUDIO.txt
├── java/
│   ├── MainActivity.java          Pantry list, menu navigation, delete dialog
│   ├── AddEditActivity.java       Add / edit form with validation
│   ├── SuggestedActivity.java     Runs the matcher and lists results
│   ├── RecipeDetailActivity.java  Ingredients and method
│   ├── SettingsActivity.java      Expiry-alert switch (SharedPreferences)
│   ├── DatabaseHelper.java        SQLite schema, seeding, CRUD
│   ├── RecipeMatcher.java         Strict-matching logic
│   ├── PantryAdapter.java         RecyclerView adapter for pantry rows
│   ├── RecipeAdapter.java         RecyclerView adapter for recipes
│   ├── PantryItem.java            Model
│   └── Recipe.java                Model (with nested RecipeIngredient)
└── res/
    ├── layout/                    activity_* and item_* layouts
    ├── menu/menu_main.xml         Toolbar menu
    └── values/strings.xml         Unit list for the spinner
```

---

## Setup and run

1. In Android Studio, create **New Project > Empty Views Activity**, with Language **Java**, Package **`com.example.smartpantry`**
2. Delete the generated `MainActivity.java` and `res/layout/activity_main.xml`.
3. Copy everything in `java/` into `app/src/main/java/com/example/smartpantry/`.
4. Copy `res/layout/*` and `res/menu/*` into `app/src/main/res/` (create `res/menu` if it is missing).
5. Merge `res/values/strings.xml`: add the `<string-array name="units">` block to the existing `strings.xml`.
6. Merge `AndroidManifest.xml`: register the 5 `<activity>` entries inside your `<application>` and keep `MainActivity` as the launcher.
7. In `app/build.gradle`, make sure these dependencies exist, then **Sync**:
   ```
   implementation 'androidx.appcompat:appcompat:1.6.1'
   implementation 'androidx.recyclerview:recyclerview:1.3.2'
   ```
8. Pick an emulator (API 24+) or a physical device and press **Run**. Recipes are created automatically on first launch.

### Troubleshooting

| Symptom | Likely cause and fix |
|---|---|
| Crash with `no such table` / `no such column` | Old database from an earlier install. Uninstall the app (or clear its data) and run again. |
| `ClassNotFoundException` / "Unable to instantiate activity" | Package name differs from `com.example.smartpantry`. Match the package, or update the `namespace` in `build.gradle`. |
| `You need to use a Theme.AppCompat theme` | The manifest theme is not an AppCompat/Material theme. Use `Theme.AppCompat.Light.DarkActionBar` or your project's Material theme. |
| Crash on tapping Add / Suggested / Settings | The activity is not registered in `AndroidManifest.xml`. |
| Crash opening the Add screen | The `units` string-array was not added to `strings.xml`. |
| `NullPointerException` on `R.id.main` | The generated `MainActivity` was not replaced by the one from `java/`. 

 Assumptions and known limitations

- Recipes are built in and cannot be added or edited from the app.
- Cooking a recipe does **not** deduct ingredients from the pantry.
- Recipe ingredients that are counted (eggs, onions, garlic cloves, etc.) use `pcs`, so pantry items must also use `pcs` to match them.
- Singular/plural handling covers common English patterns (`-s`, `-es`, `-ies`) but not irregular plurals.
- Changing `DB_VERSION` rebuilds all tables, which erases pantry data.
