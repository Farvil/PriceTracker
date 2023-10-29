package fr.villot.pricetracker.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.RecordSheet;

public class RecordSheetViewHolder extends RecyclerView.ViewHolder {

    private TextView nameTextView;
    private TextView dateTextView;
    private ImageView recordSheetImageView;

    private CardView recordSheetCardView;

    public RecordSheetViewHolder(@NonNull View itemView) {
        super(itemView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
        dateTextView = itemView.findViewById(R.id.dateTextView);
        recordSheetImageView = itemView.findViewById(R.id.recordSheetImageView);
        recordSheetCardView = itemView.findViewById(R.id.recordSheetCardView);
    }

    public void bind(RecordSheet recordSheet) {
        nameTextView.setText(recordSheet.getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm:ss", Locale.FRANCE);
        String formattedDate = dateFormat.format(recordSheet.getDate());
        dateTextView.setText(formattedDate);

        // Charger l'image du logo à partir des ressources
        int imageResource = recordSheetImageView.getContext().getResources().getIdentifier(recordSheet.getStoreLogo(), "drawable", recordSheetImageView.getContext().getPackageName());
        recordSheetImageView.setImageResource(imageResource);
    }


    public void setSelected(boolean isSelected) {

        // Couleur de selection
        if (isSelected)
            recordSheetCardView.setCardBackgroundColor(ContextCompat.getColor(recordSheetCardView.getContext(), R.color.item_product_pressed_background));
        else
            recordSheetCardView.setCardBackgroundColor(ContextCompat.getColor(recordSheetCardView.getContext(), R.color.item_product_normal_background));
    }

}
