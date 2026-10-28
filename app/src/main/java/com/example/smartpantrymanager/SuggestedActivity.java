package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.model.PantryItem;

import java.util.List;

public class SuggestedActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        setTitle("Suggested Recipes");

        DatabaseHelper db = new DatabaseHelper(this);
        List<PantryItem> pantry= db.getAllItems();
        List<Recipe> suggestions = RecipeMatcher.suggest(db.getAllRecipes(), pantry);

        TextView empty = findViewById(R.id.textNoMatch);
        RecyclerView recycler =findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        empty.setVisibility(suggestions.isEmpty() ? View.VISIBLE : View.GONE);
        recycler.setAdapter(new RecipeAdapter(suggestions, recipe -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra("RECIPE_ID", recipe.id);
            startActivity(i);
        }));
    }
    }




