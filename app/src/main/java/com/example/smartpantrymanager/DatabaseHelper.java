package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite storage. Three tables:
 *   pantry(id, name, quantity, unit, expiry)
 *   recipe(id, name, steps)
 *   recipe_ingredient(id, recipe_id -> recipe.id, name, quantity, unit)
 * Recipes are seeded once, in onCreate(), the first time the database is made.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipe (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredient (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL REFERENCES recipe(id) ON DELETE CASCADE, "
                + "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL)");
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredient");
        db.execSQL("DROP TABLE IF EXISTS recipe");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }


    public long addItem(PantryItem i) {
        return getWritableDatabase().insert("pantry", null, toValues(i));
    }


    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("pantry", null, null, null, null, null, "name COLLATE NOCASE");
        while (c.moveToNext()) list.add(fromCursor(c));
        c.close();
        return list;
    }


    public PantryItem getItem(long id) {
        Cursor c = getReadableDatabase().query("pantry", null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null);
        PantryItem item = c.moveToFirst() ? fromCursor(c) : null;
        c.close();
        return item;
    }


    public int updateItem(PantryItem i) {
        return getWritableDatabase().update("pantry", toValues(i), "id = ?", new String[]{String.valueOf(i.id)});
    }


    public int deleteItem(long id) {
        return getWritableDatabase().delete("pantry", "id = ?", new String[]{String.valueOf(id)});
    }

    private ContentValues toValues(PantryItem i) {
        ContentValues cv = new ContentValues();
        cv.put("name", i.name);
        cv.put("quantity", i.quantity);
        cv.put("unit", i.unit);
        cv.put("expiry", i.expiry);
        return cv;
    }

    private PantryItem fromCursor(Cursor c) {
        PantryItem i = new PantryItem();
        i.id = c.getLong(c.getColumnIndexOrThrow("id"));
        i.name = c.getString(c.getColumnIndexOrThrow("name"));
        i.quantity = c.getDouble(c.getColumnIndexOrThrow("quantity"));
        i.unit = c.getString(c.getColumnIndexOrThrow("unit"));
        i.expiry = c.getString(c.getColumnIndexOrThrow("expiry"));
        return i;
    }



    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        List<Recipe> list = new ArrayList<>();
        Cursor c = db.query("recipe", null, null, null, null, null, "name");
        while (c.moveToNext()) list.add(recipeFromCursor(db, c));
        c.close();
        return list;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("recipe", null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        Recipe r = c.moveToFirst() ? recipeFromCursor(db, c) : null;
        c.close();
        return r;
    }

    private Recipe recipeFromCursor(SQLiteDatabase db, Cursor c) {
        Recipe r = new Recipe();
        r.id = c.getLong(c.getColumnIndexOrThrow("id"));
        r.name = c.getString(c.getColumnIndexOrThrow("name"));
        r.steps = c.getString(c.getColumnIndexOrThrow("steps"));
        Cursor ic = db.query("recipe_ingredient", null, "recipe_id = ?",
                new String[]{String.valueOf(r.id)}, null, null, null);
        while (ic.moveToNext()) {
            Recipe.RecipeIngredient ri = new Recipe.RecipeIngredient();
            ri.name = ic.getString(ic.getColumnIndexOrThrow("name"));
            ri.quantity = ic.getDouble(ic.getColumnIndexOrThrow("quantity"));
            ri.unit = ic.getString(ic.getColumnIndexOrThrow("unit"));
            r.ingredients.add(ri);
        }
        ic.close();
        return r;
    }

    /** Ingredient format: "name:quantity:unit". */
    private static void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("steps", steps);
        long rid = db.insert("recipe", null, cv);
        for (String s : ingredients) {
            String[] p = s.split(":");
            ContentValues ic = new ContentValues();
            ic.put("recipe_id", rid);
            ic.put("name", p[0]);
            ic.put("quantity", Double.parseDouble(p[1]));
            ic.put("unit", p[2]);
            db.insert("recipe_ingredient", null, ic);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs", "1. Whisk the eggs with salt.\n2. Melt butter in a pan.\n3. Stir the eggs gently until just set.",
                "egg:3:pcs", "butter:10:g", "salt:1:tsp");
        addRecipe(db, "Cheese Omelette", "1. Beat the eggs.\n2. Fry in butter until almost set.\n3. Add grated cheese, fold and serve.",
                "egg:2:pcs", "cheese:50:g", "butter:10:g");
        addRecipe(db, "Tomato Pasta", "1. Boil the pasta.\n2. Fry onion and garlic in oil.\n3. Add chopped tomatoes and simmer 10 min.\n4. Toss with pasta.",
                "pasta:200:g", "tomato:3:pcs", "onion:1:pcs", "garlic:2:pcs", "olive oil:2:tbsp");
        addRecipe(db, "Pap and Tomato Relish", "1. Cook the maize meal into stiff pap.\n2. Fry onion in oil, add tomatoes and simmer into a relish.\n3. Serve together.",
                "maize meal:250:g", "tomato:2:pcs", "onion:1:pcs", "oil:1:tbsp");
        addRecipe(db, "Egg Fried Rice", "1. Cook and cool the rice.\n2. Stir-fry onion and carrot.\n3. Add rice and soy sauce.\n4. Push aside, scramble eggs, mix through.",
                "rice:200:g", "egg:2:pcs", "carrot:1:pcs", "onion:1:pcs", "soy sauce:2:tbsp");
        addRecipe(db, "Pancakes", "1. Mix flour, egg, milk and sugar into a batter.\n2. Rest 10 min.\n3. Fry thin pancakes on both sides.",
                "flour:200:g", "egg:1:pcs", "milk:300:ml", "sugar:1:tbsp");
        addRecipe(db, "Cheese Toastie", "1. Butter the bread.\n2. Fill with cheese.\n3. Toast in a pan until golden.",
                "bread:4:pcs", "cheese:80:g", "butter:10:g");
        addRecipe(db, "Mashed Potatoes", "1. Boil peeled potatoes until soft.\n2. Drain and mash with butter and milk.",
                "potato:4:pcs", "butter:30:g", "milk:50:ml");
        addRecipe(db, "Chicken Stir-fry", "1. Slice chicken and vegetables.\n2. Fry chicken in oil until cooked.\n3. Add onion and carrot, then soy sauce.",
                "chicken:300:g", "onion:1:pcs", "carrot:1:pcs", "soy sauce:2:tbsp", "oil:1:tbsp");
        addRecipe(db, "Vegetable Soup", "1. Dice potatoes, carrots and onion.\n2. Simmer in stock for 25 min.\n3. Season and serve.",
                "potato:2:pcs", "carrot:2:pcs", "onion:1:pcs", "vegetable stock:500:ml");
        addRecipe(db, "Garlic Bread", "1. Mix butter with crushed garlic.\n2. Spread on bread.\n3. Bake at 200C for 10 min.",
                "bread:4:pcs", "butter:40:g", "garlic:2:pcs");
        addRecipe(db, "Tuna Salad", "1. Drain the tuna.\n2. Chop tomatoes and onion.\n3. Toss with olive oil.",
                "tuna:1:pcs", "tomato:2:pcs", "onion:1:pcs", "olive oil:1:tbsp");
        addRecipe(db, "Banana Smoothie", "1. Peel the bananas.\n2. Blend with milk and sugar until smooth.",
                "banana:2:pcs", "milk:250:ml", "sugar:1:tsp");
        addRecipe(db, "Beans on Toast", "1. Heat the beans.\n2. Toast and butter the bread.\n3. Pour beans over the toast.",
                "baked beans:1:pcs", "bread:2:pcs", "butter:10:g");
        addRecipe(db, "Rice and Beans", "1. Cook the rice.\n2. Fry onion in oil, add beans and heat through.\n3. Serve over rice.",
                "rice:200:g", "baked beans:1:pcs", "onion:1:pcs", "oil:1:tbsp");
        addRecipe(db, "French Toast", "1. Whisk eggs, milk and sugar.\n2. Dip bread slices.\n3. Fry until golden on both sides.",
                "bread:4:pcs", "egg:2:pcs", "milk:100:ml", "sugar:1:tsp");
        addRecipe(db, "Potato Wedges", "1. Cut potatoes into wedges.\n2. Toss with oil and salt.\n3. Roast at 220C for 30 min.",
                "potato:4:pcs", "oil:2:tbsp", "salt:1:tsp");
    }
}
