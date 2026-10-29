package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeMatcher {

    public static String normalise(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z ]", "").replaceAll("\\s+", " ").trim();
        if (s.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        for (String w : s.split(" ")) {
            if (w.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(singular(w));
        }
        return out.toString();
    }

    private static String singular(String w) {
        if (w == null) return "";
        int n = w.length();
        if (n > 3 && w.endsWith("ies")) return w.substring(0, n - 3) + "y";
        if (n > 3 && (w.endsWith("oes") || w.endsWith("ches") || w.endsWith("shes")
                || w.endsWith("xes") || w.endsWith("sses"))) return w.substring(0, n - 2);
        if (n > 3 && w.endsWith("s") && !w.endsWith("ss")) return w.substring(0, n - 1);
        return w;
    }

    private static class Amount {
        String kind = "count";
        double value = 0;
    }

    private static Amount toBase(double qty, String unit) {
        Amount a = new Amount();
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "g":    a.kind = "mass";   a.value = qty;        break;
            case "kg":   a.kind = "mass";   a.value = qty * 1000; break;
            case "ml":   a.kind = "volume"; a.value = qty;        break;
            case "l":    a.kind = "volume"; a.value = qty * 1000; break;
            case "tsp":  a.kind = "volume"; a.value = qty * 5;    break;
            case "tbsp": a.kind = "volume"; a.value = qty * 15;   break;
            case "cup":  a.kind = "volume"; a.value = qty * 250;  break;
            default:     a.kind = "count";  a.value = qty;        break;
        }
        return a;
    }

    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        if (recipe == null || recipe.ingredients == null || pantry == null) return false;

        Map<String, Double> stock = new HashMap<>();
        for (PantryItem p : pantry) {
            if (p == null || p.name == null) continue;
            Amount a = toBase(p.quantity, p.unit);
            String key = normalise(p.name) + "|" + a.kind;
            Double old = stock.get(key);
            stock.put(key, (old == null ? 0.0 : old) + a.value);
        }

        for (Recipe.RecipeIngredient ri : recipe.ingredients) {
            if (ri == null || ri.name == null) continue;
            Amount need = toBase(ri.quantity, ri.unit);
            Double have = stock.get(normalise(ri.name) + "|" + need.kind);
            if (have == null || have + 1e-9 < need.value) return false;
        }
        return true;
    }

    public static List<Recipe> suggest(List<Recipe> recipes, List<PantryItem> pantry) {
        List<Recipe> result = new ArrayList<>();
        if (recipes == null || pantry == null) return result;
        for (Recipe r : recipes) {
            if (r != null && canMake(r, pantry)) {
                result.add(r);
            }
        }
        return result;
    }
}