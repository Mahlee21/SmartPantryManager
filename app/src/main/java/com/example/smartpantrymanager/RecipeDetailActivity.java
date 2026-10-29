package com.example.smartpantrymanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long id = getIntent().getLongExtra("RECIPE_ID", -1);
        Recipe r = new DatabaseHelper(this).getRecipe(id);
        if (r == null) {
            finish();
            return;
        }
        setTitle(r.name != null ? r.name : "Recipe Details");

        StringBuilder sb = new StringBuilder();
        if (r.ingredients != null) {
            for (Recipe.RecipeIngredient ri : r.ingredients) {
                if (ri != null) {
                    sb.append("• ").append(PantryItem.fmt(ri.quantity)).append(" ")
                            .append(ri.unit != null ? ri.unit : "").append(" ")
                            .append(ri.name != null ? ri.name : "").append("\n");
                }
            }
        }

        TextView textName = findViewById(R.id.textDetailName);
        TextView textIngredients = findViewById(R.id.textDetailIngredients);
        TextView textSteps = findViewById(R.id.textDetailSteps);

        if (textName != null) textName.setText(r.name != null ? r.name : "");
        if (textIngredients != null) textIngredients.setText(sb.toString().trim());
        if (textSteps != null) textSteps.setText(r.steps != null ? r.steps : "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}