package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.model.PantryItem;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        long id = getIntent().getLongExtra("RECIPE_ID", -1);
        Recipe r = new DatabaseHelper(this).getRecipe(id);
        if (r == null) {
            finish();
            return;
        }
        setTitle(r.name);

        StringBuilder sb = new StringBuilder();
        for (Recipe.RecipeIngredient ri : r.ingredients)
            sb.append("\u2022 ").append(PantryItem.fmt(ri.quantity)).append(" ")
                    .append(ri.unit).append(" ").append(ri.name).append("\n");

        ((TextView) findViewById(R.id.textDetailName)).setText(r.name);
        ((TextView) findViewById(R.id.textDetailIngredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.textDetailSteps)).setText(r.steps);
    }
}

