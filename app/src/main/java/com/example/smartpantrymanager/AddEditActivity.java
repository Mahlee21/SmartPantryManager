package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditActivity extends AppCompatActivity {
    private EditText editName, editQty, editExpiry;
    private Spinner spinnerUnit;
    private DatabaseHelper db;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        editName = findViewById(R.id.editName);
        editQty = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        Button save = findViewById(R.id.buttonSave);

        // Load units spinner
        ArrayAdapter<CharSequence> units;
        try {
            units = ArrayAdapter.createFromResource(
                    this, R.array.units, android.R.layout.simple_spinner_item);
        } catch (Exception e) {
            CharSequence[] defaultUnits = new CharSequence[]{"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};
            units = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, defaultUnits);
        }
        units.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(units);

        // Click listener to pop up the Calendar Date Picker
        editExpiry.setOnClickListener(v -> showDatePicker());

        itemId = getIntent().getLongExtra("ITEM_ID", -1);
        if (itemId != -1) {
            setTitle("Edit Ingredient");
            PantryItem item = db.getItem(itemId);
            if (item != null) {
                editName.setText(item.name);
                editQty.setText(PantryItem.fmt(item.quantity));
                editExpiry.setText(item.expiry != null ? item.expiry : "");
                if (item.unit != null) {
                    for (int i = 0; i < units.getCount(); i++) {
                        CharSequence u = units.getItem(i);
                        if (u != null && u.toString().equalsIgnoreCase(item.unit)) {
                            spinnerUnit.setSelection(i);
                            break;
                        }
                    }
                }
            }
        } else {
            setTitle("Add Ingredient");
        }

        save.setOnClickListener(v -> save());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        String currentText = editExpiry.getText().toString().trim();
        if (!currentText.isEmpty()) {
            try {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date d = f.parse(currentText);
                if (d != null) cal.setTime(d);
            } catch (Exception ignored) {}
        }

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selected = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    editExpiry.setText(selected);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void save() {
        String name = editName.getText().toString().trim();
        String qtyText = editQty.getText().toString().trim().replace(',', '.');
        String expiry = editExpiry.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Ingredient name is required");
            editName.requestFocus();
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            editQty.setError("Enter a valid number");
            editQty.requestFocus();
            return;
        }

        if (qty <= 0 || Double.isNaN(qty) || Double.isInfinite(qty)) {
            editQty.setError("Quantity must be greater than 0");
            editQty.requestFocus();
            return;
        }

        PantryItem item = new PantryItem();
        item.name = name;
        item.quantity = qty;
        Object selectedUnit = spinnerUnit.getSelectedItem();
        item.unit = selectedUnit != null ? selectedUnit.toString() : "pcs";
        item.expiry = expiry;

        if (itemId == -1) {
            db.addItem(item);
            Toast.makeText(this, "Ingredient added to pantry", Toast.LENGTH_SHORT).show();
        } else {
            item.id = itemId;
            db.updateItem(item);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
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