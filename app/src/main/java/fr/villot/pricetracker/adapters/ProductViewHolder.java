package fr.villot.pricetracker.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import fr.villot.pricetracker.R;
import fr.villot.pricetracker.model.Product;

public class ProductViewHolder extends RecyclerView.ViewHolder {

    private final TextView productBarcodeTextView;
    private final CardView productCardView;
    private final TextView productNameTextView;
    private final LinearLayout productBrandZone;
    private final TextView productBrandTextView;
    private final LinearLayout productQuantityZone;
    private final TextView productQuantityTextView;
    private final LinearLayout productOriginZone;

    private final TextView productOriginTextView;
    private final TextView productPriceTextView;
    private final LinearLayout productPriceZone;
    private final ImageView productImageView;
    private final ImageView productSelectionImage;

    public ProductViewHolder(View itemView) {
        super(itemView);

        // Récupérer les vues de l'élément de la liste
        productCardView = itemView.findViewById(R.id.productCardView);
        productBarcodeTextView = itemView.findViewById(R.id.productBarcodeTextView);
        productNameTextView = itemView.findViewById(R.id.productNameTextView);
        productBrandZone = itemView.findViewById(R.id.productBrandZone);
        productBrandTextView = itemView.findViewById(R.id.productBrandTextView);
        productQuantityZone = itemView.findViewById(R.id.productQuantityZone);
        productQuantityTextView = itemView.findViewById(R.id.productQuantityTextView);
        productOriginZone = itemView.findViewById(R.id.productOriginZone);
        productOriginTextView = itemView.findViewById(R.id.productOriginTextView);
        productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
        productPriceZone = itemView.findViewById(R.id.productPriceZone);
        productImageView = itemView.findViewById(R.id.productImageView);
        productSelectionImage = itemView.findViewById(R.id.productSelectionImage);

    }

    public void bind(Product product, boolean isSelected) {
        // Mettre à jour les vues avec les données du produit
        productBarcodeTextView.setText(product.getBarcode());

        // Affichage du nom du produit s'il existe
        String productName = product.getName();
        if (productName != null && !(productName.isEmpty())) {
            productNameTextView.setText(product.getName());
        }

        // Affichage de la marque du produit si elle existe
        String productBrand = product.getBrand();
        if (productBrand != null && !(productBrand.isEmpty())) {
            productBrandTextView.setText(product.getBrand());
        }
        else {
            productBrandZone.setVisibility(View.GONE);
        }

        // Affichage de la quantité du produit si elle existe
        String productQuantity = product.getQuantity();
        if (productQuantity != null && !(productQuantity.isEmpty())) {
            productQuantityTextView.setText(product.getQuantity());
        }
        else {
            productQuantityZone.setVisibility(View.GONE);
        }

        // Affichage de l'origine du produit si elle existe
        String productOrigin = product.getOrigin();
        if (productOrigin != null && !(productOrigin.isEmpty())) {
            productOriginTextView.setText(product.getOrigin());
        } else {
            productOriginZone.setVisibility(View.GONE);
        }

        // Gestion de la zone de prix
        Double price = product.getPrice();
        if (price != null) {
            productPriceZone.setVisibility(View.VISIBLE);
            productPriceTextView.setText(String.format(Locale.FRANCE, "%.2f", price));
        }
        else {
            productPriceZone.setVisibility(View.GONE);
        }

        // Afficher l'image
        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !(imageUrl.isEmpty())) {
            Picasso.get().load(imageUrl).into(productImageView);
        }

        // Gestion de la selection
        if (isSelected) {
            productCardView.setCardBackgroundColor(ContextCompat.getColor(productCardView.getContext(), R.color.item_product_pressed_background));
            productSelectionImage.setVisibility(View.VISIBLE);

        } else {
            productCardView.setCardBackgroundColor(ContextCompat.getColor(productCardView.getContext(), R.color.item_product_normal_background));
            productSelectionImage.setVisibility(View.GONE);
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
