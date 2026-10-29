package com.example.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private RecyclerView recycler;
    private TextView textEmpty;
    private View layoutEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("My Pantry");

        db = new DatabaseHelper(this);
        recycler = findViewById(R.id.recyclerPantry);
        textEmpty = findViewById(R.id.textEmpty);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        Button add = findViewById(R.id.buttonAdd);
        if (add != null) {
            add.setOnClickListener(v -> startActivity(new Intent(this, AddEditActivity.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> items = db.getAllItems();
        boolean alertsOn = getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getBoolean("expiryAlerts", true);

        boolean isEmpty = items.isEmpty();
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        } else if (textEmpty != null) {
            textEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        recycler.setAdapter(new PantryAdapter(items, alertsOn, new PantryAdapter.Listener() {
            @Override
            public void onEdit(PantryItem item) {
                Intent i = new Intent(MainActivity.this, AddEditActivity.class);
                i.putExtra("ITEM_ID", item.id);
                startActivity(i);
            }

            @Override
            public void onDelete(PantryItem item) {
                if (isFinishing()) return;
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete ingredient")
                        .setMessage("Remove \"" + item.name + "\" from your pantry?")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.deleteItem(item.id);
                            Toast.makeText(MainActivity.this, item.name + " deleted", Toast.LENGTH_SHORT).show();
                            loadItems();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_suggested) {
            startActivity(new Intent(this, SuggestedActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}