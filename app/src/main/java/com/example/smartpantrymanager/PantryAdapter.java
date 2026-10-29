package com.example.smartpantrymanager;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        h.name.setText(item.name != null ? item.name : "");

        String unitStr = item.unit != null ? item.unit : "";
        h.qty.setText(PantryItem.fmt(item.quantity) + " " + unitStr);

        boolean hasExpiry = item.expiry != null && !item.expiry.trim().isEmpty();
        if (!hasExpiry) {
            h.expiry.setText("No expiry date");
        } else {
            int days = daysUntilExpiry(item.expiry);
            if (alertsOn && days <= 3) {
                if (days < 0) {
                    h.expiry.setText("EXPIRED (" + item.expiry + ")");
                    h.expiry.setTextColor(Color.RED);
                } else if (days == 0) {
                    h.expiry.setText("Expires today!");
                    h.expiry.setTextColor(Color.RED);
                } else {
                    h.expiry.setText("Expires in " + days + " days");
                    h.expiry.setTextColor(Color.rgb(200, 100, 0));
                }
            } else {
                h.expiry.setText("Exp: " + item.expiry);
                h.expiry.setTextColor(Color.DKGRAY);
            }
        }

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        if (h.delete != null) {
            h.delete.setOnClickListener(v -> listener.onDelete(item));
        }
    }

    private int daysUntilExpiry(String expiry) {
        if (expiry == null || expiry.trim().isEmpty()) return Integer.MAX_VALUE;
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            f.setLenient(false);
            Date targetDate = f.parse(expiry);
            if (targetDate == null) return Integer.MAX_VALUE;

            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);

            Calendar target = Calendar.getInstance();
            target.setTime(targetDate);
            target.set(Calendar.HOUR_OF_DAY, 0);
            target.set(Calendar.MINUTE, 0);
            target.set(Calendar.SECOND, 0);
            target.set(Calendar.MILLISECOND, 0);

            long diffMs = target.getTimeInMillis() - now.getTimeInMillis();
            return (int) (diffMs / (24L * 60 * 60 * 1000));
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, qty, expiry;
        View delete;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.textItemName);
            qty = v.findViewById(R.id.textItemQty);
            expiry = v.findViewById(R.id.textItemExpiry);
            delete = v.findViewById(R.id.buttonDelete);
        }
    }
}