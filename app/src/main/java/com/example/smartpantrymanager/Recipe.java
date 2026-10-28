package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    public long id;
    public String name;
    public String steps;
    public List<RecipeIngredient> ingredients = new ArrayList<>();

    public static class RecipeIngredient{
        public String name;
        public double quantity;
        public String unit;
    }
}
