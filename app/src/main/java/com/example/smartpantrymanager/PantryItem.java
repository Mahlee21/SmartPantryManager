package com.example.smartpantrymanager;

public class PantryItem {
    public long id;
    public String name;
    public double quantity;
    public String unit;
    public String expiry; // yyyy-MM-dd, may be empty

    public static String fmt(double q) {
        return q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q);
    }
}
