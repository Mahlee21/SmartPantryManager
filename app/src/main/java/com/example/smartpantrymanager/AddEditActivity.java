package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.model.PantryItem;

import java.text.SimpleDateFormat;
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

        db = new DatabaseHelper(this);
        editName = findViewById(R.id.editName);
        editQty = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        Button save = findViewById(R.id.buttonSave);

        ArrayAdapter<CharSequence> units = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        units.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(units);

        itemId = getIntent().getLongExtra("ITEM_ID", -1);
        if (itemId != -1) {
            setTitle("Edit Ingredient");
            PantryItem item = db.getItem(itemId);
            if (item != null) {
                editName.setText(item.name);
                editQty.setText(PantryItem.fmt(item.quantity));
                editExpiry.setText(item.expiry);
                for (int i = 0; i < units.getCount(); i++)
                    if (units.getItem(i).toString().equals(item.unit)) spinnerUnit.setSelection(i);
            }
        } else {
            setTitle("Add Ingredient");
        }

        save.setOnClickListener(v -> save());
    }

    private void save() {
        String name = editName.getText().toString().trim();
        String qtyText = editQty.getText().toString().trim();
        String expiry = editExpiry.getText().toString().trim();

        if (name.isEmpty()) {
            editName.setError("Ingredient name is required");
            return;
        }
        double qty;
        try {
            qty = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            editQty.setError("Enter a valid number");
            return;
        }
        if (qty <= 0) {
            editQty.setError("Quantity must be greater than 0");
            return;
        }
        if (!expiry.isEmpty()) {
            try {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                f.setLenient(false);
                f.parse(expiry);
            } catch (Exception e) {
                editExpiry.setError("Use the format yyyy-MM-dd");
                return;
            }
        }

        PantryItem item = new PantryItem();
        item.name = name;
        item.quantity = qty;
        item.unit = spinnerUnit.getSelectedItem().toString();
        item.expiry = expiry;

        if (itemId == -1) {
            db.addItem(item);                 // CREATE
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            item.id = itemId;
            db.updateItem(item);              // UPDATE
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}

