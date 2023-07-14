package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Store;

public class StoreAdapter extends ArrayAdapter<Store> {

    public StoreAdapter(Context context, List<Store> stores) {
        super(context, 0, stores);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_store, parent, false);
        }

        Store store = getItem(position);

        TextView nameTextView = convertView.findViewById(R.id.storeNameTextView);
        TextView locationTextView = convertView.findViewById(R.id.storeLocationTextView);

        nameTextView.setText(store.getName());
        locationTextView.setText(store.getLocation());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position,convertView, parent);
    }

}
