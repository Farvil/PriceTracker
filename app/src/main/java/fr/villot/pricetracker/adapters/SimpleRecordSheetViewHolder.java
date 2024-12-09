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

public class SimpleRecordSheetViewHolder extends RecyclerView.ViewHolder {

    private final TextView nameTextView;
    private final TextView dateTextView;

    private final CardView recordSheetCardView;
    private final ImageView recordSheetSelectionImage;


    public SimpleRecordSheetViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        dateTextView = itemView.findViewById(R.id.dateTextView);
        recordSheetCardView = itemView.findViewById(R.id.recordSheetCardView);
        recordSheetSelectionImage = itemView.findViewById(R.id.recordSheetSelectionImage);
    }

    public void bind(RecordSheet recordSheet, boolean isSelected) {
        nameTextView.setText(recordSheet.getName());

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm:ss", Locale.FRANCE);
        String formattedDate = dateFormat.format(recordSheet.getDate());
        dateTextView.setText(formattedDate);

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
