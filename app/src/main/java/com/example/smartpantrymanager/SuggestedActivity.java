package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuggestedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        setTitle("Suggested Recipes");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DatabaseHelper db = new DatabaseHelper(this);
        List<PantryItem> pantry = db.getAllItems();
        List<Recipe> suggestions = RecipeMatcher.suggest(db.getAllRecipes(), pantry);

        TextView emptyText = findViewById(R.id.textNoMatch);
        RecyclerView recycler = findViewById(R.id.recyclerRecipes);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        boolean hasMatches = suggestions != null && !suggestions.isEmpty();
        if (emptyText != null) {
            emptyText.setVisibility(hasMatches ? View.GONE : View.VISIBLE);
        }
        if (recycler != null) {
            recycler.setVisibility(hasMatches ? View.VISIBLE : View.GONE);
            if (suggestions != null) {
                recycler.setAdapter(new RecipeAdapter(suggestions, recipe -> {
                    Intent i = new Intent(this, RecipeDetailActivity.class);
                    i.putExtra("RECIPE_ID", recipe.id);
                    startActivity(i);
                }));
            }
        }
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