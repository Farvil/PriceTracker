package fr.villot.pricetracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Store;

public class SpinnerStoreAdapter extends ArrayAdapter<Store> {

    private final List<Store> storeList;
    private final LayoutInflater inflater;

    public SpinnerStoreAdapter(Context context, List<Store> storeList) {
        super(context, R.layout.item_store, storeList);
        this.storeList = storeList;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_store_spinner, parent, false);
        }

        TextView storeNameTextView = view.findViewById(R.id.storeNameTextView);
        TextView storeLocationTextView = view.findViewById(R.id.storeLocationTextView);
        ImageView storeImageView = view.findViewById(R.id.storeImageView);


        Store store = storeList.get(position);
        storeNameTextView.setText(store.getName());
        storeLocationTextView.setText(store.getLocation());

        // Charger l'image du logo à partir des ressources
        int imageResource = storeImageView.getContext().getResources().getIdentifier(store.getLogo(), "drawable", storeImageView.getContext().getPackageName());
        storeImageView.setImageResource(imageResource);

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
