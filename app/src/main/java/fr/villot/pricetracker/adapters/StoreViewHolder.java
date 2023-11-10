package fr.villot.pricetracker.adapters;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Store;

public class StoreViewHolder extends RecyclerView.ViewHolder {

    private TextView storeNameTextView;
    private TextView storeLocationTextView;
    private ImageView storeImageView;

    private CardView storeCardView;
    private ImageView storeSelectionImage;

    public StoreViewHolder(@NonNull View itemView) {
        super(itemView);
        storeNameTextView = itemView.findViewById(R.id.storeNameTextView);
        storeLocationTextView = itemView.findViewById(R.id.storeLocationTextView);
        storeImageView = itemView.findViewById(R.id.storeImageView);
        storeCardView = itemView.findViewById(R.id.storeCardView);
        storeSelectionImage = itemView.findViewById(R.id.storeSelectionImage);
    }

//    public void bind(Store store) {
//        storeNameTextView.setText(store.getName());
//        storeLocationTextView.setText(store.getLocation());
//
//        // Charger l'image du logo à partir des ressources
//        int imageResource = storeImageView.getContext().getResources().getIdentifier(store.getLogo(), "drawable", storeImageView.getContext().getPackageName());
//        storeImageView.setImageResource(imageResource);
//
//    }

    public void bind(Store store, boolean isSelected) {
        storeNameTextView.setText(store.getName());
        storeLocationTextView.setText(store.getLocation());

        // Charger l'image du logo à partir des ressources
        int imageResource = storeImageView.getContext().getResources().getIdentifier(store.getLogo(), "drawable", storeImageView.getContext().getPackageName());
        storeImageView.setImageResource(imageResource);

        if (isSelected) {
            storeCardView.setCardBackgroundColor(ContextCompat.getColor(storeCardView.getContext(), R.color.item_product_pressed_background));
            storeSelectionImage.setVisibility(View.VISIBLE);

            // Mettez en évidence visuellement l'élément sélectionné (par exemple, changez la couleur de l'arrière-plan).
//                itemView.setBackgroundColor(context.getResources().getColor(R.color.selected_background_color));
//            itemView.setBackgroundColor(storeImageView.getResources().getColor(R.color.purple_200));

        } else {
            storeCardView.setCardBackgroundColor(ContextCompat.getColor(storeCardView.getContext(), R.color.item_product_normal_background));
            storeSelectionImage.setVisibility(View.GONE);
//            itemView.setBackgroundColor(storeImageView.getResources().getColor(android.R.color.transparent));
        }
    }


//    public void setSelected(boolean isSelected) {
//
//        // Couleur de selection
//        if (isSelected)
//            storeCardView.setCardBackgroundColor(ContextCompat.getColor(storeCardView.getContext(), R.color.item_product_pressed_background));
//        else
//            storeCardView.setCardBackgroundColor(ContextCompat.getColor(storeCardView.getContext(), R.color.item_product_normal_background));
//    }


    public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
        return new ItemDetailsLookup.ItemDetails<Long>() {
            @Override
            public int getPosition() {
                return getAdapterPosition();
            }

            @Override
            public Long getSelectionKey() {
                return (long) getAdapterPosition();
            }
        };
    }


}
