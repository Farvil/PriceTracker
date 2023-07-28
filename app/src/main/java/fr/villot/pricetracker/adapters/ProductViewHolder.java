package fr.villot.pricetracker.adapters;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Product;

public class ProductViewHolder extends RecyclerView.ViewHolder {

    private TextView productBarcodeTextView;
    private CardView productCardView;
    private TextView productNameTextView;
    private TextView productBrandTextView;
    private TextView productQuantityTextView;
    private TextView productPriceTextView;
    private ImageView productImageView;
    public CheckBox productCheckBox;

    public interface CheckBoxListener {
        void onCheckBoxClick(int position, boolean isChecked);
    }

    public ProductViewHolder(View itemView) {
        super(itemView);

        // Récupérer les vues de l'élément de la liste
        productCardView = itemView.findViewById(R.id.productCardView);
        productBarcodeTextView = itemView.findViewById(R.id.productBarcodeTextView);
        productNameTextView = itemView.findViewById(R.id.productNameTextView);
        productBrandTextView = itemView.findViewById(R.id.productBrandTextView);
        productQuantityTextView = itemView.findViewById(R.id.productQuantityTextView);
        productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
        productImageView = itemView.findViewById(R.id.productImageView);
        productCheckBox = itemView.findViewById(R.id.productCheckBox);
    }

    public void bind(Product product) {
        // Mettre à jour les vues avec les données du produit
        productBarcodeTextView.setText(product.getBarcode());
        productNameTextView.setText(product.getName());
        productBrandTextView.setText(product.getBrand());
        productQuantityTextView.setText(product.getQuantity());
        // productPriceTextView.setText(String.valueOf(product.getPrice()));
        productPriceTextView.setVisibility(View.VISIBLE);

        // Utiliser Picasso pour charger l'image à partir de l'URL et l'afficher dans ImageView
        Picasso.get().load(product.getImageUrl()).into(productImageView);
    }

    public void setSelected(boolean isSelected) {
        productCheckBox.setVisibility(View.VISIBLE);
        productCheckBox.setChecked(isSelected);
        // Couleur de selection
//        if (isSelected)
//            productCardView.setCardBackgroundColor(ContextCompat.getColor(productCardView.getContext(), R.color.item_product_pressed_background));
//        else
//            productCardView.setCardBackgroundColor(ContextCompat.getColor(productCardView.getContext(), R.color.item_product_normal_background));
    }

    public void setSelectionMode(boolean isSelectionMode) {
        if (isSelectionMode) {
            productCheckBox.setVisibility(View.VISIBLE);
        } else {
            productCheckBox.setVisibility(View.GONE);
        }
    }

    public void setCheckBoxListener(CheckBoxListener listener) {
        productCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onCheckBoxClick(getAdapterPosition(), isChecked);
            }
        });
    }

    public void setCheckBoxChecked(boolean isChecked) {
        productCheckBox.setChecked(isChecked);
    }

}

