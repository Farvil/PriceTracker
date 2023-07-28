package fr.villot.pricetracker.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Store;

public class StoreViewHolder extends RecyclerView.ViewHolder {

    private TextView storeNameTextView;
    private TextView storeLocationTextView;

    private CardView storeCardView;

    public StoreViewHolder(@NonNull View itemView) {
        super(itemView);
        storeNameTextView = itemView.findViewById(R.id.storeNameTextView);
        storeLocationTextView = itemView.findViewById(R.id.storeLocationTextView);
        storeCardView = itemView.findViewById(R.id.storeCardView);
    }

    public void bind(Store store) {
        storeNameTextView.setText(store.getName());
        storeLocationTextView.setText(store.getLocation());
    }


    public void setSelected(boolean isSelected) {

        // Couleur de selection
        if (isSelected)
            storeCardView.setCardBackgroundColor(ContextCompat.getColor(storeCardView.getContext(), R.color.item_product_pressed_background));
        else
            storeCardView.setCardBackgroundColor(ContextCompat.getColor(storeCardView.getContext(), R.color.item_product_normal_background));
    }

}
