package com.example.smartpantrymanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.smartpantrymanager.model.PantryItem;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items;
    private final Listener listener;
    private final boolean alertsOn;

    public PantryAdapter(List<PantryItem> items, boolean alertsOn, Listener listener) {
        this.items = items;
        this.alertsOn = alertsOn;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PantryItem item = items.get(position);
        h.name.setText(item.name);
        h.qty.setText(PantryItem.fmt(item.quantity) + " " + item.unit);

        boolean hasExpiry = item.expiry != null && !item.expiry.isEmpty();
        String expiryText = hasExpiry ? "Expires: " + item.expiry : "No expiry date";
        h.expiry.setTextColor(Color.DKGRAY);
        if (hasExpiry && alertsOn && daysLeft(item.expiry) <= 3) {
            expiryText += daysLeft(item.expiry) < 0 ? "  (EXPIRED)" : "  (expiring soon!)";
            h.expiry.setTextColor(Color.RED);
        }
        h.expiry.setText(expiryText);

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    private long daysLeft(String expiry) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(expiry);
            return (d.getTime() - System.currentTimeMillis()) / (24L * 60 * 60 * 1000);
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, qty, expiry;
        Button delete;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.textItemName);
            qty = v.findViewById(R.id.textItemQty);
            expiry = v.findViewById(R.id.textItemExpiry);
            delete = v.findViewById(R.id.buttonDelete);
        }
    }
}
