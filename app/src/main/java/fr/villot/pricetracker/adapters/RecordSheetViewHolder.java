package fr.villot.pricetracker.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.RecordSheet;

public class RecordSheetViewHolder extends RecyclerView.ViewHolder {

    private final TextView nameTextView;
    private final TextView dateTextView;
    private final ImageView recordSheetImageView;

    private final CardView recordSheetCardView;
    private final ImageView recordSheetSelectionImage;

    private final TextView storeNameTextView;
    private final TextView storeLocationTextView;

    private final TextView productPriceTextView;


    public RecordSheetViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        dateTextView = itemView.findViewById(R.id.dateTextView);
        recordSheetImageView = itemView.findViewById(R.id.recordSheetImageView);
        recordSheetCardView = itemView.findViewById(R.id.recordSheetCardView);
        recordSheetSelectionImage = itemView.findViewById(R.id.recordSheetSelectionImage);
        storeNameTextView = itemView.findViewById(R.id.storeNameTextView);
        storeLocationTextView = itemView.findViewById(R.id.storeLocationTextView);
        productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
    }

    public void bind(RecordSheet recordSheet, boolean isSelected, boolean isProductPrice) {
        nameTextView.setText(recordSheet.getName());
        storeNameTextView.setText(recordSheet.getStore().getName());
        storeLocationTextView.setText(recordSheet.getStore().getLocation());

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm:ss", Locale.FRANCE);
        String formattedDate = dateFormat.format(recordSheet.getDate());
        dateTextView.setText(formattedDate);

        // Charger l'image du logo à partir des ressources
        int imageResource = recordSheetImageView.getContext().getResources().getIdentifier(recordSheet.getStore().getLogo(), "drawable", recordSheetImageView.getContext().getPackageName());
        recordSheetImageView.setImageResource(imageResource);
//        Picasso.get().load(imageResource).into(recordSheetImageView);

        if (isSelected) {
            recordSheetCardView.setCardBackgroundColor(ContextCompat.getColor(recordSheetCardView.getContext(), R.color.item_product_pressed_background));
            recordSheetSelectionImage.setVisibility(View.VISIBLE);

        } else {
            recordSheetCardView.setCardBackgroundColor(ContextCompat.getColor(recordSheetCardView.getContext(), R.color.item_product_normal_background));
            recordSheetSelectionImage.setVisibility(View.GONE);
        }

        // Affichage conditionnel du prix
        if (isProductPrice) {
            productPriceTextView.setVisibility(View.VISIBLE);
            productPriceTextView.setText(String.format(Locale.FRANCE, "%.2f €", recordSheet.getLastPrice() ));
        } else {
            productPriceTextView.setVisibility(View.GONE);
        }
    }

    public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
        return new ItemDetailsLookup.ItemDetails<Long>() {
            @Override
            public int getPosition() {
                return getBindingAdapterPosition();
            }

            @Override
            public Long getSelectionKey() {
                return (long) getBindingAdapterPosition();
            }
        };
    }
}
