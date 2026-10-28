package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        // Using CompoundButton eliminates the fatal ClassCastException on Material themes
        CompoundButton sw = findViewById(R.id.switchExpiry);
        if (sw != null) {
            sw.setChecked(prefs.getBoolean("expiryAlerts", true));
            sw.setOnCheckedChangeListener((b, checked) ->
                    prefs.edit().putBoolean("expiryAlerts", checked).apply());
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
