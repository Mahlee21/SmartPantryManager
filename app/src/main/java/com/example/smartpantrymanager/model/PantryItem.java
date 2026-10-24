package com.example.smartpantrymanager.model;

public class PantryItem {
    public long id;
    public String name;
    public double quantity;
    public String unit;
    public String expiry; // yyyy-MM-dd

    public PantryItem(String name, double quantity, String unit, String expiry ){
        this.name = name;
        this.quantity=quantity;
        this.unit=unit;
        this.expiry=expiry;
    }
    public static String fmt(double q){
        return q==Math.floor(q)
                ? String.valueOf((long)q): String.valueOf(q);
    }
    }

