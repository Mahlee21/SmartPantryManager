package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
public class SettingsActivity  extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Switch sw = findViewById(R.id.switchExpiry);
        sw.setChecked(prefs.getBoolean("expiryAlerts", true));
        sw.setOnCheckedChangeListener((CompoundButton b, boolean checked) ->
                prefs.edit().putBoolean("expiryAlerts", checked).apply());

    }
}
