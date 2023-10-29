package fr.villot.pricetracker.adapters;

import android.view.View;
import android.widget.ImageView;
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
    private ImageView storeImageView;

    private CardView storeCardView;

    public StoreViewHolder(@NonNull View itemView) {
        super(itemView);
        storeNameTextView = itemView.findViewById(R.id.storeNameTextView);
        storeLocationTextView = itemView.findViewById(R.id.storeLocationTextView);
        storeImageView = itemView.findViewById(R.id.storeImageView);
        storeCardView = itemView.findViewById(R.id.storeCardView);
    }

    public void bind(Store store) {
        storeNameTextView.setText(store.getName());
        storeLocationTextView.setText(store.getLocation());

        // Charger l'image du logo à partir des ressources
        int imageResource = storeImageView.getContext().getResources().getIdentifier(store.getLogo(), "drawable", storeImageView.getContext().getPackageName());
        storeImageView.setImageResource(imageResource);

    }


    public void setSelected(boolean isSelected) {

        // Couleur de selection
        if (isSelected)
            storeCardView.setCardBackgroundColor(ContextCompat.getColor(storeCardView.getContext(), R.color.item_product_pressed_background));
        else
            storeCardView.setCardBackgroundColor(ContextCompat.getColor(storeCardView.getContext(), R.color.item_product_normal_background));
    }

}
