package fr.villot.pricetracker.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.RecordSheet;
import fr.villot.pricetracker.model.Store;

public class RecordSheetViewHolder extends RecyclerView.ViewHolder {

    private TextView nameTextView;
    private TextView dateTextView;
    private ImageView recordSheetImageView;

    private CardView recordSheetCardView;
    private ImageView recordSheetSelectionImage;


    public RecordSheetViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        dateTextView = itemView.findViewById(R.id.dateTextView);
        recordSheetImageView = itemView.findViewById(R.id.recordSheetImageView);
        recordSheetCardView = itemView.findViewById(R.id.recordSheetCardView);
        recordSheetSelectionImage = itemView.findViewById(R.id.recordSheetSelectionImage);
    }

    public void bind(RecordSheet recordSheet, boolean isSelected) {
        nameTextView.setText(recordSheet.getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm:ss", Locale.FRANCE);
        String formattedDate = dateFormat.format(recordSheet.getDate());
        dateTextView.setText(formattedDate);

        // Charger l'image du logo à partir des ressources
        int imageResource = recordSheetImageView.getContext().getResources().getIdentifier(recordSheet.getLogo(), "drawable", recordSheetImageView.getContext().getPackageName());
        recordSheetImageView.setImageResource(imageResource);
//        Picasso.get().load(imageResource).into(recordSheetImageView);

        if (isSelected) {
            recordSheetCardView.setCardBackgroundColor(ContextCompat.getColor(recordSheetCardView.getContext(), R.color.item_product_pressed_background));
            recordSheetSelectionImage.setVisibility(View.VISIBLE);

        } else {
            recordSheetCardView.setCardBackgroundColor(ContextCompat.getColor(recordSheetCardView.getContext(), R.color.item_product_normal_background));
            recordSheetSelectionImage.setVisibility(View.GONE);
        }
    }

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
